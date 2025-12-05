package sctbench.cs.origin;

// Translated from: https://github.com/mc-imperial/sctbench/blob/d59ab26ddaedcd575ffb6a1f5e9711f7d6d2d9f2/benchmarks/concurrent-software-benchmarks/sync01_bad.c

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.locks.Condition;

public class Sync01Bad {

    private static int num;

    private static Lock m;
    private static Condition empty;
    private static Condition full;

    private static AtomicBoolean bug;

    private static volatile boolean emptySignaled;

    public static Thread t1;
    public static Thread t2;

    public static boolean runOnce() {
        num = 1;
        emptySignaled = false;

        m = new ReentrantLock();
        empty = m.newCondition();
        full = m.newCondition();
        bug = new AtomicBoolean(false);


        t1 = new Thread(() -> thread1());
        t2 = new Thread(() -> thread2());

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

    private static void thread1() {
        m.lock();
        try {
            while (num > 0) {
                if (Thread.activeCount() == 2 || emptySignaled) {
                    System.out.println("Deadlock detected");
                    bug.set(true);
                    t2.interrupt();
                    throw new RuntimeException();
                }
                empty.await();  // BAD: deadlock
            }
            num++;
            full.signal();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            m.unlock();
        }
    }

    private static void thread2() {
        m.lock();
        try {
            while (num == 0) {
                if (Thread.activeCount() == 2) {
                    System.out.println("Deadlock detected");
                    bug.set(true);
                    t1.interrupt();
                    throw new RuntimeException();
                }
                full.await();
            }
            // num--;
            // System.out.println("consume ....");
            empty.signal();
            emptySignaled = true;
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            m.unlock();
        }
    }


    public static void main(String[] args) {
        runOnce();
    }


}
