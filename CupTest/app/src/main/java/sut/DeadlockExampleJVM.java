package sut;

import java.util.concurrent.locks.ReentrantLock;

public class DeadlockExampleJVM {

    public static final int answer = 5;

    private final ReentrantLock lock1 = new ReentrantLock();
    private final ReentrantLock lock2 = new ReentrantLock();

    /**
     * Returns true if we *suspect* a deadlock (threads didn't finish in time).
     */
    public boolean runForDeadlock() {
        Thread t1 = new Thread(() -> {
            try {
                lock1.lockInterruptibly();
                try {
                    System.out.println("Thread 1: Holding lock 1...");
                    try {
                        Thread.sleep(20);
                    } catch (InterruptedException ignored) {
                        // If we get interrupted during sleep, just bail out
                        return;
                    }
                    System.out.println("Thread 1: Waiting for lock 2...");
                    lock2.lockInterruptibly();
                    try {
                        System.out.println("Thread 1: Acquired lock 2!");
                    } finally {
                        lock2.unlock();
                    }
                } finally {
                    if (lock1.isHeldByCurrentThread()) {
                        lock1.unlock();
                    }
                }
            } catch (InterruptedException ignored) {
                // Woken up while waiting for a lock → just exit
            }
        }, "t1");

        Thread t2 = new Thread(() -> {
            try {
                lock2.lockInterruptibly();
                try {
//                    System.out.println("Thread 2: Holding lock 2...");
                    try {
                        Thread.sleep(20);
                    } catch (InterruptedException ignored) {
                        return;
                    }
//                    System.out.println("Thread 2: Waiting for lock 1...");
                    lock1.lockInterruptibly();
                    try {
//                        System.out.println("Thread 2: Acquired lock 1!");
                    } finally {
                        lock1.unlock();
                    }
                } finally {
                    if (lock2.isHeldByCurrentThread()) {
                        lock2.unlock();
                    }
                }
            } catch (InterruptedException ignored) {
                // Woken while waiting for a lock
            }
        }, "t2");

        t1.start();
        t2.start();

        long timeoutMs = 50;

        try {
            t1.join(timeoutMs);
            t2.join(timeoutMs);
        } catch (InterruptedException e) {
            // If the *caller* interrupts us, you can decide what to do here
            Thread.currentThread().interrupt();
        }

        boolean suspectedDeadlock = t1.isAlive() && t2.isAlive();

        if (suspectedDeadlock) {
//            System.out.println("Deadlock suspected: both threads still alive after timeout.");
            t1.interrupt();
            t2.interrupt();
        }

        return suspectedDeadlock;
    }

    public static void main(String[] args) throws InterruptedException {
        DeadlockExampleJVM example = new DeadlockExampleJVM();
        boolean deadlocked = example.runForDeadlock();
        System.out.println("Deadlock detected? " + deadlocked);
    }
}