package phase1;
/*
 * CONCEPT
 * - A thread moves through states: NEW -> RUNNABLE -> TIMED_WAITING -> TERMINATED.
 *
 * MAIN RULES
 * - NEW: created but start() not called
 * - RUNNABLE: running or ready to run
 * - TIMED_WAITING: inside sleep() or a timed wait/join
 * - TERMINATED: run() has finished
 *
 * KEY POINT
 * - getState() is only a snapshot. The state can change right after you read it.
 *
 * GOAL
 * - Print worker state at 4 moments around a 500 ms sleep.
 * - Expected: NEW, RUNNABLE (likely), TIMED_WAITING, TERMINATED
 */
public class P3_LifecycleDemo {
    public static void main(String[] args) throws InterruptedException {
        Thread t = new Thread(() -> {
            try {
                // Step 1: worker thread t sleeps for 500 ms
                Thread.sleep(500);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }, "worker");

        System.out.println("Before start:   " + t.getState());   // NEW

        // Step 1: t.start() launches thread t, its code begins running on its own counting 500 sec
        t.start();

        //get state immediately after start
        System.out.println("After start:    " + t.getState());   // likely RUNNABLE

        // Step 2: at the same time, main sleeps for 400 ms (worker is still sleeping too)
        Thread.sleep(400);

        //another way to achieve this Step 2
//        long end = System.currentTimeMillis() + 400;
//        while (System.currentTimeMillis() < end) {
//            // busy-wait for about 400 ms
//        }

        // Step 3: main wakes up at 400 ms and checks t's state
        // Step 4: t still has about 100 ms left, so the state is TIMED_WAITING
        System.out.println("While sleeping: " + t.getState());   // TIMED_WAITING

        // main waits until t finishes its remaining sleep
        t.join();
        System.out.println("After join:     " + t.getState());   // TERMINATED
    }
}