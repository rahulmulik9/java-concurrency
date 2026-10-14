package old.phase2;
/*
 * CONCEPT
 * - Thread confinement: keep data inside one thread so no sharing, no locking needed.
 *
 * MAIN RULES
 * - Local variables live on the thread's own stack, other threads cannot see them
 * - Count locally, then combine the results once at the end
 * - Only the short final merge needs a lock
 *
 * KEY POINT
 * - Best way to avoid race conditions is to not share the data at all.
 *
 * GOAL
 * - Example 1: both threads update one shared counter (no lock).
 * - Example 2: each thread counts locally, then adds to the total under one short lock.
 * - Expected: Example 1 result less than 200000 (lost updates)
 *   Example 2 result always 200000
 */
public class P7_ThreadConfinementDemo {

    // shared by both threads, no protection
    static int sharedCounter = 0;

    // sum of the two local counts, each written by one thread after its loop
    static int confinedTotal = 0;

    public static void main(String[] args) throws InterruptedException {

        Runnable sharedTask = () -> {
            for (int i = 0; i < 100_000; i++) {
                // both threads touch the same variable
                sharedCounter++;
            }
        };

        Runnable confinedTask = () -> {
            // local variable, lives on this thread's own stack
            int localCount = 0;
            for (int i = 0; i < 100_000; i++) {
                // no other thread can see localCount
                localCount++;
            }
            // one short lock to combine results, the counting itself needed no lock
            synchronized (P7_ThreadConfinementDemo.class) {
                confinedTotal += localCount;
           }
        };

        System.out.println("EXAMPLE 1 shared counter");
        Thread a1 = new Thread(sharedTask, "worker-1");
        Thread a2 = new Thread(sharedTask, "worker-2");
        a1.start();
        a2.start();
        a1.join();
        a2.join();
        System.out.println("RESULT " + sharedCounter + " -> lost updates");

        System.out.println("EXAMPLE 2 confined counter");
        Thread b1 = new Thread(confinedTask, "worker-3");
        Thread b2 = new Thread(confinedTask, "worker-4");
        b1.start();
        b2.start();
        b1.join();
        b2.join();
        System.out.println("RESULT " + confinedTotal + " -> always correct");
    }
}