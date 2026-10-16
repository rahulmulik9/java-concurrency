package phase5;

import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/*
 * WHAT WE ARE GOING TO LEARN IN THIS STEP:
 *   - ExecutorService and Executors.newFixedThreadPool
 *   - submit, shutdown, awaitTermination, shutdownNow
 *   - Thread reuse: a pool runs many tasks on a few threads
 *
 * PROBLEM:
 *   Creating a new Thread for every task is expensive.
 *   Each thread uses memory and takes time to start.
 *   With thousands of tasks, we get thousands of threads. This is slow
 *   and can crash the program (OutOfMemoryError).
 *
 * SOLUTION:
 *   A thread pool has a fixed number of worker threads.
 *   Tasks wait in a queue. Each worker takes a task, runs it,
 *   then takes the next one. Threads are reused.
 */
public class P22_b_ThreadPoolDemo {

    public static void main(String[] args) throws InterruptedException {

        // 5,000 small tasks. Each task adds 1 to a counter.

        // ---------------------------------------------------------
        // Section 1: BROKEN - one new Thread for every task
        // ---------------------------------------------------------
        AtomicInteger done1 = new AtomicInteger(0);
        Set<String> names1 = ConcurrentHashMap.newKeySet(); // thread names that did work

        long start = System.currentTimeMillis();
        Thread[] threads = new Thread[5_000];
        for (int i = 0; i < 5_000; i++) {
            threads[i] = new Thread(() -> {
                names1.add(Thread.currentThread().getName());
                done1.incrementAndGet();
            });
            threads[i].start(); // creates a real OS thread each time
        }
        for (Thread t : threads) {
            t.join();
        }
        System.out.println("Section 1 thread per task: tasks = " + done1.get()
                + ", threads used = " + names1.size()
                + ", time = " + (System.currentTimeMillis() - start) + " ms");

        // ---------------------------------------------------------
        // Section 2: FIXED - pool with 4 threads
        // ---------------------------------------------------------
        AtomicInteger done2 = new AtomicInteger(0);
        Set<String> names2 = ConcurrentHashMap.newKeySet();

        start = System.currentTimeMillis();
        ExecutorService pool = Executors.newFixedThreadPool(4);
        for (int i = 0; i < 5_000; i++) {
            pool.submit(() -> {
                names2.add(Thread.currentThread().getName());
                done2.incrementAndGet();
            });
        }
        pool.shutdown();                          // 1. no new tasks. Old tasks still run.
        pool.awaitTermination(1, TimeUnit.MINUTES); // 2. wait until all tasks are finished
        System.out.println("Section 2 fixed pool     : tasks = " + done2.get()
                + ", threads used = " + names2.size()
                + ", time = " + (System.currentTimeMillis() - start) + " ms");

        // ---------------------------------------------------------
        // Section 3: Graceful shutdown
        // One worker, 3 slow tasks. We call shutdown() at once.
        // The tasks already submitted are NOT cancelled.
        // ---------------------------------------------------------
        ExecutorService pool3 = Executors.newFixedThreadPool(1);
        for (int i = 1; i <= 3; i++) {
            final int id = i;
            pool3.submit(() -> {
                try {
                    System.out.println("Section 3 task " + id + " start");
                    Thread.sleep(300); // slow work
                    System.out.println("Section 3 task " + id + " end");
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            });
        }
        pool3.shutdown();
        try {
            pool3.submit(() -> System.out.println("never runs"));
        } catch (RejectedExecutionException e) {
            System.out.println("Section 3 new task after shutdown: rejected");
        }
        boolean finished = pool3.awaitTermination(10, TimeUnit.SECONDS);
        System.out.println("Section 3 all tasks finished = " + finished);

        // ---------------------------------------------------------
        // Section 4: shutdownNow() - stop quickly
        // One worker, 3 very slow tasks. We stop the pool while task 1 runs.
        // ---------------------------------------------------------
        ExecutorService pool4 = Executors.newFixedThreadPool(1);
        for (int i = 1; i <= 3; i++) {
            final int id = i;
            pool4.submit(() -> {
                try {
                    System.out.println("Section 4 task " + id + " start");
                    Thread.sleep(5_000);
                    System.out.println("Section 4 task " + id + " end");
                } catch (InterruptedException e) {
                    System.out.println("Section 4 task " + id + " interrupted");
                    Thread.currentThread().interrupt();
                }
            });
        }
        Thread.sleep(100); // only to let task 1 start. Never coordinate threads with sleep in real code.
        List<Runnable> neverStarted = pool4.shutdownNow(); // interrupts running tasks, returns waiting tasks
        System.out.println("Section 4 tasks that never started = " + neverStarted.size());
        pool4.awaitTermination(10, TimeUnit.SECONDS);
        System.out.println("Section 4 pool terminated");
    }
}