package utils;

public class Ccp {

    public int calcCcp(int N, double[] P, double eps) {
        int k = N;

        double sum = eps;

        while (sum >= eps) {
            sum = 0;

            for (int i = 0; i < P.length; i++) {
                sum += Math.pow((1 - P[i]), k);
            }
            k += 1;
            // System.out.println("sum: " + sum + " eps: " + eps);

        }
        return k - 1;
    }

    public static void main(String[] args) {
        Ccp myCcp = new Ccp();

        System.out.println((double) 1 / 6);

        double[] arr = new double[] { 0.16666666666666666, 0.16666666666666666, 0.16666666666666666,
                0.16666666666666666, 0.16666666666666666, 0.16666666666666666 };

        System.out.println(myCcp.calcCcp(6, arr, 0.05));
    }

}
