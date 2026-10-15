package phase2;

/*
 * WHAT WE ARE GOING TO LEARN IN THIS STEP:
 *   - Object lock (instance synchronized method, locks "this")
 *   - Class lock (static synchronized method, locks ClassName.class)
 *   - A thread waits ONLY if another thread holds the SAME lock
 *
 * PROBLEM:
 *   Many people think synchronized always makes threads wait for each other.
 *   It does not. Threads that use different locks run at the same time.
 *
 * SOLUTION:
 *   Know which lock each method uses:
 *   - instance synchronized method -> lock of that object (one lock per object)
 *   - static synchronized method   -> lock of the class (one lock for the whole class)
 *   Each task below takes 1 second, so total time shows if threads waited or not.
 */
public class P7_b_ObjectVsClassLockDemo {

    static class Worker {

        // Object lock: takes 1 second
        synchronized void instanceTask() {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }

        // Class lock: takes 1 second
        static synchronized void staticTask() {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }

    public static void main(String[] args) throws InterruptedException {

        Worker w1 = new Worker();
        Worker w2 = new Worker();

        // Section 1: same object -> same object lock -> threads wait (about 2000 ms)
        long start = System.currentTimeMillis();
        Thread a1 = new Thread(() -> w1.instanceTask());
        Thread a2 = new Thread(() -> w1.instanceTask());
        a1.start();
        a2.start();
        a1.join();
        a2.join();
        System.out.println("1. Same object, instance method        : " + (System.currentTimeMillis() - start) + " ms  (waited)");

        // Section 2: different objects -> different object locks -> run together (about 1000 ms)
        start = System.currentTimeMillis();
        Thread b1 = new Thread(() -> w1.instanceTask());
        Thread b2 = new Thread(() -> w2.instanceTask());
        b1.start();
        b2.start();
        b1.join();
        b2.join();
        System.out.println("2. Different objects, instance method  : " + (System.currentTimeMillis() - start) + " ms  (did NOT wait)");

        // Section 3: static method -> one class lock for everyone -> threads wait (about 2000 ms)
        start = System.currentTimeMillis();
        Thread c1 = new Thread(() -> Worker.staticTask());
        Thread c2 = new Thread(() -> Worker.staticTask());
        c1.start();
        c2.start();
        c1.join();
        c2.join();
        System.out.println("3. Static method (class lock)          : " + (System.currentTimeMillis() - start) + " ms  (waited)");

        // Section 4: object lock + class lock are DIFFERENT locks -> run together (about 1000 ms)
        start = System.currentTimeMillis();
        Thread d1 = new Thread(() -> w1.instanceTask());
        Thread d2 = new Thread(() -> Worker.staticTask());
        d1.start();
        d2.start();
        d1.join();
        d2.join();
        System.out.println("4. Instance method + static method     : " + (System.currentTimeMillis() - start) + " ms  (did NOT wait)");
    }
}

/*
 * NOTE: OBJECT LOCK vs CLASS LOCK
 *
 * POINT 1: Two threads, SAME object, instance synchronized method
 *   Why it happens:
 *     An instance synchronized method locks the object (this).
 *     Both threads need the lock of the same object w1.
 *     Only one thread can hold it, so the other waits for it to be released.
 *
 *
 * POINT 2: One thread uses an instance method, another uses a static method
 *
 *   Scenario:
 *     Thread 1 calls w1.instanceTask()      (instance synchronized)
 *     Thread 2 calls Worker.staticTask()    (static synchronized)
 *     Result: NO waiting, both run at the same time.
 *
 *   Why it happens:
 *     The instance method locks the object (w1).
 *     The static method locks the class (Worker.class).
 *     These are two different locks, so neither thread blocks the other.
 *
 * RULE: A thread waits ONLY if another thread holds the SAME lock.
 */