package base;

/**
 * Created by HarryC on 1/9/15.
 */
public class IDs {

    private String iid;
    private int fid;

    public IDs(String iid, int fid) {

        this.iid = iid;
        this.fid = fid;
    }

    public String getIid() {
        return iid;
    }

    public void setIid(String iid) {
        this.iid = iid;
    }

    public int getFid() {
        return fid;
    }

    public void setFid(int fid) {
        this.fid = fid;
    }
}
