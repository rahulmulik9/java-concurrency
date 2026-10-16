package phase5;

import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.Exchanger;
import java.util.concurrent.Phaser;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/*
 * WHAT WE ARE GOING TO LEARN IN THIS STEP:
 *   - Phaser(n): a barrier with many phases (phase 0, 1, 2 ...)
 *   - arriveAndAwaitAdvance(): "I finished this phase. Wait for the others."
 *   - arriveAndDeregister(): "I finished and I leave. Do not wait for me anymore."
 *   - A Phaser can change its number of threads. CyclicBarrier cannot.
 *   - Exchanger: two threads swap one object. Each waits for the other.
 *
 * REAL-LIFE EXAMPLE 1: FRIENDS ON A TRIP (Phaser)
 *   - 3 friends visit stop 1 and stop 2. Each stop = one phase.
 *   - At the end of stop 1, friend 3 goes home.
 *   - Friends 1 and 2 must go to stop 2 without waiting for friend 3.
 *
 * REAL-LIFE EXAMPLE 2: SWAPPING CARDS (Exchanger)
 *   - Kid A and kid B swap cards. Nobody gives a card before the other is ready.
 *
 * PROBLEM:
 *   CyclicBarrier(3) always waits for 3 threads. If worker 3 leaves,
 *   workers 1 and 2 wait for him forever.
 *
 * SOLUTION:
 *   Phaser. Worker 3 calls arriveAndDeregister(). The phaser now waits for 2 threads only.
 */
public class P29_PhaserExchangerDemo {

    public static void main(String[] args) throws InterruptedException {

        // ---------------------------------------------------------
        // Section 1: BROKEN - CyclicBarrier(3), worker 3 leaves after phase 1
        // Worker 3 is slow (1000 ms). We use a timeout, so the demo does not hang.
        // (sleep only pretends to be work.)
        // ---------------------------------------------------------
        CyclicBarrier barrier = new CyclicBarrier(3);

        Thread[] t1 = new Thread[3];
        for (int i = 0; i < 3; i++) {
            final int id = i + 1;
            t1[i] = new Thread(() -> {
                try {
                    if (id == 3) {
                        Thread.sleep(1000);
                    }
                    System.out.println("Section 1 worker " + id + " finished phase 1");
                    barrier.await();                          // all 3 meet here
                    if (id == 3) {
                        System.out.println("Section 1 worker 3 leaves");
                        return;                               // worker 3 never comes to phase 2
                    }
                    System.out.println("Section 1 worker " + id + " started phase 2");
                    barrier.await(1, TimeUnit.SECONDS);       // waits for worker 3, who left
                } catch (TimeoutException | BrokenBarrierException e) {
                    System.out.println("Section 1 worker " + id + " STUCK: " + e.getClass().getSimpleName());
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            });
            t1[i].start();
        }
        for (Thread t : t1) {
            t.join();
        }

        // ---------------------------------------------------------
        // Section 2: FIXED - Phaser(3), worker 3 deregisters
        // Same workers, same words in the prints.
        // ---------------------------------------------------------
        Phaser phaser = new Phaser(3);                        // 3 threads are registered

        Thread[] t2 = new Thread[3];
        for (int i = 0; i < 3; i++) {
            final int id = i + 1;
            t2[i] = new Thread(() -> {
                try {
                    if (id == 3) {
                        Thread.sleep(1000);
                    }
                    System.out.println("Section 2 worker " + id + " finished phase 1");
                    phaser.arriveAndAwaitAdvance();           // all 3 meet here
                    if (id == 3) {
                        System.out.println("Section 2 worker 3 leaves");
                        phaser.arriveAndDeregister();         // "do not wait for me anymore"
                        return;
                    }
                    System.out.println("Section 2 worker " + id + " started phase 2");
                    phaser.arriveAndAwaitAdvance();           // waits for 2 threads only
                    System.out.println("Section 2 worker " + id + " finished phase 2");
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            });
            t2[i].start();
        }
        for (Thread t : t2) {
            t.join();
        }

        // ---------------------------------------------------------
        // Section 3: Exchanger - two kids swap cards
        // Kid B is slow (500 ms). Kid A waits inside exchange().
        // ---------------------------------------------------------
        Exchanger<String> exchanger = new Exchanger<>();

        Thread kidA = new Thread(() -> {
            try {
                System.out.println("Section 3 kid A has card A, waits for kid B");
                String got = exchanger.exchange("card A");    // give card A, wait, get B's card
                System.out.println("Section 3 kid A got " + got);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        });

        Thread kidB = new Thread(() -> {
            try {
                Thread.sleep(500);                            // kid B is slow
                System.out.println("Section 3 kid B has card B, arrives");
                String got = exchanger.exchange("card B");
                System.out.println("Section 3 kid B got " + got);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        });

        kidA.start();
        kidB.start();
        kidA.join();
        kidB.join();
    }
}