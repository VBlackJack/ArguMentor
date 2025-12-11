# ADR 002: JSON Arrays for Many-to-Many Relationships

## Status
Accepted

## Date
2024-01-15

## Context

ArguMentor has several many-to-many relationships:
- **Topic ↔ Claim**: A claim can belong to multiple topics
- **Claim ↔ Fallacy**: A claim can have multiple identified fallacies
- **Topic ↔ Tag**: A topic can have multiple tags

The traditional relational approach would use junction tables:
```sql
CREATE TABLE topic_claim (
    topic_id TEXT,
    claim_id TEXT,
    PRIMARY KEY (topic_id, claim_id),
    FOREIGN KEY (topic_id) REFERENCES topics(id),
    FOREIGN KEY (claim_id) REFERENCES claims(id)
);
```

## Decision

We use **JSON arrays stored as TEXT** instead of junction tables for many-to-many relationships.

```kotlin
@Entity(tableName = "claims")
data class Claim(
    @PrimaryKey val id: String,
    val text: String,
    val topics: List<String> = emptyList(),      // JSON array of topic IDs
    val fallacyIds: List<String> = emptyList()   // JSON array of fallacy IDs
)
```

Room TypeConverters handle serialization:
```kotlin
@TypeConverter
fun fromStringList(value: List<String>): String = gson.toJson(value)

@TypeConverter
fun toStringList(value: String): List<String> = gson.fromJson(value, ...)
```

## Rationale

### Advantages of JSON Arrays

1. **Simpler Schema**: No junction tables to manage
2. **Faster Reads**: No JOIN queries for common operations
3. **Atomic Updates**: Update entire relationship in one operation
4. **Easier Migration**: Adding new relationships doesn't require schema changes
5. **Export/Import**: Data is self-contained and portable

### Trade-offs Accepted

1. **No Referential Integrity**: Orphaned IDs possible if referenced entity deleted
   - *Mitigation*: Cascade deletes handled in repository layer

2. **No Reverse Queries**: Can't easily query "all topics for a tag" via SQL
   - *Mitigation*: Filter in code (acceptable for small datasets)

3. **No Database-Level Constraints**: Can't use UNIQUE or CHECK constraints
   - *Mitigation*: Validation in repository layer

### Why This Works for ArguMentor

1. **Read-Heavy Workload**: Most queries are "get topics → show their tags"
2. **Small Cardinality**: Topics typically have <10 tags, claims have <5 fallacies
3. **Rare Deletions**: Tags and fallacies are rarely deleted
4. **Local-Only**: No sync conflicts to resolve

## Consequences

### Positive
- 40% fewer database tables
- Simpler DAO queries
- Faster common operations
- Easier JSON import/export

### Negative
- Manual cleanup needed if referenced entities are deleted
- Complex queries require application-level filtering
- Cannot use SQL `json_each()` on older SQLite versions

### Monitoring
- Log warnings if orphaned IDs detected during import
- Periodic cleanup job to remove orphaned references (not implemented yet)

## Implementation Notes

### Querying by Tag
```kotlin
// In TopicRepository
fun getTopicsByTag(tag: String): Flow<List<Topic>> {
    return getAllTopics().map { topics ->
        topics.filter { it.tags.contains(tag) }
    }
}
```

### Validating IDs Before Insert
```kotlin
// In ClaimRepository
private val VALID_ID_PATTERN = "[a-zA-Z0-9_-]+".toRegex()

private fun validateIds(ids: List<String>, fieldName: String) {
    ids.forEach { id ->
        require(id.matches(VALID_ID_PATTERN)) {
            "Invalid $fieldName format: '$id'"
        }
    }
}
```

## Alternatives Considered

### Junction Tables
- **Rejected**: Too much overhead for simple use case
- Would require 3+ extra tables and complex JOINs

### Room @Relation
- **Rejected**: Still requires junction tables
- Adds complexity without benefit for JSON approach

### Embedded Objects
- **Rejected**: Doesn't support many-to-many

## References
- [Room TypeConverters](https://developer.android.com/training/data-storage/room/referencing-data)
- [SQLite JSON1 Extension](https://www.sqlite.org/json1.html)
