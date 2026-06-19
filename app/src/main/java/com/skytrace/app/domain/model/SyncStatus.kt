package com.skytrace.app.domain.model

/**
 * Tracks the status of data catalog syncs.
 */
data class SyncStatus(
    val catalogName: String,
    val lastSyncTime: Long? = null,
    val objectCount: Int = 0,
    val state: SyncState = SyncState.NEVER_SYNCED,
    val errorMessage: String? = null
)

enum class SyncState {
    NEVER_SYNCED,
    SYNCING,
    SYNCED,
    FAILED,
    OUTDATED
}
