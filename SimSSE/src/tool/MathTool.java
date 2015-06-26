package tool;

import base.BOWVector;

import java.util.HashSet;
import java.util.Iterator;

/**
 * Created by HarryC on 10/5/14.
 * <p/>
 * This class is used to implement several math functions.
 */
public class MathTool {

    public static double calculateDistance(int id1, String bowStr1, int id2, String bowStr2, int dimension) {

        double distance = 0.0;

        BOWVector bow1 = new BOWVector(id1, bowStr1, dimension);
        BOWVector bow2 = new BOWVector(id2, bowStr2, dimension);

        HashSet<Integer> keys = new HashSet<Integer>();
        Iterator<Integer> it = bow1.getValuesMap().keySet().iterator();

        while (it.hasNext()) {
            keys.add(it.next());
        }

        it = bow2.getValuesMap().keySet().iterator();

        while (it.hasNext()) {
            keys.add(it.next());
        }

        for (Integer index : keys) {

            if (bow1.getValuesMap().containsKey(index) && bow2.getValuesMap().containsKey(index)) {
                distance += Math.pow((bow1.getValuesMap().get(index) - bow2.getValuesMap().get(index)), 2);
            } else if (bow1.getValuesMap().containsKey(index)) {
                distance += Math.pow(bow1.getValuesMap().get(index), 2);
            } else if (bow2.getValuesMap().containsKey(index)) {
                distance += Math.pow(bow2.getValuesMap().get(index), 2);
            }
        }

        return Math.pow(distance, 0.5);
    }

    public static int getUpperPrimeNumber(int startNum) {

        int prime = startNum;

        boolean isPrime = false;
        while (!isPrime) {

            int upper = (int) Math.pow(prime, 0.5) + 1;
            for (int i = 2; i <= upper; ++i) {
                if (prime % i == 0) {
                    ++prime;
                    break;
                } else if (i == upper) {
                    isPrime = true;
                }
            }
        }

        return prime;
    }
}
