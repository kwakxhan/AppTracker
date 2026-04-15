# AppTracker SDK

Android 앱 이벤트 트래킹 SDK. 외부 라이브러리 없이 동작합니다.

## 구현 영상

1. 큐에 10개의 이벤트가 쌓이면 서버에 전송됩니다. [동영상](https://github.com/user-attachments/assets/6ab69885-03e6-4513-a2bd-2d4397b9e310)

2. 30초가 지나면 큐에 쌓인 이벤트를 서버에 전송합니다. [동영상](https://github.com/user-attachments/assets/b894720d-efb6-459e-a470-c0645fa71c94)

3. Flush 버튼을 클릭 시 큐에 쌓인 이벤트를 즉시 전송합니다. [동영상](https://github.com/user-attachments/assets/39b412b7-b85e-4014-837b-5a94de75aa6c)


## 설치

```kotlin
// app/build.gradle.kts
dependencies {
    implementation("com.xhan.tracker:tracker-sdk:1.0.0")
}
```

`AndroidManifest.xml`에 인터넷 권한을 추가합니다:

```xml
<uses-permission android:name="android.permission.INTERNET" />
```

## 사용법

### 1. 초기화

```kotlin
// Application.onCreate()에서 호출
AppTracker.initialize(
    context = this,
    config = AppTrackerConfig.Builder("your-app-key")
        .baseUrl("https://your-server.com")
        .build()
)
```

### 2. 이벤트 전송

```kotlin
AppTracker.trackEvent("button_click")

AppTracker.trackEvent("purchase", mapOf(
    "amount" to 9900,
    "currency" to "KRW"
))
```

### 3. 수동 전송 / 종료

```kotlin
AppTracker.flush()    // 즉시 전송
AppTracker.shutdown() // SDK 종료
```

## 설정 옵션

```kotlin
AppTrackerConfig.Builder("your-app-key")
    .baseUrl("https://your-server.com") // 서버 URL
    .flushEventCount(10)                // N개 쌓이면 자동 전송 (기본: 10)
    .flushInterval(30_000L)             // 자동 전송 주기 ms (기본: 30초)
    .debug(true)                        // 디버그 로그 (기본: false)
    .build()
```

## 서버 연동

`POST {baseUrl}/events`로 아래 JSON이 전송됩니다:

```json
{
  "events": [
    {
      "name": "purchase",
      "properties": { "amount": 9900, "currency": "KRW" },
      "timestamp": 1700000000000
    }
  ]
}
```

## 테스트

네트워크 없이 테스트하려면 `FakeNetworkClient`를 주입합니다:

```kotlin
AppTracker.initialize(
    context = this,
    config = config,
    networkClient = FakeNetworkClient()
)
```

## 라이선스

MIT License
