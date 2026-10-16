package phase4;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/*
 * WHAT WE ARE GOING TO LEARN IN THIS STEP:
 *   - Why HashMap is not safe with many threads
 *   - Collections.synchronizedMap vs ConcurrentHashMap
 *   - Why get() then put() is still NOT atomic, even on ConcurrentHashMap
 *   - Atomic methods: merge, putIfAbsent, computeIfAbsent
 *
 * PROBLEM:
 *   HashMap is not thread-safe. If many threads call put() at the same time,
 *   entries are lost, and the map can even break inside.
 *
 * SOLUTION:
 *   ConcurrentHashMap is safe for many threads, and it does not lock the whole map.
 *   For "read, change, write" use one atomic method like merge().
 */
public class P19_ConcurrentHashMapDemo {

    public static void main(String[] args) throws InterruptedException {

        // 4 threads, each puts 100,000 DIFFERENT keys. Expected size: 400,000

        // ---------------------------------------------------------
        // Section 1: BROKEN - plain HashMap
        // ---------------------------------------------------------
        Map<Integer, Integer> hashMap = new HashMap<>();
        long start = System.currentTimeMillis();
        Thread[] t1 = new Thread[4];
        for (int i = 0; i < 4; i++) {
            final int id = i;
            t1[i] = new Thread(() -> {
                for (int j = 0; j < 100_000; j++) {
                    hashMap.put(id * 100_000 + j, j); // keys never repeat
                }
            });
            t1[i].start();
        }
        for (Thread t : t1) {
            t.join();
        }
        System.out.println("Section 1 HashMap           : size = " + hashMap.size()
                + " (expected 400000) in " + (System.currentTimeMillis() - start) + " ms");

        // ---------------------------------------------------------
        // Section 2: FIX 1 - synchronizedMap (correct, ONE lock for the whole map)
        // ---------------------------------------------------------
        Map<Integer, Integer> syncMap = Collections.synchronizedMap(new HashMap<>());
        start = System.currentTimeMillis();
        Thread[] t2 = new Thread[4];
        for (int i = 0; i < 4; i++) {
            final int id = i;
            t2[i] = new Thread(() -> {
                for (int j = 0; j < 100_000; j++) {
                    syncMap.put(id * 100_000 + j, j);
                }
            });
            t2[i].start();
        }
        for (Thread t : t2) {
            t.join();
        }
        System.out.println("Section 2 synchronizedMap   : size = " + syncMap.size()
                + " (expected 400000) in " + (System.currentTimeMillis() - start) + " ms");

        // ---------------------------------------------------------
        // Section 3: FIX 2 - ConcurrentHashMap (correct, locks only a small part)
        // ---------------------------------------------------------
        Map<Integer, Integer> chm = new ConcurrentHashMap<>();
        start = System.currentTimeMillis();
        Thread[] t3 = new Thread[4];
        for (int i = 0; i < 4; i++) {
            final int id = i;
            t3[i] = new Thread(() -> {
                for (int j = 0; j < 100_000; j++) {
                    chm.put(id * 100_000 + j, j);
                }
            });
            t3[i].start();
        }
        for (Thread t : t3) {
            t.join();
        }
        System.out.println("Section 3 ConcurrentHashMap : size = " + chm.size()
                + " (expected 400000) in " + (System.currentTimeMillis() - start) + " ms");


        // ---------------------------------------------------------
        // Section 4: Other atomic methods (single thread, to see the API)
        // ---------------------------------------------------------
        Map<String, String> m = new ConcurrentHashMap<>();

        m.putIfAbsent("a", "first");   // puts, because "a" is missing
        m.putIfAbsent("a", "second");  // does nothing, "a" already exists
        System.out.println("Section 6 putIfAbsent       : " + m.get("a"));

        m.computeIfAbsent("b", key -> "made-for-" + key); // runs the function only if missing
        System.out.println("Section 6 computeIfAbsent   : " + m.get("b"));
    }
}