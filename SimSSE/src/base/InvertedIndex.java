package base;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

/**
 * Created by HarryC on 29/5/14.
 * <p/>
 * This class is used to store inverted indexTable.
 * <p/>
 * 28/5/2014: combine probing D with counter C.
 */
public class InvertedIndex {

    private short L;

    private ArrayList<HashMap<Long, ArrayList<Integer>>> indexTable;

    public InvertedIndex(short _L) {

        this.L = _L;
        this.indexTable = new ArrayList<HashMap<Long, ArrayList<Integer>>>(L);

        for (int i = 0; i < L; ++i) {

            indexTable.add(new HashMap<Long, ArrayList<Integer>>());
        }
    }

    public void insertInvertedIndex(LSHVector _lshVector, int _id) {

        for (int i = 0; i < L; ++i) {


            if (!indexTable.get(i).containsKey(_lshVector.getLSHValueByIndex(i))) {
                ArrayList<Integer> idList = new ArrayList<Integer>();

                //idList.add(_lshVector.getId());
                idList.add(_id);

                indexTable.get(i).put(_lshVector.getLSHValueByIndex(i), idList);
            } else {

                //indexTable.get(i).get(_lshVector.getLSHValueByIndex(i)).add(_lshVector.getId());
                indexTable.get(i).get(_lshVector.getLSHValueByIndex(i)).add(_id);
            }
        }
    }

    /*public void insertInvertedIndex(LSHVector _lshVector) {

        for (int i = 0; i < L; ++i) {


            if (!indexTable.get(i).containsKey(_lshVector.getLSHValueByIndex(i))) {
                ArrayList<Integer> idList = new ArrayList<Integer>();

                idList.add(_lshVector.getId());

                indexTable.get(i).put(_lshVector.getLSHValueByIndex(i), idList);
            } else {

                indexTable.get(i).get(_lshVector.getLSHValueByIndex(i)).add(_lshVector.getId());
            }
        }
    }*/

    public HashSet<Integer> search(LSHVector query) {

        HashSet<Integer> result = new HashSet<Integer>();

        for (int i = 0; i < L; ++i) {

            result.addAll(indexTable.get(i).get(query.getLSHValueByIndex(i)));
        }

        return result;
    }

    public ArrayList<HashMap<Long, ArrayList<Integer>>> getIndexTable() {
        return indexTable;
    }

    public void setIndexTable(ArrayList<HashMap<Long, ArrayList<Integer>>> indexTable) {
        this.indexTable = indexTable;
    }
}
