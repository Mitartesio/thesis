package SUT;

import gov.nasa.jpf.vm.Verify;

public class One_Percent_Example {
    private static int x = 0;
    private static int answer = 0;

    public static void main(String[] args) throws InterruptedException {
        Thread t1 = new Thread(() -> {
            for (int i = 0; i < 1000; i++) {
                Verify.beginAtomic();
                x++;
                Verify.endAtomic();
            }
        }, "t1");

        Thread t2 = new Thread(() -> {
            Verify.beginAtomic();
            answer = x;
            Verify.endAtomic();
        }, "t2");

        t1.start();
        t2.start();

        t1.join();
        t2.join();

        assert (!(answer < 2) || !(answer > 995) || !(answer == 500 || answer == 699))
                : "Found one wrone asnwer with x: " + x;
    }

}
