package utils;

public class Ccp {

    public int calcCcp(int N, double[] P, double eps) {

        //Raise value error if the probabilities are greater than 1

        double violationSum = 0.0;
        for(double prob : P){
            violationSum += prob;
        }

        if(violationSum > 1 + 1e-3){
            throw new IllegalArgumentException("Probabilities must sum to ≤ 1");
        }

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

    public static void main(String[] args) {
        Ccp myCcp = new Ccp();

        double[] arr = new double[] { 0.99, 0.01 };

        double eps = 0.1;

        System.out.println(myCcp.calcCcp(2, arr, eps));
    }

}
