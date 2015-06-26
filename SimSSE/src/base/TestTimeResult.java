package base;

/**
 * Created by HarryC on 20/6/14.
 */
public class TestTimeResult {

    private boolean isSuccess;

    private int number;

    private long clientTotalTime;

    private long clientAvgTime;

    private long serverTotalTime;

    private long serverAvgTime;

    public TestTimeResult(boolean isSuccess, int number, long clientTotalTime, long clientAvgTime, long serverTotalTime, long serverAvgTime) {
        this.isSuccess = isSuccess;
        this.number = number;
        this.clientTotalTime = clientTotalTime;
        this.clientAvgTime = clientAvgTime;
        this.serverTotalTime = serverTotalTime;
        this.serverAvgTime = serverAvgTime;
    }

    public boolean getIsSuccess() {
        return isSuccess;
    }

    public void setSuccess(boolean isSuccess) {
        this.isSuccess = isSuccess;
    }

    public int getNumber() {
        return number;
    }

    public void setNumber(int number) {
        this.number = number;
    }

    public long getClientTotalTime() {
        return clientTotalTime;
    }

    public void setClientTotalTime(long clientTotalTime) {
        this.clientTotalTime = clientTotalTime;
    }

    public long getClientAvgTime() {
        return clientAvgTime;
    }

    public void setClientAvgTime(long clientAvgTime) {
        this.clientAvgTime = clientAvgTime;
    }

    public long getServerTotalTime() {
        return serverTotalTime;
    }

    public void setServerTotalTime(long serverTotalTime) {
        this.serverTotalTime = serverTotalTime;
    }

    public long getServerAvgTime() {
        return serverAvgTime;
    }

    public void setServerAvgTime(long serverAvgTime) {
        this.serverAvgTime = serverAvgTime;
    }
}
