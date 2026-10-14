package old.phase2;
/*
 * CONCEPT
 * - synchronized lets only one thread at a time run a block of code.
 *
 * MAIN RULES
 * - Both threads must lock the SAME object
 * - Lock is released automatically when the block ends
 * - It gives both atomicity and visibility
 *
 * KEY POINT
 * - Locks on different objects do not protect each other.
 *   The shared lock object is what makes it work.
 *
 * GOAL
 * - Same program as P1_RaceConditionDemo, but counter++ is inside synchronized (lock).
 * - Expected: RESULT is always 200000, "CHECK: correct -> no lost updates"
 * - Broken version: P1_RaceConditionDemo
 */
public class P2_SynchronizedFixDemo {

    static int counter = 0;

    // one shared lock object, both threads must use the same one
    static final Object lock = new Object();

    public static void main(String[] args) throws InterruptedException {

        Runnable task = () -> {
            for (int i = 0; i < 100_000; i++) {
                // only one thread at a time can be inside this block
                synchronized (lock) {
                    counter++;
                }
            }
        };

        Thread t1 = new Thread(task, "worker-1");
        Thread t2 = new Thread(task, "worker-2");

        System.out.println("main: starting workers");
        t1.start();
        t2.start();

        t1.join();
        t2.join();

        System.out.println("EXPECTED: 200000");
        System.out.println("RESULT: " + counter);
        System.out.println(counter == 200_000 ? "CHECK: correct -> no lost updates" : "CHECK: wrong -> lost updates");
    }
}