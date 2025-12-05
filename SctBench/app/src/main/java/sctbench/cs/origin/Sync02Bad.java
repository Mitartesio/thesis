package sctbench.cs.origin;

// Translated from: https://github.com/mc-imperial/sctbench/blob/d59ab26ddaedcd575ffb6a1f5e9711f7d6d2d9f2/benchmarks/concurrent-software-benchmarks/sync02_bad.c

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class Sync02Bad {
    private static final int N = 2;
    private static int num;
    private static Boolean waiting;
    private static Lock m;
    private static Condition empty;
    private static Condition full;

    private static AtomicBoolean bug;

    private static void producer() {
        int i = 0;
        while (i < N) {
            m.lock();
            try {
                while (num > 0) {
                    if (Thread.activeCount() == 2) {
                        System.out.println("Deadlock detected");
                        bug.set(true);
//                        throw new RuntimeException();
                    }
                    synchronized (waiting) {
                        if (waiting) {
                            t2.interrupt();
                            throw new RuntimeException();
                        }
                        waiting = true;
                    }
                    empty.await();
                    waiting = false;
                }
                num++; // produce
                full.signal();
            } catch (InterruptedException e) {
                e.printStackTrace();
            } finally {
                m.unlock();
            }
            i++;
        }
    }

    private static void consumer() {
        int j = 0;
        while (j < N) {
            m.lock();
            try {
                while (num == 0)
                    if (Thread.activeCount() == 2) {
                        System.out.println("Deadlock detected");
                        bug.set(true);
                        throw new RuntimeException();
                    }
                synchronized (waiting) {
                    if (waiting == true) {
                        System.out.println("Deadlock detected");
                        bug.set(true);
                        t1.interrupt();
                        throw new RuntimeException();
                    }
                    waiting = true;
                }

                full.await();
                waiting = false;
                num--; // consume
                empty.signal();
            } catch (InterruptedException e) {
                e.printStackTrace();
            } finally {
                m.unlock();
            }
            j++;
        }
    }

    public static Thread t1;
    public static Thread t2;

    public static boolean runOnce() {
        num = 2;
        waiting = false;
        m = new ReentrantLock();
        empty = m.newCondition();
        full = m.newCondition();
        bug = new AtomicBoolean(false);


        t1 = new Thread(() -> {
            producer();
        });
        t2 = new Thread(() -> {
            consumer();
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

    public static void main(String[] args) {
        runOnce();
    }


}