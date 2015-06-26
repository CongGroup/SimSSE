package base;

import tool.BaseTool;
import tool.PRF;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Random;

/**
 * Created by HarryC on 8/5/14.
 * <p/>
 * This class is used to store secure indexTable.
 * <p/>
 * 28/5/2014: combine probing D with counter C.
 * 31/5/2014: modify all the background structure.
 */
public class SecureIndex {

    private static Random random;

    public static int numOfKick;

    private short L;

    private int W;

    private int initialC;

    private short thresholdOfKick;

    private short counterLimit;

    private int totalSize;

    private int loopSize;

    private int maxC;

    private long indexTable[][];

    public LSHVector lshVectors[];

    //private ArrayList<IndexRow> indexTable;

    private ArrayList<HashMap<Long, Integer>> counterSpace;

    public SecureIndex(short _L, int _W, int _initialC, short _thresholdOfKick, short _counterLimit, int _totalSize, int _loopSize) {

        this.maxC = 0;

        this.L = _L;
        this.W = _W;
        this.initialC = _initialC;
        this.thresholdOfKick = _thresholdOfKick;
        this.counterLimit = _counterLimit;

        this.totalSize = _totalSize;
        this.loopSize = _loopSize;

        //this.indexTable = new ArrayList<IndexRow>(L);
        this.indexTable = new long[L][W];

        this.counterSpace = new ArrayList<HashMap<Long, Integer>>(L);  // key: LSH value in specific row, value: maximum counter

        this.lshVectors = new LSHVector[totalSize + 1];

        for (int i = 0; i < L; ++i) {

            //indexTable.add(new IndexRow(W));

            counterSpace.add(new HashMap<Long, Integer>());
        }

        random = new Random(1);
        numOfKick = 0;
    }

