package base;

/**
 * Created by HarryC on 8/5/14.
 *
 * This class is used to record each result of insert operation.
 */
public class InsertResult {

    private short type;

    private int numOfKick;

    private int numOfTry;

    public InsertResult(short type, int numOfKick, int numOfTry) {
        this.type = type;
        this.numOfKick = numOfKick;
        this.numOfTry = numOfTry;
    }

    public short getType() {
        return type;
    }

    public void setType(short type) {
        this.type = type;
    }

    public int getNumOfKick() {
        return numOfKick;
    }

    public void setNumOfKick(int numOfKick) {
        this.numOfKick = numOfKick;
    }

    public int getNumOfTry() {
        return numOfTry;
    }

    public void setNumOfTry(int numOfTry) {
        this.numOfTry = numOfTry;
    }
}
