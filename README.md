# AppTracker SDK

Android 앱에서 사용자 이벤트를 수집하고 서버로 전송하는 경량 트래킹 SDK입니다.

---

## 설치 방법

`app/build.gradle.kts`에 SDK 모듈 의존성을 추가합니다:

```kotlin
dependencies {
    implementation(project(":tracker-sdk"))
}
```

---

## 초기화

`Application.onCreate()`에서 SDK를 초기화합니다:

```kotlin
class MyApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        AppTracker.initialize(
            context = this,
            config = AppTrackerConfig.Builder("your-app-key")
                .baseUrl("https://your-server.com")
                .debug(true)
                .flushEventCount(10)
                .flushInterval(30_000L)
                .build()
        )
    }
}
```

테스트 시에는 `FakeNetworkClient`를 주입할 수 있습니다:

```kotlin
AppTracker.initialize(
    context = this,
    config = config,
    networkClient = FakeNetworkClient()
)
```

---

## 이벤트 트래킹

```kotlin
// 기본 이벤트
AppTracker.trackEvent("button_click")

// 속성 포함 이벤트
AppTracker.trackEvent("purchase", mapOf(
    "amount" to 9900,
    "currency" to "KRW"
))

// 수동 전송
AppTracker.flush()

// SDK 종료
AppTracker.shutdown()
```

---

## 아키텍처

```
앱 → AppTracker.trackEvent()
      ↓
  EventQueue (thread-safe, ReentrantLock)
      ↓ (자동: 10개 도달 or 30초 주기 / 수동: flush())
  RetryNetworkClient (지수 백오프: 1초 → 2초 → 4초)
      ↓
  HttpNetworkClient (HttpURLConnection)
      ↓
  서버 (POST /events)
```

---

## 주요 설계 결정

### ReentrantLock 사용 (vs @Synchronized)
`@Synchronized`는 단순하지만 `tryLock`, 공정성 설정, 다중 Condition 등 세밀한 제어가 불가능합니다.
`ReentrantLock`은 이벤트 큐처럼 여러 스레드에서 동시 접근하는 자원을 더 유연하게 관리할 수 있습니다.

### Decorator 패턴 (RetryNetworkClient)
`RetryNetworkClient`는 기존 `NetworkClient`를 감싸는 Decorator 패턴으로 구현되어,
재시도 로직과 실제 전송 로직을 분리합니다. 이를 통해:
- `HttpNetworkClient`는 순수하게 HTTP 전송만 담당
- 재시도 정책을 독립적으로 변경 가능
- 테스트 시 각각을 개별적으로 검증 가능

### NetworkClient 외부 주입
`initialize()`에서 `networkClient` 파라미터를 통해 커스텀 구현체를 주입할 수 있어,
테스트 환경에서는 `FakeNetworkClient`, 프로덕션에서는 `HttpNetworkClient`를 사용합니다.
