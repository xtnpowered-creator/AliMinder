# Room Database Implementation Plan
**Phase 2: Data Layer Foundation**

> **⚠️ IMPORTANT CONTEXT FOR IMPLEMENTATION**:  
> This plan assumes **Phase 1 (UI) is already complete**.  
> Do NOT modify any existing UI files unless explicitly stated in this plan.

Replace hardcoded `MockData.kt` with persistent Room database storage.

---

## Naming Conventions Review

### ✅ APPROVED Names

| Entity/Class | Rationale |
|--------------|-----------|
| `DutyEntity` | Matches UI ("All Upcoming Duties"), inclusive of events/tasks/pending |
| `DutyDao` | Consistent with entity name |
| `DutyRepository` | Repository for duty data |
| `UserSettingsEntity` | Clear, standard naming for settings singleton |
| `AliMinderDatabase` | App-specific, identifies as the main database |
| `DatabaseInitializer` | Clear purpose, standard naming |

### ⚠️ FIELD Names Reviewed

| Field | Final Name | Rationale |
|-------|-----------|-----------|
| `provider` | ✅ `provider` | Clear: MICROSOFT_365, GOOGLE, SHADOW |
| `sourceType` | ✅ `sourceType` | Distinguishes CALENDAR_EVENT vs TASK vs MEETING |
| ~~`responseStatus`~~ | ✅ `acceptanceStatus` | More accurate for invites |
| `isDeleted` | ✅ `isDeleted` | Standard soft delete flag |
| ~~`providerEventId`~~ | ✅ `providerDutyId` | Consistent with "duty" terminology |

### 📝 Naming Strategy

**Database Layer** (new files):
- Use `Duty` prefix: `DutyEntity`, `DutyDao`, `DutyRepository`

**Domain Model** (existing):
- Keep `Event.kt` unchanged for now
- Future manual rename: Event → Duty (optional, not required for Phase 2)

**Why**: AS agent cannot rename files. Database layer uses Duty naming independently.

---

## Goal

Create a local Room database that:
- Stores events persistently across app restarts
- Supports the normalized schema for M365/Google/Shadow events
- Provides a foundation for future calendar sync
- Enables testing PoNR calculations with realistic data

---

## Implementation Details

See full implementation plan for:
- Complete entity schemas with all fields
- DAO interfaces with Flow-based queries
- Repository pattern with domain/data separation
- Hilt dependency injection modules
- Database initialization with mock data
- ViewModel integration examples
- Complete code examples for all files

---

## Files Summary

### ✅ NEW Files (11) - CREATE THESE
1. `app/src/main/kotlin/com/aliminder/app/data/local/entity/DutyEntity.kt`
2. `app/src/main/kotlin/com/aliminder/app/data/local/entity/UserSettingsEntity.kt`
3. `app/src/main/kotlin/com/aliminder/app/data/local/dao/DutyDao.kt`
4. `app/src/main/kotlin/com/aliminder/app/data/local/dao/UserSettingsDao.kt`
5. `app/src/main/kotlin/com/aliminder/app/data/local/AliMinderDatabase.kt`
6. `app/src/main/kotlin/com/aliminder/app/data/local/Converters.kt`
7. `app/src/main/kotlin/com/aliminder/app/domain/repository/DutyRepository.kt`
8. `app/src/main/kotlin/com/aliminder/app/data/repository/DutyRepositoryImpl.kt`
9. `app/src/main/kotlin/com/aliminder/app/data/mapper/DutyMapper.kt`
10. `app/src/main/kotlin/com/aliminder/app/di/DatabaseModule.kt`
11. `app/src/main/kotlin/com/aliminder/app/data/local/DatabaseInitializer.kt`

### ⚠️ MODIFY Files (2) - UPDATE THESE
1. `app/src/main/kotlin/com/aliminder/app/presentation/screens/all/AllViewModel.kt`
2. `app/build.gradle.kts`

### ❌ NO FILES TO RENAME OR DELETE

**Battery Impact**: <0.2% daily  
**Estimated effort**: 2-3 hours

---

## Key Implementation Steps

**Step 1**: ✅ Room dependencies already in `build.gradle.kts`

**Step 2**: Create `DutyEntity.kt` with all fields

**Step 3**: Create `UserSettingsEntity.kt`

**Step 4**: Create `DutyDao.kt` with Flow-based queries

**Step 5**: Create `UserSettingsDao.kt`

**Step 6**: Create `AliMinderDatabase.kt`

**Step 7**: Create `DutyRepository.kt` interface

**Step 8**: Create `DutyRepositoryImpl.kt` implementation

**Step 9**: Create `DutyMapper.kt` (maps `DutyEntity` ↔ `Event` domain model)

**Step 10**: Create `DatabaseModule.kt` for Hilt DI

**Step 11**: Create `DatabaseInitializer.kt` for seed data

**Step 12**: Update `AllViewModel.kt` to use `DutyRepository`

**Step 13**: Test with Database Inspector

---

## Important Mapper Note

The mapper will convert between **database naming** and **domain naming**:

```kotlin
// DutyMapper.kt
fun DutyEntity.toDomainEvent(): Event {  // Database → Domain
    return Event(
        id = id,
        title = title,
        // ... map fields
    )
}

fun Event.toDutyEntity(): DutyEntity {  // Domain → Database
    return DutyEntity(
        id = id,
        title = title,
        // ... map fields  
    )
}
```

This allows database layer to use "Duty" while domain/UI still uses "Event".

---

## sourceType Values (Granular)

For future provider integration, `sourceType` supports:

**Microsoft 365**:
- `M365_CALENDAR_EVENT`
- `M365_TEAMS_MEETING`
- `M365_PLANNER_TASK`
- `M365_TODO`

**Google Workspace**:
- `GOOGLE_CALENDAR_EVENT`
- `GOOGLE_MEET`
- `GOOGLE_TASK`

**Shadow (Local)**:
- `SHADOW_EVENT`
- `SHADOW_TASK`
