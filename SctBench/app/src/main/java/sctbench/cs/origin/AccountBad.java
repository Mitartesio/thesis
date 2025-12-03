package sctbench.cs.origin;

// Translated from: https://github.com/mc-imperial/sctbench/blob/d59ab26ddaedcd575ffb6a1f5e9711f7d6d2d9f2/benchmarks/concurrent-software-benchmarks/account_bad.c

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class AccountBad {

    private static Lock m;
    private static int x, y, z, balance;
    private static boolean deposit_done = false, withdraw_done = false;

    private static AtomicBoolean bug;

    private static void deposit() {
        m.lock();
        try {
            balance = balance + y;
            deposit_done = true;
        } finally {
            m.unlock();
        }
    }

    private static void withdraw() {
        m.lock();
        try {
            balance = balance - z;
            withdraw_done = true;
        } finally {
            m.unlock();
        }
    }

    private static void check_result() {
        m.lock();
        try {
            if (deposit_done && withdraw_done)
                bug.set(true);
            assert balance == (x - y) - z : "Assert failed - Bug found!"; /* BAD */
        } finally {
            m.unlock();
        }
    }

    public static void main(String[] args) {
        runOnce();
    }

    public static boolean runOnce() {
        x = 1;
        y = 2;
        z = 4;
        balance = x;
        bug = new AtomicBoolean(false);
        m = new ReentrantLock();
        deposit_done = false;
        withdraw_done = false;


        Thread t1 = new Thread(() -> deposit());
        Thread t2 = new Thread(() -> withdraw());
        Thread t3 = new Thread(() -> check_result());


        t1.start();
        t2.start();
        t3.start();


        try {
            t1.join();
            t2.join();
            t3.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return bug.get();

    }
}