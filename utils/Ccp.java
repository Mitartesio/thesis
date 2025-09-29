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

        }
        return k - 1;
    }

}
