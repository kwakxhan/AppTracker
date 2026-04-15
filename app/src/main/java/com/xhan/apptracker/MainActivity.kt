package com.xhan.apptracker

import android.os.Bundle
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.xhan.tracker.AppTracker
import com.xhan.tracker.AppTrackerConfig
import com.xhan.tracker.model.Event
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * 디버그 패널 화면.
 * 이벤트 트래킹 버튼, 큐 상태, 전송 로그를 실시간으로 확인할 수 있다.
 */
class MainActivity : AppCompatActivity() {

    private lateinit var tvQueueStatus: TextView   // 큐 상태 표시 ("이벤트 큐 (N/10)")
    private lateinit var llQueueList: LinearLayout  // 큐에 쌓인 이벤트 목록 표시 영역
    private lateinit var tvFlushLog: TextView       // 전송 로그 표시 영역

    // 시간 포맷 (HH:mm:ss)
    private val timeFormat = SimpleDateFormat("HH:mm:ss", Locale.getDefault())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // UI 요소 바인딩
        tvQueueStatus = findViewById(R.id.tvQueueStatus)
        llQueueList = findViewById(R.id.llQueueList)
        tvFlushLog = findViewById(R.id.tvFlushLog)

        // AppTracker를 큐 변경 콜백과 함께 재초기화
        AppTracker.shutdown()
        AppTracker.initialize(
            context = applicationContext,
            config = AppTrackerConfig.Builder("sample-app-key")
                .debug(true)
                .flushEventCount(10)
                .flushInterval(30_000L)
                .build(),
            onQueueChanged = { events -> onQueueChanged(events) }
        )

        // 버튼 클릭 이벤트 트래킹
        findViewById<Button>(R.id.btnClick).setOnClickListener {
            AppTracker.trackEvent("button_click", mapOf("button_id" to "main_cta"))
        }

        // 구매 이벤트 트래킹
        findViewById<Button>(R.id.btnPurchase).setOnClickListener {
            AppTracker.trackEvent("purchase", mapOf("amount" to 9900, "currency" to "KRW"))
        }

        // 로그인 이벤트 트래킹
        findViewById<Button>(R.id.btnLogin).setOnClickListener {
            AppTracker.trackEvent("login", mapOf("method" to "google"))
        }

        // 페이지뷰 이벤트 트래킹
        findViewById<Button>(R.id.btnPageView).setOnClickListener {
            AppTracker.trackEvent("page_view", mapOf("screen" to "home"))
        }

        // 수동 전송 버튼: 큐의 모든 이벤트를 즉시 전송
        findViewById<Button>(R.id.btnFlush).setOnClickListener {
            val count = llQueueList.childCount // 현재 큐에 쌓인 이벤트 수
            AppTracker.flush()
            runOnUiThread {
                val time = timeFormat.format(Date())
                val log = tvFlushLog.text.toString()
                tvFlushLog.text = "${log}✓ Flushed $count events $time\n"
            }
        }
    }

    /**
     * 큐 상태가 변경될 때 호출되는 콜백.
     * UI 스레드에서 큐 카운터와 이벤트 목록을 갱신한다.
     */
    private fun onQueueChanged(events: List<Event>) {
        runOnUiThread {
            // 큐 카운터 업데이트
            tvQueueStatus.text = "── 이벤트 큐 (${events.size}/10) ──"

            // 큐 목록 갱신: 각 이벤트의 이름과 시간을 표시
            llQueueList.removeAllViews()
            for (event in events) {
                val tv = TextView(this)
                val time = timeFormat.format(Date(event.timestamp))
                tv.text = "${event.name} — $time"
                tv.setPadding(0, 4, 0, 4)
                llQueueList.addView(tv)
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        // 액티비티 종료 시 SDK 리소스 해제
        AppTracker.shutdown()
    }
}
