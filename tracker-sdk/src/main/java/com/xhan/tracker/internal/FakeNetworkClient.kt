package com.xhan.tracker.internal

import android.util.Log
import com.xhan.tracker.model.Event

/**
 * 테스트 및 개발용 가짜 네트워크 클라이언트.
 * 실제 서버 통신 없이 300ms 딜레이만 시뮬레이션하고 항상 성공을 반환한다.
 */
internal class FakeNetworkClient : NetworkClient {
    override fun send(events: List<Event>): Boolean {
        Log.d("FakeNetwork", "Sending ${events.size} events")
        Thread.sleep(300) // 네트워크 지연 시뮬레이션
        return true
    }
}
