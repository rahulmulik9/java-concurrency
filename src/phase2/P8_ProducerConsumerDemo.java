package phase2;

import java.util.LinkedList;
import java.util.Queue;

public class P8_ProducerConsumerDemo {

    static final Queue<Integer> buffer = new LinkedList<>();
    static final int CAPACITY = 2;

    // one lock protects the buffer, both threads must use it
    static final Object lock = new Object();

    public static void main(String[] args) throws InterruptedException {

        Thread producer = new Thread(() -> {
            try {
                for (int i = 1; i <= 5; i++) {
                    synchronized (lock) {
                        // while, not if: re-check after every wake-up
                        while (buffer.size() == CAPACITY) {
                            System.out.println("buffer full, producer waiting");
                            lock.wait();
                        }
                        buffer.add(i);
                        System.out.println("producer put " + i);
                        // wake the consumer, it may be waiting on an empty buffer
                        lock.notifyAll();
                    }
                }
            } catch (InterruptedException e) {
                return;
            }
        }, "producer");

        Thread consumer = new Thread(() -> {
            try {
                for (int i = 1; i <= 5; i++) {
                    synchronized (lock) {
                        while (buffer.isEmpty()) {
                            System.out.println("buffer empty, consumer waiting");
                            lock.wait();
                        }
                        int item = buffer.remove();
                        System.out.println("consumer took " + item);
                        // wake the producer, it may be waiting on a full buffer
                        lock.notifyAll();
                    }
                    // slow consumer, sleep outside the lock so the producer can work
                    Thread.sleep(300);
                }
            } catch (InterruptedException e) {
                return;
            }
        }, "consumer");

        producer.start();
        consumer.start();

        producer.join();
        consumer.join();
        System.out.println("main thread done");
    }
}