package phase5;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/*
 * WHAT WE ARE GOING TO LEARN IN THIS STEP:
 *   - ThreadPoolExecutor: the real class behind Executors.newFixedThreadPool
 *   - Core pool size, max pool size, queue size
 *   - The order: core threads first, then the queue, then extra threads, then reject
 *   - Rejection policy: AbortPolicy (default) and CallerRunsPolicy
 *   - ThreadFactory: give our own names to the worker threads
 *   - newCachedThreadPool and newSingleThreadExecutor, compared with newFixedThreadPool
 *
 * PROBLEM:
 *   newFixedThreadPool has a queue with NO size limit.
 *   If tasks come faster than the workers can run them, the queue grows
 *   and uses all the memory. newCachedThreadPool has the opposite problem:
 *   NO limit on threads. Both can crash the program.
 *
 * SOLUTION:
 *   Create ThreadPoolExecutor ourselves. We choose the core size, the max size,
 *   a queue with a fixed size, and what to do when the pool is full.
 */
public class P25_CustomThreadPoolDemo {

    public static void main(String[] args) throws InterruptedException {

        // ---------------------------------------------------------
        // Section 1: BROKEN - newFixedThreadPool has an unlimited queue
        // 2 workers, 20 slow tasks. Nobody says "stop" when the queue gets big.
        // (sleep is only here to pretend the work is slow.)
        // ---------------------------------------------------------
        ThreadPoolExecutor pool1 = (ThreadPoolExecutor) Executors.newFixedThreadPool(2);
        for (int i = 1; i <= 20; i++) {
            pool1.submit(() -> {
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            });
        }
        System.out.println("Section 1 threads = " + pool1.getPoolSize()
                + ", tasks waiting in queue = " + pool1.getQueue().size());
        pool1.shutdownNow(); // we do not need these tasks. Stop quickly.
        pool1.awaitTermination(10, TimeUnit.SECONDS);

        // ---------------------------------------------------------
        // Section 2: FIXED - our own pool with a limit
        // core = 2, max = 4, queue size = 2, default policy (AbortPolicy)
        // We submit 7 tasks. Each task takes 500 ms.
        // ---------------------------------------------------------
        ThreadPoolExecutor pool2 = new ThreadPoolExecutor(
                2,                              // core pool size
                4,                              // max pool size
                60, TimeUnit.SECONDS,           // idle time for threads above core size
                new ArrayBlockingQueue<>(2));   // queue with a fixed size

        for (int i = 1; i <= 7; i++) {
            try {
                pool2.submit(() -> {
                    try {
                        Thread.sleep(500);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                });
                System.out.println("Section 2 task " + i + " accepted.  threads = " + pool2.getPoolSize() + ", queue = " + pool2.getQueue().size());
            } catch (RejectedExecutionException e) {
                System.out.println("Section 2 task " + i + " REJECTED.  threads = " + pool2.getPoolSize() + ", queue = " + pool2.getQueue().size());
            }
        }
        pool2.shutdown();
        pool2.awaitTermination(10, TimeUnit.SECONDS);

        // ---------------------------------------------------------
        // Section 3: CallerRunsPolicy - when the pool is full,
        // the thread that called submit() runs the task itself.
        // core = 1, max = 1, queue size = 1. We submit 3 tasks.
        // ---------------------------------------------------------
        ThreadPoolExecutor pool3 = new ThreadPoolExecutor(
                1, 1, 0, TimeUnit.SECONDS,
                new ArrayBlockingQueue<>(1),
                new ThreadPoolExecutor.CallerRunsPolicy());

        for (int i = 1; i <= 3; i++) {
            final int id = i;
            pool3.submit(() -> {
                System.out.println("Section 3 task " + id + " runs on " + Thread.currentThread().getName());
                try {
                    Thread.sleep(300);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            });
        }
        pool3.shutdown();
        pool3.awaitTermination(10, TimeUnit.SECONDS);

        // ---------------------------------------------------------
        // Section 4: ThreadFactory - our own thread names
        // "pool-1-thread-1" does not tell us much in a thread dump.
        // ---------------------------------------------------------
        AtomicInteger counter = new AtomicInteger(0);
        ThreadFactory factory = r -> new Thread(r, "worker-" + counter.incrementAndGet());

        ExecutorService pool4 = Executors.newFixedThreadPool(2, factory);
        for (int i = 1; i <= 4; i++) {
            final int id = i;
            pool4.submit(() -> {
                System.out.println("Section 4 task " + id + " runs on " + Thread.currentThread().getName());
            });
        }
        pool4.shutdown();
        pool4.awaitTermination(10, TimeUnit.SECONDS);

        // ---------------------------------------------------------
        // Section 5: newCachedThreadPool - NO limit on threads
        // 5 slow tasks arrive together. The pool creates 5 threads.
        // With 100,000 tasks it would try to create 100,000 threads.
        // ---------------------------------------------------------
        ThreadPoolExecutor pool5 = (ThreadPoolExecutor) Executors.newCachedThreadPool();
        for (int i = 1; i <= 5; i++) {
            pool5.submit(() -> {
                try {
                    Thread.sleep(300);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            });
        }
        System.out.println("Section 5 cached pool: 5 tasks, threads = " + pool5.getPoolSize());
        pool5.shutdown();
        pool5.awaitTermination(10, TimeUnit.SECONDS);

        // ---------------------------------------------------------
        // Section 6: newSingleThreadExecutor - exactly 1 worker
        // Tasks run one by one, in the order we submitted them.
        // ---------------------------------------------------------
        ExecutorService pool6 = Executors.newSingleThreadExecutor();
        for (int i = 1; i <= 3; i++) {
            final int id = i;
            pool6.submit(() -> {
                System.out.println("Section 6 task " + id + " runs on " + Thread.currentThread().getName());
            });
        }
        pool6.shutdown();
        pool6.awaitTermination(10, TimeUnit.SECONDS);
    }
}