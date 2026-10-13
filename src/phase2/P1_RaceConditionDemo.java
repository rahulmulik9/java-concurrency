package phase2;

public class P1_RaceConditionDemo {

    // shared by both threads, no protection
    static int counter = 0;

    public static void main(String[] args) throws InterruptedException {

        Runnable task = () -> {
            for (int i = 0; i < 100_000; i++) {
                // read + add + write: 3 steps, threads can interleave
                counter++;
            }
        };

        Thread t1 = new Thread(task, "worker-1");
        Thread t2 = new Thread(task, "worker-2");

        System.out.println("main: starting workers");
        t1.start();
        t2.start();

        // wait so we read the counter only after both finish
        t1.join();
        t2.join();

        System.out.println("EXPECTED: 200000");
        System.out.println("RESULT: " + counter);
        System.out.println("LOST: " + (200_000 - counter) + " -> updates overwritten");
    }
}