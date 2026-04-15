package com.xhan.tracker.internal

import android.util.Log
import com.xhan.tracker.model.Event

/**
 * 네트워크 전송 실패 시 재시도를 수행하는 Decorator 패턴 래퍼.
 * 기존 NetworkClient를 감싸서 지수 백오프(1초→2초→4초) 방식으로 재시도한다.
 *
 * @param delegate 실제 전송을 담당하는 NetworkClient
 * @param maxRetries 최대 재시도 횟수 (기본값: 3)
 * @param isDebug true이면 재시도 로그 출력
 */
internal class RetryNetworkClient(
    private val delegate: NetworkClient,
    private val maxRetries: Int = 3,
    private val isDebug: Boolean = false
) : NetworkClient {

    override fun send(events: List<Event>): Boolean {
        // 첫 번째 시도
        if (delegate.send(events)) return true

        // 실패 시 재시도
        for (attempt in 1..maxRetries) {
            // 지수 백오프: 1초, 2초, 4초, ...
            val delayMs = (1000L * (1L shl (attempt - 1)))

            if (isDebug) {
                Log.d("RetryNetworkClient", "재시도 $attempt/$maxRetries (${delayMs}ms 후)")
            }

            Thread.sleep(delayMs)

            if (delegate.send(events)) {
                if (isDebug) {
                    Log.d("RetryNetworkClient", "재시도 $attempt 성공")
                }
                return true
            }
        }

        if (isDebug) {
            Log.e("RetryNetworkClient", "모든 재시도 실패 (${maxRetries}회)")
        }
        return false
    }
}
