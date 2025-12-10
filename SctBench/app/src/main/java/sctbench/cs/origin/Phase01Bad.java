package sctbench.cs.origin;

import java.util.concurrent.atomic.AtomicBoolean;

// Translated from: https://github.com/mc-imperial/sctbench/blob/d59ab26ddaedcd575ffb6a1f5e9711f7d6d2d9f2/benchmarks/concurrent-software-benchmarks/phase01_bad.c

import java.util.concurrent.locks.AbstractQueuedSynchronizer;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

public class Phase01Bad {

    static ReentrantLock x = new ReentrantLock();
    static ReentrantLock y = new ReentrantLock();
    static int lockStatus = 0;
    static AtomicBoolean bug;

    static void thread1() throws InterruptedException {
        if (lockStatus == 1) {
            System.out.println("Deadlock detected");
            bug.set(true);
            throw new RuntimeException();
        }
        x.lockInterruptibly(); // BAD: deadlock
        // x.lock(); // BAD: deadlock
        x.unlock();
        if (lockStatus == 1) {
            System.out.println("Deadlock detected");
            bug.set(true);
            throw new RuntimeException();
        }
        x.lockInterruptibly();
//        x.lock();
        lockStatus = 1;
        // x.unlock();

        y.lockInterruptibly();
//        y.lock();
        y.unlock();
        y.lockInterruptibly();
//        y.lock();
        y.unlock();
    }

    public static boolean run() {
        x = new ReentrantLock();
        y = new ReentrantLock();
        lockStatus = 0;
        bug = new AtomicBoolean(false);
        Thread t1;
        Thread t2;

        t1 = new Thread(() -> {
            try {
                thread1();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        });
        t2 = new Thread(() -> {
            try {
                thread1();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        });

        t1.start();
        t2.start();

        try {
            t1.join();
            t2.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        return bug.get();

    }

    public static boolean run(long timeoutMs) {
        x = new ReentrantLock();
        y = new ReentrantLock();
        lockStatus = 0;
        bug = new AtomicBoolean(false);


        Thread t1 = new Thread(() -> {
            try {
                thread1();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        });
        Thread t2 = new Thread(() -> {
            try {
                thread1();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        });

        t1.start();
        t2.start();

        try {
            t1.join(timeoutMs);
            t2.join(timeoutMs);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        // If any thread is still alive after the timeout we "suspect" that we have a deadlock
        if (t1.isAlive() || t2.isAlive()) {
            System.out.println("Deadlock suspected due to timeout");
            bug.set(true);

            // Try to clean up so we don't leak threads during repeated tests
            // using reentrantlock lockInterruptibility
            t1.interrupt();
            t2.interrupt();
        }

        return bug.get();
    }

    public static void main(String[] args) {
        run();
    }
}