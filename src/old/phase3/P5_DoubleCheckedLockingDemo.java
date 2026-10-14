package old.phase3;

/*
 * THE PROBLEM
 * - Singleton: only one object of the class exists.
 * - Lazy: create the object only when someone first asks for it.
 *
 * SIMPLE WAY (broken)
 *     static Singleton getInstance() {
 *         if (instance == null) {
 *             instance = new Singleton();   // two threads can both get here
 *         }
 *         return instance;
 *     }
 * - Two threads arrive at the same time, both see null, both create an object.
 * - Now there are 2 Singletons.
 *
 * FIRST FIX: synchronized on the whole method
 * - Safe, but every call takes the lock, even after the object exists.
 * - That is slow.
 *
 * SECOND FIX: double-checked locking (used below)
 * - Check 1 (no lock): fast path, after the object exists nobody takes the lock.
 * - Check 2 (with lock): if two threads passed check 1 together,
 *   only the first creates the object.
 *
 * WHY volatile
 * - instance = new Singleton() is 3 steps: create, run constructor, publish.
 * - Without volatile, publish can be seen before the constructor finishes (Step 4).
 * - A thread in check 1 could then use a half-built object.
 * - volatile on instance stops this.
 *
 * GOAL
 * - 5 threads ask for the Singleton at the same time.
 * - Only one object must be created.
 */
public class P5_DoubleCheckedLockingDemo {

    static class Singleton {
        // volatile: safe publication (Step 4 problem)
        private static volatile Singleton instance;
        static int created = 0;

        private Singleton() {
            created++;
            System.out.println("Singleton: created");
        }

        static Singleton getInstance() {
            if (instance == null) {                      // check 1: no lock
                synchronized (Singleton.class) {
                    if (instance == null) {              // check 2: with lock
                        instance = new Singleton();
                    }
                }
            }
            return instance;
        }
    }

    public static void main(String[] args) throws InterruptedException {
        Thread[] threads = new Thread[5];

        for (int i = 0; i < threads.length; i++) {
            threads[i] = new Thread(() -> Singleton.getInstance());
            threads[i].start();
        }

        for (Thread t : threads) {
            t.join();
        }

        System.out.println("objects created: " + Singleton.created);
    }
}