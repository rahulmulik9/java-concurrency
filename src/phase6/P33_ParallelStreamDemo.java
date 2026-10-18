package phase6;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.LongStream;

/*
 * WHAT WE ARE GOING TO LEARN IN THIS STEP:
 *   - parallel(): run a stream on many threads. Just one word.
 *   - It uses ForkJoinPool.commonPool() (step 32) behind the scenes.
 *   - When it HELPS: big data + heavy CPU work per item
 *   - When it HURTS: small data, or items that change a shared variable
 *   - forEach vs forEachOrdered: order is NOT kept with parallel forEach
 *
 * REAL-LIFE EXAMPLE: COUNTING VOTES
 *   - 1 clerk counts all the votes.               = normal stream
 *   - 4 clerks each count a part, then add up.    = parallel stream
 *   - 100 votes only: calling 4 clerks is slower. = small data hurts
 *   - 4 clerks write on the SAME notebook page.   = shared variable, a mess
 *
 * PROBLEM:
 *   A normal stream uses only one thread. For a big, heavy job, other CPU cores do nothing.
 *   But parallel() is not always better. It can be slower, or even give WRONG results.
 *
 * SOLUTION:
 *   Use parallel() only for big, heavy, independent work.
 *   Never change a shared variable inside a parallel stream. Use collect() or sum().
 */
public class P33_ParallelStreamDemo {

    static boolean isPrime(long n) {
        if (n < 2) {
            return false;
        }
        for (long i = 2; i * i <= n; i++) {
            if (n % i == 0) {
                return false;
            }
        }
        return true;
    }

    public static void main(String[] args) {

        // ---------------------------------------------------------
        // Section 1: HEAVY work, big data. Normal stream vs parallel stream.
        // Count the prime numbers from 1 to 3,000,000.
        // ---------------------------------------------------------
        long start = System.currentTimeMillis();
        long count1 = LongStream.rangeClosed(1, 3_000_000)
                .filter(P33_ParallelStreamDemo::isPrime)
                .count();
        System.out.println("Section 1 normal stream   : primes = " + count1
                + " in " + (System.currentTimeMillis() - start) + " ms");

        start = System.currentTimeMillis();
        long count2 = LongStream.rangeClosed(1, 3_000_000)
                .parallel()                                   // the only change
                .filter(P33_ParallelStreamDemo::isPrime)
                .count();
        System.out.println("Section 1 parallel stream : primes = " + count2
                + " in " + (System.currentTimeMillis() - start) + " ms");

        // ---------------------------------------------------------
        // Section 2: BROKEN - parallel stream writes to a shared ArrayList
        // 4 clerks write on the same notebook page.
        // ---------------------------------------------------------
        List<Integer> shared = new ArrayList<>();
        try {
            IntStream.range(0, 10_000)
                    .parallel()
                    .forEach(i -> shared.add(i));             // ArrayList is not thread-safe
            System.out.println("Section 2 shared list     : size = " + shared.size() + " (expected 10000)");
        } catch (Exception e) {
            System.out.println("Section 2 shared list     : " + e.getClass().getSimpleName());
        }

        // FIXED: let the stream collect the result. Each thread fills its own part,
        // and the stream joins the parts at the end.
        List<Integer> collected = IntStream.range(0, 10_000)
                .parallel()
                .boxed()
                .collect(Collectors.toList());
        System.out.println("Section 2 collect()       : size = " + collected.size() + " (expected 10000)");

        // ---------------------------------------------------------
        // Section 3: SMALL data. Parallel is slower.
        // Add 100 numbers, 10,000 times.
        // ---------------------------------------------------------
        int[] small = IntStream.rangeClosed(1, 100).toArray();
        long total = 0;                                       // use the result, so the work is not skipped

        start = System.currentTimeMillis();
        for (int r = 0; r < 10_000; r++) {
            total += Arrays.stream(small).sum();
        }
        System.out.println("Section 3 normal, 100 items   : " + (System.currentTimeMillis() - start) + " ms");

        start = System.currentTimeMillis();
        for (int r = 0; r < 10_000; r++) {
            total += Arrays.stream(small).parallel().sum();
        }
        System.out.println("Section 3 parallel, 100 items : " + (System.currentTimeMillis() - start)
                + " ms (total = " + total + ")");

        // ---------------------------------------------------------
        // Section 4: ORDER. forEach does not keep the order. forEachOrdered does.
        // ---------------------------------------------------------
        System.out.print("Section 4 parallel forEach        : ");
        IntStream.rangeClosed(1, 8).parallel().forEach(i -> System.out.print(i + " "));
        System.out.println();

        System.out.print("Section 4 parallel forEachOrdered : ");
        IntStream.rangeClosed(1, 8).parallel().forEachOrdered(i -> System.out.print(i + " "));
        System.out.println();
    }
}