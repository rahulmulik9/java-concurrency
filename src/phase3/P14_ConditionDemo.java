package phase3;

import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

/*
 * WHAT WE ARE GOING TO LEARN IN THIS STEP:
 *   - Condition: the ReentrantLock version of wait() / notify()
 *   - await() = wait(), signal() = notify()
 *   - One lock can have MANY conditions (many wait-sets)
 *   - Bounded buffer: producer waits when full, consumer waits when empty
 *
 * PROBLEM:
 *   With synchronized there is only ONE wait-set per lock.
 *   Producers waiting for "not full" and consumers waiting for "not empty"
 *   sit in the same list, so notify() can wake the wrong kind of thread.
 *   The only safe choice is notifyAll(), which wakes everyone just to re-check.
 *
 * SOLUTION:
 *   Create two Conditions from the same lock:
 *     notFull  -> producers wait here
 *     notEmpty -> consumers wait here
 *   A producer signals notEmpty (wakes a consumer), a consumer signals notFull
 *   (wakes a producer). Each signal reaches the right kind of thread.
 */
public class P14_ConditionDemo {

    static final int CAPACITY = 2;
    static Queue<Integer> buffer = new LinkedList<>();

    static ReentrantLock lock = new ReentrantLock();
    static Condition notFull = lock.newCondition();     // producers wait here
    static Condition notEmpty = lock.newCondition();    // consumers wait here

    static void pause(long ms) {
        try {
            Thread.sleep(ms);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    public static void main(String[] args) throws InterruptedException {

        // Section 1: await() without holding the lock (BROKEN)
        System.out.println("1. await() without lock()");
        try {
            notEmpty.await();                            // we do not hold the lock here
        } catch (IllegalMonitorStateException e) {
            System.out.println("   IllegalMonitorStateException: call lock() before await()\n");
        }

        // Section 2: bounded buffer with two conditions (FIXED)
        System.out.println("2. Bounded buffer (capacity " + CAPACITY + ") with notFull and notEmpty");

        Thread producer = new Thread(() -> {
            for (int i = 1; i <= 5; i++) {
                lock.lock();
                try {
                    while (buffer.size() == CAPACITY) {          // while, not if
                        System.out.println("   [producer] buffer full, waiting");
                        notFull.await();                         // releases the lock and sleeps
                    }
                    buffer.add(i);
                    System.out.println("   [producer] put " + i + "   buffer = " + buffer);
                    notEmpty.signal();                           // wake a consumer
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    return;
                } finally {
                    lock.unlock();
                }
            }
        });

        Thread consumer = new Thread(() -> {
            for (int i = 1; i <= 5; i++) {
                lock.lock();
                try {
                    while (buffer.isEmpty()) {                   // while, not if
                        System.out.println("   [consumer] buffer empty, waiting");
                        notEmpty.await();                        // releases the lock and sleeps
                    }
                    int item = buffer.remove();
                    System.out.println("   [consumer] took " + item + "  buffer = " + buffer);
                    notFull.signal();                            // wake a producer
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    return;
                } finally {
                    lock.unlock();
                }
                pause(300);                                      // slow consumer, outside the lock
            }
        });

        consumer.start();
        Thread.sleep(100);                                       // consumer finds the buffer empty first
        producer.start();
        producer.join();
        consumer.join();
        System.out.println("   all 5 items produced and consumed");
    }
}