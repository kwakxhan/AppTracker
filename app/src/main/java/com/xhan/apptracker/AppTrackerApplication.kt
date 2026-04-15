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
        // SDK 초기화: 앱 키, 디버그 모드, flush 설정 등을 빌더로 구성
        // 실제 서버가 없으므로 FakeNetworkClient를 주입하여 테스트
        AppTracker.initialize(
            context = this,
            config = AppTrackerConfig.Builder("sample-app-key")
                .debug(true)          // 디버그 로그 활성화
                .flushEventCount(10)  // 10개 이벤트 쌓이면 자동 전송
                .flushInterval(30_000L) // 30초마다 자동 전송
                .build(),
            networkClient = FakeNetworkClient() // 테스트용 가짜 네트워크 클라이언트
        )
    }
}
