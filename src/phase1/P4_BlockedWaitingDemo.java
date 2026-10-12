package phase1;

public class P4_BlockedWaitingDemo {
    private static final Object lock = new Object();

    public static void main(String[] args) throws InterruptedException {

        // sleeper: sleeps for 1 second -> TIMED_WAITING
        Thread sleeper = new Thread(() -> {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException ignored) {}
        }, "sleeper");

        // waiter: waits for sleeper to finish, no time limit -> WAITING
        Thread waiter = new Thread(() -> {
            try {
                sleeper.join();
            } catch (InterruptedException ignored) {}
        }, "waiter");

        // blocked: needs the lock that main is holding -> BLOCKED
        Thread blocked = new Thread(() -> {
            synchronized (lock) {
                System.out.println("blocked thread got the lock");
            }
        }, "blocked");

        //first way => Main method get the lock
        sleeper.start();
        waiter.start();
        synchronized (lock) {                 // main takes the lock
            blocked.start();            // blocked tries to enter, but main holds the lock
            Thread.sleep(200);                // give the threads time to reach their states

            System.out.println("sleeper: " + sleeper.getState());   // TIMED_WAITING
            System.out.println("waiter:  " + waiter.getState());    // WAITING
            System.out.println("blocked: " + blocked.getState());   // BLOCKED
        }                                     // main releases the lock here

        blocked.join();
        waiter.join();


        //second way => blocked thread get the lock
        sleeper.start();
        waiter.start();
        blocked.start();
        Thread.sleep(100);        // blocked now has time to grab the lock and finish
        synchronized (lock) {
            Thread.sleep(200);                // give the threads time to reach their states

            System.out.println("sleeper: " + sleeper.getState());   // TIMED_WAITING
            System.out.println("waiter:  " + waiter.getState());    // WAITING
            System.out.println("blocked: " + blocked.getState());   // BLOCKED
        }

        //to see working comment out either first way or second way
    }
}