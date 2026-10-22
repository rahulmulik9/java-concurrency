package randomExample.accessingNumber;
/*
* Ways to make 3 threads increment one variable, taking turns

Here are the approaches, grouped by what they guarantee.

Group A: Mutual exclusion (threads wait, order not guaranteed)
Only one thread is inside the critical section at a time, but the scheduler decides who goes first.
synchronized block or method - built-in intrinsic lock
ReentrantLock - explicit lock, supports tryLock, timeout, fairness
ReentrantLock(true) (fair lock) - same as above, but longest-waiting thread goes next
Semaphore(1) - one permit means one thread at a time
StampedLock (write lock) - works, but overkill for this problem
ReadWriteLock (write lock) - also works, but unnecessary here

Group B: Strict order (T1, then T2, then T3)
Threads take turns in a guaranteed sequence.
join() chaining - start a thread, join it, start the next
wait / notifyAll with a turn variable - each thread waits for its turn number
ReentrantLock + Condition - same turn-taking idea, with separate wait queues
Semaphore chain - T1 releases T2’s semaphore, T2 releases T3’s
CountDownLatch chain - T2 waits on latch 1, T3 waits on latch 2
CyclicBarrier - threads meet at a point (better for rounds than strict order)
Phaser - phase-based turn-taking

Group C: Task-based (no manual locks)
Single-thread ExecutorService - tasks run one by one in submission order
CompletableFuture chaining - thenRun / thenCompose makes tasks sequential
BlockingQueue handoff - a token passed between threads

Group D: Safe count, but NO waiting
AtomicInteger - correct result, threads run in parallel
LongAdder - same idea, built for heavy contention
ConcurrentHashMap.merge / compute - atomic update on a map entry*/
public class ProblemStatement {

}
