package phase1;

/*
 * WHAT WE ARE GOING TO LEARN IN THIS STEP:
 *   - A thread's state depends on WHEN we check it
 *   - Right after start(), the thread is usually RUNNABLE (it hasn't reached sleep() yet)
 *   - After giving it a little time, the same thread shows TIMED_WAITING
 *
 * PROBLEM:
 *   Checking getState() immediately after start() gives a misleading snapshot.
 *   The new thread hasn't run its own code yet, so it can't be in sleep().
 *
 * SOLUTION:
 *   Give the thread time to reach its sleep() call before checking.
 *   (Fine for a demo, but never use sleep() to coordinate threads in real code.)
 */
public class P3_JoinTimeDiff {

    // A slow task: sleeps for 2 seconds to simulate work
    static Runnable slowTask() {
        return () -> {
            try {
                Thread.sleep(2000);          // pauses THIS thread only
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        };
    }

    public static void main(String[] args) throws InterruptedException {

        // 1. Broken version: check the state immediately after start()
        //note some time it result is runnable, it depends on speed of CPU, if CPU is faster you get TIMED_WAITING state or else most time you get runnable
        Thread broken = new Thread(slowTask(), "broken");
        broken.start();
        System.out.println("1. Checked immediately after start()");
        System.out.println("   State: " + broken.getState());
        System.out.println("   Why: the thread was just started and has not reached sleep() yet.");
        broken.join();

        // 2. Fixed version: give the thread time to reach sleep()
        Thread fixed = new Thread(slowTask(), "fixed");
        fixed.start();
        Thread.sleep(100);                   // main waits 100 ms, so fixed reaches its sleep()
        System.out.println("\n2. Checked 100 ms after start()");
        System.out.println("   State: " + fixed.getState());
        System.out.println("   Why: Main is sleep for 100 ms so fixed get time to execute.");
        fixed.join();


        // 2. Terminate version: give the thread time to reach sleep()
        Thread terminate = new Thread(slowTask(), "terminate");
        System.out.println("\n3. Checked after finishing the job");
        terminate.start();
        terminate.join();
        System.out.println("   State: " + terminate.getState());
        System.out.println("   Why: Main wait till terminate finish it job.");

    }
}