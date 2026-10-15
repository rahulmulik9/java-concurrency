package phase3;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

/*
 * WHAT WE ARE GOING TO LEARN IN THIS STEP:
 *   - ReentrantLock: lock() and unlock() (same job as synchronized, but manual)
 *   - Always unlock in a finally block
 *   - tryLock(): try to get the lock without waiting
 *   - tryLock(timeout): wait for the lock, but give up after some time
 *   - Reentrant: the same thread can lock the same lock more than once
 *
 * PROBLEM:
 *   synchronized has no way to say "if the lock is busy, do something else".
 *   A thread that needs a synchronized lock waits forever, with no timeout.
 *
 * SOLUTION:
 *   ReentrantLock gives you control:
 *   - lock() / unlock()   -> works like synchronized, but YOU must unlock
 *   - tryLock()           -> returns true or false at once
 *   - tryLock(time, unit) -> waits up to the time, then gives up
 */
public class P11_ReentrantLockDemo {

    static int count = 0;

    public static void main(String[] args) throws InterruptedException {

        // Section 1: lock() and unlock() (FIXED counter)
        ReentrantLock lock = new ReentrantLock();
        Thread a1 = new Thread(() -> {
            for (int i = 0; i < 100000; i++) {
                lock.lock();
                try {
                    count++;
                } finally {
                    lock.unlock();               // ALWAYS unlock in finally
                }
            }
        });
        Thread a2 = new Thread(() -> {
            for (int i = 0; i < 100000; i++) {
                lock.lock();
                try {
                    count++;
                } finally {
                    lock.unlock();
                }
            }
        });
        a1.start();
        a2.start();
        a1.join();
        a2.join();
        System.out.println("1. Counter with ReentrantLock : " + count + "  (expected 200000)");

        // Section 2: forgetting unlock() (BROKEN)
        ReentrantLock forgotLock = new ReentrantLock();
        Thread b = new Thread(() -> {
            forgotLock.lock();
            // no unlock(): the thread ends, but the lock stays locked forever
        });
        b.start();
        b.join();
        System.out.println("\n2. Lock was never unlocked");
        System.out.println("   isLocked          : " + forgotLock.isLocked() + "  (thread is dead, lock is still held)");
        System.out.println("   main tryLock()    : " + forgotLock.tryLock() + "  (nobody can ever get it again)");


        // Section 3: tryLock() with no waiting
        ReentrantLock busyLock = new ReentrantLock();
        Thread holder1 = new Thread(() -> {
            busyLock.lock();
            try {
                Thread.sleep(1000);              // holds the lock for 1 second
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            } finally {
                busyLock.unlock();
            }
        });
        holder1.start();
        Thread.sleep(200);                       // make sure holder1 has the lock

        System.out.println("\n3. tryLock() while another thread holds the lock");
        boolean got = busyLock.tryLock();        // returns at once
        System.out.println("   tryLock()         : " + got + "  (did not wait, so main can do something else)");
        holder1.join();

        // Section 4: tryLock(timeout)
        ReentrantLock timedLock = new ReentrantLock();
        Thread holder2 = new Thread(() -> {
            timedLock.lock();
            try {
                Thread.sleep(1500);              // holds the lock for 1.5 seconds
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            } finally {
                timedLock.unlock();
            }
        });
        holder2.start();
        Thread.sleep(200);

        System.out.println("\n4. tryLock(timeout)");
        boolean got1 = timedLock.tryLock(500, TimeUnit.MILLISECONDS);
        System.out.println("   wait up to 500 ms : " + got1 + "  (gave up, lock is held for 1.5 s)");

        boolean got2 = timedLock.tryLock(2000, TimeUnit.MILLISECONDS);
        System.out.println("   wait up to 2000 ms: " + got2 + "  (lock was released while we waited)");
        if (got2) {
            timedLock.unlock();                  // only unlock if we really got it
        }
        holder2.join();

        // Section 5: reentrant (same thread locks twice)
        ReentrantLock reLock = new ReentrantLock();
        System.out.println("\n5. Reentrant: same thread locks twice");
        reLock.lock();
        reLock.lock();                           // does NOT block itself
        System.out.println("   hold count after 2 lock()   : " + reLock.getHoldCount());
        reLock.unlock();
        System.out.println("   hold count after 1 unlock() : " + reLock.getHoldCount() + "  (still locked)");
        reLock.unlock();
        System.out.println("   hold count after 2 unlock() : " + reLock.getHoldCount() + "  (free now)");
    }
}