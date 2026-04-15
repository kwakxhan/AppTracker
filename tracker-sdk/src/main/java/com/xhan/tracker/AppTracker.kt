package com.xhan.tracker

import android.content.Context
import android.util.Log
import com.xhan.tracker.internal.EventQueue
import com.xhan.tracker.internal.FakeNetworkClient
import com.xhan.tracker.model.Event

/**
 * AppTracker SDK의 공개 API 진입점.
 * 싱글톤 객체로 앱 전체에서 하나의 인스턴스만 사용된다.
 *
 * 사용 순서: initialize() → trackEvent() → flush()/shutdown()
 */
object AppTracker {

    // 멀티스레드 환경에서 초기화 상태의 가시성을 보장하기 위해 @Volatile 사용
    @Volatile
    private var isInitialized = false
    private lateinit var config: AppTrackerConfig
    private lateinit var eventQueue: EventQueue

    /**
     * SDK를 초기화한다. Application.onCreate()에서 반드시 한 번 호출해야 한다.
     *
     * @param context 애플리케이션 컨텍스트
     * @param config SDK 설정 (앱 키, flush 주기 등)
     * @param onQueueChanged 큐 상태 변경 시 호출되는 콜백 (UI 업데이트 등에 활용)
     */
    fun initialize(
        context: Context,
        config: AppTrackerConfig,
        onQueueChanged: (List<Event>) -> Unit = {}
    ) {
        if (isInitialized) {
            Log.w("AppTracker", "Already initialized. Ignoring duplicate call.")
            return
        }
        this.config = config
        this.eventQueue = EventQueue(config, FakeNetworkClient(), onQueueChanged)
        isInitialized = true
        if (config.isDebug) {
            Log.d("AppTracker", "Initialized with appKey=${config.appKey}")
        }
    }

    /**
     * 사용자 이벤트를 추적한다.
     *
     * @param name 이벤트 이름 (예: "button_click", "purchase")
     * @param properties 이벤트에 첨부할 추가 속성 (키-값 쌍)
     */
    fun trackEvent(name: String, properties: Map<String, Any> = emptyMap()) {
        checkInitialized()
        eventQueue.enqueue(Event(name, properties))
    }

    /**
     * 큐에 쌓인 이벤트를 즉시 서버로 전송한다.
     */
    fun flush() {
        checkInitialized()
        eventQueue.flush()
    }

    /**
     * SDK를 종료하고 리소스를 해제한다.
     * 종료 후 다시 사용하려면 initialize()를 재호출해야 한다.
     */
    fun shutdown() {
        checkInitialized()
        eventQueue.shutdown()
        isInitialized = false
    }

    /**
     * 초기화 여부를 확인하고, 초기화되지 않았으면 예외를 던진다.
     */
    private fun checkInitialized() {
        if (!isInitialized) throw IllegalStateException("AppTracker is not initialized. Call initialize() first.")
    }
}
