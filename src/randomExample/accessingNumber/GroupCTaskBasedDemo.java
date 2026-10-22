package randomExample.accessingNumber;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

/*
 * WHAT WE ARE GOING TO LEARN IN THIS STEP:
 *   Group C: task-based ordering, no manual locks.
 *   Single-thread ExecutorService, CompletableFuture chaining, BlockingQueue handoff
 *
 * PROBLEM:
 *   Locks and wait/notify are low level and easy to get wrong.
 *
 * SOLUTION:
 *   Let the framework do the ordering:
 *   - an executor with ONE thread runs tasks one by one,
 *   - a CompletableFuture chain starts the next task only after the previous one,
 *   - a BlockingQueue passes a "token" from thread to thread.
 */
public class GroupCTaskBasedDemo {

    private static int counter = 0;

    @FunctionalInterface
    interface Way { void run() throws Exception; }

    // ---------- helpers ----------

    private static void sleep1s() {
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    private static void incrementSlowly() {
        int read = counter;
        sleep1s();
        counter = read + 1;
        System.out.println("   " + Thread.currentThread().getName() + " -> counter = " + counter);
    }

    private static void run(String name, Way way) throws Exception {
        counter = 0;
        System.out.println("=== " + name + " ===");
        long start = System.currentTimeMillis();
        way.run();
        long ms = System.currentTimeMillis() - start;
        System.out.println("   final counter = " + counter + ", time = " + ms + " ms\n");
    }

    // ---------- the ways ----------

    /**
     * Way 1: single-thread executor. 3 tasks, but only ONE worker thread,
     * so they run one by one in submission order.
     * (Note: the thread name is the pool's thread, not T1/T2/T3.)
     */
    static void singleThreadExecutorWay() throws InterruptedException {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        for (int i = 0; i < 3; i++) {
            executor.submit(GroupCTaskBasedDemo::incrementSlowly);
        }
        executor.shutdown();                              // no new tasks, finish queued ones
        executor.awaitTermination(1, TimeUnit.MINUTES);   // wait until all 3 are done
    }

    /**
     * Way 2: CompletableFuture chain. thenRun starts only after the previous stage completes.
     * Different pool threads may run each stage, but never two at the same time.
     */
    static void completableFutureWay() {
        CompletableFuture
                .runAsync(GroupCTaskBasedDemo::incrementSlowly)
                .thenRun(GroupCTaskBasedDemo::incrementSlowly)
                .thenRun(GroupCTaskBasedDemo::incrementSlowly)
                .join(); // block main until the whole chain finishes
    }

    /**
     * Way 3: BlockingQueue handoff. There are 3 real threads (T1..T3).
     * Thread i blocks on queue i (take), works, then puts the token into queue i+1.
     */
    static void blockingQueueWay() throws InterruptedException {
        BlockingQueue<Object> q1 = new LinkedBlockingQueue<>();
        BlockingQueue<Object> q2 = new LinkedBlockingQueue<>();
        BlockingQueue<Object> q3 = new LinkedBlockingQueue<>();
        Object token = new Object();

        Thread t3 = new Thread(() -> handOff(q3, null, token), "T3");
        Thread t2 = new Thread(() -> handOff(q2, q3, token), "T2");
        Thread t1 = new Thread(() -> handOff(q1, q2, token), "T1");

        t3.start(); t2.start(); t1.start();   // reverse start, order still holds
        q1.put(token);                        // give the token to T1 to begin
        t1.join(); t2.join(); t3.join();
    }

    private static void handOff(BlockingQueue<Object> mine, BlockingQueue<Object> next, Object token) {
        try {
            mine.take();                      // wait for my turn
            incrementSlowly();
            if (next != null) {
                next.put(token);              // pass the token on
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    // ---------- main ----------

    public static void main(String[] args) throws Exception {
        run("1. Single-thread ExecutorService", GroupCTaskBasedDemo::singleThreadExecutorWay);
        run("2. CompletableFuture chain", GroupCTaskBasedDemo::completableFutureWay);
        run("3. BlockingQueue handoff", GroupCTaskBasedDemo::blockingQueueWay);

        /*
         * OUTPUT EXPLANATION:
         *   All three end with counter = 3 in about 3 sec, in strict order.
         *   No synchronized or Lock appears in the code: the framework enforces the order.
         */
    }
}