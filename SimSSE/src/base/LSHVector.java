package base;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by HarryC on 8/5/14.
 *
 * This class is used to record a LSH vector.
 */
public class LSHVector {

    private short dimension;

    private int id;

    private List<Long> lshValues;

    public LSHVector(LSHVector _lshVector) {

        this.dimension = _lshVector.getDimension();
        this.id = _lshVector.getId();
        this.lshValues = new ArrayList<Long>(dimension);

        for (int i = 0; i < dimension; ++i) {
            this.lshValues.add(i, _lshVector.getLSHValueByIndex(i));
        }
    }

    public LSHVector(int _id, ArrayList<Long> _lshValues, final short _dimension) {

        this.dimension = _dimension;
        this.id = _id;
        this.lshValues = new ArrayList<Long>(_lshValues);
    }

    public LSHVector(int _id, String _itemStr, final short _dimension) {

        this.dimension = _dimension;
        this.id = _id;

        this.lshValues = new ArrayList<Long>(dimension);

        String[] strValues = _itemStr.split(" ");

        for (int i = 0; i < dimension; ++i) {
            this.lshValues.add(i, Long.valueOf(strValues[i]));
        }
    }

    public long getLSHValueByIndex(int index) {

        return this.lshValues.get(index);
    }

    public boolean isSimilar(LSHVector query) {

        if (this.getDimension() != query.getDimension()) {
            return false;
        } else {

            for (int i = 0; i < this.lshValues.size(); ++i) {

                if (this.lshValues.get(i) == query.getLSHValueByIndex(i)) {
                    return true;
                }
            }
        }

        return false;
    }

    public int getId() {
        return id;
    }

    public short getDimension() {
        return dimension;
    }

    public List<Long> getLshValues() {
        return lshValues;
    }
}
