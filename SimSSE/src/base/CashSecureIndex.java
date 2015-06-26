package base;

import tool.BaseTool;
import tool.PRF;

import java.util.*;

/**
 * Created by HarryC on 8/5/14.
 * <p/>
 * This class is used to store secure indexTable.
 * <p/>
 * 28/5/2014: combine probing D with counter C.
 * 31/5/2014: modify all the background structure.
 */
public class CashSecureIndex {

    private static Random random;

    private int L;

    private int totalSize;

    public LSHVector lshVectors[];

    //private ArrayList<IndexRow> indexTable;

    private ArrayList<HashMap<Long, ArrayList<Long>>> invertedIndexTable;

    private ArrayList<HashMap<Long, Long>> secureIndexTable;

    public CashSecureIndex(short _L, int _totalSize) {

        this.L = _L;

        this.totalSize = _totalSize;

        this.invertedIndexTable = new ArrayList<HashMap<Long, ArrayList<Long>>>(L);  // key: LSH value in specific row, value: id list

        this.secureIndexTable = new ArrayList<HashMap<Long, Long>>(L);  // key: PRF(LSH) value in specific row, value: id

        this.lshVectors = new LSHVector[totalSize + 1];

        for (int i = 0; i < L; ++i) {

            invertedIndexTable.add(new HashMap<Long, ArrayList<Long>>()); // note that, once add a new key, should initial the value ArrayList.

            secureIndexTable.add(new HashMap<Long, Long>());
        }

        random = new Random(1);
    }


    public void insertInvertedIndex(int lshId) {

        for (int i = 0; i < L; ++i) {

            long lshValue = lshVectors[lshId].getLSHValueByIndex(i);

            if (invertedIndexTable.get(i).containsKey(lshValue)) {

                invertedIndexTable.get(i).get(lshValue).add((long) lshId);
            } else {

                ArrayList<Long> idList = new ArrayList<Long>();
                idList.add((long) lshId);

                invertedIndexTable.get(i).put(lshValue, idList);
            }
        }
    }

    public void buildSecureIndex(String key1, String key2) {

        for (int i = 0; i < L; ++i) {

            Iterator<Long> it = invertedIndexTable.get(i).keySet().iterator();

            while (it.hasNext()) {

                long lshValue = it.next();

                for (int j = 0; j < invertedIndexTable.get(i).get(lshValue).size(); ++j) {

                    long searchKey = encryptPosition(key1, lshValue, j);

                    long encryptedValue = encryptValue(key2, lshValue, j, invertedIndexTable.get(i).get(lshValue).get(j));

                    secureIndexTable.get(i).put(searchKey, encryptedValue);
                }
            }
        }
    }

    public TreeMap<Integer, ArrayList<LSHVector>> searchSecure(LSHVector query, String key1, String key2) {

        TreeMap<Integer, ArrayList<LSHVector>> similarItemMap = new TreeMap<Integer, ArrayList<LSHVector>>();

        HashSet<Integer> similarIdSet = new HashSet<Integer>();

        long searchKey;

        long clientTotalTime = 0;
        long startTimeServer = System.nanoTime();

        for (int i = 0; i < L; ++i) {

            long lshValue = query.getLSHValueByIndex(i);

            long startTimeClient = System.nanoTime();

            long k1Vj = clientK1Vj(key1, lshValue);
            long k2Vj = clientK2Vj(key2, lshValue);

            clientTotalTime += System.nanoTime() - startTimeClient;

            int counter = 0;

            while (true) {

                searchKey = serverPosition(k1Vj, counter);

                if (!secureIndexTable.get(i).containsKey(searchKey)) {
                    break;
                } else {

                    long idValue = serverDecryptValue(k2Vj, counter, secureIndexTable.get(i).get(searchKey));

                    similarIdSet.add(recoverIndex(idValue));

                    ++counter;
                }
            }
        }

        // Rank the result
        Iterator<Integer> it = similarIdSet.iterator();
        while (it.hasNext()) {

            int id = it.next();

            int numOfSame = 0;


            for (int j = 0; j < L; ++j) {
                if (query.getLSHValueByIndex(j) == this.lshVectors[id].getLSHValueByIndex(j)) {
                    numOfSame++;
                }
            }

            if (similarItemMap.containsKey(numOfSame)) {

                similarItemMap.get(numOfSame).add(this.lshVectors[id]);
            } else {

                ArrayList<LSHVector> lshList = new ArrayList<LSHVector>();
                lshList.add(this.lshVectors[id]);

                similarItemMap.put(numOfSame, lshList);
            }

        }


        System.out.println("\n\t\t(Client) Average trapdoor build time is : " + clientTotalTime / L / 1000 + " us");
        System.out.println("\t\t(Client) Total trapdoor build time is   : " + clientTotalTime / 1000 + " us");
        System.out.println("\n\t\t(Server) Average search time is         : " + (System.nanoTime() - startTimeServer - clientTotalTime) / L / 1000 + " us");
        System.out.println("\t\t(Server) Total search time is           : " + (System.nanoTime() - startTimeServer - clientTotalTime) / 1000 + " us\n");

        return similarItemMap;
    }
/*
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
                //for (int j = 0; j <= initialC; ++j) {

                try {

                    searchKey = serverPosition(k1Vj, j);

                    //searchKey = encryptPosition(key1, probValue, j, W);

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

        System.out.println("\n\t\t(Client) Average trapdoor build time is : " + clientTotalTime / L / 1000 + " us");
        System.out.println("\t\t(Client) Total trapdoor build time is   : " + clientTotalTime / 1000 + " us");
        System.out.println("\n\t\t(Server) Average search time is         : " + (System.nanoTime() - startTimeServer - uselessTime - clientTotalTime) / L / (this.maxC + 1) / 1000 + " us");
        System.out.println("\t\t(Server) Total search time is           : " + (System.nanoTime() - startTimeServer - uselessTime - clientTotalTime) / 1000 + " us\n");

        return similarItemList;
    }
*/

    public int recoverIndex(long longValue) {

        return (int) longValue;
    }

    private long clientK1Vj(String key1, long lshValue) {

        return PRF.HMACSHA256ToUnsignedInt(String.valueOf(lshValue), key1);
    }

    private int serverPosition(long k1Vj, int counter) {

        return (int) (PRF.HMACSHA256ToUnsignedInt(String.valueOf(counter), String.valueOf(k1Vj)));
    }

    private long clientK2Vj(String key2, long lshValue) {

        return PRF.HMACSHA256ToUnsignedInt(String.valueOf(lshValue), key2);
    }

    private long serverDecryptValue(long k2Vj, int counter, long cipherValue) {

        long r = BaseTool.flod256Bytes(PRF.HMACSHA256(String.valueOf(counter), String.valueOf(k2Vj)));

        return cipherValue ^ r;
    }


    private long encryptPosition(String key1, long lshValue, int counter) {

        long k1Vj = PRF.HMACSHA256ToUnsignedInt(String.valueOf(lshValue), key1);

        return (PRF.HMACSHA256ToUnsignedInt(String.valueOf(counter), String.valueOf(k1Vj)));
    }

    private long encryptValue(String key2, long lshValue, int counter, long id) {

        long k2Vj = PRF.HMACSHA256ToUnsignedInt(String.valueOf(lshValue), key2);

        long r = BaseTool.flod256Bytes(PRF.HMACSHA256(String.valueOf(counter), String.valueOf(k2Vj)));

        return (long) id ^ r;
    }
}
