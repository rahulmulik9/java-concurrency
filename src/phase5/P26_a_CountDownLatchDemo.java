package phase5;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/*
 * WHAT WE ARE GOING TO LEARN IN THIS STEP:
 *   - CountDownLatch: a counter that starts at N and goes down to 0
 *   - countDown(): reduce the counter by 1 (called by the worker threads)
 *   - await(): wait until the counter is 0 (called by the waiting thread)
 *   - await(timeout): wait only a limited time
 *   - Start gate: many threads wait for ONE signal to start together
 *   - A latch is single-use. It cannot be reset.
 *
 * PROBLEM:
 *   main starts 3 workers and needs ALL of them to finish before it continues.
 *   If main does not wait, it continues too early and reads incomplete results.
 *   Using sleep() to wait is a guess. Too short = wrong. Too long = wasted time.
 *
 * SOLUTION:
 *   Create a CountDownLatch(3). Each worker calls countDown() when it is done.
 *   main calls await(). It continues exactly when the counter reaches 0.
 */
public class P26_a_CountDownLatchDemo {

    public static void main(String[] args) throws InterruptedException {

        // ---------------------------------------------------------
        // Section 1: BROKEN - main does not wait
        // 3 workers, each takes 300 ms. main reads the result at once.
        // ---------------------------------------------------------
        ExecutorService pool1 = Executors.newFixedThreadPool(3);
        int[] finished1 = {0}; // simple demo only. Not safe for many writers.

        for (int i = 1; i <= 3; i++) {
            pool1.submit(() -> {
                try {
                    Thread.sleep(300); // pretend this is real work
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
                synchronized (finished1) {
                    finished1[0]++;
                }
            });
        }
        synchronized (finished1) {
            System.out.println("Section 1 main reads too early: finished = " + finished1[0] + " (expected 3)");
        }
        pool1.shutdown();
        pool1.awaitTermination(1, TimeUnit.MINUTES);

        // ---------------------------------------------------------
        // Section 2: FIXED - CountDownLatch(3)
        // main waits in await() until all 3 workers call countDown().
        // ---------------------------------------------------------
        ExecutorService pool2 = Executors.newFixedThreadPool(3);
        CountDownLatch done = new CountDownLatch(3);   // counter = 3
        int[] finished2 = {0};

        for (int i = 1; i <= 3; i++) {
            final int id = i;
            pool2.submit(() -> {
                try {
                    Thread.sleep(300);
                    synchronized (finished2) {
                        finished2[0]++;
                    }
                    System.out.println("Section 2 worker " + id + " done, counter before = " + done.getCount());
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                } finally {
                    done.countDown();                  // counter - 1, always in finally
                    System.out.println("Section 2 worker " + id + " done, counter after = " + done.getCount());
                }
            });
        }
        done.await();                                  // main waits here until counter = 0
        synchronized (finished2) {
            System.out.println("Section 2 main continues: finished = " + finished2[0] + " (expected 3)");
        }
        pool2.shutdown();
        pool2.awaitTermination(1, TimeUnit.MINUTES);

        // ---------------------------------------------------------
        // Section 3: await(timeout) - do not wait forever
        // The latch needs 2 countDown() calls, but only 1 worker calls it.
        // ---------------------------------------------------------
        CountDownLatch broken = new CountDownLatch(2);
        Thread w = new Thread(broken::countDown);      // only one countDown
        w.start();
        w.join();
        boolean ok = broken.await(500, TimeUnit.MILLISECONDS);
        System.out.println("Section 3 await returned " + ok + ", counter = " + broken.getCount());

        // ---------------------------------------------------------
        // Section 4: Start gate - the latch is used the other way around.
        // 3 runners wait for ONE signal. main gives it with countDown().
        // ---------------------------------------------------------
        CountDownLatch startSignal = new CountDownLatch(1);
        CountDownLatch allFinished = new CountDownLatch(3);

        for (int i = 1; i <= 3; i++) {
            final int id = i;
            new Thread(() -> {
                try {
                    startSignal.await();               // wait for the signal
                    System.out.println("Section 4 runner " + id + " starts");
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                } finally {
                    allFinished.countDown();
                }
            }).start();
        }
        Thread.sleep(200); // only to show that runners really wait. Never coordinate with sleep in real code.
        System.out.println("Section 4 main gives the signal");
        startSignal.countDown();                       // counter 1 -> 0. All runners start.
        allFinished.await();

        // ---------------------------------------------------------
        // Section 5: A latch is single-use
        // After the counter is 0, await() returns at once, forever.
        // ---------------------------------------------------------
        CountDownLatch used = new CountDownLatch(1);
        used.countDown();
        used.countDown();                              // counter stays 0. No error, no negative.
        used.await();                                  // returns at once
        System.out.println("Section 5 counter = " + used.getCount() + ". Cannot be reset.");
    }
}

        /*
         * ---------------------------------------------------------
         * Section 4: FLOW (CountDownLatch as start gate and finish line)
         * ---------------------------------------------------------
         * startSignal(1) = start gun   (runners wait, main fires it)
         * allFinished(3) = finish line (runners report, main waits)
         *
         * 1. Three threads are created and started.
         *    Each thread reaches startSignal.await() and stops there.
         *    This tells the thread: "start your work only when startSignal reaches 0, otherwise wait." So all three threads are waiting.
         *
         * 2. main sleeps 200 ms (only to show the runners really wait).
         *    Then main calls startSignal.countDown().
         *    The counter goes 1 -> 0, the gate opens, and all three threads
         *    start together.
         *
         * 3. Right after that, main reaches allFinished.await().
         *    This tells the calling thread (main) to wait until allFinished becomes 0. So main is blocked.
         *
         * 4. Each of the three threads prints "runner X starts". The order is random.
         *
         * 5. Each thread has a finally block. In it, the thread calls allFinished.countDown(), one by one:
         *        3 -> 2 -> 1 -> 0
         *    finally guarantees the countDown even if an exception happens, so main never waits forever.
         *
         * 6. allFinished is 0, so main wakes up and continues to Section 5.
         *
         * GUARANTEED:
         *   - No runner prints before startSignal is 0.
         *   - main does not continue before allFinished is 0.
         * NOT GUARANTEED:
         *   - The order of the three runners.
         */