    /**
     * @return InsertResult: type:0-failed, 1-directly insert, 2-kick insert
     */
    /*public InsertResult insert(int newLshID, int level, int numOfTry, int numOfCounter) {

        InsertResult result = null;

        boolean isSuccess = false;

        // Process 1: directly insert
        //TODO: now the "random" is just a start point
        int idx = random.nextInt(L);
        int firstIdx = idx;
        int numOfLevelTry = 0;

        int counter = 0;
        int searchKey = 0;

        numOfLevelTry = 0;

        // The following section is used to search empty bucket in L bone position
        while (numOfLevelTry < L) {

            // control the L levels
            ++numOfLevelTry;

            // record the total number of steps
            ++numOfTry;

            searchKey = (int) (PRF.SHA256ToUnsignedInt(String.valueOf(this.lshDataSpace.get(newLshID).getLSHValueByIndex(idx)) + String.valueOf(0)) % W);

            if (indexTable[idx][searchKey] == 0) {

                // directly insert
                indexTable[idx][searchKey] = newLshID;

                // record the position's counter as 0
                counterSpace.get(idx).put(this.lshDataSpace.get(newLshID).getLSHValueByIndex(idx), 0);

                isSuccess = true;
                break;
            }

            idx = (++idx) % L;

            //System.out.println("Number of level try : " + numOfLevelTry);
        }

        // search on initial counter's space
        //if (!isSuccess && level == 0) { // if only kick the bone
        if (!isSuccess) {

            numOfLevelTry = 0;

            while (numOfLevelTry < L && !isSuccess) {

                ++numOfLevelTry;

                // range from 1 to initialC
                for (int j = 1; j <= initialC; ++j) {

                    ++numOfTry;

                    searchKey = (int) (PRF.SHA256ToUnsignedInt(String.valueOf(this.lshDataSpace.get(newLshID).getLSHValueByIndex(idx)) + String.valueOf(j)) % W);

                    if (indexTable[idx][searchKey] == 0) {

                        // directly insert
                        indexTable[idx][searchKey] = newLshID;

                        // record the position's counter as 0
                        counterSpace.get(idx).put(this.lshDataSpace.get(newLshID).getLSHValueByIndex(idx), 0);

                        if (this.maxC < j) {
                            this.maxC = j;
                        }

                        isSuccess = true;
                        break;
                    }
                }

                idx = (++idx) % L;
            }
        }

        // start kick-away
        if (!isSuccess) {
            if (thresholdOfKick > 0) {
                if (level < this.thresholdOfKick) {

                    // Handle Kick-away
                    // Add: if fail, recover all positions
                    //counter = counterSpace.get(firstIdx).get(_lshVector.getLSHValueByIndex(firstIdx));

                    searchKey = (int) (PRF.SHA256ToUnsignedInt(String.valueOf(this.lshDataSpace.get(newLshID).getLSHValueByIndex(firstIdx)) + String.valueOf(0)) % W);

                    long tempId = indexTable[firstIdx][searchKey];

                    indexTable[firstIdx][searchKey] = newLshID;

                    //System.out.println("Swap id: " + tempId + " --> " + newLshID);

                    result = this.insert((int) tempId, level + 1, numOfTry, numOfCounter);

                    // if fail, recovery all positions
                    if (result.getType() == Constant.INSERT_FAIL) {

                        indexTable[firstIdx][searchKey] = tempId;

                        //System.out.println("Fail recover swap id: " + newLshID + " --> " + tempId);
                    } else {
                        return result;
                    }
                } else {
                    result = new InsertResult(Constant.INSERT_FAIL, level, numOfTry);

                    return result;
                }
            }
        } else {
            if (level == 0) {
                result = new InsertResult(Constant.INSERT_DIRECT, level, numOfTry);
            } else {
                result = new InsertResult(Constant.INSERT_KICK, level, numOfTry);
            }
        }

        if (!isSuccess && level == 0 && numOfCounter < counterLimit) {

            int minRow = 0;
            int minCounter = counterLimit;

            boolean fail = true;

            for (int i = 0; i < L; ++i) {
                for (int j = initialC + 1; j <= counterLimit; ++j) {

                    searchKey = (int) (PRF.SHA256ToUnsignedInt(String.valueOf(this.lshDataSpace.get(newLshID).getLSHValueByIndex(i)) + String.valueOf(j)) % W);

                    if (indexTable[idx][searchKey] == 0) {
                        if (j < minCounter) {
                            minCounter = j;
                            minRow = i;
                            fail = false;
                            break;
                        }
                    }
                }
            }

            if (fail) {

                result = new InsertResult(Constant.INSERT_FAIL, level, numOfTry);

            } else {

                ++numOfTry;

                // insert to the minimum counter position
                searchKey = (int) (PRF.SHA256ToUnsignedInt(String.valueOf(this.lshDataSpace.get(newLshID).getLSHValueByIndex(minRow)) + String.valueOf(minCounter)) % W);

                indexTable[minRow][searchKey] = newLshID;

                // record the position's counter as j
                counterSpace.get(idx).put(this.lshDataSpace.get(newLshID).getLSHValueByIndex(minRow), minCounter);

                if (this.maxC < minCounter) {
                    this.maxC = minCounter;
                }

                result = new InsertResult(Constant.INSERT_COUNTER, level, numOfTry);

                isSuccess = true;
            }
        }

        return result;
    }

*/
    public InsertResult insertSecure(int newLshID, int level, int numOfTry, int numOfCounter, String key1) {

        InsertResult result = null;

        boolean isSuccess = false;

        // Process 1: directly insert
        int idx = random.nextInt(L);
        int firstIdx = idx;
        int numOfLevelTry = 0;

        int counter = 0;
        int searchKey = 0;

        numOfLevelTry = 0;

        // The following section is used to search empty bucket in L bone position
        while (numOfLevelTry < L) {

            // control the L levels
            ++numOfLevelTry;

            // record the total number of steps
            ++numOfTry;

            searchKey = encryptPosition(key1, this.lshVectors[BaseTool.mapIndex(newLshID, loopSize)].getLSHValueByIndex(idx), 0, W);

            if (indexTable[idx][searchKey] == 0) {

                // directly insert
                indexTable[idx][searchKey] = newLshID;

                // record the position's counter as 0
                counterSpace.get(idx).put(this.lshVectors[BaseTool.mapIndex(newLshID, loopSize)].getLSHValueByIndex(idx), 0);

                isSuccess = true;
                break;
            }

            idx = (++idx) % L;

            //System.out.println("Number of level try : " + numOfLevelTry);
        }

        // search on initial counter's space
        //if (!isSuccess && level == 0) { // if only kick the bone
        if (!isSuccess) {

            numOfLevelTry = 0;

            while (numOfLevelTry < L && !isSuccess) {

                ++numOfLevelTry;

                // range from 1 to initialC
                for (int j = 1; j <= initialC; ++j) {

                    ++numOfTry;

                    searchKey = encryptPosition(key1, this.lshVectors[BaseTool.mapIndex(newLshID, loopSize)].getLSHValueByIndex(idx), j, W);

                    if (indexTable[idx][searchKey] == 0) {

                        // directly insert
                        indexTable[idx][searchKey] = newLshID;

                        // record the position's counter as 0
                        counterSpace.get(idx).put(this.lshVectors[BaseTool.mapIndex(newLshID, loopSize)].getLSHValueByIndex(idx), 0);

                        if (this.maxC < j) {
                            this.maxC = j;
                        }

                        isSuccess = true;
                        break;
                    }
                }

                idx = (++idx) % L;
            }
        }

        // start kick-away
        if (!isSuccess) {
            if (thresholdOfKick > 0) {
                if (level < this.thresholdOfKick) {

                    // Handle Kick-away
                    // Add: if fail, recover all positions

                    searchKey = encryptPosition(key1, this.lshVectors[BaseTool.mapIndex(newLshID, loopSize)].getLSHValueByIndex(firstIdx), 0, W);

                    long tempId = indexTable[firstIdx][searchKey];

                    indexTable[firstIdx][searchKey] = newLshID;

                    //System.out.println("Swap id: " + tempId + " --> " + newLshID);

                    ++numOfKick;
                    result = this.insertSecure((int) tempId, level + 1, numOfTry, numOfCounter, key1);

                    // if fail, recovery all positions
                    if (result.getType() == Constant.INSERT_FAIL) {

                        indexTable[firstIdx][searchKey] = tempId;

                        //System.out.println("Fail recover swap id: " + newLshID + " --> " + tempId);
                    } else {
                        return result;
                    }
                } else {
                    result = new InsertResult(Constant.INSERT_FAIL, level, numOfTry);

                    return result;
                }
            }
        } else {
            if (level == 0) {
                result = new InsertResult(Constant.INSERT_DIRECT, level, numOfTry);
            } else {
                result = new InsertResult(Constant.INSERT_KICK, level, numOfTry);
            }
        }

        if (!isSuccess && level == 0 && numOfCounter < counterLimit) {

            int minRow = 0;
            int minCounter = counterLimit;

            boolean fail = true;

            for (int i = 0; i < L; ++i) {
                for (int j = initialC + 1; j <= counterLimit; ++j) {

                    searchKey = encryptPosition(key1, this.lshVectors[BaseTool.mapIndex(newLshID, loopSize)].getLSHValueByIndex(i), j, W);

                    if (indexTable[idx][searchKey] == 0) {
                        if (j < minCounter) {
                            minCounter = j;
                            minRow = i;
                            fail = false;
                            break;
                        }
                    }
                }
            }

            if (fail) {

                result = new InsertResult(Constant.INSERT_FAIL, level, numOfTry);

            } else {

                ++numOfTry;

                // insert to the minimum counter position
                searchKey = encryptPosition(key1, this.lshVectors[BaseTool.mapIndex(newLshID, loopSize)].getLSHValueByIndex(minRow), minCounter, W);

                indexTable[minRow][searchKey] = newLshID;

                // record the position's counter as j
                counterSpace.get(idx).put(this.lshVectors[BaseTool.mapIndex(newLshID, loopSize)].getLSHValueByIndex(minRow), minCounter);

                if (this.maxC < minCounter) {
                    this.maxC = minCounter;
                }

                result = new InsertResult(Constant.INSERT_COUNTER, level, numOfTry);

                isSuccess = true;
            }
        }

        return result;
    }

