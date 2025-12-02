package sut;

import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

public class MinimizationTestWithNoise {
    public int x, y, z; // , w = 0;
    private boolean conditionMet;
    public int o, p = 0;

    public MinimizationTestWithNoise() {
        x = 0;
        y = 0;
        z = 0;
        conditionMet = false;

    }

    public static void main(String[] args) throws InterruptedException {
        MinimizationTest test = new MinimizationTest();
        test.run();
    }

    public boolean run() throws InterruptedException {
        ReentrantLock lock = new ReentrantLock();
        Condition w = lock.newCondition();

        Thread Tb = new Thread(() -> {
            int a = 0, b = 0;
            x = 1;
            a = x;
            y = a;
            lock.lock();
            try {
                while (!conditionMet) {
                    w.await();
                }
                b = y;
                z = a + b;
            } catch (InterruptedException e) {
                e.printStackTrace();
            } finally {
                lock.unlock();
            }
        }, "t2");

        Thread Ta = new Thread(() -> {
            lock.lock();
            x++;
            y++;
            conditionMet = true;
            w.signal();
            lock.unlock();
            assert (z < 5) : "I found the error where !(z < 5)";
        }, "t1");


        // Noise threads: increase branching, but don't change correctness
        final int NOISE_THREADS = 4;
        Thread[] noise = new Thread[NOISE_THREADS];

        for (int i = 0; i < NOISE_THREADS; i++) {
            final int id = i;
            noise[i] = new Thread(() -> {
                for (int k = 0; k < 5; k++) {
                    lock.lock();
                    try {
                        // touch shared vars a bit to defeat POR
                        o++;
                        p ^= id;
                    } finally {
                        lock.unlock();
                    }
                    // maybe a yield point
                    Thread.yield();
                }
            }, "noise-" + i);
        }

        // start everything
        for (Thread t : noise) t.start();

        Ta.start();
        Tb.start();

        for (Thread t : noise) t.join();

        Ta.join();
        Tb.join();

        return z < 5;
        // Only interleaving sequence in which the assert fails is B1, A1, B2, B3, A2,
        // A3, B4, B5, B6, A4
    }
}
