package phase4;

import java.util.concurrent.atomic.AtomicInteger;

/*
 * WHAT WE ARE GOING TO LEARN IN THIS STEP:
 *   - AtomicInteger: incrementAndGet, get, compareAndSet
 *   - CAS (Compare-And-Swap): the idea behind all atomic classes
 *   - A CAS retry loop
 *
 * PROBLEM:
 *   count++ is not atomic (read, add, write). Many threads give a wrong result.
 *   synchronized fixes it, but threads must wait for the lock.
 *
 * SOLUTION:
 *   AtomicInteger does the update as ONE atomic step using a CPU instruction (CAS).
 *   No lock. No blocking. The result is always correct.
 */
public class P17_AtomicDemo {

    static int plainCount = 0;
    static int syncCount = 0;
    static final Object lock = new Object();
    static final AtomicInteger atomicCount = new AtomicInteger(0);

    public static void main(String[] args) throws InterruptedException {

        // 4 threads, each adds 500,000 times. Expected result: 2,000,000

        // ---------------------------------------------------------
        // Section 1: BROKEN - plain int, count++ is not atomic
        // ---------------------------------------------------------
        long start = System.currentTimeMillis();
        Thread[] t1 = new Thread[4];
        for (int i = 0; i < 4; i++) {
            t1[i] = new Thread(() -> {
                for (int j = 0; j < 500_000; j++) {
                    plainCount++; // read, add, write: threads overwrite each other
                }
            });
            t1[i].start();
        }
        for (Thread t : t1) {
            t.join();
        }
        System.out.println("Section 1 plain int    : " + plainCount
                + " (expected 2000000) in " + (System.currentTimeMillis() - start) + " ms");

        // ---------------------------------------------------------
        // Section 2: FIX 1 - synchronized (correct, but threads wait)
        // ---------------------------------------------------------
        start = System.currentTimeMillis();
        Thread[] t2 = new Thread[4];
        for (int i = 0; i < 4; i++) {
            t2[i] = new Thread(() -> {
                for (int j = 0; j < 500_000; j++) {
                    synchronized (lock) {
                        syncCount++;
                    }
                }
            });
            t2[i].start();
        }
        for (Thread t : t2) {
            t.join();
        }
        System.out.println("Section 2 synchronized : " + syncCount
                + " (expected 2000000) in " + (System.currentTimeMillis() - start) + " ms");

        // ---------------------------------------------------------
        // Section 3: FIX 2 - AtomicInteger (correct, no lock)
        // ---------------------------------------------------------
        start = System.currentTimeMillis();
        Thread[] t3 = new Thread[4];
        for (int i = 0; i < 4; i++) {
            t3[i] = new Thread(() -> {
                for (int j = 0; j < 500_000; j++) {
                    atomicCount.incrementAndGet(); // one atomic step
                }
            });
            t3[i].start();
        }
        for (Thread t : t3) {
            t.join();
        }
        System.out.println("Section 3 AtomicInteger: " + atomicCount.get()
                + " (expected 2000000) in " + (System.currentTimeMillis() - start) + " ms");

        // ---------------------------------------------------------
        // Section 4: CAS by hand (single thread, to see the idea)
        // compareAndSet(expected, newValue):
        //   "If the value is still 'expected', change it to 'newValue'."
        //   Returns true if it changed, false if not.
        // ---------------------------------------------------------
        AtomicInteger x = new AtomicInteger(10);

        boolean ok1 = x.compareAndSet(10, 20); // value is 10, so it changes
        System.out.println("Section 4 CAS(10 -> 20): " + ok1 + ", value = " + x.get());

        boolean ok2 = x.compareAndSet(10, 30); // value is 20 now, so it fails
        System.out.println("Section 4 CAS(10 -> 30): " + ok2 + ", value = " + x.get());

    }
}