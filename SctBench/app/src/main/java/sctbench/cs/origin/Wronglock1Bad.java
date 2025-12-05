package sctbench.cs.origin;

// Translated from: https://github.com/mc-imperial/sctbench/blob/d59ab26ddaedcd575ffb6a1f5e9711f7d6d2d9f2/benchmarks/concurrent-software-benchmarks/wronglock_bad.c

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.ReentrantLock;

public class Wronglock1Bad {
    private static final String USAGE = "./wronglock <param1> <param2>\n";

    private static int iNum1 = 1;
    private static int iNum2 = 1;
    private static volatile int dataValue = 0;
    private static ReentrantLock dataLock;
    private static ReentrantLock thisLock;

    private static AtomicBoolean bug = new AtomicBoolean(false);

    private static void lock(ReentrantLock lock) {
        lock.lock();
    }

    private static void unlock(ReentrantLock lock) {
        lock.unlock();
    }


    public static void setiNum1(int iNum1) {
        Wronglock1Bad.iNum1 = iNum1;
    }

    public static void setiNum2(int iNum2) {
        Wronglock1Bad.iNum2 = iNum2;
    }

    public static void setBoolean(boolean b) {
        bug.set(b);
    }

    private static void funcA() {
        lock(dataLock);
        int x = dataValue;
        dataValue++;
        if (dataValue != (x + 1)) {
            System.err.println("Bug Found!");
            bug.set(true);
            assert false : "Bug Found"; // BAD
        }
        unlock(dataLock);
    }

    private static void funcB() {
        lock(thisLock);
        dataValue++;
        unlock(thisLock);
    }

    public static void main(String[] args) {
        runOnce();
    }

    public static boolean runOnce() {
        dataValue = 0;

        dataLock = new ReentrantLock();
        thisLock = new ReentrantLock();

        Thread[] num1Pool = new Thread[iNum1];
        Thread[] num2Pool = new Thread[iNum2];

        for (int i = 0; i < iNum1; i++) {
            num1Pool[i] = new Thread(() -> {
                funcA();
            });
            num1Pool[i].start();
        }

        for (int i = 0; i < iNum2; i++) {
            num2Pool[i] = new Thread(() -> {
                funcB();
            });
            num2Pool[i].start();
        }

        for (int i = 0; i < iNum1; i++) {
            try {
                num1Pool[i].join();
            } catch (InterruptedException e) {
                System.err.println("Thread join error: " + e.getMessage());
                throw new RuntimeException();
            }
        }

        for (int i = 0; i < iNum2; i++) {
            try {
                num2Pool[i].join();
            } catch (InterruptedException e) {
                System.err.println("Thread join error: " + e.getMessage());
                throw new RuntimeException();
            }
        }

        return bug.get();

    }
}
