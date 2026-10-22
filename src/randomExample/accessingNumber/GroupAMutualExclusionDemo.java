package randomExample.accessingNumber;

import java.util.concurrent.Semaphore;
import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.concurrent.locks.StampedLock;

/*
 * WHAT WE ARE GOING TO LEARN IN THIS STEP:
 *   Group A: mutual exclusion. Only one thread is in the critical section at a time.
 *   synchronized, ReentrantLock, fair ReentrantLock, Semaphore(1), StampedLock, ReadWriteLock
 *
 * PROBLEM:
 *   3 threads each do "read counter, sleep 1 sec, write counter + 1".
 *   Without protection they all read 0 and all write 1, so we lose updates.
 *
 * SOLUTION:
 *   Make threads wait for each other. Total time is about 3 seconds and the result is 3.
 *   The ORDER (T1, T2, T3) is decided by the scheduler, not guaranteed.
 */
public class GroupAMutualExclusionDemo {

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

    /** read -> sleep -> write. Unsafe on purpose, so a missing lock shows up. */
    private static void incrementSlowly() {
        int read = counter;
        sleep1s();
        counter = read + 1;
        System.out.println("   " + Thread.currentThread().getName() + " -> counter = " + counter);
    }

    private static void runThreads(Runnable task) throws InterruptedException {
        Thread[] threads = new Thread[3];
        for (int i = 0; i < 3; i++) {
            threads[i] = new Thread(task, "T" + (i + 1));
            threads[i].start();
        }
        for (Thread t : threads) {
            t.join();
        }
    }

    private static void run(String name, Way way) throws Exception {
        counter = 0; // reset so ways don't affect each other
        System.out.println("=== " + name + " ===");
        long start = System.currentTimeMillis();
        way.run();
        long ms = System.currentTimeMillis() - start;
        System.out.println("   final counter = " + counter + ", time = " + ms + " ms\n");
    }

    // ---------- the ways ----------

    /** Baseline (broken): no protection. Expect counter = 1, time about 1 sec. */
    static void noLockBrokenWay() throws InterruptedException {
        runThreads(GroupAMutualExclusionDemo::incrementSlowly);
    }

    /** Way 1: intrinsic lock. */
    private static final Object LOCK = new Object();

    static void synchronizedWay() throws InterruptedException {
        runThreads(() -> {
            synchronized (LOCK) {
                incrementSlowly();
            }
        });
    }

    /** Way 2: explicit lock. Always unlock in finally. */
    private static final ReentrantLock reentrantLock = new ReentrantLock();

    static void reentrantLockWay() throws InterruptedException {
        runThreads(() -> {
            reentrantLock.lock();
            try {
                incrementSlowly();
            } finally {
                reentrantLock.unlock();
            }
        });
    }

    /** Way 3: fair lock. The longest-waiting thread gets the lock next. */
    private static final ReentrantLock fairLock = new ReentrantLock(true);

    static void fairLockWay() throws InterruptedException {
        runThreads(() -> {
            fairLock.lock();
            try {
                incrementSlowly();
            } finally {
                fairLock.unlock();
            }
        });
    }

    /** Way 4: a semaphore with 1 permit behaves like a lock. */
    private static final Semaphore semaphore = new Semaphore(1);

    static void semaphoreWay() throws InterruptedException {
        runThreads(() -> {
            try {
                semaphore.acquire();
                try {
                    incrementSlowly();
                } finally {
                    semaphore.release();
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        });
    }

    /**
     * Way 5: StampedLock write lock.
     * Works, but overkill. StampedLock shines for optimistic READS, and it is not reentrant.
     */
    private static final StampedLock stampedLock = new StampedLock();

    static void stampedLockWay() throws InterruptedException {
        runThreads(() -> {
            long stamp = stampedLock.writeLock();
            try {
                incrementSlowly();
            } finally {
                stampedLock.unlockWrite(stamp);
            }
        });
    }

    /**
     * Way 6: ReadWriteLock write lock.
     * Works, but unnecessary. Every thread is a writer, so the "many readers" benefit is unused.
     */
    private static final ReentrantReadWriteLock rwLock = new ReentrantReadWriteLock();

    static void readWriteLockWay() throws InterruptedException {
        runThreads(() -> {
            rwLock.writeLock().lock();
            try {
                incrementSlowly();
            } finally {
                rwLock.writeLock().unlock();
            }
        });
    }

    // ---------- main ----------

    public static void main(String[] args) throws Exception {
        run("0. No lock (BROKEN baseline)", GroupAMutualExclusionDemo::noLockBrokenWay);
        run("1. synchronized", GroupAMutualExclusionDemo::synchronizedWay);
        run("2. ReentrantLock", GroupAMutualExclusionDemo::reentrantLockWay);
        run("3. ReentrantLock (fair)", GroupAMutualExclusionDemo::fairLockWay);
        run("4. Semaphore(1)", GroupAMutualExclusionDemo::semaphoreWay);
        run("5. StampedLock (write)", GroupAMutualExclusionDemo::stampedLockWay);
        run("6. ReadWriteLock (write)", GroupAMutualExclusionDemo::readWriteLockWay);

        /*
         * OUTPUT EXPLANATION:
         *   Way 0 ends with 1 in about 1 sec: all three read 0 at the same time.
         *   Ways 1-6 end with 3 in about 3 sec: threads took turns, one second each.
         */
    }
}