package phase3;

import java.util.LinkedList;
import java.util.Queue;

/*
 * WHAT WE ARE GOING TO LEARN IN THIS STEP:
 *   - Bounded buffer using only synchronized + wait() / notifyAll()
 *   - Producer waits when the buffer is full, consumer waits when it is empty
 *   - The limit: one lock has only ONE wait-set (see P14_ConditionDemo for the fix)
 *
 * PROBLEM:
 *   Producers and consumers wait in the SAME list on the same lock.
 *   notify() might wake the wrong kind of thread (a producer wakes another producer),
 *   so we have to use notifyAll() and wake everyone, just to re-check the condition.
 *
 * SOLUTION (in the next class):
 *   Condition gives one lock several wait-sets: notFull for producers, notEmpty for consumers.
 */
public class P14_a_BoundedBufferWaitNotify {

    static final int CAPACITY = 2;
    static Queue<Integer> buffer = new LinkedList<>();
    static final Object lock = new Object();

    public static void main(String[] args) throws InterruptedException {

        Thread producer = new Thread(() -> {
            try {
                for (int i = 1; i <= 5; i++) {
                    synchronized (lock) {
                        while (buffer.size() == CAPACITY) {          // while, not if
                            System.out.println("   [producer] buffer full, waiting");
                            lock.wait();                             // releases the lock and sleeps
                        }
                        buffer.add(i);
                        System.out.println("   [producer] put " + i + "   buffer = " + buffer);
                        lock.notifyAll();                            // wakes EVERYONE waiting on this lock
                    }
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        });

        Thread consumer = new Thread(() -> {
            try {
                for (int i = 1; i <= 5; i++) {
                    synchronized (lock) {
                        while (buffer.isEmpty()) {                   // while, not if
                            System.out.println("   [consumer] buffer empty, waiting");
                            lock.wait();                             // releases the lock and sleeps
                        }
                        int item = buffer.remove();
                        System.out.println("   [consumer] took " + item + "  buffer = " + buffer);
                        lock.notifyAll();                            // wakes EVERYONE waiting on this lock
                    }
                    Thread.sleep(300);                               // slow consumer, outside the lock
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        });

        consumer.start();
        Thread.sleep(100);                                           // consumer finds the buffer empty first
        producer.start();
        producer.join();
        consumer.join();
        System.out.println("   all 5 items produced and consumed");
    }
}