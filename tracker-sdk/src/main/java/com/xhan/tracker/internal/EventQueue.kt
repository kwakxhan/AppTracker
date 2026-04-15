package com.xhan.tracker.internal

import android.util.Log
import com.xhan.tracker.AppTrackerConfig
import com.xhan.tracker.model.Event
import java.util.concurrent.Executors
import java.util.concurrent.ScheduledExecutorService
import java.util.concurrent.TimeUnit
import java.util.concurrent.locks.ReentrantLock

/**
 * 이벤트를 큐에 쌓고, 일정 조건이 되면 네트워크로 전송하는 내부 클래스.
 *
 * - ReentrantLock 사용 이유: @Synchronized보다 세밀한 제어가 가능하다.
 *   (tryLock, 공정성 설정, 다중 Condition 등)
 * - ScheduledExecutorService로 주기적 자동 전송을 수행한다.
 *
 * @param config SDK 설정
 * @param networkClient 이벤트를 전송할 네트워크 클라이언트
 * @param onQueueChanged 큐 상태가 변경될 때마다 호출되는 콜백
 */
internal class EventQueue(
    private val config: AppTrackerConfig,
    private val networkClient: NetworkClient,
    private val onQueueChanged: (List<Event>) -> Unit
) {
    // 이벤트를 보관하는 큐
    private val queue = mutableListOf<Event>()

    // 스레드 안전을 위한 락
    private val lock = ReentrantLock()

    // flush 중복 실행 방지 플래그
    @Volatile
    private var isFlushing = false

    // 주기적 flush를 위한 단일 스레드 스케줄러
    private val executor: ScheduledExecutorService = Executors.newSingleThreadScheduledExecutor { r ->
        Thread(r, "AppTracker-Flush").apply { isDaemon = true }
    }

    init {
        // 설정된 주기(flushInterval)마다 자동으로 전송 시도
        executor.scheduleAtFixedRate(
            { flushInternal() },
            config.flushInterval,
            config.flushInterval,
            TimeUnit.MILLISECONDS
        )
    }

    /**
     * 이벤트를 큐에 추가한다.
     * 큐 크기가 flushEventCount 이상이면 자동으로 flush를 트리거한다.
     */
    fun enqueue(event: Event) {
        lock.lock()
        try {
            queue.add(event)
            if (config.isDebug) {
                Log.d("EventQueue", "Enqueued: ${event.name} (queue size: ${queue.size})")
            }
            val snapshot = queue.toList()
            val shouldFlush = queue.size >= config.flushEventCount && !isFlushing
            onQueueChanged(snapshot) // 큐 변경 알림
            if (shouldFlush) {
                // 임계치 도달 시 백그라운드에서 전송 (flush 중이 아닐 때만)
                isFlushing = true
                executor.submit { flushInternal() }
            }
        } finally {
            lock.unlock()
        }
    }

    /**
     * 수동으로 큐의 모든 이벤트를 전송한다.
     * 백그라운드 스레드에서 실행되므로 호출자를 블로킹하지 않는다.
     */
    fun flush() {
        executor.submit { flushInternal() }
    }

    /**
     * 스케줄러를 종료하고 리소스를 해제한다.
     * 최대 5초간 진행 중인 작업 완료를 기다린다.
     */
    fun shutdown() {
        executor.shutdown()
        executor.awaitTermination(5, TimeUnit.SECONDS)
    }

    /**
     * 현재 큐에 쌓인 이벤트의 복사본을 반환한다. (스레드 안전)
     */
    fun getSnapshot(): List<Event> {
        lock.lock()
        try {
            return queue.toList()
        } finally {
            lock.unlock()
        }
    }

    /**
     * 실제 전송 로직. 큐에서 이벤트를 꺼내 네트워크로 전송한다.
     * 전송 실패 시 이벤트를 다시 큐에 넣어 재시도할 수 있도록 한다.
     */
    private fun flushInternal() {
        try {
            val eventsToSend: List<Event>
            lock.lock()
            try {
                if (queue.isEmpty()) return // 보낼 이벤트가 없으면 종료
                eventsToSend = queue.toList()
                queue.clear()
            } finally {
                lock.unlock()
            }

            if (config.isDebug) {
                Log.d("EventQueue", "Flushing ${eventsToSend.size} events")
            }

            // 네트워크 전송 시도
            val success = networkClient.send(eventsToSend)

            // 전송 실패 시 이벤트를 큐 앞에 다시 삽입
            if (!success) {
                lock.lock()
                try {
                    queue.addAll(0, eventsToSend)
                } finally {
                    lock.unlock()
                }
            }

            // 큐 상태 변경 알림
            lock.lock()
            try {
                onQueueChanged(queue.toList())
            } finally {
                lock.unlock()
            }
        } finally {
            // flush 완료 후 플래그 해제
            isFlushing = false

            // flush 중에 쌓인 이벤트가 임계치 이상이면 다시 flush
            lock.lock()
            try {
                if (queue.size >= config.flushEventCount && !isFlushing) {
                    isFlushing = true
                    executor.submit { flushInternal() }
                }
            } finally {
                lock.unlock()
            }
        }
    }
}
