package com.xhan.tracker

/**
 * AppTracker SDK의 설정을 담는 데이터 클래스.
 *
 * @property appKey 앱 식별 키
 * @property baseUrl 서버 기본 URL (기본값: "https://api.apptracker.com")
 * @property flushInterval 자동 전송 주기 (밀리초, 기본값: 30초)
 * @property flushEventCount 이 개수 이상 쌓이면 자동 전송 (기본값: 10)
 * @property isDebug true이면 디버그 로그 출력
 */
data class AppTrackerConfig(
    val appKey: String,
    val baseUrl: String = "https://api.apptracker.com",
    val flushInterval: Long = 30_000L,
    val flushEventCount: Int = 10,
    val isDebug: Boolean = false
) {
    /**
     * AppTrackerConfig를 단계적으로 구성하기 위한 빌더 클래스.
     * @param appKey 필수 앱 식별 키
     */
    class Builder(private val appKey: String) {
        private var baseUrl: String = "https://api.apptracker.com"
        private var flushInterval: Long = 30_000L
        private var flushEventCount: Int = 10
        private var isDebug: Boolean = false

        /** 서버 기본 URL 설정 */
        fun baseUrl(url: String) = apply { this.baseUrl = url }

        /** 자동 전송 주기 설정 (밀리초) */
        fun flushInterval(ms: Long) = apply { this.flushInterval = ms }

        /** 자동 전송 트리거 이벤트 개수 설정 */
        fun flushEventCount(count: Int) = apply { this.flushEventCount = count }

        /** 디버그 모드 활성화 여부 설정 */
        fun debug(enabled: Boolean) = apply { this.isDebug = enabled }

        /** 설정값으로 AppTrackerConfig 인스턴스 생성 */
        fun build() = AppTrackerConfig(appKey, baseUrl, flushInterval, flushEventCount, isDebug)
    }
}
