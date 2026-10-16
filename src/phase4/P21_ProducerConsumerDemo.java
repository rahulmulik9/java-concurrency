package phase4;

import java.util.Queue;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentLinkedQueue;

/*
 * WHAT WE ARE GOING TO LEARN IN THIS STEP:
 *   - Producer-Consumer pattern
 *   - BlockingQueue: put, take
 *   - Bounded queue: the producer waits when the queue is full
 *   - Poison pill: a special item that tells the consumer to stop
 *
 * PROBLEM:
 *   A producer makes items. A consumer uses them. They run at different speeds.
 *   With a normal queue, the consumer must keep asking "is there an item?"
 *   (busy-waiting). It wastes CPU. And nothing stops a fast producer
 *   from filling the memory.
 *
 * SOLUTION:
 *   BlockingQueue does the waiting for us.
 *   take() sleeps when the queue is empty. put() sleeps when the queue is full.
 *   No busy loop. No lost items. No thread-safety code from us.
 */
public class P21_ProducerConsumerDemo {

    public static void main(String[] args) throws InterruptedException {

        // ---------------------------------------------------------
        // Section 1: BROKEN - normal queue, consumer busy-waits
        // The producer is slow (100 ms per item).
        // The consumer keeps asking again and again. We count the empty answers.
        // (sleep is only here to make the producer slow. Never coordinate threads with sleep in real code.)
        // ---------------------------------------------------------
        Queue<Integer> queue1 = new ConcurrentLinkedQueue<>();
        long[] emptyPolls = {0};

        Thread producer1 = new Thread(() -> {
            try {
                for (int i = 1; i <= 5; i++) {
                    Thread.sleep(100);
                    queue1.add(i);
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        });

        Thread consumer1 = new Thread(() -> {
            int received = 0;
            while (received < 5) {
                Integer item = queue1.poll();   // returns null if the queue is empty
                if (item == null) {
                    emptyPolls[0]++;            // nothing to do, ask again. CPU is wasted.
                } else {
                    received++;
                }
            }
        });

        producer1.start();
        consumer1.start();
        producer1.join();
        consumer1.join();
        System.out.println("Section 1: consumer asked with no result " + emptyPolls[0] + " times");

        // ---------------------------------------------------------
        // Section 2: FIXED - ArrayBlockingQueue with size 2
        // The producer is fast. The consumer is slow (200 ms per item).
        // When the queue is full, put() waits. When it is empty, take() waits.
        // ---------------------------------------------------------
        BlockingQueue<Integer> queue2 = new ArrayBlockingQueue<>(2);
        final int STOP = -1; // poison pill

        Thread producer2 = new Thread(() -> {
            try {
                for (int i = 1; i <= 6; i++) {
                    queue2.put(i);   // waits here if the queue is full
                    System.out.println("Section 2 produced " + i);
                }
                queue2.put(STOP);    // tell the consumer: no more items
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        });

        Thread consumer2 = new Thread(() -> {
            try {
                while (true) {
                    int item = queue2.take();   // waits here if the queue is empty
                    if (item == STOP) {
                        break;
                    }
                    System.out.println("Section 2 consumed " + item);
                    Thread.sleep(200);          // slow work
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        });

        producer2.start();
        consumer2.start();
        producer2.join();
        consumer2.join();
        System.out.println("Section 2: done");
    }
}