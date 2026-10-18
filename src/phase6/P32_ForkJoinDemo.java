package phase6;

import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.RecursiveTask;

/*
 * WHAT WE ARE GOING TO LEARN IN THIS STEP:
 *   - Fork/Join: divide and conquer. Split a big job into small jobs.
 *   - RecursiveTask<V>: a task that returns a result. We write compute().
 *   - fork(): start a sub-task in the background
 *   - join(): wait for a sub-task and get its result
 *   - Threshold: the pile size where we stop splitting and just count
 *   - ForkJoinPool: the pool that runs these tasks (work stealing)
 *
 * REAL-LIFE EXAMPLE: COUNTING A PILE OF PAPERS
 *   - One big pile.                                  = the array
 *   - Split the pile in two halves.                  = fork()
 *   - A pile small enough to count by hand.          = threshold
 *   - Helpers give their numbers back, we add them.  = join()
 *
 * PROBLEM:
 *   One thread adds 20 million numbers alone. Other CPU cores do nothing.
 *   A normal pool (step 22) does not help, because we have ONE big job, not many jobs.
 *
 * SOLUTION:
 *   Split the one big job into many small jobs. ForkJoinPool runs them on all cores.
 */
public class P32_ForkJoinDemo {

    // One task = "add the numbers from index 'from' to index 'to'"
    static class SumTask extends RecursiveTask<Long> {
        final int[] data;
        final int from;
        final int to;
        final int threshold;

        SumTask(int[] data, int from, int to, int threshold) {
            this.data = data;
            this.from = from;
            this.to = to;
            this.threshold = threshold;
        }

        @Override
        protected Long compute() {
            if (to - from <= threshold) {
                // small pile: count by hand
                long sum = 0;
                for (int i = from; i < to; i++) {
                    sum += data[i];
                }
                return sum;
            }
            // big pile: split in two halves
            int mid = (from + to) / 2;
            SumTask left = new SumTask(data, from, mid, threshold);
            SumTask right = new SumTask(data, mid, to, threshold);

            left.fork();                         // left half runs in the background
            long rightResult = right.compute();  // this thread does the right half itself
            long leftResult = left.join();       // wait for the left half
            return leftResult + rightResult;
        }
    }

    public static void main(String[] args) {

        int[] data = new int[20_000_000];
        for (int i = 0; i < data.length; i++) {
            data[i] = 1;                         // every number is 1, so the sum = 20,000,000
        }

        // ---------------------------------------------------------
        // Section 1: BROKEN - one thread adds everything
        // ---------------------------------------------------------
        long start = System.currentTimeMillis();
        long sum1 = 0;
        for (int i = 0; i < data.length; i++) {
            sum1 += data[i];
        }
        System.out.println("Section 1 one thread   : sum = " + sum1
                + " in " + (System.currentTimeMillis() - start) + " ms");

        // ---------------------------------------------------------
        // Section 2: FIXED - Fork/Join, threshold = 1,000,000
        // ---------------------------------------------------------
        ForkJoinPool pool = new ForkJoinPool(4);          // 4 worker threads
        try {
            start = System.currentTimeMillis();
            long sum2 = pool.invoke(new SumTask(data, 0, data.length, 1_000_000));
            System.out.println("Section 2 fork/join    : sum = " + sum2
                    + " in " + (System.currentTimeMillis() - start) + " ms");

            // ---------------------------------------------------------
            // Section 3: threshold is important
            // Same job (1,000,000 numbers). Tiny threshold = too many tiny tasks.
            // ---------------------------------------------------------
            start = System.currentTimeMillis();
            pool.invoke(new SumTask(data, 0, 1_000_000, 10));
            System.out.println("Section 3 threshold 10      : " + (System.currentTimeMillis() - start) + " ms");

            start = System.currentTimeMillis();
            pool.invoke(new SumTask(data, 0, 1_000_000, 100_000));
            System.out.println("Section 3 threshold 100,000 : " + (System.currentTimeMillis() - start) + " ms");
        } finally {
            pool.shutdown();
        }
    }
}