package phase2;

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