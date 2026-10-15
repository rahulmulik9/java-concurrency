package phase2;

import java.util.concurrent.atomic.AtomicInteger;

/*
 * WHAT WE ARE GOING TO LEARN IN THIS STEP:
 *   - Busy-waiting (checking a flag in a loop) wastes CPU
 *   - wait() and notify(): sleep until another thread says "wake up"
 *   - wait() and notify() must be called inside synchronized
 *
 * PROBLEM:
 *   A consumer must wait until a producer has the data ready.
 *   If the consumer keeps checking a flag in a loop (busy-waiting), it uses
 *   100% of a CPU core and does nothing useful.
 *
 * SOLUTION:
 *   The consumer calls lock.wait(). It releases the lock and sleeps without using CPU.
 *   The producer sets the flag, then calls lock.notify() to wake the consumer.
 */
public class P9_WaitNotifyDemo {

    static volatile boolean busyReady = false;   // Section 1 flag
    static final Object lock = new Object();     // Section 2 and 3 lock
    static boolean ready = false;                // Section 2 flag
    static AtomicInteger checks = new AtomicInteger(0);

    public static void main(String[] args) throws InterruptedException {

        // Section 1: busy-waiting (WASTEFUL)
        System.out.println("1. Busy-waiting used a CPU core for the whole second");
        Thread consumer1 = new Thread(() -> {
            while (!busyReady) {
                checks.incrementAndGet();                        // keeps checking, never rests
            }
            System.out.println("   [consumer-1] got the data after " + checks + " loop checks\n");
        });

        consumer1.start();
        Thread.sleep(1000);                      // producer needs 1 second to prepare data
        busyReady = true;
        consumer1.join();


        // Section 2: wait / notify (FIXED)
        checks = new AtomicInteger(0);
        System.out.println("2. wait/notify: consumer used no CPU while waiting");
        Thread consumer2 = new Thread(() -> {
            synchronized (lock) {
                while (!ready) {                 // while, not if (see key points)
                    try {
                        checks.incrementAndGet();
                        System.out.println("   [consumer-2] data not ready, calling wait().");
                        lock.wait();             // releases the lock and sleeps
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        return;
                    }
                }
                System.out.println("   [consumer-2] woke up, data is ready, got data after "+checks+" loop checks\n");
            }
        });

        consumer2.start();
        Thread.sleep(1000);                      // producer needs 1 second to prepare data
        synchronized (lock) {
            ready = true;                        // set the condition FIRST
            lock.notify();                       // then wake the waiting thread
            System.out.println("   [main] set ready to true,that is data is ready,  called notify()");
        }
        consumer2.join();

        // Section 3: wait() without synchronized (BROKEN)
        try {
            lock.wait();                         // we do not hold the lock here
        } catch (IllegalMonitorStateException e) {
            System.out.println("3. IllegalMonitorStateException: wait() must be called inside synchronized");
        }
    }
}