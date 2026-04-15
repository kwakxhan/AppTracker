package com.xhan.tracker.internal

import com.xhan.tracker.model.Event

/**
 * 이벤트를 서버로 전송하는 네트워크 클라이언트 인터페이스.
 * 실제 구현체와 테스트용 구현체를 교체할 수 있도록 추상화한다.
 */
internal interface NetworkClient {
    /**
     * 이벤트 목록을 서버로 전송한다.
     * @return 전송 성공 여부
     */
    fun send(events: List<Event>): Boolean
}
