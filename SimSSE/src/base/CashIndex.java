package base;

import tool.PRF;

import java.util.*;

/**
 * Created by HarryC on 1/7/15.
 */
public class CashIndex {

    private HashMap<Long, String> rawIndex;

    private HashMap<String, Long> maxC; // used to record the maximum c for each lsh tag;

    public CashIndex(int limit) {

        this.rawIndex = new HashMap<Long, String>(limit);
        this.maxC = new HashMap<String, Long>();
    }

    public void insert(LSHVector lshVector, String imageId, int fid) {

        for (int i = 0; i < lshVector.getDimension(); ++i) {

            Long lshValue = lshVector.getLSHValueByIndex(i);

            long c = 1; // start from 1

            // TODO: double check the connection method
            String k1 = 1 + "xx" + lshValue + "xx" + i;
            //long k2 = Long.parseLong(2 + "00" + lshValue + "00" + i);

            if (maxC.containsKey(k1)) {

                c = maxC.get(k1) + 1;
                maxC.put(k1, c);
            } else {
                maxC.put(k1, 1L);
            }

            boolean successInsert = false;

            while (!successInsert) {

                long a = serverPosition(k1, c);

                //long tag = Long.parseLong(c + "0000" + lshValue + "0" + i);

                // if does not exist, directly insert
                if (!rawIndex.containsKey(a)) {
                    //System.out.println(a);
                    rawIndex.put(a, imageId);
                    successInsert = true;
                    maxC.put(k1, c);
                }

                ++c;
            }
        }
    }

    public HashMap<String, Integer> search(List<LSHVector> query) {

        HashMap<String, Integer> result = new HashMap<String, Integer>();

        for (LSHVector lshVector : query) {

            //HashSet<String> idSet = new HashSet<String>();

            for (int i = 0; i < lshVector.getDimension(); ++i) {

                Long lshValue = lshVector.getLSHValueByIndex(i);

                long c = 1; // start from 1

                // TODO: double check the connection method
                String k1 = 1 + "xx" + lshValue + "xx" + i;
                //long k2 = Long.parseLong(2 + "00" + lshValue + "00" + i);

                boolean notExist = false;

                //int tmp = 0;

                while (!notExist) {

                    long a = serverPosition(k1, c);
                    //long tag = Long.parseLong(c + "0000" + lshValue + "0" + i);

                    // if does not exist, directly insert
                    if (rawIndex.containsKey(a)) {

                        //System.out.println(k1);
                        //System.out.println(c);

                        String imageId = rawIndex.get(a);

                        //if (!idSet.contains(imageId)) {
                        if (!result.containsKey(imageId)) {

                            result.put(imageId, 1);
                        } else {
                            result.put(imageId, result.get(imageId) + 1);
                            //System.out.println(result.get(imageId));
                        }

                        //idSet.add(imageId);
                        //}
                    } else {
                        notExist = true;
                    }

                    ++c;
                }
            }
        }

        return result;
    }

    public static List<String> topK(int topK, HashMap<String, Integer> cc) {

        List<String> result = new ArrayList<String>();

        List<Map.Entry<String, Integer>> list = new ArrayList<Map.Entry<String, Integer>>();
        // 把map转化为Map.Entry然后放到用于排序的list里面
        list.addAll(cc.entrySet());
        // 调用内部类的构造器，如果这个内部类是静态内部类，就比这个好办点了。。
        CashIndex.ValueComparator mc = new ValueComparator();
        // 开始排序，传入比较器对象
        Collections.sort(list, mc);

        // 遍历在list中排序之后的HashMap
        for (Iterator<Map.Entry<String, Integer>> it = list.iterator(); it.hasNext(); ) {

            result.add(it.next().getKey());

            if (--topK <= 0) {
                break;
            }
        }

        return result;
    }

    private static class ValueComparator implements Comparator<Map.Entry<String, Integer>> {
        public int compare(Map.Entry<String, Integer> m, Map.Entry<String, Integer> n) {
            return (int) (n.getValue() - m.getValue());
        }
    }

    private int serverPosition(String k1Vj, long counter) {

        return (int) (PRF.HMACSHA256ToUnsignedInt(String.valueOf(counter), k1Vj));
    }
}
