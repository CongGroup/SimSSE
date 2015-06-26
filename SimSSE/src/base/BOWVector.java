package base;

import java.util.HashMap;

/**
 * Created by HarryC on 12/5/14.
 * <p/>
 * This class represents a BoW vector.
 */
public class BOWVector {

    private int dimension;

    private int id;

    private HashMap<Integer, Double> valuesMap;

    public BOWVector(int dimension, int id, HashMap<Integer, Double> valuesMap) {
        this.dimension = dimension;
        this.id = id;
        this.valuesMap = valuesMap;
    }

    public BOWVector(int id, String dataString, int dimension) {
        this.id = id;
        this.dimension = dimension;
        this.valuesMap = new HashMap<Integer, Double>();

        String[] strValues = dataString.split(" ");

        int i = 0;
        while (i < strValues.length - 1) {

            valuesMap.put(Integer.parseInt(strValues[i]), Double.parseDouble(strValues[i + 1]));
            i += 2;
        }
    }

    public int getDimension() {
        return dimension;
    }

    public void setDimension(int dimension) {
        this.dimension = dimension;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public HashMap<Integer, Double> getValuesMap() {
        return valuesMap;
    }

    public void setValuesMap(HashMap<Integer, Double> valuesMap) {
        this.valuesMap = valuesMap;
    }
}
