package base;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Random;

import tool.AESCoder;
import tool.BaseTool;
import tool.PRF;

/**
 * Created by HarryC on 20/6/14.
 * <p/>
 * This class is used to dynamically maintain secure indexTable.
 * <p/>
 * 28/5/2014: combine probing D with counter C.
 * 31/5/2014: modify all the background structure.
 * 20/6/2014: add dynamic feature, modify each bucket's content
 */
public class DynamicSecureIndex {

    private static Random random;

    public static int numOfKick;

    private short l;

    private int w;

    private int d;

    private int maxC;

    private short thresholdOfKick;

    private short counterLimit;

    private int totalSize;

    private int loopSize;

    private long indexTable[][];

    private byte[] maskTable[][];

    public LSHVector lshVectors[];

    private ArrayList<HashMap<Long, Integer>> counterDict;

    public DynamicSecureIndex(short _l, int _w, int _d, short _thresholdOfKick, short _counterLimit, int _totalSize, int _loopSize) {

        this.maxC = 0;

        this.l = _l;
        this.w = _w;
        this.d = _d;
        this.thresholdOfKick = _thresholdOfKick;
        this.counterLimit = _counterLimit;

        this.totalSize = _totalSize;
        this.loopSize = _loopSize;

        //this.indexTable = new ArrayList<IndexRow>(L);
        this.indexTable = new long[l][w];

        this.maskTable = new byte[l][w][];

        this.counterDict = new ArrayList<HashMap<Long, Integer>>(l);  // key: LSH value in specific row, value: maximum counter

        this.lshVectors = new LSHVector[loopSize];

        for (int i = 0; i < l; ++i) {

            //indexTable.add(new IndexRow(W));

            counterDict.add(new HashMap<Long, Integer>());
        }

        random = new Random(1);
        numOfKick = 0;
    }


