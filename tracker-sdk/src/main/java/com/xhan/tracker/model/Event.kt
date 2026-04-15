package com.xhan.tracker.model

/**
 * 추적할 이벤트를 나타내는 데이터 클래스.
 *
 * @property name 이벤트 이름 (예: "button_click", "purchase")
 * @property properties 이벤트에 첨부할 추가 속성 (키-값 쌍)
 * @property timestamp 이벤트 발생 시각 (밀리초 단위, 기본값: 현재 시각)
 */
data class Event(
    val name: String,
    val properties: Map<String, Any> = emptyMap(),
    val timestamp: Long = System.currentTimeMillis()
)
