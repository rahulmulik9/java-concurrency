package phase3;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

/*
 * WHAT WE ARE GOING TO LEARN IN THIS STEP:
 *   - Deadlock: two threads wait for each other's lock forever
 *   - Cause: threads take the same locks in the OPPOSITE order
 *   - Fix 1: always take locks in the SAME order
 *   - Fix 2: tryLock(timeout), and give up instead of waiting forever
 *
 * PROBLEM:
 *   Thread 1 holds lock A and wants lock B.
 *   Thread 2 holds lock B and wants lock A.
 *   Neither can continue, and neither will ever let go. The program hangs.
 *
 * SOLUTION:
 *   - Same lock order for every thread (A then B, always), or
 *   - tryLock with a timeout, so a thread can give up and release its lock.
 */
public class P12_DeadlockDemo {

    static void pause(long ms) {
        try {
            Thread.sleep(ms);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    public static void main(String[] args) throws InterruptedException {

        // Section 1: opposite lock order (BROKEN, deadlock)
        // Daemon threads, so the JVM can still exit even though they are stuck forever
        System.out.println("1. Opposite lock order (BROKEN)");
        Object lockA = new Object();
        Object lockB = new Object();

        Thread a1 = new Thread(() -> {
            synchronized (lockA) {
                System.out.println("   [t1] has lockA, wants lockB");
                pause(200);                          // time for t2 to take lockB
                synchronized (lockB) {
                    System.out.println("   [t1] got both locks");
                }
            }
        });
        Thread a2 = new Thread(() -> {
            synchronized (lockB) {
                System.out.println("   [t2] has lockB, wants lockA");
                pause(200);                          // time for t1 to take lockA
                synchronized (lockA) {
                    System.out.println("   [t2] got both locks");
                }
            }
        });
        a1.setDaemon(true);
        a2.setDaemon(true);
        a1.start();
        a2.start();
        a1.join(2000);                               // wait at most 2 seconds
        a2.join(2000);
        System.out.println("   after 2 seconds: t1 = " + a1.getState() + ", t2 = " + a2.getState());
        System.out.println("   DEADLOCK: both threads are stuck forever\n");

        // Section 2: same lock order (FIXED)
        System.out.println("2. Same lock order (FIXED)");
        Object lockX = new Object();
        Object lockY = new Object();

        Thread b1 = new Thread(() -> {
            synchronized (lockX) {                   // X first
                pause(200);
                synchronized (lockY) {               // then Y
                    System.out.println("   [t1] got both locks");
                }
            }
        });
        Thread b2 = new Thread(() -> {
            synchronized (lockX) {                   // X first, same order as t1
                pause(200);
                synchronized (lockY) {               // then Y
                    System.out.println("   [t2] got both locks");
                }
            }
        });
        b1.start();
        b2.start();
        b1.join();
        b2.join();
        System.out.println("   both finished, no deadlock\n");

        // Section 3: tryLock with timeout (FIXED)
        System.out.println("3. tryLock(timeout) (FIXED)");
        ReentrantLock lockP = new ReentrantLock();
        ReentrantLock lockQ = new ReentrantLock();

        // Opposite order again, but t1 waits only 300 ms and t2 waits up to 2000 ms
        Thread c1 = new Thread(() -> {
            lockP.lock();
            try {
                pause(200);
                if (lockQ.tryLock(300, TimeUnit.MILLISECONDS)) {
                    try {
                        System.out.println("   [t1] got both locks");
                    } finally {
                        lockQ.unlock();
                    }
                } else {
                    System.out.println("   [t1] could not get lockQ, gave up");
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            } finally {
                lockP.unlock();                      // releases lockP, so t2 can continue
            }
        });
        Thread c2 = new Thread(() -> {
            lockQ.lock();
            try {
                pause(200);
                if (lockP.tryLock(2000, TimeUnit.MILLISECONDS)) {
                    try {
                        System.out.println("   [t2] got both locks");
                    } finally {
                        lockP.unlock();
                    }
                } else {
                    System.out.println("   [t2] could not get lockP, gave up");
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            } finally {
                lockQ.unlock();
            }
        });
        c1.start();
        c2.start();
        c1.join();
        c2.join();
        System.out.println("   both finished, no deadlock");
    }
}