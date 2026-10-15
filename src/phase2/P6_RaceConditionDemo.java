package phase2;

import java.util.concurrent.atomic.AtomicInteger;

/*
 * WHAT WE ARE GOING TO LEARN IN THIS STEP:
 *   - What a race condition is
 *   - Why count++ is not atomic (read, add, write)
 *   - How synchronized and AtomicInteger fix it
 *
 * PROBLEM:
 *   Two threads increment the same counter 100,000 times each.
 *   Expected result is 200,000, but we get a smaller, random number.
 *
 * SOLUTION:
 *   Make the update mutually exclusive (synchronized) or atomic (AtomicInteger).
 */
public class P6_RaceConditionDemo {

    static int count = 0;
    static AtomicInteger atomicCount = new AtomicInteger(0);

    public static void main(String[] args) throws InterruptedException {

        // Section 1: no protection (BROKEN)
        count = 0;
        Thread a1 = new Thread(() -> {
            for (int i = 0; i < 100000; i++) {
                count++;
            }
        });
        Thread a2 = new Thread(() -> {
            for (int i = 0; i < 100000; i++) {
                count++;
            }
        });
        a1.start();
        a2.start();
        a1.join();
        a2.join();
        System.out.println("1. Without synchronized : " + count + "  (expected 200000)");

        // Section 2: synchronized (FIXED)
        count = 0;                                   // set count back to 0
        Thread b1 = new Thread(() -> {
            for (int i = 0; i < 100000; i++) {
                synchronized (P6_RaceConditionDemo.class) {
                    count++;
                }
            }
        });
        Thread b2 = new Thread(() -> {
            for (int i = 0; i < 100000; i++) {
                synchronized (P6_RaceConditionDemo.class) {
                    count++;
                }
            }
        });
        b1.start();
        b2.start();
        b1.join();
        b2.join();
        System.out.println("2. With synchronized    : " + count + "  (expected 200000)");

        // Section 3: AtomicInteger (FIXED)
        Thread c1 = new Thread(() -> {
            for (int i = 0; i < 100000; i++) {
                atomicCount.incrementAndGet();
            }
        });
        Thread c2 = new Thread(() -> {
            for (int i = 0; i < 100000; i++) {
                atomicCount.incrementAndGet();
            }
        });
        c1.start();
        c2.start();
        c1.join();
        c2.join();
        System.out.println("3. With AtomicInteger   : " + atomicCount.get() + "  (expected 200000)");
    }
}