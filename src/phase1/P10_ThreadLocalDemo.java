package phase1;
/*
 * CONCEPT
 * - ThreadLocal gives every thread its own private copy of a variable.
 *
 * MAIN RULES
 * - Normal static field: one box shared by all threads
 * - ThreadLocal: one box PER thread, other threads cannot see it
 * - withInitial() sets the starting value for each thread
 *
 * KEY POINT
 * - Shared field loses values when threads overwrite each other.
 *   ThreadLocal never does, because nothing is shared.
 *
 * GOAL
 * - 3 workers write their name into both boxes, sleep, then read back.
 * - Expected: sharedName = same value (last writer) for all workers,
 *   privateName = each worker's own name
 * - main: sharedName = last writer's name, privateName = "nobody" (main never set it)
 */
public class P10_ThreadLocalDemo {

    // ONE box shared by all threads
    private static String sharedName = "nobody";

    // One box PER thread
    private static final ThreadLocal<String> privateName = ThreadLocal.withInitial(() -> "nobody");

    public static void main(String[] args) throws InterruptedException {

        Runnable task = () -> {
            String me = Thread.currentThread().getName();

            // Step 1: write
            System.out.println(me +" Changing sharedName and privateName to current thread name");
            sharedName = me;
            privateName.set(me);

            // Step 2: wait
            System.out.println(me + " GOING TO: sleep 300 ms so the other threads can write too");
            try {
                Thread.sleep(300);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }

            // Step 3: read back
            String shared = sharedName;
            String privat = privateName.get();
            System.out.println(me + " GOING to read both names : Shared Name : "+ shared +", private Name : "+ privat);
        };

        Thread t1 = new Thread(task, "worker1");
        Thread t2 = new Thread(task, "worker2");
        Thread t3 = new Thread(task, "worker3");

        t1.start();
        t2.start();
        t3.start();

        t1.join();
        t2.join();
        t3.join();

        System.out.println();
        System.out.println("main GOING TO: read both boxes (main never wrote anything)");
        System.out.println("main RESULT: sharedName  = " + sharedName + " -> main can see the shared value");
        System.out.println("main RESULT: localName = " + privateName.get() + " -> main has its own box, it was never set");

        System.out.println();
        System.out.println("CONCLUSION: shared box lost values, private boxes never did");
    }
}