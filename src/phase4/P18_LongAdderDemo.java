package phase4;

import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.LongAdder;

/*
 * WHAT WE ARE GOING TO LEARN IN THIS STEP:
 *   - LongAdder: increment, add, sum, sumThenReset
 *   - Why many threads on ONE atomic variable can be slow
 *   - When LongAdder is better than AtomicLong, and when it is not
 *
 * PROBLEM:
 *   AtomicLong is correct, but all threads fight for the same variable.
 *   When a CAS fails, the thread must retry. With many threads,
 *   there are many failed CAS and many retries. CPU time is wasted.
 *
 * SOLUTION:
 *   LongAdder keeps many small counters (cells), not one.
 *   Under contention, different threads update different cells.
 *   Nobody fights. When we need the total, sum() adds all the cells.
 */
public class P18_LongAdderDemo {

    static final AtomicLong atomicCount = new AtomicLong(0);
    static final LongAdder adderCount = new LongAdder();

    public static void main(String[] args) throws InterruptedException {

        // 8 threads, each adds 5,000,000 times. Expected result: 40,000,000

        // ---------------------------------------------------------
        // Section 1: AtomicLong - correct, but all threads hit ONE variable
        // ---------------------------------------------------------
        long start = System.currentTimeMillis();
        Thread[] t1 = new Thread[8];
        for (int i = 0; i < 8; i++) {
            t1[i] = new Thread(() -> {
                for (int j = 0; j < 5_000_000; j++) {
                    atomicCount.incrementAndGet(); // CAS on the same variable
                }
            });
            t1[i].start();
        }
        for (Thread t : t1) {
            t.join();
        }
        System.out.println("Section 1 AtomicLong: " + atomicCount.get()
                + " (expected 40000000) in " + (System.currentTimeMillis() - start) + " ms");

        // ---------------------------------------------------------
        // Section 2: LongAdder - correct, threads use different cells
        // ---------------------------------------------------------
        start = System.currentTimeMillis();
        Thread[] t2 = new Thread[8];
        for (int i = 0; i < 8; i++) {
            t2[i] = new Thread(() -> {
                for (int j = 0; j < 5_000_000; j++) {
                    adderCount.increment(); // updates a cell, not one shared variable
                }
            });
            t2[i].start();
        }
        for (Thread t : t2) {
            t.join();
        }
        System.out.println("Section 2 LongAdder : " + adderCount.sum()
                + " (expected 40000000) in " + (System.currentTimeMillis() - start) + " ms");

        // ---------------------------------------------------------
        // Section 3: Basic methods (single thread, to see the API)
        // ---------------------------------------------------------
        LongAdder a = new LongAdder();
        a.increment();   // +1
        a.add(10);       // +10
        a.decrement();   // -1
        System.out.println("Section 3 sum            = " + a.sum());            // 10

        long old = a.sumThenReset();                                           // get total, set to 0
        System.out.println("Section 3 sumThenReset    = " + old + ", now = " + a.sum());

        // ---------------------------------------------------------
        // Section 4: sum() while threads are still running
        // sum() adds the cells one by one. Other threads keep changing them.
        // So the result is NOT an exact snapshot.
        // ---------------------------------------------------------
        LongAdder live = new LongAdder();
        Thread[] t4 = new Thread[4];
        for (int i = 0; i < 4; i++) {
            t4[i] = new Thread(() -> {
                for (int j = 0; j < 5_000_000; j++) {
                    live.increment();
                }
            });
            t4[i].start();
        }
        long during = live.sum(); // threads are still working
        for (Thread t : t4) {
            t.join();
        }
        System.out.println("Section 4 sum during work = " + during
                + ", final sum = " + live.sum());
    }
}