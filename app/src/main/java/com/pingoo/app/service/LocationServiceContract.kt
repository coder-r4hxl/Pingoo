package com.pingoo.app.service

/**
 * Placeholder interface for location monitoring service to be implemented in Phase 2.
 * Serves as the clean architecture boundary between background location processing
 * and application domain/UI layers.
 */
interface LocationServiceContract {
    fun startMonitoring()
    fun stopMonitoring()
    fun isMonitoring(): Boolean
}
