# Phase 2 Notes: Synchronization and Thread Safety

Package: `phase2`

## Class list

| Class | Topic |
| --- | --- |
| P1_RaceConditionDemo | race condition, lost updates |
| P2_SynchronizedFixDemo | `synchronized`, intrinsic lock |
| P3_WaitNotifyDemo | `wait`, `notify`, `notifyAll`, `while` loop |
| P4_VolatileDemo | `volatile`, visibility vs atomicity |
| P5_DeadlockDemo | deadlock, lock ordering |
| P6_ImmutabilityDemo | immutability, new object plus new reference |
| P7_ThreadConfinementDemo | thread confinement, local variables |
| P8_ProducerConsumerDemo | producer-consumer with `wait/notify` |
| P9_DiningPhilosophersDemo | circular wait, lock ordering fix |

---

## P1 Race condition

- `counter++` is 3 steps: read, add 1, write. It is **not atomic**.
- Two threads can read the same old value, so one update is lost.
- Result is different on every run, depends on the scheduler.
- `join()` is needed so main reads the result only after both threads finish.
- A program that is right 99 times out of 100 is **not** thread-safe.

```
counter = 5
t1 read 5, t2 read 5, t1 write 6, t2 write 6 -> one update lost
```

## P2 synchronized

- Every object has one **intrinsic lock** (monitor).
- Only one thread can hold a given lock. Others are BLOCKED.
- `synchronized` gives **atomicity** and **visibility**.
- Both threads must use the **same** lock object, or there is no protection.

| Form | Lock used |
| --- | --- |
| instance method | `this` |
| static method | the `Class` object |
| `synchronized (obj)` | `obj` |

- Result is guaranteed correct, but slower because threads wait for each other.

## P3 wait / notify

- `wait()` **releases the lock** and sleeps in the wait set (state WAITING).
- `Thread.sleep()` does **not** release any lock.
- `notify()` wakes one thread, `notifyAll()` wakes all.
- After waking, the thread is BLOCKED until it gets the lock back.
- `ready = true` changes the **data**. `notifyAll()` **wakes** the waiter. Both are needed.
- Without `notifyAll()` the waiter sleeps forever.
- Always check the condition in a `while` loop (spurious wakeups, and condition may be false again).
- `wait/notify` must be inside `synchronized` on the same object, or `IllegalMonitorStateException`.
- After wake-up, `ready` is true, so `!ready` is false and the loop exits.

## P4 volatile

- `volatile` fixes **visibility** only. Readers always see the latest value.
- `volatile` does **not** make `counter++` atomic.
- Good for a simple stop flag with one writer.
- Without `volatile`, a busy loop may never see the change and hang (likely, not guaranteed).

| | volatile | synchronized | Atomic classes |
| --- | --- | --- | --- |
| Visibility | yes | yes | yes |
| `x++` safe | no | yes | yes |
| Blocks threads | no | yes | no (CAS) |

- Rule: one writer, many readers -> `volatile`. Many writers using the old value -> `synchronized` or atomic.

## P5 Deadlock

- Deadlock needs two locks held in opposite order (circular wait).
- t1 holds A and wants B. t2 holds B and wants A. Both BLOCKED forever.
- A thread leaves its outer block only after the inner one finishes, so it cannot release.
- The `sleep()` only makes the timing more certain. Prints can also cause it.
- Deadlock does not happen on every run. Code is wrong every time, but it hangs only sometimes.
- **Fix:** all threads take locks in the **same order**. Guaranteed no deadlock.
- One lock only -> no deadlock.
- Daemon thread plus `join(3000)` only **hides** the deadlock, it does not fix it.
- Find it with `jstack` or the IntelliJ thread dump (look for `Found one Java-level deadlock`).

| Problem | Meaning |
| --- | --- |
| Deadlock | blocked forever, waiting for each other |
| Livelock | busy reacting to each other, no progress |
| Starvation | one thread never gets a turn |

## P6 Immutability

- Immutable object never changes after creation. Always thread-safe, no lock needed.
- Rules: `final` class, `final` fields, set only in the constructor, no setters.
- To "change" it, create a **new object** and replace the reference.
- The old object is never modified. A reader holding it keeps a complete, consistent object.
- The reference is shared, so make it `volatile`.
- Mutable point is updated in two steps (x then y). A reader can look **in between** and see x new, y old.
- Read the reference **once** into a local variable, then use only that copy.
- `final int[] data` is **not** immutable. The array contents can still change.

## P7 Thread confinement

- Data used by only **one thread** needs no lock.
- Immutable data is shared but cannot change. Confined data is **not shared**.
- Stack confinement: a local variable lives on the thread's own stack. Two threads running the same lambda each get their own copy.
- Making it `static` or an instance field of a shared object shares it again.
- If a local variable holds a reference and you pass it to another thread, the object is shared again.
- Combining the results into a shared total still needs `synchronized`.

| Variable | Where | Shared? |
| --- | --- | --- |
| local variable | thread's stack | no |
| `static` field | shared memory | yes |
| field of shared object | shared memory | yes |

## P8 Producer-consumer

- Producer waits when the buffer is **full**. Consumer waits when it is **empty**.
- One lock, two kinds of waiters, so use `notifyAll()` (not `notify()`).
- Check the condition in a `while` loop, using the real condition (`size() == CAPACITY`, `isEmpty()`).
- Call `notifyAll()` after every change to the buffer.
- Sleep **outside** the `synchronized` block, so the other thread can work.
- Guaranteed: items come out in order, buffer never above capacity.
- Only likely: the exact order of print lines.

## P9 Dining Philosophers

- 5 philosophers, 5 forks, each needs two forks.
- Everyone takes the left fork first -> everyone holds one, waits for the other -> deadlock (same as P5, but in a circle).
- **Fix:** number the forks and always take the **lower number first**.
- Philosopher 4 (left = 4, right = 0) takes fork 0 first. This breaks the circle.
- Lock ordering does not stop waiting. It only stops waiting **forever**.

---

## Phase 2 key rules

1. Shared mutable data needs one of: `synchronized`, atomic, immutability, or confinement.
2. `volatile` = visibility only. `synchronized` = visibility + atomicity.
3. `wait()` in a `while` loop, always.
4. Take multiple locks in the same order.
5. Prefer no sharing (confinement, immutability) over locking.
