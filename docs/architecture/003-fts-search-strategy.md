# ADR 003: Full-Text Search Strategy with FTS4

## Status
Accepted

## Date
2024-02-01

## Context

ArguMentor needs efficient text search across multiple entity types:
- Topics (title, summary)
- Claims (text)
- Rebuttals (text)
- Questions (text)
- Sources (title, citation)
- Evidences (content)
- Tags (label)

Users expect instant search results as they type, even with thousands of records.

## Decision

We implement **FTS4 (Full-Text Search)** virtual tables with the following strategy:

### Architecture
```
┌─────────────┐     ┌─────────────┐
│   claims    │ ──► │ claims_fts  │  FTS4 virtual table
└─────────────┘     └─────────────┘
      │                    │
      └── TRIGGERS ────────┘  (auto-sync)
```

### Implementation
7 FTS4 tables shadow the main content tables:
- `claims_fts`
- `topics_fts`
- `rebuttals_fts`
- `questions_fts`
- `sources_fts`
- `evidences_fts`
- `tags_fts`

```kotlin
@Fts4(contentEntity = Claim::class)
@Entity(tableName = "claims_fts")
data class ClaimFts(
    val text: String
)
```

### Search Flow
```
User Input → Sanitize → FTS Query → Results
                ↓
          (fallback if invalid)
                ↓
            LIKE Query
```

## Rationale

### Why FTS4 over FTS5?
1. **Broader Compatibility**: FTS4 works on all Android versions
2. **Sufficient Features**: ArguMentor doesn't need FTS5's advanced features
3. **Smaller Index Size**: FTS4 indexes are typically smaller

### Why Virtual Tables with Content Sync?
1. **Automatic Sync**: Room handles content table triggers
2. **Deduplication**: No duplicate data storage
3. **Consistent State**: FTS always reflects current data

### Performance Gains
- FTS4: 10-100x faster than `LIKE '%query%'`
- Prefix matching: `text:argu*` matches "argument", "argumentation"
- Boolean queries: `AND`, `OR`, `NOT` operators supported

## Security Considerations

### FTS Injection Prevention
FTS queries accept special operators that could be exploited:
```
"text MATCH 'a OR b'"  -- Boolean injection
"text MATCH 'a NEAR/2 b'"  -- NEAR operator injection
```

**Solution**: `SearchUtils.kt` validates and sanitizes queries:
```kotlin
fun isSafeFtsQuery(query: String): Boolean {
    if (trimmed.any { it in FTS_SPECIAL_CHARS }) return false
    if (NEAR_OPERATOR_PATTERN.containsMatchIn(trimmed)) return false
    return true
}

fun sanitizeLikeQuery(query: String): String {
    var escaped = query.replace("\\", "\\\\")
    escaped = escaped.replace("%", "\\%")
    escaped = escaped.replace("_", "\\_")
    return escaped
}
```

### Fallback Strategy
If query contains unsafe characters:
1. Skip FTS query
2. Fall back to `LIKE '%escaped_query%' ESCAPE '\'`
3. Log warning for monitoring

## Consequences

### Positive
- Instant search across all entities
- Prefix and phrase matching
- Ranking by relevance (if needed)
- Works offline

### Negative
- Increased database size (~20% overhead)
- Schema complexity (7 extra virtual tables)
- Must validate all search inputs

### Monitoring
- Track FTS query vs LIKE fallback ratio
- Alert if >10% queries fall back to LIKE

## Implementation Notes

### Search DAO Pattern
```kotlin
@Dao
interface ClaimDao {
    // FTS search - use when query is safe
    @Query("""
        SELECT claims.* FROM claims
        JOIN claims_fts ON claims.rowid = claims_fts.rowid
        WHERE claims_fts MATCH :query
    """)
    suspend fun searchClaimsFts(query: String): List<Claim>

    // LIKE fallback - use when query has special chars
    @Query("""
        SELECT * FROM claims
        WHERE text LIKE '%' || :query || '%' ESCAPE '\'
    """)
    suspend fun searchClaimsLike(query: String): List<Claim>
}
```

### Repository Search Logic
```kotlin
suspend fun searchClaims(query: String): List<Claim> {
    val trimmed = query.trim()
    if (trimmed.isEmpty()) return emptyList()

    return if (SearchUtils.isSafeFtsQuery(trimmed)) {
        claimDao.searchClaimsFts("$trimmed*")  // Prefix match
    } else {
        Timber.w("Unsafe FTS query, falling back to LIKE: $trimmed")
        val escaped = SearchUtils.sanitizeLikeQuery(trimmed)
        claimDao.searchClaimsLike(escaped)
    }
}
```

## Migrations
FTS tables are created via migrations:
```kotlin
val MIGRATION_3_4 = object : Migration(3, 4) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("""
            CREATE VIRTUAL TABLE IF NOT EXISTS `claims_fts`
            USING fts4(content=`claims`, text)
        """)
        db.execSQL("INSERT INTO claims_fts(claims_fts) VALUES('rebuild')")
    }
}
```

## References
- [SQLite FTS4](https://www.sqlite.org/fts3.html)
- [Room FTS Support](https://developer.android.com/training/data-storage/room/defining-data#fts)
- [FTS Injection Prevention](https://cheatsheetseries.owasp.org/cheatsheets/Query_Parameterization_Cheat_Sheet.html)
