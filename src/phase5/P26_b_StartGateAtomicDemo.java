package phase5;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;

/*
 * WHAT WE ARE GOING TO LEARN IN THIS STEP:
 *   - Start gate with CountDownLatch(1): 50 threads wait, main fires once
 *   - Many threads hit the same variable at the same time
 *   - allFinished latch: main waits until all 50 are done
 *
 * PROBLEM:
 *   Without a gate, thread 1 may finish before thread 50 even starts.
 *   The threads do not really compete, so a race condition may stay hidden.
 *
 * SOLUTION:
 *   All threads wait at the gate. One countDown() releases them together.
 *   Now the competition is real.
 */
public class P26_b_StartGateAtomicDemo {

    static int plainCount = 0;                               // not safe

    public static void main(String[] args) throws InterruptedException {

        // ---------------------------------------------------------
        // Section 1: BROKEN - 50 threads, start gate, plain int
        // ---------------------------------------------------------
        CountDownLatch gate1 = new CountDownLatch(1);
        CountDownLatch finished1 = new CountDownLatch(50);

        for (int i = 0; i < 50; i++) {
            new Thread(() -> {
                try {
                    gate1.await();                           // wait for the gun
                    for (int j = 0; j < 1000; j++) {
                        plainCount++;                        // read, add, write
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                } finally {
                    finished1.countDown();
                }
            }).start();
        }
        gate1.countDown();                                   // fire: all 50 start
        finished1.await();                                   // main waits for all 50
        System.out.println("Section 1 plain int    : " + plainCount + " (expected 50000)");

        // ---------------------------------------------------------
        // Section 2: FIXED - same code, AtomicInteger
        // ---------------------------------------------------------
        AtomicInteger atomicCount = new AtomicInteger(0);
        CountDownLatch gate2 = new CountDownLatch(1);
        CountDownLatch finished2 = new CountDownLatch(50);

        for (int i = 0; i < 50; i++) {
            new Thread(() -> {
                try {
                    gate2.await();
                    for (int j = 0; j < 1000; j++) {
                        atomicCount.incrementAndGet();       // one atomic step
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                } finally {
                    finished2.countDown();
                }
            }).start();
        }
        gate2.countDown();
        finished2.await();
        System.out.println("Section 2 AtomicInteger: " + atomicCount.get() + " (expected 50000)");
    }
}