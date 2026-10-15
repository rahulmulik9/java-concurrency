package phase1;

/*
 * WHAT WE ARE GOING TO LEARN IN THIS STEP:
 *   - The 6 thread states: NEW, RUNNABLE, BLOCKED, WAITING, TIMED_WAITING, TERMINATED
 *   - How to observe a state with Thread.getState()
 *   - What causes a thread to move from one state to another
 *
 * PROBLEM:
 *   When an app hangs or is slow, you can't tell why unless you know what
 *   state each thread is in. "Thread is stuck" could mean it is waiting
 *   for a lock, waiting for a signal, sleeping, or just busy computing.
 *
 * SOLUTION:
 *   Every thread is always in exactly one of 6 states. Thread.getState()
 *   shows it. Learn what puts a thread in each state, and thread dumps
 *   (step 34) will make sense later.
 */
public class P2_ThreadLifecycleDemo {

    static final Object lock = new Object();

    public static void main(String[] args) throws InterruptedException {

        // 1. NEW: created but start() not called yet
        Thread newThread = new Thread(() -> { });
        System.out.println("1. Before start()        : " + newThread.getState());

        // 2. RUNNABLE: running, or ready to run (busy loop for 500 ms)
        Thread runnable = new Thread(() -> {
            long end = System.currentTimeMillis() + 500;
            while (System.currentTimeMillis() < end) { /* burn CPU */ }
        });
        runnable.start();
        Thread.sleep(100); // give it time to start
        System.out.println("2. Busy looping          : " + runnable.getState());
        runnable.join();

        // 3. BLOCKED: wants a synchronized lock that another thread holds
        Thread blocked = new Thread(() -> {
            synchronized (lock) { /* never gets in while main holds the lock */ }
        });
        synchronized (lock) {              // main holds the lock
            blocked.start();
            Thread.sleep(100);
            System.out.println("3. Waiting for the lock : " + blocked.getState());
        }                                  // main releases the lock here
        blocked.join();

        // 4. WAITING: waits forever until another thread signals it
        Thread waiting = new Thread(() -> {
            synchronized (lock) {
                try {
                    lock.wait();           // releases the lock and waits
                } catch (InterruptedException e) { }
            }
        });
        waiting.start();
        Thread.sleep(100);
        System.out.println("4. Inside wait()         : " + waiting.getState());
        synchronized (lock) {
            lock.notify();                 // wake it up
        }
        waiting.join();

        // 5. TIMED_WAITING: waits, but only for a limited time
        Thread timed = new Thread(() -> {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) { }
        });
        timed.start();
        Thread.sleep(100);
        System.out.println("5. Inside sleep(1000)    : " + timed.getState());

        // 6. TERMINATED: run() finished, the thread is dead
        timed.join();
        System.out.println("6. After finishing       : " + timed.getState());

    }
}