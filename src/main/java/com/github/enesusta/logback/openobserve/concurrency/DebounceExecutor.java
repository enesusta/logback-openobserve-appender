package com.github.enesusta.logback.openobserve.concurrency;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

/**
 * A Debouncer is responsible for executing a task with a delay, and cancelling any previous
 * unexecuted task before doing so.
 */
public class DebounceExecutor<T> {

  private ScheduledExecutorService executor;
  private ScheduledFuture<T> future;

  public DebounceExecutor() {
    this.executor = Executors.newSingleThreadScheduledExecutor();
  }

  public void debounce(long delay, Runnable task) {
    if (future != null && !future.isDone()) {
      future.cancel(false);
    }

    future = (ScheduledFuture<T>) executor.schedule(task, delay, TimeUnit.MILLISECONDS);
  }
}
