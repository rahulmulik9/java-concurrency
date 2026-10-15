package phase1;

/*
 * WHAT WE ARE GOING TO LEARN IN THIS STEP:
 *   - Thread.sleep(ms): pause the CURRENT thread for a while
 *   - thread.join(): make the current thread WAIT until another thread finishes
 *   - thread.join(ms): wait, but give up after a timeout
 *
 * PROBLEM:
 *   main starts a worker thread and immediately reads the worker's result.
 *   The worker is still running, so main sees an incomplete result (0).
 *   start() returns right away; it does NOT wait for the thread to finish.
 *
 * SOLUTION:
 *   Call worker.join() before reading the result. main waits until the
 *   worker is TERMINATED, and join() also guarantees main can see
 *   everything the worker wrote (a happens-before relationship).
 */
public class P3_JoinSleepDemo {

    static int result = 0; // written by the worker, read by main

    // A slow task: sleeps 1 second to simulate work, then sets the result
    static Runnable slowTask() {
        return () -> {
            try {
                System.out.println("  [worker] started, working for 1 second...");
                Thread.sleep(1000);          // sleep: pause THIS thread only
                result = 42;
                System.out.println("  [worker] finished, result = 42");
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        };
    }

    public static void main(String[] args) throws InterruptedException {

        // 1. Broken version: read the result without waiting
        System.out.println("1. BROKEN: no join()");
        result = 0;
        Thread broken = new Thread(slowTask(), "worker-broken");
        broken.start();
        System.out.println("Worker-broken has not done yet so result is :"+ result +", state of the thread :" + broken.getState());
        broken.join(); // only so the worker's output finishes before step 2


        // 2. Fixed version: join() waits for the worker
        System.out.println("\n2. FIXED: with join()");
        result = 0;
        Thread fixed = new Thread(slowTask(), "worker-fixed");
        fixed.start();
        fixed.join();                        // main waits here until worker is TERMINATED
        System.out.println("Main wait for the worker-fixed to finish, now result is :"+result+", state of the thread :" + fixed.getState());

        // 3. join with timeout: don't wait forever
        System.out.println("\n3. join(ms): give up after a timeout");
        Thread slow = new Thread(slowTask(), "worker-slow");
        result = 0;
        slow.start();
        slow.join(300);                      // wait at most 300 ms
        System.out.println("Main wait for the worker-slow to finish, now result is :"+result+", state of the thread :" + slow .getState());
        slow.join();                         // now wait for real

    }
}