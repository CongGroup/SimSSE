package base;

import java.util.HashMap;

/**
 * Created by HarryC on 9/5/14.
 * <p/>
 * This class is used to represent a row of secure index table.
 */
public class IndexRow {

    private int W;

    private HashMap<Long, LSHVector> rowBuckets;

    public IndexRow(int _W) {
        this.W = _W;
        this.rowBuckets = new HashMap<Long, LSHVector>(W);
    }

    public int getW() {
        return W;
    }

    public void setW(int w) {
        W = w;
    }

    public HashMap<Long, LSHVector> getRowBuckets() {
        return rowBuckets;
    }

    public void setRowBuckets(HashMap<Long, LSHVector> rowBuckets) {
        this.rowBuckets = rowBuckets;
    }
}