    public void encryptAllTable(String key2) {

        for (int i = 0; i < L; ++i) {

            for (int j = 0; j < W; ++j) {
                Integer mask = random.nextInt(65535);
                // if not empty
                if (indexTable[i][j] != 0) {

                    indexTable[i][j] = encryptValue(key2, this.lshVectors[BaseTool.mapIndex((int) indexTable[i][j], loopSize)].getLSHValueByIndex(i), j, (int) indexTable[i][j]);
                } else {

                    indexTable[i][j] = encryptValue(key2, 0, j, 0);
                }
            }
        }
    }

    /**
     * Search similar items in secure index table
     *
     * @param query query
     * @return limited in L * initialC
     */
    public HashSet<LSHVector> searchSecure(LSHVector query, String key1, String key2) {

        HashSet<LSHVector> similarItemList = new HashSet<LSHVector>(L * initialC);

        long probValue;
        int searchKey;
        for (int i = 0; i < L; ++i) {

            probValue = query.getLSHValueByIndex(i);

            long k1Vj = clientK1Vj(key1, probValue);
            long k2Vj = clientK2Vj(key2, probValue);

            for (int j = 0; j <= initialC; ++j) {


                searchKey = serverPosition(k1Vj, j);

                long testValue = serverDecryptValue(k2Vj, searchKey, indexTable[i][searchKey]);

                if (testValue >> 32 == 0 && recoverIndex(testValue) > 0) {

                    similarItemList.add(this.lshVectors[BaseTool.mapIndex(recoverIndex(testValue), loopSize)]);
                }
            }
        }

        return similarItemList;
    }

