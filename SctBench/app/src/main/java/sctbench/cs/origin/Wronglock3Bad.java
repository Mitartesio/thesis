package sctbench.cs.origin;

// Translated from: https://github.com/mc-imperial/sctbench/blob/d59ab26ddaedcd575ffb6a1f5e9711f7d6d2d9f2/benchmarks/concurrent-software-benchmarks/wronglock_3_bad.c

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class Wronglock3Bad {
    private static int iNum1 = 1;
    private static int iNum2 = 3;
    private static volatile int dataValue = 0;
    private static Lock dataLock;
    private static Lock thisLock;

    private static AtomicBoolean bug = new AtomicBoolean(false);

    public static void main(String[] args) {
        runOnce();
    }

    public static void setiNum1(int iNum1) {
        Wronglock3Bad.iNum1 = iNum1;
    }

    public static void setiNum2(int iNum2) {
        Wronglock3Bad.iNum2 = iNum2;
    }

    public static void setBoolean(boolean b) {
        bug.set(b);
    }

    public static boolean runOnce() {
        int i;

        dataValue = 0;
        dataLock = new ReentrantLock();
        thisLock = new ReentrantLock();

        Thread[] num1Pool = new Thread[iNum1];
        Thread[] num2Pool = new Thread[iNum2];

        for (i = 0; i < iNum1; i++) {
            final int id = i;
            num1Pool[i] = new Thread(() -> funcA(id));
            num1Pool[i].start();
        }

        for (i = 0; i < iNum2; i++) {
            final int id = i;
            num2Pool[i] = new Thread(() -> funcB(id));
            num2Pool[i].start();
        }

        for (i = 0; i < iNum1; i++) {
            try {
                num1Pool[i].join();
            } catch (InterruptedException e) {
                System.err.println("Thread join interrupted: " + e);
                throw new RuntimeException();
            }
        }

        for (i = 0; i < iNum2; i++) {
            try {
                num2Pool[i].join();
            } catch (InterruptedException e) {
                System.err.println("Thread join interrupted: " + e);
                throw new RuntimeException();
            }
        }

        return bug.get();

    }

    private static void funcA(int id) {
        lock(dataLock);
        int x = dataValue;
        dataValue++;
        if (dataValue != (x + 1)) {
            System.err.println("Bug Found!");
            bug.set(true);
            assert false : "Bug Found!";
        }
        unlock(dataLock);
    }

    private static void funcB(int id) {
        lock(thisLock);
        dataValue++;
        unlock(thisLock);
    }

    private static void lock(Lock lock) {
        lock.lock();
    }

    private static void unlock(Lock lock) {
        lock.unlock();
    }
}
