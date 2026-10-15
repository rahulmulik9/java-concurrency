package phase2;

/*
 * WHAT WE ARE GOING TO LEARN IN THIS STEP:
 *   - synchronized method and synchronized block
 *   - synchronized static method
 *
 * PROBLEM:
 *   Two threads changing the same variable lose updates (step 6).
 *
 * SOLUTION:
 *   - synchronized method        -> whole method is protected
 *   - synchronized block         -> only the lines inside the block are protected
 *   - synchronized static method -> protects static data (shared by the whole class)
 */
public class P7_a_SynchronizedDemo {

    static class Counter {
        int count = 0;
        static int staticCount = 0;

        // BROKEN: no protection
        void unsafeIncrement() {
            count++;
        }

        // FIXED: whole method is protected
        synchronized void incrementMethod() {
            count++;
        }

        // FIXED: only this block is protected
        void incrementBlock() {
            synchronized (this) {
                count++;
            }
        }

        // FIXED: protects the static variable
        static synchronized void incrementStatic() {
            staticCount++;
        }
    }

    public static void main(String[] args) throws InterruptedException {

        Counter c = new Counter();

        // Section 1: no synchronized (BROKEN)
        Thread a1 = new Thread(() -> {
            for (int i = 0; i < 100000; i++) {
                c.unsafeIncrement();
            }
        });
        Thread a2 = new Thread(() -> {
            for (int i = 0; i < 100000; i++) {
                c.unsafeIncrement();
            }
        });
        a1.start();
        a2.start();
        a1.join();
        a2.join();
        System.out.println("1. No synchronized            : " + c.count + "  (expected 200000)");

        // Section 2: synchronized method (FIXED)
        c.count = 0;
        Thread b1 = new Thread(() -> {
            for (int i = 0; i < 100000; i++) {
                c.incrementMethod();
            }
        });
        Thread b2 = new Thread(() -> {
            for (int i = 0; i < 100000; i++) {
                c.incrementMethod();
            }
        });
        b1.start();
        b2.start();
        b1.join();
        b2.join();
        System.out.println("2. synchronized method        : " + c.count + "  (expected 200000)");

        // Section 3: synchronized block (FIXED)
        c.count = 0;
        Thread d1 = new Thread(() -> {
            for (int i = 0; i < 100000; i++) {
                c.incrementBlock();
            }
        });
        Thread d2 = new Thread(() -> {
            for (int i = 0; i < 100000; i++) {
                c.incrementBlock();
            }
        });
        d1.start();
        d2.start();
        d1.join();
        d2.join();
        System.out.println("3. synchronized block         : " + c.count + "  (expected 200000)");

        // Section 4: synchronized static method (FIXED)
        Thread e1 = new Thread(() -> {
            for (int i = 0; i < 100000; i++) {
                Counter.incrementStatic();
            }
        });
        Thread e2 = new Thread(() -> {
            for (int i = 0; i < 100000; i++) {
                Counter.incrementStatic();
            }
        });
        e1.start();
        e2.start();
        e1.join();
        e2.join();
        System.out.println("4. synchronized static method: " + Counter.staticCount + "  (expected 200000)");
    }
}