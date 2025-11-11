# Testing Recommendations for ArguMentor

This document outlines recommendations for improving test coverage across the codebase.

## Current State

**Existing Tests:**
- ✅ ClaimTest.kt
- ✅ TopicTest.kt
- ✅ ClaimDaoTest.kt
- ✅ TopicDaoTest.kt
- ✅ FallacyCatalogTest.kt
- ✅ TemplateLibraryTest.kt
- ✅ FingerprintUtilsTest.kt

**Coverage Gaps:**
- ❌ Entity tests: Evidence, Question, Source, Rebuttal, Tag
- ❌ DAO tests: Evidence, Question, Source, Rebuttal, Tag
- ❌ Repository tests: All repositories
- ❌ ViewModel tests: All ViewModels
- ❌ FTS search tests

## Priority Recommendations

### 🔴 High Priority (Critical Paths)

#### 1. Repository Tests
Test the data layer that orchestrates multiple DAOs and handles complex operations.

**Recommended tests:**
- `TopicRepositoryTest`: Test cascade deletion logic (complex manual cascade)
- `ClaimRepositoryTest`: Test FTS search with fallback, fingerprint deduplication
- `ImportExportRepositoryTest`: Test import/export with similarity matching

**Why:** These contain complex business logic that must work correctly.

#### 2. ViewModel Tests
Test the presentation logic that coordinates multiple repositories.

**Recommended tests:**
- `TopicCreateEditViewModel Test`: Test hasUnsavedChanges(), state management
- `ClaimCreateEditViewModel Test`: Test hasUnsavedChanges(), state management
- `HomeViewModel Test`: Test filtering logic, search debounce
- `DebateModeViewModel Test`: Test card loading optimization

**Why:** ViewModels contain user-facing logic with multiple edge cases.

### 🟡 Medium Priority (Data Integrity)

#### 3. Entity Tests
Test validation and business rules embedded in entities.

**Recommended tests:**
- `SourceTest`: Test reliabilityScore validation (0.0-1.0 constraint)
- `EvidenceTest`: Test Foreign Key relationships, CASCADE behavior
- `QuestionTest`: Test polymorphic targetId behavior

**Why:** Entities enforce invariants that must never be violated.

#### 4. DAO Tests
Test database operations, especially complex queries.

**Recommended tests:**
- `ClaimDaoTest`: Test json_each() queries (getClaimsByTopicId, etc.)
- `QuestionDaoTest`: Test deleteOrphanQuestions()
- `TagDaoTest`: Test FTS search (new functionality)
- `SourceDaoTest`: Test multi-field FTS search (title + citation)

**Why:** Complex SQL queries need verification, especially json_each() usage.

### ⚪ Low Priority (Nice to Have)

#### 5. Utility Tests
- SearchUtils tests (if/when implemented)
- Additional FingerprintUtils edge cases
- Converters tests (JSON serialization)

#### 6. Integration Tests
- Full Topic CRUD with cascade operations
- Import/Export round-trip tests
- FTS search across all entity types

## Testing Strategy

### For Repositories
```kotlin
@Test
fun `deleteTopic deletes claims and questions in cascade`() {
    // Given: Topic with claims and questions
    // When: deleteTopic() called
    // Then: All related entities deleted
}
```

### For ViewModels
```kotlin
@Test
fun `hasUnsavedChanges returns true when title changed`() {
    // Given: ViewModel with initial state
    // When: Title is modified
    // Then: hasUnsavedChanges() returns true
}
```

### For DAOs with FTS
```kotlin
@Test
fun `searchTagsFts returns matching tags`() {
    // Given: Tags in database
    // When: FTS search executed
    // Then: Correct tags returned in order
}
```

### For Complex Queries
```kotlin
@Test
fun `getClaimsByTopicId returns claims with json_each`() {
    // Given: Claims with topics as JSON array
    // When: Query executed
    // Then: Correct claims returned
}
```

## Target Coverage

**Recommended minimum coverage:**
- **Repositories**: 80% (focus on complex logic)
- **ViewModels**: 70% (focus on state management and validation)
- **DAOs**: 60% (focus on complex queries)
- **Entities**: 50% (focus on validation and constraints)

**Current coverage:** ~20% (estimated based on existing tests)

## Next Steps

1. **Phase 1 (2-3 days)**: Add Repository tests for TopicRepository, ClaimRepository
2. **Phase 2 (2-3 days)**: Add ViewModel tests for CreateEdit ViewModels
3. **Phase 3 (1-2 days)**: Add DAO tests for complex queries (json_each, FTS)
4. **Phase 4 (1 day)**: Add Entity tests for validation logic

**Total estimate:** 6-9 days to reach 70% coverage

## Tools & Setup

**Dependencies (already in build.gradle.kts):**
- JUnit 4
- Mockito
- AndroidX Test
- Coroutines Test
- Room Testing

**CI/CD Integration:**
- Add coverage reporting with JaCoCo
- Set minimum coverage threshold (60%)
- Fail builds below threshold

## Notes

- Focus on **behavior** not implementation details
- Prioritize tests for **complex logic** and **edge cases**
- Keep tests **fast** (use in-memory database)
- Make tests **independent** (no shared state)
- Follow **AAA pattern** (Arrange, Act, Assert)
