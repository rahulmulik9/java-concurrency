package phase1;

public class P4_BlockedWaitingDemo {
    private static final Object lock = new Object();

    public static void main(String[] args) throws InterruptedException {

        // sleeper: sleeps for 1 second -> TIMED_WAITING (sleep has a time limit)
        Thread sleeper = new Thread(() -> {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException ignored) {}
        }, "sleeper");

        // waiter: waits for sleeper to finish, with no time limit -> WAITING
        Thread waiter = new Thread(() -> {
            try {
                sleeper.join();
            } catch (InterruptedException ignored) {}
        }, "waiter");

        // blocked: needs the lock. Its state depends on who holds the lock when it starts.
        Thread blocked = new Thread(() -> {
            synchronized (lock) {
                System.out.println("blocked thread got the lock");
            }
        }, "blocked");

        // ====== First way: main takes the lock BEFORE starting blocked (reliable BLOCKED)
        sleeper.start();
        waiter.start();
        synchronized (lock) {               // main takes the lock first
            blocked.start();                // blocked tries to enter, but main holds the lock
            Thread.sleep(200);              // main sleeps 200 ms (still holding the lock)
            // so the other threads reach their states

            System.out.println("sleeper: " + sleeper.getState());   // TIMED_WAITING: ~800 ms of sleep left
            System.out.println("waiter:  " + waiter.getState());    // WAITING: sleeper.join() with no timeout
            System.out.println("blocked: " + blocked.getState());   // BLOCKED: main owns the lock
        }                                   // main releases the lock here, blocked can now enter

        blocked.join();                     // wait for blocked to print and finish
        waiter.join();                      // wait for waiter (which waits for sleeper)

        // Output:
        /*
        sleeper: TIMED_WAITING
        waiter:  WAITING
        blocked: BLOCKED
        blocked thread got the lock
        */


        // ====== Second way: blocked starts BEFORE main takes the lock (timing dependent)
        // blocked gets the lock first, prints, and finishes. By the time main checks, it is TERMINATED.
//        sleeper.start();
//        waiter.start();
//        blocked.start();
//        Thread.sleep(100);                // gives blocked time to grab the lock and finish
//        synchronized (lock) {             // main gets the lock only after blocked is done
//            Thread.sleep(200);
//
//            System.out.println("sleeper: " + sleeper.getState());   // TIMED_WAITING: ~700 ms of sleep left
//            System.out.println("waiter:  " + waiter.getState());    // WAITING: sleeper not finished yet
//            System.out.println("blocked: " + blocked.getState());   // TERMINATED: already got the lock and finished
//        }

        // Output:
        /*
        blocked thread got the lock
        sleeper: TIMED_WAITING
        waiter:  WAITING
        blocked: TERMINATED
        */

        // Variant: sleeper sleeps less than ~300 ms (shorter than main's total wait)
        // sleeper finishes before main checks, so waiter's join() also returns.
        // Expected:
        /*  sleeper: TERMINATED,
            waiter: TERMINATED,
            blocked: TERMINATED.*/
        // You may see sleeper: RUNNABLE only if you check at the exact moment it wakes up.

        // Note: run only one way at a time. Running both in the same main() throws
        // IllegalThreadStateException, because a thread can be started only once.
    }
}