package com.xhan.apptracker

import android.app.Application
import com.xhan.tracker.AppTracker
import com.xhan.tracker.AppTrackerConfig
import com.xhan.tracker.internal.FakeNetworkClient

/**
 * 앱 시작 시 가장 먼저 실행되는 Application 클래스.
 * AppTracker SDK를 여기서 초기화한다.
 */
class AppTrackerApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        // SDK 초기화: 실제 webhook 엔드포인트로 이벤트 전송
        // networkClient를 주입하지 않으므로 HttpNetworkClient + RetryNetworkClient 자동 사용
        AppTracker.initialize(
            context = this,
            config = AppTrackerConfig.Builder("sample-app-key")
                .debug(true)          // 디버그 로그 활성화
                .flushEventCount(10)  // 10개 이벤트 쌓이면 자동 전송
                .flushInterval(30_000L) // 30초마다 자동 전송
                .baseUrl("https://webhook.site/84b44f2f-7367-4e95-a283-e31542f206e7")
                .build()
        )
    }
}
