package com.chweibo.android.utils

import android.os.SystemClock
import android.util.Log
import com.chweibo.android.data.api.RateLimitStatus
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicLong
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RateLimitManager @Inject constructor() {
    private val TAG = "RateLimitManager"

    private val API_CALL_INTERVALS = mapOf(
        "home_timeline" to 5000L,
        "user_timeline" to 5000L,
        "comments" to 3000L,
        "post" to 10000L,
        "like" to 1000L,
        "default" to 1000L
    )

    private val lastCallTimes = ConcurrentHashMap<String, AtomicLong>()

    private val _rateLimitStatus = MutableStateFlow<RateLimitStatus?>(null)
    val rateLimitStatus: StateFlow<RateLimitStatus?> = _rateLimitStatus.asStateFlow()

    private val _isInCooldown = MutableStateFlow(false)
    val isInCooldown: StateFlow<Boolean> = _isInCooldown.asStateFlow()

    fun canMakeCall(apiName: String): Pair<Boolean, Long> {
        val interval = API_CALL_INTERVALS[apiName] ?: API_CALL_INTERVALS["default"]!!
        val now = SystemClock.elapsedRealtime()
        val lastCall = lastCallTimes[apiName]?.get() ?: 0L
        val timeSinceLastCall = now - lastCall
        return if (timeSinceLastCall >= interval) {
            Pair(true, 0L)
        } else {
            Pair(false, interval - timeSinceLastCall)
        }
    }

    fun recordCall(apiName: String) {
        lastCallTimes[apiName] = AtomicLong(SystemClock.elapsedRealtime())
        Log.d(TAG, "Recorded call for $apiName")
    }

    fun updateRateLimitStatus(status: RateLimitStatus) {
        _rateLimitStatus.value = status
        Log.d(TAG, "Rate limit updated: ${status.remaining_user_hits}/${status.user_limit} remaining")
    }

    fun hasRemainingCalls(): Boolean {
        val status = _rateLimitStatus.value
        return status == null || status.remaining_user_hits > 5
    }

    fun getRemainingCalls(): Int {
        return _rateLimitStatus.value?.remaining_user_hits ?: Int.MAX_VALUE
    }

    fun getResetTimeInSeconds(): Int {
        return _rateLimitStatus.value?.reset_time_in_seconds ?: 0
    }

    fun setCooldown(active: Boolean) {
        _isInCooldown.value = active
    }

    fun getWaitMessage(apiName: String): String {
        val (_, waitTime) = canMakeCall(apiName)
        return when {
            waitTime > 60000 -> "请等待 ${waitTime / 60000} 分钟后再试"
            waitTime > 1000 -> "请等待 ${waitTime / 1000} 秒后再试"
            else -> "操作太频繁，请稍后再试"
        }
    }

    fun reset() {
        lastCallTimes.clear()
        _rateLimitStatus.value = null
        _isInCooldown.value = false
        Log.d(TAG, "Rate limit manager reset")
    }
}
