package sctbench.cs.origin;

import java.util.Timer;
import java.util.concurrent.atomic.AtomicBoolean;

// Translated from: https://github.com/mc-imperial/sctbench/blob/d59ab26ddaedcd575ffb6a1f5e9711f7d6d2d9f2/benchmarks/concurrent-software-benchmarks/deadlock01_bad.c

import java.util.concurrent.locks.ReentrantLock;

public class Deadlock01Bad {
    static ReentrantLock a = new ReentrantLock();
    static ReentrantLock b = new ReentrantLock();
    static int counter = 1;
    static AtomicBoolean bug = new AtomicBoolean(false);

    static void thread1() {
        a.lock();
        if (b.isLocked()) {
        a.unlock();
        throw new RuntimeException("deadlock");
        }
        b.lock(); /* BAD: deadlock */
        try {
            counter++;
        } finally {
            b.unlock();
            a.unlock();
        }
    }

    static void thread2() {
        b.lock();
        if (a.isLocked()) {
        b.unlock();
        throw new RuntimeException("deadlock");
        }
        a.lock(); /* BAD: deadlock */
        try {
            counter--;
        } finally {
            a.unlock();
            b.unlock();
        }
    }

    public static boolean run() {
        a = new ReentrantLock();
        b = new ReentrantLock();
        counter = 1;

        Thread t1 = new Thread(() -> thread1());
        Thread t2 = new Thread(() -> thread2());

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

    public static void main(String[] args) throws InterruptedException {
        run();
    }
}