package phase2;
/*
 * WHAT WE ARE GOING TO LEARN IN THIS STEP (STEP 8, PART A):
 *   - Visibility problem: one thread changes a variable, another thread never sees it
 *   - volatile keyword fixes visibility
 *   - Write the data first, then the volatile flag (the reader sees the data too)
 *
 * PROBLEM:
 *   main sets a flag to false to stop a worker, but the worker keeps running forever.
 *   Each thread can keep its own cached copy of a variable (CPU cache or register),
 *   and the JIT compiler may read the flag only once outside the loop.
 *   So the worker never sees the new value.
 *
 * SOLUTION:
 *   Mark the flag volatile. Every read goes to main memory and every write is
 *   visible to other threads straight away.
 *   (Java Memory Model: a write to a volatile variable happens-before every later
 *   read of that same variable, so the reader sees everything written before it.)
 *
 * NOTE:
 *   Section 1 is not guaranteed to hang on every machine, so run it a few times.
 *   volatile does NOT make count++ atomic. See P8_b_Atomic_vs_Volatile.
 */
public class P8_a_VolatileVisibilityDemo {

    static boolean running = true;                 // NOT volatile
    static int myNumber = 1011;                    // NOT volatile

    static volatile boolean volatileRunning = true;
    static int volatileNumber = 1011;              // plain variable, made visible by the volatile flag

    public static void main(String[] args) throws InterruptedException {

        // Section 1: no volatile (BROKEN)
        // Daemon thread, so the JVM can still exit at the end even if it never stops
        Thread a = new Thread(() -> {
            while (running) {
                // empty loop: the thread may never re-read 'running'
            }
            System.out.println("   [worker-1] saw running = false, myNumber = " + myNumber);
        });
        a.setDaemon(true);
        a.start();

        Thread.sleep(1000);
        myNumber = 8085;
        running = false;
        System.out.println("1. main set myNumber = 8085,\n   then running = false");
        a.join(2000);                              // wait at most 2 seconds
        System.out.println("   Worker still alive after 2 seconds? " + a.isAlive() + "  (expected false)");

        // Section 2: volatile (FIXED)
        Thread b = new Thread(() -> {
            while (volatileRunning) {
                // empty loop: volatile forces a fresh read every time
            }
            System.out.println("   [worker-2] saw volatileRunning is set false, and volatileNumber is set to "+volatileNumber);
        });
        b.start();

        Thread.sleep(1000);
        volatileNumber = 8085;                     // data FIRST
        volatileRunning = false;                   // volatile flag LAST
        System.out.println("\n2. main set volatileNumber = 8085,\n   then volatileRunning = false");
        b.join(2000);
        System.out.println("   Worker still alive after 2 seconds? " + b.isAlive() + "  (expected false)");

    }
}