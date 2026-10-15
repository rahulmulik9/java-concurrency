package phase3;

import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/*
 * WHAT WE ARE GOING TO LEARN IN THIS STEP:
 *   - ReadWriteLock: a lock with TWO parts, a read lock and a write lock
 *   - Many threads can READ at the same time
 *   - Only ONE thread can WRITE, and nobody can read while it writes
 *
 * PROBLEM:
 *   A normal lock lets only one thread in at a time, even if all of them
 *   only READ. Reading does not change data, so readers waiting for each
 *   other is a waste of time.
 *
 * SOLUTION:
 *   ReentrantReadWriteLock:
 *   - readLock()  -> shared: many readers at once
 *   - writeLock() -> exclusive: one writer, and no readers during the write
 *   Best when there are many more reads than writes.
 *   Each read and write below takes 500 ms, so total time shows who waited.
 */
public class P15_ReadWriteLockDemo {

    static int data = 0;

    public static void main(String[] args) throws InterruptedException {

        // Section 1: normal lock, 3 readers (SLOW)
        ReentrantLock normalLock = new ReentrantLock();

        Runnable reader1 = () -> {
            normalLock.lock();
            try {
                Thread.sleep(500);                   // reading takes 500 ms
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            } finally {
                normalLock.unlock();
            }
        };

        long start = System.currentTimeMillis();
        Thread a1 = new Thread(reader1);
        Thread a2 = new Thread(reader1);
        Thread a3 = new Thread(reader1);
        a1.start();
        a2.start();
        a3.start();
        a1.join();
        a2.join();
        a3.join();
        System.out.println("1. Normal lock, 3 readers        : "
                + (System.currentTimeMillis() - start) + " ms  (readers took turns)");

        // Section 2: read lock, 3 readers (FAST)
        ReentrantReadWriteLock rwLock = new ReentrantReadWriteLock();

        Runnable reader2 = () -> {
            rwLock.readLock().lock();
            try {
                Thread.sleep(500);                   // reading takes 500 ms
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            } finally {
                rwLock.readLock().unlock();
            }
        };

        start = System.currentTimeMillis();
        Thread b1 = new Thread(reader2);
        Thread b2 = new Thread(reader2);
        Thread b3 = new Thread(reader2);
        b1.start();
        b2.start();
        b3.start();
        b1.join();
        b2.join();
        b3.join();
        System.out.println("2. Read lock, 3 readers          : "
                + (System.currentTimeMillis() - start) + " ms  (readers ran together)");

        // Section 3: a writer blocks readers
        Thread writer = new Thread(() -> {
            rwLock.writeLock().lock();
            try {
                data = 42;
                Thread.sleep(1000);                  // writing takes 1 second
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            } finally {
                rwLock.writeLock().unlock();
            }
        });
        writer.start();
        Thread.sleep(200);                           // make sure the writer has the lock

        long readerStart = System.currentTimeMillis();
        rwLock.readLock().lock();                    // main tries to read
        try {
            long waited = System.currentTimeMillis() - readerStart;
            System.out.println("3. Reader while writer is writing : waited " + waited
                    + " ms, then read data = " + data);
        } finally {
            rwLock.readLock().unlock();
        }
        writer.join();

        // Section 4: readers block a writer
        Thread reader3 = new Thread(() -> {
            rwLock.readLock().lock();
            try {
                Thread.sleep(1000);                  // reading takes 1 second
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            } finally {
                rwLock.readLock().unlock();
            }
        });
        reader3.start();
        Thread.sleep(200);                           // make sure the reader has the lock

        long writerStart = System.currentTimeMillis();
        rwLock.writeLock().lock();                   // main tries to write
        try {
            long waited = System.currentTimeMillis() - writerStart;
            data = 99;
            System.out.println("4. Writer while reader is reading : waited " + waited
                    + " ms, then wrote data = " + data);
        } finally {
            rwLock.writeLock().unlock();
        }
        reader3.join();
    }
}