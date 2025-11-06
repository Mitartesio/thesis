package sut;

import gov.nasa.jpf.vm.Verify;

public class ArrayExample {
    static int pointer = 0;
    static int[] arr = new int[4];

    public static void main(String[] args) throws InterruptedException {
        Thread t1 = new Thread(() -> {
            Verify.beginAtomic();
            arr[pointer] = 1;
            pointer++;
            Verify.endAtomic();

            Verify.beginAtomic();
            arr[pointer] = 2;
            pointer++;

            Verify.endAtomic();
        }, "T1");

        Thread t2 = new Thread(() -> {
            Verify.beginAtomic();
            arr[pointer] = 3;
            pointer++;
            Verify.endAtomic();

            Verify.beginAtomic();
            arr[pointer] = 4;
            pointer++;

            Verify.endAtomic();
        }, "T2");

        t1.start();
        t2.start();

        t1.join();
        t2.join();

        // assert (arr[0] != 1 && arr[1] != 2 && arr[2] != 3 && arr[3] != 4) :
        // "Correctly found the bug";
    }
}
