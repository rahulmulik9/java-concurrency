package phase2;

public class P7_ThreadConfinementDemo {

    // shared by both threads, no protection
    static int sharedCounter = 0;

    // sum of the two local counts, each written by one thread after its loop
    static int confinedTotal = 0;

    public static void main(String[] args) throws InterruptedException {

        Runnable sharedTask = () -> {
            for (int i = 0; i < 100_000; i++) {
                // both threads touch the same variable
                sharedCounter++;
            }
        };

        Runnable confinedTask = () -> {
            // local variable, lives on this thread's own stack
            int localCount = 0;
            for (int i = 0; i < 100_000; i++) {
                // no other thread can see localCount
                localCount++;
            }
            // one short lock to combine results, the counting itself needed no lock
            synchronized (P7_ThreadConfinementDemo.class) {
                confinedTotal += localCount;
           }
        };

        System.out.println("EXAMPLE 1 shared counter");
        Thread a1 = new Thread(sharedTask, "worker-1");
        Thread a2 = new Thread(sharedTask, "worker-2");
        a1.start();
        a2.start();
        a1.join();
        a2.join();
        System.out.println("RESULT " + sharedCounter + " -> lost updates");

        System.out.println("EXAMPLE 2 confined counter");
        Thread b1 = new Thread(confinedTask, "worker-3");
        Thread b2 = new Thread(confinedTask, "worker-4");
        b1.start();
        b2.start();
        b1.join();
        b2.join();
        System.out.println("RESULT " + confinedTotal + " -> always correct");
    }
}