    public ArrayList<LSHVector> searchForTruePositive(LSHVector query, String key1, String key2) {

        //HashSet<LSHVector> similarItemList = new HashSet<LSHVector>(L * this.maxC);
        ArrayList<LSHVector> similarItemList = new ArrayList<LSHVector>(L * this.initialC);

        long probValue;
        int searchKey;

        long uselessTime = 0;

        long clientTotalTime = 0;
        long startTimeServer = System.nanoTime();

        for (int i = 0; i < L; ++i) {

            probValue = query.getLSHValueByIndex(i);

            long startTimeClient = System.nanoTime();

            long k1Vj = clientK1Vj(key1, probValue);
            long k2Vj = clientK2Vj(key2, probValue);

            clientTotalTime += System.nanoTime() - startTimeClient;

            for (int j = 0; j <= this.initialC; ++j) {

                try {

                    searchKey = serverPosition(k1Vj, j);

                    long testValue = serverDecryptValue(k2Vj, searchKey, indexTable[i][searchKey]);

                    if ((int) (testValue >> 32) == 0 && recoverIndex(testValue) > 0) {

                        long startTimeUseless = System.nanoTime();

                        if (query.isSimilar(this.lshVectors[BaseTool.mapIndex(recoverIndex(testValue), loopSize)])) {

                            //System.out.println("test value = " + recoverIndex(testValue));
                            try {
                                similarItemList.add(this.lshVectors[BaseTool.mapIndex(recoverIndex(testValue), loopSize)]);
                            } catch (NullPointerException e) {
                                System.err.println(e);
                            }
                        }
                        uselessTime += System.nanoTime() - startTimeUseless;
                    }
                } catch (NullPointerException e) {
                    System.err.println(e);
                }
            }
        }

        //System.out.println("\n\t\t(Client) Average trapdoor build time is : " + clientTotalTime / L / 1000 + " us");
        //System.out.println("\t\t(Client) Total trapdoor build time is   : " + clientTotalTime / 1000 + " us");
        //System.out.println("\n\t\t(Server) Average search time is         : " + (System.nanoTime() - startTimeServer - uselessTime - clientTotalTime) / L / (this.maxC + 1) / 1000 + " us");
        //System.out.println("\t\t(Server) Total search time is           : " + (System.nanoTime() - startTimeServer - uselessTime - clientTotalTime) / 1000 + " us\n");

        return similarItemList;
    }