    public InsertResult insertSecure(int newLshID, int level, int numOfTry, int numOfCounter, String key1) {

        InsertResult result = null;

        boolean isSuccess = false;

        // Process 1: directly insert
        int idx = random.nextInt(l);
        int firstIdx = idx;
        int numOfLevelTry = 0;

        int counter = 0;
        int searchKey = 0;

        numOfLevelTry = 0;

        // The following section is used to search empty bucket in L bone position
        while (numOfLevelTry < l) {

            // control the L levels
            ++numOfLevelTry;

            // record the total number of steps
            ++numOfTry;

            searchKey = encryptPosition(key1, this.lshVectors[BaseTool.mapIndex(newLshID, loopSize)].getLSHValueByIndex(idx), 0, w);

            if (indexTable[idx][searchKey] == 0) {

                // directly insert
                indexTable[idx][searchKey] = newLshID;

                isSuccess = true;
                break;
            }

            idx = (++idx) % l;

            //System.out.println("Number of level try : " + numOfLevelTry);
        }

        // search on initial counter's space
        //if (!isSuccess && level == 0) { // if only kick the bone
        if (!isSuccess) {

            numOfLevelTry = 0;

            while (numOfLevelTry < l && !isSuccess) {

                ++numOfLevelTry;

                // range from 1 to initialC
                for (int j = 1; j <= d; ++j) {

                    ++numOfTry;

                    searchKey = encryptPosition(key1, this.lshVectors[BaseTool.mapIndex(newLshID, loopSize)].getLSHValueByIndex(idx), j, w);

                    if (indexTable[idx][searchKey] == 0) {

                        // directly insert
                        indexTable[idx][searchKey] = newLshID;

                        if (this.maxC < j) {
                            this.maxC = j;
                        }

                        isSuccess = true;
                        break;
                    }
                }

                idx = (++idx) % l;
            }
        }

        // start kick-away
        if (!isSuccess) {
            if (thresholdOfKick > 0) {
                if (level < this.thresholdOfKick) {

                    // Handle Kick-away
                    // Add: if fail, recover all positions
                    //counter = counterSpace.get(firstIdx).get(_lshVector.getLSHValueByIndex(firstIdx));

                    searchKey = encryptPosition(key1, this.lshVectors[BaseTool.mapIndex(newLshID, loopSize)].getLSHValueByIndex(firstIdx), 0, w);

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

            for (int i = 0; i < l; ++i) {
                for (int j = d + 1; j <= counterLimit; ++j) {

                    searchKey = encryptPosition(key1, this.lshVectors[BaseTool.mapIndex(newLshID, loopSize)].getLSHValueByIndex(i), j, w);

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
                searchKey = encryptPosition(key1, this.lshVectors[BaseTool.mapIndex(newLshID, loopSize)].getLSHValueByIndex(minRow), minCounter, w);

                indexTable[minRow][searchKey] = newLshID;

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

        for (int i = 0; i < l; ++i) {

            for (int j = 0; j < w; ++j) {

                Integer mask = random.nextInt(65535);
                try {
                    maskTable[i][j] = AESCoder.encrypt(mask.toString().getBytes(), AESCoder.toKey(PRF.SHA256(key2, 64).getBytes()));
                } catch (Exception e) {
                    e.printStackTrace();
                }
                indexTable[i][j] = mask ^ indexTable[i][j];

            }
        }
    }


    /**
     * Search similar items in secure index table
     *
     * @param query query
     * @return limited in L*D
     */
    /*
    public ArrayList<LSHVector> search(LSHVector query) {

        ArrayList<LSHVector> similarItemList = new ArrayList<LSHVector>(L * initialC);

        long probValue;
        int searchKey;
        for (int i = 0; i < L; ++i) {

            probValue = query.getLSHValueByIndex(i);

            searchKey = (int) (PRF.SHA256ToUnsignedInt(String.valueOf(probValue) + "0") % W);

            if (indexTable[i][searchKey] != 0) {

                similarItemList.add(this.lshDataSpace.get(recoverIndex(indexTable[i][searchKey])));
            }

            for (int j = 1; j <= initialC; ++j) {
                searchKey = (int) (PRF.SHA256ToUnsignedInt(String.valueOf(probValue) + String.valueOf(j)) % W);

                if (indexTable[i][searchKey] != 0) {

                    similarItemList.add(this.lshDataSpace.get(recoverIndex(indexTable[i][searchKey])));
                }
            }
        }

        return similarItemList;
    }
*/
    public HashSet<LSHVector> searchSecure(LSHVector query, String key1, String key2) {

        HashSet<LSHVector> similarItemList = new HashSet<LSHVector>(l * d);

        long probValue;
        int searchKey;
        for (int i = 0; i < l; ++i) {

            probValue = query.getLSHValueByIndex(i);

            long k1Vj = clientK1Vj(key1, probValue);
            long k2Vj = clientK2Vj(key2, probValue);

            for (int j = 0; j <= d; ++j) {


                searchKey = serverPosition(k1Vj, j);

                byte[] encryptedMask = maskTable[i][searchKey];

                try {
                    Integer mask = Integer.parseInt(new String(AESCoder.decrypt(encryptedMask, AESCoder.toKey(PRF.SHA256(key2, 64).getBytes()))));

                    long id = mask ^ indexTable[i][searchKey];

                    if (id != 0) {
                        similarItemList.add(this.lshVectors[BaseTool.mapIndex(recoverIndex(id), loopSize)]);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }

                //searchKey = encryptPosition(key1, probValue, j, W);
                /*
                long testValue = serverDecryptValue(k2Vj, searchKey, indexTable[i][searchKey]);

                if (testValue >> 32 == 0 && recoverIndex(testValue) > 0) {

                    similarItemList.add(this.lshVectors[recoverIndex(testValue)]);
                }*/
            }
        }

        return similarItemList;
    }

    public HashSet<LSHVector> searchForTruePositive(LSHVector query, String key1, String key2) {

        HashSet<LSHVector> similarItemList = new HashSet<LSHVector>(l * d);

        long probValue;

        int searchKey;

        int testC = 0;

        ArrayList<Integer> rowList = new ArrayList<Integer>(l * this.maxC);
        ArrayList<Integer> columnList = new ArrayList<Integer>(l * this.maxC);

        long serverTotalTime = 0;

        long clientTotalTime = 0;

        for (int i = 0; i < l; ++i) {

            probValue = query.getLSHValueByIndex(i);

            long k1Vj = clientK1Vj(key1, probValue);

            long serverStartTime = System.nanoTime();

            if (counterDict.get(i).get(probValue) == null) {
                counterDict.get(i).put(probValue, this.maxC);
                testC = this.maxC;
            } else {
                testC = counterDict.get(i).get(probValue);
            }


            for (int j = 0; j <= testC; ++j) {

                searchKey = serverPosition(k1Vj, j);

                rowList.add(i);
                columnList.add(searchKey);
            }

            serverTotalTime += System.nanoTime() - serverStartTime;
        }

        // client side
        for (int i = 0; i < rowList.size(); ++i) {

            try {

                long clientStartTime = System.nanoTime();

                byte[] encryptedMask = maskTable[rowList.get(i)][columnList.get(i)];

                try {
                    Integer mask = Integer.parseInt(new String(AESCoder.decrypt(encryptedMask, AESCoder.toKey(PRF.SHA256(key2, 64).getBytes()))));

                    long id = mask ^ indexTable[rowList.get(i)][columnList.get(i)];

                    clientTotalTime += System.nanoTime() - clientStartTime;

                    if (id != 0 && query.isSimilar(this.lshVectors[BaseTool.mapIndex(recoverIndex(id), loopSize)])) {
                        similarItemList.add(this.lshVectors[BaseTool.mapIndex(recoverIndex(id), loopSize)]);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } catch (NullPointerException e) {
                System.err.println(e);
            }
        }

        System.out.println("\n\t\t(Client) Average decryption time is : " + clientTotalTime / rowList.size() / 1000 + " us");
        System.out.println("\t\t(Client) Total decryption time for one item is   : " + clientTotalTime / 1000 + " us");
        System.out.println("\n\t\t(Server) Average search time is         : " + serverTotalTime / rowList.size() / 1000 + " us");
        System.out.println("\t\t(Server) Total search time  for one item is           : " + serverTotalTime / 1000 + " us\n");

        return similarItemList;
    }

    public TestTimeResult dynamicInsert(long queryId, LSHVector query, String key1, String key2) {

        int numOfInteraction = 0;

        long probValue;

        int searchKey;

        int testC = 0;

        boolean isSuccess = false;

        long serverTotalTime = 0;

        long clientTotalTime = 0;

        ArrayList<Integer> columnList = new ArrayList<Integer>(l);

        while (!isSuccess && numOfInteraction < 100) {

            ++numOfInteraction;

            columnList.clear();


            for (int i = 0; i < l; ++i) {

                probValue = query.getLSHValueByIndex(i);
                long k1Vj = clientK1Vj(key1, probValue);

                long serverStartTime = System.nanoTime();

                if (counterDict.get(i).get(probValue) == null) {
                    counterDict.get(i).put(probValue, this.maxC);
                    testC = this.maxC;
                } else {
                    testC = counterDict.get(i).get(probValue) + numOfInteraction - 1;
                    counterDict.get(i).put(probValue, testC);
                }

                searchKey = serverPosition(k1Vj, testC);

                columnList.add(searchKey);

                serverTotalTime += System.nanoTime() - serverStartTime;
            }

            // client side
            for (int i = 0; i < l; ++i) {

                try {

                    long clientStartTime = System.nanoTime();

                    byte[] encryptedMask = maskTable[i][columnList.get(i)];

                    try {
                        Integer mask = Integer.parseInt(new String(AESCoder.decrypt(encryptedMask, AESCoder.toKey(PRF.SHA256(key2, 64).getBytes()))));

                        long id = mask ^ indexTable[i][columnList.get(i)];

                        Integer newMask = random.nextInt(65535);

                        // if empty and not insert in this routine, do it. Or re-mask such bucket
                        if (id == 0 && !isSuccess) {

                            isSuccess = true;

                            try {
                                maskTable[i][columnList.get(i)] = AESCoder.encrypt(newMask.toString().getBytes(), AESCoder.toKey(PRF.SHA256(key2, 64).getBytes()));
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                            indexTable[i][columnList.get(i)] = newMask ^ queryId;

                        } else {

                            try {
                                maskTable[i][columnList.get(i)] = AESCoder.encrypt(newMask.toString().getBytes(), AESCoder.toKey(PRF.SHA256(key2, 64).getBytes()));
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                            indexTable[i][columnList.get(i)] = newMask ^ id;
                        }

                        clientTotalTime += System.nanoTime() - clientStartTime;

                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                } catch (NullPointerException e) {
                    System.err.println(e);
                }
            }
        }

        return new TestTimeResult(isSuccess, numOfInteraction, clientTotalTime / 1000, clientTotalTime / (numOfInteraction * l) / 1000, serverTotalTime / 1000, serverTotalTime / (numOfInteraction * l) / 1000);
    }

    public TestTimeResult dynamicDelete(long queryId, LSHVector query, String key1, String key2) {

        boolean isSuccess = false;

        long probValue;

        int searchKey;

        int testC = 0;

        ArrayList<Integer> rowList = new ArrayList<Integer>(l * this.maxC);
        ArrayList<Integer> columnList = new ArrayList<Integer>(l * this.maxC);

        long serverTotalTime = 0;

        long clientTotalTime = 0;

        for (int i = 0; i < l; ++i) {

            probValue = query.getLSHValueByIndex(i);

            long k1Vj = clientK1Vj(key1, probValue);

            long serverStartTime = System.nanoTime();

            if (counterDict.get(i).get(probValue) == null) {
                counterDict.get(i).put(probValue, this.maxC);
                testC = this.maxC;
            } else {
                testC = counterDict.get(i).get(probValue);
            }


            for (int j = 0; j <= testC; ++j) {

                searchKey = serverPosition(k1Vj, j);

                rowList.add(i);
                columnList.add(searchKey);
            }

            serverTotalTime += System.nanoTime() - serverStartTime;
        }

        // client side
        for (int i = 0; i < rowList.size(); ++i) {

            try {

                long clientStartTime = System.nanoTime();

                byte[] encryptedMask = maskTable[rowList.get(i)][columnList.get(i)];

                try {
                    Integer mask = Integer.parseInt(new String(AESCoder.decrypt(encryptedMask, AESCoder.toKey(PRF.SHA256(key2, 64).getBytes()))));

                    long id = mask ^ indexTable[rowList.get(i)][columnList.get(i)];

                    Integer newMask = random.nextInt(65535);

                    // if empty and not insert in this routine, do it. Or re-mask such bucket
                    if (id == queryId) {

                        isSuccess = true;

                        try {
                            maskTable[rowList.get(i)][columnList.get(i)] = AESCoder.encrypt(newMask.toString().getBytes(), AESCoder.toKey(PRF.SHA256(key2, 64).getBytes()));
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        indexTable[rowList.get(i)][columnList.get(i)] = newMask ^ 0;

                    } else {

                        try {
                            maskTable[rowList.get(i)][columnList.get(i)] = AESCoder.encrypt(newMask.toString().getBytes(), AESCoder.toKey(PRF.SHA256(key2, 64).getBytes()));
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        indexTable[rowList.get(i)][columnList.get(i)] = newMask ^ id;
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }

                clientTotalTime += System.nanoTime() - clientStartTime;

            } catch (NullPointerException e) {
                System.err.println(e);
            }
        }

        return new TestTimeResult(isSuccess, 0, clientTotalTime / 1000, clientTotalTime / rowList.size() / 1000, serverTotalTime / 1000, serverTotalTime / rowList.size() / 1000);
    }

    public String analyse(LSHVector query, String key1, String key2) {

        StringBuilder sb = new StringBuilder();

        long numOfTruePos = 0;
        long numOfTrueNeg = 0;

        HashSet<Integer> idSet = new HashSet<Integer>();

        long probValue;
        int searchKey;
        for (int i = 0; i < l; ++i) {

            probValue = query.getLSHValueByIndex(i);

            long k1Vj = clientK1Vj(key1, probValue);
            long k2Vj = clientK2Vj(key2, probValue);

            for (int j = 0; j <= d; ++j) {

                searchKey = serverPosition(k1Vj, j);

                //searchKey = encryptPosition(key1, probValue, j, W);

                byte[] encryptedMask = maskTable[i][searchKey];

                try {
                    Integer mask = Integer.parseInt(new String(AESCoder.decrypt(encryptedMask, AESCoder.toKey(PRF.SHA256(key2, 64).getBytes()))));

                    long id = mask ^ indexTable[i][searchKey];

                    if (id != 0) {

                        if (!idSet.contains(recoverIndex(id))) {
                            idSet.add(recoverIndex(id));

                            if (query.isSimilar(this.lshVectors[BaseTool.mapIndex(recoverIndex(id), loopSize)])) {

                                ++numOfTruePos;
                            } else {

                                ++numOfTrueNeg;
                            }
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }

                /*
                long testValue = serverDecryptValue(k2Vj, searchKey, indexTable[i][searchKey]);

                if (testValue >> 32 == 0 && recoverIndex(testValue) > 0) {

                    if (!idSet.contains(recoverIndex(testValue))) {
                        idSet.add(recoverIndex(testValue));

                        if (query.isSimilar(this.lshVectors[recoverIndex(testValue)])) {

                            ++numOfTruePos;
                        } else {

                            ++numOfTrueNeg;
                        }
                    }
                }
                */
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
        for (int i = 0; i < l; ++i) {

            probValue = query.getLSHValueByIndex(i);

            long k1Vj = clientK1Vj(key1, probValue);
            long k2Vj = clientK2Vj(key2, probValue);

            for (int j = 1; j <= d; ++j) {

                searchKey = serverPosition(k1Vj, j);

                //searchKey = encryptPosition(key1, probValue, j, W);

                byte[] encryptedMask = maskTable[i][searchKey];

                try {
                    Integer mask = Integer.parseInt(new String(AESCoder.decrypt(encryptedMask, AESCoder.toKey(PRF.SHA256(key2, 64).getBytes()))));

                    long id = mask ^ indexTable[i][searchKey];

                    if (id != 0) {

                        if (!idSet.contains(recoverIndex(id))) {

                            idSet.add(recoverIndex(id));

                            if (query.isSimilar(this.lshVectors[BaseTool.mapIndex(recoverIndex(id), loopSize)])) {

                                ++numOfTruePos;
                            } else {

                                ++numOfTrueNeg;
                            }
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }

                /*long testValue = serverDecryptValue(k2Vj, searchKey, indexTable[i][searchKey]);

                if (testValue >> 32 == 0 && recoverIndex(testValue) > 0) {

                    if (!idSet.contains(recoverIndex(testValue))) {

                        idSet.add(recoverIndex(testValue));

                        if (query.isSimilar(this.lshVectors[recoverIndex(testValue)])) {

                            ++numOfTruePos;
                        } else {

                            ++numOfTrueNeg;
                        }
                    }
                }*/
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

        return (int) (PRF.HMACSHA256ToUnsignedInt(String.valueOf(counter), String.valueOf(k1Vj)) % this.w);
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


    /*public static long decryptValue(String key2, long lshValue, int position, long cipherValue) {

        long k2Vj = PRF.HMACSHA256ToUnsignedInt(String.valueOf(lshValue), key2);

        long r = BaseTool.flod256Bytes(PRF.HMACSHA256(String.valueOf(position), String.valueOf(k2Vj)));

        long mid = cipherValue ^ r;

        return mid;
    }*/

    public int getMaxC() {
        return maxC;
    }

    public void setMaxC(int maxC) {
        this.maxC = maxC;
    }

}
