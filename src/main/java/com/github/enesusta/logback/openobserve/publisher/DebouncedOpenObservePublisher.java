package com.github.enesusta.logback.openobserve.publisher;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Date;
import java.util.Queue;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public final class DebouncedOpenObservePublisher<T> implements Runnable {

  private final Queue<T> events = new ArrayBlockingQueue<>(10);
  private static final AtomicInteger THREAD_COUNTER = new AtomicInteger(1);
  public static final String THREAD_NAME_PREFIX = "openobserve-writer-";

  // private static final DebounceExecutor<Void> debounceExecutor = new
  // DebounceExecutor<>();
  private final ScheduledExecutorService scheduledExecutorService = Executors.newScheduledThreadPool(1);
  private static DebouncedOpenObservePublisher<?> instance;

  private DebouncedOpenObservePublisher() {
  }

  public static DebouncedOpenObservePublisher<?> singleton() {
    if (instance == null) {
      instance = new DebouncedOpenObservePublisher<>();
    }
    return instance;
  }

  public void callback() {
    try {
      final var objectMapper = new ObjectMapper();
      System.out.println(objectMapper.writeValueAsString(events));
      events.clear();
    } catch (final Throwable e) {
      System.out.println("PUBLISH_EVENT" + e);
    }
  }

  public void start() {
    // Thread thread = new Thread(this, THREAD_NAME_PREFIX +
    // THREAD_COUNTER.getAndIncrement());
    // thread.start();
    run();
  }

  public void addEvent(final T event) {
    events.add(event);
  }

  @Override
  public void run() {
    try {
      System.out.println(instance);
      if (instance != null) {
        scheduledExecutorService.scheduleAtFixedRate(
            () -> {
              System.out.println(new Date());
              callback();
            },
            2,
            1,
            TimeUnit.SECONDS);

        // debounceExecutor.debounce(
        // 500,
        // () -> {
        // callback();
        // });
      } else {
      }
    } catch (final Exception e) {
      System.out.println(e);
    }
  }
}