    public String analyse(LSHVector query, String key1, String key2) {

        StringBuilder sb = new StringBuilder();

        long numOfTruePos = 0;
        long numOfTrueNeg = 0;

        HashSet<Integer> idSet = new HashSet<Integer>();

        long probValue;
        int searchKey;
        for (int i = 0; i < L; ++i) {

            probValue = query.getLSHValueByIndex(i);

            long k1Vj = clientK1Vj(key1, probValue);
            long k2Vj = clientK2Vj(key2, probValue);

            for (int j = 0; j <= this.maxC; ++j) {

                searchKey = serverPosition(k1Vj, j);

                //searchKey = encryptPosition(key1, probValue, j, W);

                long testValue = serverDecryptValue(k2Vj, searchKey, indexTable[i][searchKey]);

                if (testValue >> 32 == 0 && recoverIndex(testValue) > 0) {

                    if (!idSet.contains(recoverIndex(testValue))) {
                        idSet.add(recoverIndex(testValue));

                        if (query.isSimilar(this.lshVectors[BaseTool.mapIndex(recoverIndex(testValue), loopSize)])) {

                            ++numOfTruePos;
                        } else {

                            ++numOfTrueNeg;
                        }
                    }
                }
            }
        }

        if (numOfTruePos + numOfTrueNeg > 0) {

            sb.append("Similar item(s)               : ");
            sb.append(numOfTrueNeg + numOfTruePos);
            sb.append("\nNumber of positive            : ");
            sb.append(numOfTruePos);
            sb.append("\nNumber of negative            : ");
            sb.append(numOfTrueNeg);
            sb.append("\nAverage lsh accuracy rate     : ");
            sb.append((double) numOfTruePos / (double) (numOfTrueNeg + numOfTruePos));
            sb.append("\n");
        } else {
            sb.append("No similar items founded!");
        }

        return sb.toString();
    }

    public double analyseAccuracy(LSHVector query, String key1, String key2) {

        double accuracy = 0.0;

        long numOfTruePos = 0;
        long numOfTrueNeg = 0;

        HashSet<Integer> idSet = new HashSet<Integer>();

        long probValue;
        int searchKey;
        for (int i = 0; i < L; ++i) {

            probValue = query.getLSHValueByIndex(i);

            long k1Vj = clientK1Vj(key1, probValue);
            long k2Vj = clientK2Vj(key2, probValue);

            for (int j = 1; j <= this.maxC; ++j) {

                searchKey = serverPosition(k1Vj, j);

                long testValue = serverDecryptValue(k2Vj, searchKey, indexTable[i][searchKey]);

                if (testValue >> 32 == 0 && recoverIndex(testValue) > 0) {

                    if (!idSet.contains(recoverIndex(testValue))) {

                        idSet.add(recoverIndex(testValue));

                        if (query.isSimilar(this.lshVectors[BaseTool.mapIndex(recoverIndex(testValue), loopSize)])) {

                            ++numOfTruePos;
                        } else {

                            ++numOfTrueNeg;
                        }
                    }
                }
            }
        }

        if (numOfTruePos + numOfTrueNeg > 0) {

            accuracy = (double) numOfTruePos / (double) (numOfTrueNeg + numOfTruePos);
        }

        return accuracy;
    }

    public int recoverIndex(long longValue) {

        return (int) longValue;
    }

    private long clientK1Vj(String key1, long lshValue) {

        return PRF.HMACSHA256ToUnsignedInt(String.valueOf(lshValue), key1);
    }

    private int serverPosition(long k1Vj, int counter) {

        return (int) (PRF.HMACSHA256ToUnsignedInt(String.valueOf(counter), String.valueOf(k1Vj)) % this.W);
    }

    private long clientK2Vj(String key2, long lshValue) {

        return PRF.HMACSHA256ToUnsignedInt(String.valueOf(lshValue), key2);
    }

    private long serverDecryptValue(long k2Vj, int position, long cipherValue) {

        long r = BaseTool.flod256Bytes(PRF.HMACSHA256(String.valueOf(position), String.valueOf(k2Vj)));

        return cipherValue ^ r;
    }

    private int encryptPosition(String key1, long lshValue, int counter, int W) {

        long k1Vj = PRF.HMACSHA256ToUnsignedInt(String.valueOf(lshValue), key1);

        return (int) (PRF.HMACSHA256ToUnsignedInt(String.valueOf(counter), String.valueOf(k1Vj)) % W);
    }

    private long encryptValue(String key2, long lshValue, int position, int id) {

        long k2Vj = PRF.HMACSHA256ToUnsignedInt(String.valueOf(lshValue), key2);

        long r = BaseTool.flod256Bytes(PRF.HMACSHA256(String.valueOf(position), String.valueOf(k2Vj)));

        return (long) id ^ r;
    }

    public int getMaxC() {
        return maxC;
    }

    public void setMaxC(int maxC) {
        this.maxC = maxC;
    }

}
