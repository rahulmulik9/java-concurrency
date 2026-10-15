package phase3;

import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.locks.ReentrantLock;

/*
 * WHAT WE ARE GOING TO LEARN IN THIS STEP:
 *   - Livelock: threads keep running and retrying, but make no progress
 *   - Fix for livelock: wait a RANDOM time before retrying
 *   - Starvation: one thread almost never gets the lock
 *   - Fix for starvation: a fair lock, new ReentrantLock(true)
 *
 * PROBLEM:
 *   Livelock: two threads are too polite. Each one finds the other's lock busy,
 *   releases its own lock, waits, and retries at the same moment as the other one.
 *   They keep stepping aside for each other forever.
 *   (Deadlock = stuck and waiting. Livelock = busy, but going nowhere.)
 *   Starvation: with a normal (unfair) lock, one thread can win again and again
 *   while another thread rarely gets a turn.
 *
 * SOLUTION:
 *   - Livelock: random wait before retrying, so the threads stop moving in step.
 *   - Starvation: a fair lock gives the lock to the thread that has waited longest.
 */
public class P13_LivelockStarvationDemo {

    static volatile boolean running = true;
    static int count1 = 0;
    static int count2 = 0;

    public static void main(String[] args) throws InterruptedException {

        // Section 1: livelock (BROKEN)
        // Each thread holds one lock, tries for the other, fails, releases, waits the SAME time
        System.out.println("1. Livelock: both threads retry at the same time");
        ReentrantLock lockP = new ReentrantLock();
        ReentrantLock lockQ = new ReentrantLock();

        Thread a1 = new Thread(() -> {
            try {
                for (int attempt = 1; attempt <= 3; attempt++) {
                    lockP.lock();
                    Thread.sleep(100);                       // time for t2 to take lockQ
                    if (lockQ.tryLock()) {
                        System.out.println("   [t1] got both locks");
                        lockQ.unlock();
                        lockP.unlock();
                        return;
                    }
                    System.out.println("   [t1] attempt " + attempt + ": lockQ busy, release lockP");
                    lockP.unlock();
                    Thread.sleep(100);                       // same wait every time
                }
                System.out.println("   [t1] gave up, no progress");
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        });
        Thread a2 = new Thread(() -> {
            try {
                for (int attempt = 1; attempt <= 3; attempt++) {
                    lockQ.lock();
                    Thread.sleep(100);
                    if (lockP.tryLock()) {
                        System.out.println("   [t2] got both locks");
                        lockP.unlock();
                        lockQ.unlock();
                        return;
                    }
                    System.out.println("   [t2] attempt " + attempt + ": lockP busy, release lockQ");
                    lockQ.unlock();
                    Thread.sleep(100);                       // same wait every time
                }
                System.out.println("   [t2] gave up, no progress");
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        });
        a1.start();
        a2.start();
        a1.join();
        a2.join();

        // Section 2: random wait (FIXED)
        System.out.println("\n2. Livelock fixed: random wait before retrying");
        ReentrantLock lockX = new ReentrantLock();
        ReentrantLock lockY = new ReentrantLock();

        Thread b1 = new Thread(() -> {
            try {
                for (int attempt = 1; attempt <= 20; attempt++) {
                    lockX.lock();
                    Thread.sleep(100);
                    if (lockY.tryLock()) {
                        System.out.println("   [t1] got both locks on attempt " + attempt);
                        lockY.unlock();
                        lockX.unlock();
                        return;
                    }
                    lockX.unlock();
                    Thread.sleep(ThreadLocalRandom.current().nextInt(50, 300));   // RANDOM wait
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        });
        Thread b2 = new Thread(() -> {
            try {
                for (int attempt = 1; attempt <= 20; attempt++) {
                    lockY.lock();
                    Thread.sleep(100);
                    if (lockX.tryLock()) {
                        System.out.println("   [t2] got both locks on attempt " + attempt);
                        lockX.unlock();
                        lockY.unlock();
                        return;
                    }
                    lockY.unlock();
                    Thread.sleep(ThreadLocalRandom.current().nextInt(50, 300));   // RANDOM wait
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        });
        b1.start();
        b2.start();
        b1.join();
        b2.join();

        // Section 3: unfair lock (default)
        System.out.println("\n3. Unfair lock: lock wins in 1 second");
        ReentrantLock unfairLock = new ReentrantLock();         // default = unfair
        running = true;
        count1 = 0;
        count2 = 0;
        Thread c1 = new Thread(() -> {
            while (running) {
                unfairLock.lock();
                count1++;
                unfairLock.unlock();
            }
        });
        Thread c2 = new Thread(() -> {
            while (running) {
                unfairLock.lock();
                count2++;
                unfairLock.unlock();
            }
        });
        c1.start();
        c2.start();
        Thread.sleep(1000);
        running = false;
        c1.join();
        c2.join();
        System.out.println("   t1 = " + count1 + ", t2 = " + count2);

        // Section 4: fair lock (FIXED)
        System.out.println("\n4. Fair lock: lock wins in 1 second");
        ReentrantLock fairLock = new ReentrantLock(true);       // true = fair
        running = true;
        count1 = 0;
        count2 = 0;
        Thread d1 = new Thread(() -> {
            while (running) {
                fairLock.lock();
                count1++;
                fairLock.unlock();
            }
        });
        Thread d2 = new Thread(() -> {
            while (running) {
                fairLock.lock();
                count2++;
                fairLock.unlock();
            }
        });
        d1.start();
        d2.start();
        Thread.sleep(1000);
        running = false;
        d1.join();
        d2.join();
        System.out.println("   t1 = " + count1 + ", t2 = " + count2);
    }
}