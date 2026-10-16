package phase4;

import java.util.ArrayList;
import java.util.ConcurrentModificationException;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/*
 * WHAT WE ARE GOING TO LEARN IN THIS STEP:
 *   - ConcurrentModificationException: why it happens
 *   - CopyOnWriteArrayList: iterate safely while another thread changes the list
 *   - Snapshot iterator: the loop sees the list as it was when the loop started
 *
 * PROBLEM:
 *   One thread loops over an ArrayList. Another thread adds an item.
 *   The loop fails with ConcurrentModificationException.
 *
 * SOLUTION:
 *   CopyOnWriteArrayList makes a new copy of its array on every write.
 *   A running loop keeps using the old copy, so it never fails.
 */
public class P20_CopyOnWriteDemo {

    public static void main(String[] args) throws InterruptedException {

        // ---------------------------------------------------------
        // Section 1: BROKEN - ArrayList
        // The reader loops slowly. Main adds "D" while the loop is running.
        // (sleep is only here to make the timing visible. Never coordinate threads with sleep in real code.)
        // ---------------------------------------------------------
        List<String> arrayList = new ArrayList<>(List.of("A", "B", "C"));

        Thread reader1 = new Thread(() -> {
            try {
                for (String s : arrayList) {
                    System.out.println("Section 1 reader sees: " + s);
                    Thread.sleep(100);
                }
            } catch (ConcurrentModificationException e) {
                System.out.println("Section 1 reader got ConcurrentModificationException");
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        });

        reader1.start();
        Thread.sleep(50);     // reader is inside the loop now
        arrayList.add("D");   // the list changes during the loop
        reader1.join();

        // ---------------------------------------------------------
        // Section 2: FIXED - CopyOnWriteArrayList
        // Same code. Only the list type is different.
        // ---------------------------------------------------------
        List<String> cowList = new CopyOnWriteArrayList<>(List.of("A", "B", "C"));

        Thread reader2 = new Thread(() -> {
            try {
                for (String s : cowList) {
                    System.out.println("Section 2 reader sees: " + s);
                    Thread.sleep(100);
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        });

        reader2.start();
        Thread.sleep(50);
        cowList.add("D");     // makes a NEW copy of the array. The reader keeps the old one.
        reader2.join();

        System.out.println("Section 2 list after loop: " + cowList);
    }
}