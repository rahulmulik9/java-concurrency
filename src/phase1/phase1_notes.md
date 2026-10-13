# Phase 1 Notes: Thread Basics

## Class list

| Class | Topic |
| --- | --- |
| P1_StartVsRunDemo | `start()` vs `run()` |
| P2_BreakIt | `start()` twice, `join()` |
| P3_LifecycleDemo | Thread states over time |
| P4_BlockedWaitingDemo | `BLOCKED`, `WAITING`, `TIMED_WAITING` |
| P5_DaemonDemo | Daemon vs user threads |
| P6_PriorityDemo | Priorities are only hints |
| P7_InterruptSleepDemo | Interrupt a sleeping thread |
| P8_InterruptBusyDemo | Interrupt a busy thread |
| P9_StopCleanlyWorker | Worker that stops cleanly |
| P10_ThreadLocalDemo | `ThreadLocal` |
| P11_UncaughtExceptionDemo | Uncaught exception handlers |
| P12_JoinOrderDemo | `join()` in different loops vs same loop |

---

## P1: start() vs run()

| Call | New thread? | Who runs `run()`? | Caller waits? |
| --- | --- | --- | --- |
| `t.run()` | No | Current thread | Yes (normal method call) |
| `t.start()` | Yes | The new thread | No |
| `t.join()` | No | n/a | Yes, until `t` finishes |

- `start()` can be called only once (`IllegalThreadStateException` on the second call).
- Prefer `Runnable` / `Callable` over extending `Thread`.

## P2: join()

- `w.join()` = the **calling thread waits** until `w` finishes. Only the caller waits.
- Without `join()`, "main done" can print before the workers finish.
- Worker output order changes between runs (scheduler decides).

## P3: Lifecycle states

| State | Meaning |
| --- | --- |
| `NEW` | Created, not started |
| `RUNNABLE` | Running or ready to run |
| `BLOCKED` | Waiting for a `synchronized` lock held by another thread |
| `WAITING` | Waiting, no time limit (`join()`, `wait()`) |
| `TIMED_WAITING` | Waiting with a time limit (`sleep(ms)`, `join(ms)`) |
| `TERMINATED` | `run()` finished |

- `getState()` is a **snapshot**. What you see depends on when you look.
- `Thread.sleep()` sleeps **the thread that runs that line**. `t.sleep()` in main sleeps main (static method).
- One thread cannot force another thread to sleep. Control is cooperative.
- Never use `getState()` to coordinate threads. Use `join()`.

## P4: BLOCKED vs WAITING

- `BLOCKED`: stuck at the door of a `synchronized` block, another thread owns the lock.
- `WAITING`: the thread chose to wait (`join()`, `wait()`).
- To get a reliable `BLOCKED`, main must hold the lock **before** starting the other thread.
- If the order depends on timing, it is a race.

## P5: Daemon threads

- JVM exits when only daemon threads are left, and kills them immediately.
- The kill comes from **main ending**, not from main waking up from sleep.
- `setDaemon(true)` must be called **before** `start()`.
- Daemon threads are bad for work that must finish (file writes, saving data).

## P6: Priorities

- Range 1 to 10, default 5. Only a hint to the OS.
- With more cores than busy threads, priority makes almost no difference.
- Never build correctness on priorities.

## P7 to P9: Interrupt

- `interrupt()` is a **request**. It never stops a thread by force.

| Thread was... | `interrupt()` does | Flag afterwards |
| --- | --- | --- |
| Sleeping / waiting | Throws `InterruptedException` | `false` (cleared) |
| Running / busy | Sets the flag only | `true` |

- `isInterrupted()` reads the flag. `Thread.interrupted()` reads and **clears** it.
- A clean worker handles both cases:
  - loop condition `!Thread.currentThread().isInterrupted()` for the busy case
  - `catch (InterruptedException e)` for the sleeping case
- In the catch block, **restore the flag** with `Thread.currentThread().interrupt()`.
- Never swallow the exception with an empty catch.
- Put cleanup in `finally`.

## P10: ThreadLocal

- `ThreadLocal<T>` = one object, but a **separate value per thread**.
- `set()` and `get()` only touch the current thread's value.
- `ThreadLocal.withInitial(() -> value)` gives each thread a starting value (otherwise `null`).
- Holds object types only (`Integer`, not `int`).
- Needs no `synchronized`. Call `remove()` when threads are reused (pools).

| Variable | Shared between threads? |
| --- | --- |
| Local variable (in a method) | No |
| Instance field | Yes, if same object |
| Static field | Yes |
| `ThreadLocal` | No, one value per thread |

## P11: Uncaught exceptions

- An exception can only be caught inside the thread that threw it.
- If uncaught, it kills **only that thread**. main and other threads keep running.
- `try/catch` around `start()` does NOT catch it.
- Handlers (run on the dying thread, thread still ends as `TERMINATED`):
  - `t.setUncaughtExceptionHandler(...)` for one thread
  - `Thread.setDefaultUncaughtExceptionHandler(...)` for all threads without their own
- If both exist, only the thread's own handler runs.
- Order of defence: `try/catch` in `run()` -> per-thread handler -> default handler -> JVM prints stack trace.

## P12: join() in loops

```java
// Different loops: all threads run in parallel (about 1000 ms)
for (Thread t : threads) t.start();
for (Thread t : threads) t.join();

// Same loop: one after another (about 4000 ms)
for (Thread t : threads) {
    t.start();
    t.join();
}
```

**Note:** each time main waits on `join()`, no other thread is waiting.
In the second loop it looks like thread 2 is waiting for thread 1, but it is actually the **main thread** that is waiting.
Once thread 1 is done, main's join returns and main goes to the next thread in the loop.

## Key rules from Phase 1

- Start all threads first, then join all.
- Each thread writing its own variable needs no lock. Many threads writing one shared variable breaks (Phase 2).
- Thread control is cooperative: you request, the thread decides.
- Anything that depends on timing is a race.

## Class naming convention

Each phase package restarts numbering at `P1`, in exercise order (`phase1/P1_...`, `phase2/P1_...`).

## Next: Phase 2

Synchronization and thread safety, starting with `phase2/P1_RaceConditionDemo`.
