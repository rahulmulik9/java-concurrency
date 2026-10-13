package phase2;

public class P3_WaitNotifyDemo {

    static final Object lock = new Object();

    // the condition the waiter is waiting for
    static boolean ready = false;

    public static void main(String[] args) throws InterruptedException {

        Thread waiter = new Thread(() -> {
            synchronized (lock) {
                // while, not if: protects against spurious wakeups
                while (!ready) {
                    try {
                        System.out.println("waiter is waiting");
                        lock.wait();  //release the block
                    } catch (InterruptedException e) {
                        return;
                    }
                }
                //now when setter notify, it checks condition again, failed condition !ready,
                System.out.println("waiter got signal, continuing");
            }
        }, "waiter");

        Thread setter = new Thread(() -> {
            try {
                // give the waiter time to start waiting first
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                return;
            }
            synchronized (lock) {
                ready = true;
                System.out.println("setter changes ready to true, notifying");
                lock.notifyAll();
            }
        }, "setter");

        waiter.start();
        setter.start();

        waiter.join();
        setter.join();
        System.out.println("main thread done");
    }
}