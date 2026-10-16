package phase5;

import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicInteger;

/*
 * WHAT WE ARE GOING TO LEARN IN THIS STEP:
 *   - Semaphore(n): a counter of n permits
 *   - acquire(): take 1 permit. If no permit is free, WAIT.
 *   - release(): give 1 permit back (always in finally)
 *   - Only n threads go in. The others wait until someone leaves.
 *
 * REAL-LIFE EXAMPLE: PARKING LOT
 *   - The parking lot has ONLY 2 spaces.   = Semaphore(2)
 *   - A car is a thread.
 *   - Car takes a space.                   = acquire()
 *   - Car leaves and frees the space.      = release()
 *   - Cars waiting at the gate.            = threads blocked in acquire()
 *
 * PROBLEM:
 *   5 cars arrive, but the lot has only 2 spaces.
 *   Without a semaphore, a car that finds the lot full cannot wait.
 *   It gives up and goes to another place. Cars 3, 4 and 5 are lost.
 *
 * SOLUTION:
 *   Semaphore(2). A car calls acquire() before parking.
 *   If the lot is full, the car waits at the gate until a car leaves.
 *   All 5 cars get a space, but only 2 at the same time.
 */
public class P28_SemaphoreDemo {

    static final int SPACES = 2;   // the lot has only 2 spaces

    public static void main(String[] args) throws InterruptedException {

        // ---------------------------------------------------------
        // Section 1: BROKEN - no semaphore
        // A counter shows how many cars are in the lot.
        // If the lot is full, the car cannot wait. It goes to another place.
        // (sleep only pretends that the car is parked.)
        // ---------------------------------------------------------
        AtomicInteger carsInLot = new AtomicInteger(0);

        Thread[] t1 = new Thread[5];
        for (int i = 0; i < 5; i++) {
            final int id = i + 1;
            t1[i] = new Thread(() -> {
                try {
                 //   System.out.println("Section 1 car " + id + " arrived at gate");
                    int now = carsInLot.incrementAndGet();
                    if (now > SPACES) {
                        carsInLot.decrementAndGet();   // lot is full, take the car back out
                        System.out.println("Section 1 car " + id + " NO SPACE. Goes to another place.");
                        return;
                    }
                    System.out.println("Section 1 car " + id + " PARKED");
                    Thread.sleep(500);
                    carsInLot.decrementAndGet();
                    System.out.println("Section 1 car " + id + " left");
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            });
            t1[i].start();
        }
        for (Thread t : t1) {
            t.join();
        }
        System.out.println("Section 1, 3 car are still in waiting after park space emptied");
        // ---------------------------------------------------------
        // Section 2: FIXED - Semaphore(2)
        // Same cars. A car that finds the lot full WAITS at the gate.
        // ---------------------------------------------------------
        Semaphore parkingSpaces = new Semaphore(SPACES);

        Thread[] t2 = new Thread[5];
        for (int i = 0; i < 5; i++) {
            final int id = i + 1;
            t2[i] = new Thread(() -> {
                try {
                    System.out.println("Section 2 car " + id + " arrived at gate");
                    parkingSpaces.acquire();           // take a space. Wait here if the lot is full.
                    try {
                        System.out.println("Section 2 car " + id + " PARKED");
                        Thread.sleep(500);
                        System.out.println("Section 2 car " + id + " left");
                    } finally {
                        parkingSpaces.release();       // free the space for the next car
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            });
            t2[i].start();
        }
        for (Thread t : t2) {
            t.join();
        }
    }
}


/*package phase5;
public class P28_SemaphoreDemo {

    static final int SPACES = 2;   // the lot has only 2 spaces

    public static void main(String[] args) throws InterruptedException {

        // ---------------------------------------------------------
        // Section 1: BROKEN - no semaphore
        // We count the cars in the lot. Nothing stops it from going above 2.
        // (sleep only pretends that the car is parked.)
        // ---------------------------------------------------------
        AtomicInteger carsInLot1 = new AtomicInteger(0);

        Thread[] t1 = new Thread[5];
        for (int i = 0; i < 5; i++) {
            final int id = i + 1;
            t1[i] = new Thread(() -> {
                try {
                    int now = carsInLot1.incrementAndGet();
                    System.out.println("Section 1 car " + id + " parked. Cars in lot = " + now
                            + (now > SPACES ? "  <-- TOO MANY, only " + SPACES + " spaces!" : ""));
                    Thread.sleep(500);
                    carsInLot1.decrementAndGet();
                    System.out.println("Section 1 car " + id + " left");
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
        // Section 2: FIXED - Semaphore(2)
        // Same cars, same counter. The count never goes above 2.
        // ---------------------------------------------------------
        Semaphore parkingSpaces = new Semaphore(SPACES);
        AtomicInteger carsInLot2 = new AtomicInteger(0);

        Thread[] t2 = new Thread[5];
        for (int i = 0; i < 5; i++) {
            final int id = i + 1;
            t2[i] = new Thread(() -> {
                try {
                    parkingSpaces.acquire();          // take a space. Wait at the gate if none is free.
                    try {
                        int now = carsInLot2.incrementAndGet();
                        System.out.println("Section 2 car " + id + " parked. Cars in lot = " + now
                                + (now > SPACES ? "  <-- TOO MANY, only " + SPACES + " spaces!" : ""));
                        Thread.sleep(500);
                        carsInLot2.decrementAndGet();
                        System.out.println("Section 2 car " + id + " left");
                    } finally {
                        parkingSpaces.release();      // always free the space
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            });
            t2[i].start();
        }
        for (Thread t : t2) {
            t.join();
        }
    }
}*/