package phase2;

public class P9_DiningPhilosophersDemo {

    static final int COUNT = 5;

    // true = fix with lock ordering, false = deadlock version
    static final boolean USE_ORDERING = true;

    static final Object[] forks = new Object[COUNT];

    public static void main(String[] args) throws InterruptedException {

        for (int i = 0; i < COUNT; i++) {
            forks[i] = new Object();
        }

        Thread[] philosophers = new Thread[COUNT];

        for (int i = 0; i < COUNT; i++) {
            final int id = i;
            final int left = i;
            final int right = (i + 1) % COUNT;

            // with ordering, always take the lower numbered fork first
            final int first = USE_ORDERING ? Math.min(left, right) : left;
            final int second = USE_ORDERING ? Math.max(left, right) : right;

            philosophers[i] = new Thread(() -> {
                try {
                    for (int meal = 1; meal <= 3; meal++) {
                        synchronized (forks[first]) {
                            System.out.println("philosopher " + id + " took fork " + first);
                            // sleep so everyone grabs the first fork before asking for the second
                            Thread.sleep(100);
                            synchronized (forks[second]) {
                                System.out.println("philosopher " + id + " took fork " + second + " -> eating meal " + meal);
                                Thread.sleep(50);
                            }
                        }
                    }
                    System.out.println("philosopher " + id + " finished");
                } catch (InterruptedException e) {
                    return;
                }
            }, "philosopher-" + id);
        }

        for (Thread p : philosophers) {
            p.start();
        }
        for (Thread p : philosophers) {
            p.join();
        }
        System.out.println("main thread done");
    }
}