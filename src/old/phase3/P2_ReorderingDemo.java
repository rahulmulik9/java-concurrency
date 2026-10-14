package old.phase3;
/*
 * CONCEPT
 * - Java (compiler, JIT, CPU) can run your instructions in a different order
 *   than you wrote them, as long as a single thread cannot tell the difference.
 *
 * MAIN RULES
 * - t1: x = 1; r1 = y   and   t2: y = 1; r2 = x
 * - In written order, r1 = 0 and r2 = 0 together is impossible
 * - With reordering (a read moving before a write), both can be 0
 *
 * KEY POINT
 * - With only 100 runs you will almost never see it. Starting a thread is slow,
 *   so t1 usually finishes before t2 starts.
 *   To really see it, both threads must start at the same moment (CountDownLatch version).
 *
 * GOAL
 * - Run the pair of threads 100 times and count how often r1 = 0 and r2 = 0.
 * - Expected: usually 0 of 100 runs (reordering is real but very hard to catch here)
 * - Fix: make x and y volatile, then r1 = 0 and r2 = 0 can never happen
 */
// runs = 100 is too small to see reordering
// starting a thread is slow, so t1 usually finishes before t2 starts
// to really see it, both threads must start at the same moment (CountDownLatch version)

//But idea that Java (compiler, JIT, CPU) can run your instructions in a different order than you wrote them

public class P2_ReorderingDemo {

    // no volatile: writes and reads may be reordered
    static int x, y, r1, r2;

    public static void main(String[] args) throws InterruptedException {
        int runs = 100;
        int reordered = 0;

        for (int i = 0; i < runs; i++) {
            x = 0;
            y = 0;

            Thread t1 = new Thread(() -> {
                x = 1;
                r1 = y;
                if(r1==1) System.out.println("Here in t1 get the r1 value 1 which mean t2 thread set y value to 1 first");
            });
            Thread t2 = new Thread(() -> {
                y = 1;
                r2 = x;
            });

            t1.start();
            t2.start();
            t1.join();
            t2.join();

            // impossible in written order
            if (r1 == 0 && r2 == 0) {
                reordered++;
            }
        }

        System.out.println("RESULT: " + reordered + " of " + runs + " runs -> r1=0 and r2=0");
    }
}