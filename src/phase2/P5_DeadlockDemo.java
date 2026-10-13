package phase2;

/*
* Deadlock: two or more threads wait for each other forever, so none can continue.
It needs two locks and opposite order:
t1 takes A, then wants B
t2 takes B, then wants A
Both threads hold one lock and wait for the other. Nobody releases, so the program hangs.
Fix: all threads take locks in the same order (lock ordering).
Related problems (we only name them here):
Problem	Meaning
Deadlock	: threads blocked forever, waiting for each other
Livelock	: threads keep reacting to each other, but make no progress
Starvation	: one thread never gets a turn because others always win
* */


public class P5_DeadlockDemo {

    static final Object lockA = new Object();
    static final Object lockB = new Object();

    public static void main(String[] args) throws InterruptedException {

        Thread t1 = new Thread(() -> {
            synchronized (lockA) {
                System.out.println("t1 holds lockA");
                sleep();
                System.out.println("t1 wants lockB");
                synchronized (lockB) {
                    System.out.println("t1 holds both locks");
                }
            }
        }, "t1");

        Thread t2 = new Thread(() -> {
            // opposite order: B first, then A
            synchronized (lockB) {
                System.out.println("t2 holds lockB");
                sleep();
                System.out.println("t2 wants lockA");
                synchronized (lockA) {
                    System.out.println("t2 holds both locks");
                }
            }
        }, "t2");
        t1.start();
        t2.start();

        t1.join();
        t2.join();
        System.out.println("main thread done");
    }

    // sleep makes sure both threads hold their first lock before asking for the second
    static void sleep() {
        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            return;
        }
    }
}