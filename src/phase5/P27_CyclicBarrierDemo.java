package phase5;

import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CyclicBarrier;

/*
 * WHAT WE ARE GOING TO LEARN IN THIS STEP:
 *   - CyclicBarrier(n): a meeting point for n threads
 *   - await(): "I arrived. I wait until all n threads arrive."
 *   - The last thread to arrive opens the barrier. Then ALL threads continue.
 *   - Reusable (cyclic): after it opens, it can be used again
 *
 * PROBLEM:
 *   3 threads do part 1, then part 2. Part 2 needs part 1 of ALL threads to be done.
 *   A fast thread starts part 2 too early, while a slow thread is still in part 1.
 *
 * SOLUTION:
 *   Every thread calls barrier.await() after part 1.
 *   Nobody starts part 2 until all 3 threads have arrived.
 */
public class P27_CyclicBarrierDemo {

    public static void main(String[] args) throws InterruptedException {

        // ---------------------------------------------------------
        // Section 1: BROKEN - no barrier
        // Worker 1 is fast (200 ms). Worker 3 is slow (600 ms).
        // (sleep only pretends to be work.)
        // ---------------------------------------------------------
        Thread[] t1 = new Thread[3];
        for (int i = 0; i < 3; i++) {
            final int id = i + 1;
            t1[i] = new Thread(() -> {
                try {
                    Thread.sleep(id * 200);
                    System.out.println("Section 1 worker " + id + " arrived at gate");
                    System.out.println("Section 1 worker " + id + " started work");
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            });
            t1[i].start();
        }
        for (Thread t : t1) {
            t.join();
        }

        // ---------------------------------------------------------
        // Section 2: FIXED - CyclicBarrier(3)
        // Same workers. Now they wait for each other after part 1.
        // ---------------------------------------------------------
        CyclicBarrier barrier = new CyclicBarrier(3);

        Thread[] t2 = new Thread[3];
        for (int i = 0; i < 3; i++) {
            final int id = i + 1;
            t2[i] = new Thread(() -> {
                try {
                    Thread.sleep(id * 200);   // remove the // so workers arrive at different times
                    System.out.println("Section 2 worker " + id + " arrived at task");
                    barrier.await();
                    System.out.println("Section 2 worker " + id + " starts work");
                } catch (InterruptedException | BrokenBarrierException e) {
                    Thread.currentThread().interrupt();
                }
            });
            t2[i].start();
        }
        for (Thread t : t2) {
            t.join();
        }
    }
}