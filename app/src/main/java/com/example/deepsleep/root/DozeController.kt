package com.example.deepsleep.root

import com.example.deepsleep.model.DozeState
import kotlinx.coroutines.delay

object DozeController {

    suspend fun getState(): DozeState {
        val output = RootCommander.exec(
            "dumpsys deviceidle | grep 'mState=' | head -1"
        ).out.firstOrNull() ?: ""

        return when {
            output.contains("ACTIVE") -> DozeState.ACTIVE
            output.contains("INACTIVE") -> DozeState.INACTIVE
            output.contains("IDLE_MAINTENANCE") -> DozeState.IDLE_MAINTENANCE
            output.contains("IDLE") -> DozeState.IDLE
            else -> DozeState.UNKNOWN
        }
    }

    suspend fun enterDeepSleep(): Boolean {
        RootCommander.exec("dumpsys deviceidle force-idle")
        delay(3000)
        return getState() == DozeState.IDLE
    }

    suspend fun exitDeepSleep(): Boolean {
        RootCommander.exec("dumpsys deviceidle unforce")
        delay(2000)
        return getState() == DozeState.ACTIVE
    }

    suspend fun disableMotion(): Boolean {
        return RootCommander.exec("dumpsys deviceidle disable motion").isSuccess
    }

    suspend fun enableMotion(): Boolean {
        return RootCommander.exec("dumpsys deviceidle enable motion").isSuccess
    }

    suspend fun backupMotionState(): String {
        val output = RootCommander.exec(
            "dumpsys deviceidle enabled motion 2>&1"
        ).out.joinToString()

        return if (output.contains("false", ignoreCase = true)) "disabled" else "enabled"
    }

    suspend fun restoreMotionState(state: String) {
        if (state == "disabled") disableMotion() else enableMotion()
    }

    // 新增：强制进入 idle 模式（供 DeepSleepController 调用）
    suspend fun forceIdle(): Boolean {
        return RootCommander.exec("dumpsys deviceidle force-idle").isSuccess
    }

    // 新增：手动步进 Doze 状态（供 DeepSleepController 调用）
    suspend fun step(): Boolean {
        return RootCommander.exec("dumpsys deviceidle step").isSuccess
    }
}