package test;

import base.*;
import tool.BaseTool;
import tool.MathTool;
import tool.TimeUtil;

import java.io.*;
import java.util.*;

/**
 * Created by HarryC on 8/5/14.
 * <p/>
 * This function is used to test insert and search.
 */
public class staticTest {

    public static void main(String args[]) {

        String lshFileName = null;

        String bowFileName = null;

        short l = 0;

        int d = 0;

        int w = 0;

        short thresholdOfKick = 10;

        short counterLimit = 5;

        int limit = 1000000;

        //int insertSize = (int)(0.9 * limit);

        int loopSize = 0;

        double r = 0.0;

        int limitTruePositive = 100;

        String key1 = null;
        String key2 = null;

        //double loadFactor = 0.0;

        int times = 0;

        String sampleIndexFileName = "./sampleIndex.txt";

        if (args.length < 12) {
            System.err.println("Error: arguments are not enough! Please follow the format:\n\t[lsh file path] [bow file path] [L] [D] [R] [loadFactor] [thresholdOfKick] [counterLimit] [LIMIT] [key1] [key2] [times]");

            System.exit(Constant.ERROR_ARGUMENTS);
        } else {

            lshFileName = args[0];
            bowFileName = args[1];


            times = Integer.parseInt(args[11]);

            l = Short.parseShort(args[2]);
            d = Short.parseShort(args[3]);
            r = Double.parseDouble(args[4]);
            thresholdOfKick = Short.parseShort(args[6]);
            counterLimit = Short.parseShort(args[7]);
            loopSize = Integer.parseInt(args[8]);
            limit = Integer.parseInt(args[8]) * times;

            w = MathTool.getUpperPrimeNumber((int) (limit / l / Double.parseDouble(args[5])));

            //insertSize = (int)(0.9 * limit);

            key1 = args[9];
            key2 = args[10];


            System.out.println("Select prime W as " + w);
        }

        SecureIndex secureIndex = new SecureIndex(l, w, d, thresholdOfKick, counterLimit, limit, loopSize);
        //DynamicSecureIndex secureIndex = new DynamicSecureIndex(L, W, initialC, thresholdOfKick, counterLimit, limit, loopSize);

        InvertedIndex invertedIndex = new InvertedIndex(l);

        System.out.println("Initialize Secure Index and Inverted Index           ---> Done");

        if (counterLimit > 0) {
            System.out.println("Version: with counter");
        } else {
            System.out.println("Version: no counter");
        }

        ArrayList<InsertResult> insertResultList = new ArrayList<InsertResult>(limit);

        File file = new File(lshFileName);
        BufferedReader reader = null;
        BufferedWriter writer = null;

        try {
            System.out.print("Start reading files by line...\n");

            reader = new BufferedReader(new FileReader(file));

            String tempString;

            int lineNumber = 0;


            long startTime = System.currentTimeMillis();
            // read util null
            while ((tempString = reader.readLine()) != null) {

                LSHVector lshVector = new LSHVector(lineNumber, tempString.replace("\n", ""), l);

                secureIndex.lshVectors[lineNumber] = lshVector;

                //insertResultList.add(secureIndex.insertSecure(lineNumber, 0, 0, 0, key1));

                //System.out.println("Insert " + line + ", result type: " + insertResult.getType() + ", kick-away: " + insertResult.getNumOfKick());

                ++lineNumber;

                if (lineNumber == loopSize) {
                    break;
                }
            }


            for (int i = 1; i <= limit; ++i) {

                insertResultList.add(secureIndex.insertSecure(i, 0, 0, 0, key1));

                if (i % (limit / 100) == 0) {
                    System.out.println("Inserting " + i / (limit / 100) + "%");
                }
            }

            long insertTime = System.currentTimeMillis() - startTime;

            reader.close();

            long startTimeEncrypt = System.currentTimeMillis();

            secureIndex.encryptAllTable(key2); // encrypt all values

            long encryptTime = System.currentTimeMillis() - startTimeEncrypt;

            // -------------------------------------------------------------------------------------------------------
            System.out.print("     ---> Done\n\nProcessed " + lineNumber + " records!! Total cost " + (insertTime + encryptTime) + " ms\n\t\t\tInsert time " + insertTime + " ms\n\t\t\tEncryption time " + encryptTime + " ms\n\n\tTotal kick number: " + secureIndex.numOfKick + "\n\nWriting result into file...");

            writer = new BufferedWriter(new FileWriter("./insertResult.txt", true));

            int numOfDirectInsert = 0;
            int numOfKickAway = 0;
            int numOfFail = 0;
            int numOfCounterTry = 0;
            long totalNumOfTry = 0;


            writer.write("-.-.-.-.-.-.-.-.-.-.-.-.-.-.-.-.-.-.-.-.-.-.-.-.-.-.-.-.-.-.-.-.-.\n\nDSSSE Experiment - Building Secure Index:" +
                    "\n\n\tData set: " + lshFileName);

            if (counterLimit > 0) {
                writer.write("\n\n\tVersion: with counter, limited in " + counterLimit);
            } else {
                writer.write("\n\n\tVersion: no counter");
            }

            writer.write("\n\n\tTotal time cost: " + (insertTime + encryptTime) + " ms:" +
                    "\n\n\t\tInsert time: " + insertTime + " ms" +
                    "\n\t\tEncryption time: " + encryptTime + " ms" +
                    "\n\n\tSetting:\n\t\tL = " + l + ", initialC = " + d + ", W = " + w + ", threshold = " + thresholdOfKick +
                    "\n\n\tResult:");

            for (InsertResult oneItem : insertResultList) {

                totalNumOfTry += oneItem.getNumOfTry();

                switch (oneItem.getType()) {
                    case Constant.INSERT_DIRECT:
                        ++numOfDirectInsert;
                        break;
                    case Constant.INSERT_KICK:
                        ++numOfKickAway;
                        break;
                    case Constant.INSERT_COUNTER:
                        ++numOfCounterTry;
                        break;
                    case Constant.INSERT_FAIL:
                        ++numOfFail;
                        break;
                }
            }
            writer.write("\n\t\tLoad factor:                            " + (float) insertResultList.size() * 100 / (l * w));
            writer.write("%\n\t\tDirectly insert successfully:           " + numOfDirectInsert);
            writer.write("\n\t\tKick-away insert successfully:          " + numOfKickAway);
            writer.write("\n\t\tIncreasing counter insert successfully: " + numOfCounterTry);
            writer.write("\n\t\tInsert failed:                          " + numOfFail);
            writer.write("\n\t\tMaximum counter:                        " + secureIndex.getMaxC());

            writer.write("\n\t\tTotal kick number:                      " + secureIndex.numOfKick);

            writer.write("\n\n\t\tAverage try time:                       " + (float) totalNumOfTry / insertResultList.size());

            writer.write("\n\nInsert testing finished at " + TimeUtil.timeToString(Calendar.getInstance(), TimeUtil.TIME_FORMAT_YMD_HMS) + ".\n\n");

            writer.close();

            System.out.println("        ---> Done");

        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
            }
            if (writer != null) {
                try {
                    writer.close();
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
            }
        }

        // find good sample
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        boolean rootFlag = true;
        boolean bowLoaded = false;
        boolean invertedIndexBuilt = false;
        ArrayList<String> bowStrings = null;

        while (rootFlag) {
            System.out.print("\n\n----------------------- Root Menu -----------------------\n" +
                    "Please select an operation:\n" +
                    "[1]  ...\n" +
                    "[2]  load BOW file;\n" +
                    "[3]  query test;\n" +
                    "[4]  random sample test;\n" +
                    "[5]  find good LSH points;\n" +
                    "[6]  find good BOW points;\n" +
                    "[7]  build inverted index of LSH;\n" +
                    "[8]  test good points method 1;\n" +
                    //"[9]  insert;\n" +
                    //"[10] delete;\n" +
                    //"[11] batch insert;\n" +
                    //"[12] batch delete;\n" +
                    "[13] measure recall ratio;\n" +
                    "[14] test good points method 2;\n" +
                    "[15] measure inverted index;\n" +
                    "[QUIT] quit system.\n\n" +
                    "--->");
            String inputStr;
            int operationType;
            try {
                inputStr = br.readLine();

                try {
                    if (inputStr == null || inputStr.toLowerCase().equals("quit") || inputStr.toLowerCase().equals("q")) {

                        System.out.println("Quit!");

                        break;
                    } else if (Integer.parseInt(inputStr) > 15 || Integer.parseInt(inputStr) < 1) {

                        System.out.println("Warning: operation type should be limited in [1, 15], please try again!");

                        continue;
                    } else {
                        operationType = Integer.parseInt(inputStr);
                    }
                } catch (NumberFormatException e) {
                    System.out.println("Warning: operation type should be limited in [1, 15], please try again!");
                    continue;
                }

            } catch (IOException e) {
                e.printStackTrace();
                continue;
            }

            if (operationType == Constant.OPERATION_FIND_GOOD_LSH_POINT) {


                System.out.println("Model: find good LSH points. Please indicate the accuracy rate (double):");

                try {
                    Scanner scan = new Scanner(System.in);
                    double accuracyRate = scan.nextDouble();

                    int maxLoop = (limit < 100000 ? limit : 100000);
                    int sampleLimit = 1000;
                    int idx = 1;

                    System.out.println("Finding good LSH points whose accuracy rate is larger than " + accuracyRate);
                    ArrayList<Integer> goodPoints = new ArrayList<Integer>(sampleLimit);
                    while (idx <= maxLoop && goodPoints.size() < sampleLimit) {

                        if (secureIndex.analyseAccuracy(secureIndex.lshVectors[BaseTool.mapIndex(idx, loopSize)], key1, key2) > accuracyRate) {

                            goodPoints.add(idx);
                            System.out.print(idx + "\t");
                        }
                        ++idx;
                    }

                    System.out.println("\n\nThere are " + goodPoints.size() + " good points in " + idx + " points.");
                } catch (InputMismatchException ime) {
                    //ime.printStackTrace();
                    System.out.println("Error: please input a float value!");
                }

            } else if (operationType == Constant.OPERATION_LOAD_BOW_FILE) {

                if (!bowLoaded) {
                    System.out.println("\nModel: load BOW file.");

                    //ArrayList<BOWVector> bows = loadBOW(bowFileName, 10000, limit);
                    bowStrings = new ArrayList<String>(loopSize);

                    loadBOWString(bowFileName, bowStrings, loopSize);

                    bowLoaded = true;
                } else {
                    System.out.println("\nWarning: BOW file has been loaded!");
                }
            } else if (operationType == Constant.OPERATION_QUERY) {

                System.out.println("\nModel: query point.");

                //System.out.println("bows.size() = " + bows.size());

                //BufferedReader br2 = new BufferedReader(new InputStreamReader(System.in));

                while (true) {
                    System.out.println("Now, you can search by input you query index range from [1, " + limit + "]: (-1 means return to root menu)");

                    String queryStr = null;
                    int queryIndex;

                    try {
                        queryStr = br.readLine();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                    try {
                        if (queryStr == null || queryStr.equals("-1")) {

                            System.out.println("Return to root menu!");

                            break;
                        } else if (Integer.parseInt(queryStr) > limit || Integer.parseInt(queryStr) <= 0) {

                            System.out.println("Warning: query index should be limited in [1, limit]");

                            continue;
                        } else {
                            queryIndex = Integer.parseInt(queryStr);

                            System.out.println("For query lsh vector index: " + queryIndex);
                        }
                    } catch (NumberFormatException e) {
                        System.out.println("Warning: query index should be limited in [1, limit]");
                        continue;
                    }

                    ArrayList<LSHVector> searchResult = secureIndex.searchForTruePositive(secureIndex.lshVectors[BaseTool.mapIndex(queryIndex, loopSize)], key1, key2);

                    HashSet<Integer> invertedIndexIdSet = null;


                    if (invertedIndexBuilt) {
                        invertedIndexIdSet = invertedIndex.search(secureIndex.lshVectors[BaseTool.mapIndex(queryIndex, loopSize)]); // id is from 0

                        /*
                        double recallRatio = (double) searchResult.size() / (double) invertedIndexIdSet.size();

                        System.out.println("\nRecall ratio is  " + recallRatio);
                        */
                    }

                    int correctBow = 0;
                    int faultBow = 0;
                    int correctInvertedIndex = 0;
                    int faultInvertedIndex = 0;

                    if (searchResult != null && searchResult.size() > 0) {
                        System.out.println("There are " + searchResult.size() + " similar item(s):");
                        for (LSHVector oneItem : searchResult) {
                            System.out.print(oneItem.getId() + 1 + "\t");

                            if (bowLoaded) {
                                //if (calculateDistance(bows.get(queryIndex), bows.get(oneItem.getId())) <= R) {
                                if (MathTool.calculateDistance(BaseTool.mapIndex(queryIndex, loopSize), bowStrings.get(BaseTool.mapIndex(queryIndex, loopSize)), BaseTool.mapIndex(oneItem.getId() + 1, loopSize), bowStrings.get(BaseTool.mapIndex(oneItem.getId() + 1, loopSize)), 10000) <= r) {

                                    ++correctBow;
                                } else {
                                    ++faultBow;
                                }
                            }

                            if (invertedIndexBuilt) {
                                if (invertedIndexIdSet.contains(oneItem.getId() + 1)) {

                                    ++correctInvertedIndex;
                                } else {
                                    ++faultInvertedIndex;
                                }
                            }
                        }
                        System.out.println("\n---> For lsh test:");
                        System.out.println(secureIndex.analyse(secureIndex.lshVectors[BaseTool.mapIndex(queryIndex, loopSize)], key1, key2));

                        if (bowLoaded) {
                            System.out.println("---> For BOW distance test:\nNumber of correct items       : " + correctBow + "\nNumber of fault items         : " + faultBow + "\nBOW accuracy rate             : " + (double) correctBow / searchResult.size());

                            if (invertedIndexBuilt) {
                                System.out.println("---> For comparing with inverted index test:\nNumber of correct items       : " + correctInvertedIndex + "\nNumber of fault items         : " + faultInvertedIndex + "\nBOW accuracy rate             : " + (double) correctInvertedIndex / searchResult.size());

                                //int minK = (searchResult.size() < invertedIndexIdSet.size() ? searchResult.size() : invertedIndexIdSet.size());

                                // sort the distances

                                LSHVector query = secureIndex.lshVectors[BaseTool.mapIndex(queryIndex, loopSize)];

                                ArrayList<Double> secureDisList = new ArrayList<Double>();

                                ArrayList<Double> invertedDisList = new ArrayList<Double>();

                                // Step 1: sort distance in secure index
                                for (LSHVector oneItem : searchResult) {

                                    double e2Distance = MathTool.calculateDistance(query.getId(), bowStrings.get(query.getId()), oneItem.getId(), bowStrings.get(oneItem.getId()), 10000);

                                    if (e2Distance == 0.0) {
                                        continue;
                                    }

                                    sortInsert(e2Distance, secureDisList);
                                }

                                // Step 2: sort distance in inverted index
                                for (Integer oneItem : invertedIndexIdSet) {

                                    double e2Distance = MathTool.calculateDistance(query.getId(), bowStrings.get(query.getId()), BaseTool.mapIndex(oneItem, loopSize), bowStrings.get(BaseTool.mapIndex(oneItem, loopSize)), 10000);

                                    if (e2Distance == 0.0) {
                                        continue;
                                    }

                                    sortInsert(e2Distance, invertedDisList);
                                }

                                int minK = (secureDisList.size() < invertedDisList.size() ? secureDisList.size() : invertedDisList.size());

                                System.out.println("\nThe size of secure index is : " + secureDisList.size() + ", the size of inverted index is : " + invertedDisList.size());

                                ArrayList<Double> distOfPure = calculateTopKDistanceForAll(query, minK, limit, loopSize, bowStrings);

                                double sumSecure = 0.0;
                                double sumInverted = 0.0;
                                double sumPure = 0.0;

                                //int limit = (secureDisList.size() < invertedDisList.size() ? secureDisList.size() : invertedDisList.size());

                                for (int i = 0; i < minK; ++i) {

                                    sumSecure += secureDisList.get(i);
                                    sumInverted += invertedDisList.get(i);
                                    sumPure += distOfPure.get(i);

                                    System.out.println(secureDisList.get(i) + " -- " + invertedDisList.get(i) + " -- " + distOfPure.get(i));

                                    if (i == 4) {
                                        System.out.println("\t\tFor top k = 5, our accuracy is " + sumPure / sumSecure + ", inverted accuracy is " + sumPure / sumInverted);
                                    } else if (i == 9) {
                                        System.out.println("\t\tFor top k = 10, our accuracy is " + sumPure / sumSecure + ", inverted accuracy is " + sumPure / sumInverted);
                                    } else if (i == 14) {
                                        System.out.println("\t\tFor top k = 15, our accuracy is " + sumPure / sumSecure + ", inverted accuracy is " + sumPure / sumInverted);
                                    } else if (i == 19) {
                                        System.out.println("\t\tFor top k = 20, our accuracy is " + sumPure / sumSecure + ", inverted accuracy is " + sumPure / sumInverted);
                                    } else if (i == 29) {
                                        System.out.println("\t\tFor top k = 30, our accuracy is " + sumPure / sumSecure + ", inverted accuracy is " + sumPure / sumInverted);
                                    } /*else if(i == 39) {
                                        System.out.println("\t\tFor top k = 40, our accuracy is " + sumPure/sumSecure + ", inverted accuracy is " + sumPure/sumInverted);
                                    } else if(i == 49) {
                                        System.out.println("\t\tFor top k = 50, our accuracy is " + sumPure/sumSecure + ", inverted accuracy is " + sumPure/sumInverted);
                                    }*/
                                }


                                /*

                                if (minK >= 5) {
                                    double accuracy = calculateGroundTruth(secureIndex.lshVectors[BaseTool.mapIndex(queryIndex, loopSize)], searchResult, invertedIndexIdSet, bowStrings, limit, loopSize, 5);

                                    System.out.println("\t\tFor top k = 5, the accuracy is " + accuracy);
                                }
                                if (minK >= 10) {
                                    double accuracy = calculateGroundTruth(secureIndex.lshVectors[BaseTool.mapIndex(queryIndex, loopSize)], searchResult, invertedIndexIdSet, bowStrings, limit, loopSize, 10);

                                    System.out.println("\t\tFor top k = 10, the accuracy is " + accuracy);
                                }
                                if (minK >= 20) {
                                    double accuracy = calculateGroundTruth(secureIndex.lshVectors[BaseTool.mapIndex(queryIndex, loopSize)], searchResult, invertedIndexIdSet, bowStrings, limit, loopSize, 20);

                                    System.out.println("\t\tFor top k = 20, the accuracy is " + accuracy);
                                }
                                if (minK >= 30) {
                                    double accuracy = calculateGroundTruth(secureIndex.lshVectors[BaseTool.mapIndex(queryIndex, loopSize)], searchResult, invertedIndexIdSet, bowStrings, limit, loopSize, 30);

                                    System.out.println("\t\tFor top k = 30, the accuracy is " + accuracy);
                                }
                                if (minK >= 40) {
                                    double accuracy = calculateGroundTruth(secureIndex.lshVectors[BaseTool.mapIndex(queryIndex, loopSize)], searchResult, invertedIndexIdSet, bowStrings, limit, loopSize, 40);

                                    System.out.println("\t\tFor top k = 40, the accuracy is " + accuracy);
                                }
                                if (minK >= 50) {
                                    double accuracy = calculateGroundTruth(secureIndex.lshVectors[BaseTool.mapIndex(queryIndex, loopSize)], searchResult, invertedIndexIdSet, bowStrings, limit, loopSize, 50);

                                    System.out.println("\t\tFor top k = 50, the accuracy is " + accuracy);
                                }
                                */
                            }
                        }
                    } else {
                        System.out.println("No similar item!!!");
                    }
                }
            } else if (operationType == Constant.OPERATION_RANDOM_SAMPLE) {


                System.out.println("Model: random sample test. Please indicate the number of samples: ");

                try {
                    Scanner scan = new Scanner(System.in);
                    int numOfSample = scan.nextInt();

                    System.out.println("For " + numOfSample + "samples:");

                    ArrayList<LSHVector> searchResult;

                    HashSet<Integer> invertedIndexIdSet = null;

                    long correctBow = 0;
                    long faultBow = 0;
                    long numOfSearchable = 0;

                    long correctInvertedIndex = 0;
                    long faultInvertedIndex = 0;

                    double lshAccuracy = 0.0;

                    int numOfOurs = 0;
                    int numOfInverted = 0;

                    Random randomGenerator = new Random();

                    int idx = randomGenerator.nextInt(limit) + 1; // range from 1 to limit

                    double recallRatio = 0.0;


                    for (int i = 0; i < numOfSample; ++i) {

                        //searchResult = secureIndex.search(lshVectors.get(idx));
                        searchResult = secureIndex.searchForTruePositive(secureIndex.lshVectors[BaseTool.mapIndex(idx, loopSize)], key1, key2);

                        if (invertedIndexBuilt) {
                            invertedIndexIdSet = invertedIndex.search(secureIndex.lshVectors[BaseTool.mapIndex(idx, loopSize)]);

                            //recallRatio += (double) searchResult.size() / (double) invertedIndexIdSet.size();
                        }


                        numOfOurs += searchResult.size();

                        if (invertedIndexBuilt) {
                            numOfInverted += invertedIndexIdSet.size();
                        }


                        if (searchResult != null && searchResult.size() > 0) {

                            ++numOfSearchable;
                            if (bowLoaded) {
                                for (LSHVector oneItem : searchResult) {


                                    //if (calculateDistance(bows.get(queryIndex), bows.get(oneItem.getId())) <= R) {
                                    if (MathTool.calculateDistance(BaseTool.mapIndex(idx, loopSize), bowStrings.get(BaseTool.mapIndex(idx, loopSize)), BaseTool.mapIndex(oneItem.getId(), loopSize), bowStrings.get(BaseTool.mapIndex(oneItem.getId(), loopSize)), 10000) <= r) {

                                        ++correctBow;
                                    } else {
                                        ++faultBow;
                                    }

                                    if (invertedIndexBuilt) {
                                        if (invertedIndexIdSet.contains(oneItem.getId())) {

                                            ++correctInvertedIndex;
                                        } else {
                                            ++faultInvertedIndex;
                                        }
                                    }
                                }
                            }

                            lshAccuracy += secureIndex.analyseAccuracy(secureIndex.lshVectors[BaseTool.mapIndex(idx, loopSize)], key1, key2);
                        }

                        idx = randomGenerator.nextInt(limit) + 1;
                        searchResult.clear();
                    }

                    System.out.println("\nAverage size of secure index is :" + (double) numOfOurs / numOfSample);
                    System.out.println("\nAverage size of inverted index is :" + (double) numOfInverted / numOfSample);

                    //System.out.println("\nAverage recall ratio is  " + recallRatio / numOfSample);
                    System.out.println("\nAverage similar items are     : " + (double) numOfSearchable / numOfSample);
                    System.out.println("Average lsh accuracy rate is  : " + (double) lshAccuracy / numOfSample);
                    if (bowLoaded) {
                        System.out.println("Average bow accuracy rate is  : " + (double) correctBow / (correctBow + faultBow));
                    }
                    if (invertedIndexBuilt) {
                        System.out.println("Average inverted index correct rate is  : " + (double) correctInvertedIndex / (correctInvertedIndex + faultInvertedIndex));
                    }
                } catch (InputMismatchException ime) {
                    //ime.printStackTrace();
                    System.out.println("Error: please input an integer value!");
                }
            } else if (operationType == Constant.OPERATION_FIND_GOOD_BOW_POINT) {

                if (!bowLoaded) {
                    System.out.println("\nError: please load BOW file first!");
                } else {

                    System.out.println("Model: find good BOW points. Please indicate the accuracy rate (double):");

                    try {
                        Scanner scan = new Scanner(System.in);
                        double accuracyRate = scan.nextDouble();

                        System.out.println("Please indicate the number of sample:");
                        scan = new Scanner(System.in);
                        int sampleLimit = scan.nextInt();

                        System.out.println("Please indicate the limited number of TRUE POSITIVE:");
                        scan = new Scanner(System.in);
                        limitTruePositive = scan.nextInt();

                        writer = new BufferedWriter(new FileWriter(sampleIndexFileName, false));

                        int maxLoop = (limit < 1000000 ? limit : 1000000);
                        //int sampleLimit = 1000;
                        int idx = 1;

                        System.out.println("Finding good BOW points whose accuracy rate is larger than " + accuracyRate);
                        ArrayList<Integer> goodPoints = new ArrayList<Integer>(sampleLimit);
                        while (idx <= maxLoop && goodPoints.size() < sampleLimit) {

                            //ArrayList<LSHVector> searchResult = secureIndex.search(lshVectors.get(idx));
                            ArrayList<LSHVector> searchResult = secureIndex.searchForTruePositive(secureIndex.lshVectors[BaseTool.mapIndex(idx, loopSize)], key1, key2);

                            int correct = 0;

                            if (searchResult != null && searchResult.size() > limitTruePositive) {
                                for (LSHVector oneItem : searchResult) {
                                    if (MathTool.calculateDistance(BaseTool.mapIndex(idx, loopSize), bowStrings.get(BaseTool.mapIndex(idx, loopSize)), BaseTool.mapIndex(oneItem.getId(), loopSize), bowStrings.get(BaseTool.mapIndex(oneItem.getId(), loopSize)), 10000) <= r) {

                                        ++correct;
                                    }
                                }
                            }

                            if ((double) correct / searchResult.size() > accuracyRate) {

                                goodPoints.add(idx);
                                System.out.print(idx + "\t");
                                writer.write(idx + "\n");
                            }
                            ++idx;
                        }

                        writer.close();
                        System.out.println("\n\nThere are " + goodPoints.size() + " good points in " + idx + " points.");
                    } catch (InputMismatchException ime) {
                        //ime.printStackTrace();
                        System.out.println("Error: please input a float value!");
                    } catch (IOException e) {
                        System.out.println("Error: file can not be opened!");
                    }
                }

            } else if (operationType == Constant.OPERATION_BUILD_INVERTED_INDEX) {

                System.out.println("Start building inverted index table...");

                for (int i = 1; i <= limit; ++i) {
                    invertedIndex.insertInvertedIndex(secureIndex.lshVectors[BaseTool.mapIndex(i, loopSize)], i);

                    if (i % (limit / 100) == 0) {
                        System.out.println("Inserting " + i / (limit / 100) + "%");
                    }
                }
                invertedIndexBuilt = true;
                System.out.println("Done.");

            } else if (operationType == Constant.OPERATION_TEST_AVERAGE_BOW_ACCUARY) {

                if (!bowLoaded) {
                    System.out.println("\nError: please load BOW file first!");
                } else {

                    System.out.println("Model: test average box accuracy based on selected index.");
                    try {
                        System.out.println("Please indicate the limited number of TRUE POSITIVE:");
                        Scanner scan = new Scanner(System.in);
                        limitTruePositive = scan.nextInt();

                        System.out.print("Now, processing ");
                        reader = new BufferedReader(new FileReader(sampleIndexFileName));

                        String tempString;
                        double bowDistanceAvgAccuracy = 0.0;
                        double invertedIndexAccuracy = 0.0;

                        double acc5 = 0.0, acc10 = 0.0, acc15 = 0.0, acc20 = 0.0, acc30 = 0.0, acc40 = 0.0, acc50 = 0.0;
                        double acci5 = 0.0, acci10 = 0.0, acci15 = 0.0, acci20 = 0.0, acci30 = 0.0, acci40 = 0.0, acci50 = 0.0;

                        int lineNumber = 0;

                        // read util null
                        while ((tempString = reader.readLine()) != null) {

                            int idx = Integer.parseInt(tempString);

                            ArrayList<LSHVector> searchResult = secureIndex.searchForTruePositive(secureIndex.lshVectors[BaseTool.mapIndex(idx, loopSize)], key1, key2);

                            HashSet<Integer> invertedIndexIdSet = null;

                            if (invertedIndexBuilt) {
                                invertedIndexIdSet = invertedIndex.search(secureIndex.lshVectors[BaseTool.mapIndex(idx, loopSize)]); // id is from 0
                            }

                            int correctBow = 0;
                            int correctInvertedIndex = 0;

                            if (searchResult != null && searchResult.size() > limitTruePositive) {
                                for (LSHVector oneItem : searchResult) {
                                    if (MathTool.calculateDistance(BaseTool.mapIndex(idx, loopSize), bowStrings.get(BaseTool.mapIndex(idx, loopSize)), BaseTool.mapIndex(oneItem.getId() + 1, loopSize), bowStrings.get(BaseTool.mapIndex(oneItem.getId() + 1, loopSize)), 10000) <= r) {

                                        ++correctBow;
                                    }

                                    if (invertedIndexBuilt) {
                                        if (invertedIndexIdSet.contains(oneItem.getId() + 1)) {

                                            ++correctInvertedIndex;
                                        }
                                    }
                                }
                            }


                            bowDistanceAvgAccuracy += (double) correctBow / searchResult.size();


                            if (invertedIndexBuilt) {

                                invertedIndexAccuracy += (double) correctInvertedIndex / searchResult.size();

                                //int minK = (searchResult.size() < invertedIndexIdSet.size() ? searchResult.size() : invertedIndexIdSet.size());

                                LSHVector query = secureIndex.lshVectors[BaseTool.mapIndex(idx, loopSize)];

                                ArrayList<Double> secureDisList = new ArrayList<Double>();

                                ArrayList<Double> invertedDisList = new ArrayList<Double>();

                                // Step 1: sort distance in secure index
                                for (LSHVector oneItem : searchResult) {

                                    double e2Distance = MathTool.calculateDistance(query.getId(), bowStrings.get(query.getId()), oneItem.getId(), bowStrings.get(oneItem.getId()), 10000);

                                    if (e2Distance == 0.0) {
                                        continue;
                                    }

                                    sortInsert(e2Distance, secureDisList);
                                }

                                // Step 2: sort distance in inverted index
                                for (Integer oneItem : invertedIndexIdSet) {

                                    double e2Distance = MathTool.calculateDistance(query.getId(), bowStrings.get(query.getId()), BaseTool.mapIndex(oneItem, loopSize), bowStrings.get(BaseTool.mapIndex(oneItem, loopSize)), 10000);

                                    if (e2Distance == 0.0) {
                                        continue;
                                    }

                                    sortInsert(e2Distance, invertedDisList);
                                }

                                int minK = (secureDisList.size() < invertedDisList.size() ? secureDisList.size() : invertedDisList.size());

                                System.out.println("\nThe size of secure index is : " + secureDisList.size() + ", the size of inverted index is : " + invertedDisList.size());

                                ArrayList<Double> distOfPure = calculateTopKDistanceForAll(query, minK, limit, loopSize, bowStrings);

                                double sumSecure = 0.0;
                                double sumInverted = 0.0;
                                double sumPure = 0.0;

                                //int limit = (secureDisList.size() < invertedDisList.size() ? secureDisList.size() : invertedDisList.size());

                                for (int i = 0; i < minK; ++i) {

                                    sumSecure += secureDisList.get(i);
                                    sumInverted += invertedDisList.get(i);
                                    sumPure += distOfPure.get(i);

                                    System.out.println(secureDisList.get(i) + " -- " + invertedDisList.get(i) + " -- " + distOfPure.get(i));

                                    if (i == 4) {

                                        acc5 += sumPure / sumSecure;
                                        acci5 += sumPure / sumInverted;

                                    } else if (i == 9) {
                                        acc10 += sumPure / sumSecure;
                                        acci10 += sumPure / sumInverted;
                                    } else if (i == 14) {
                                        acc15 += sumPure / sumSecure;
                                        acci15 += sumPure / sumInverted;
                                    } else if (i == 19) {
                                        acc20 += sumPure / sumSecure;
                                        acci20 += sumPure / sumInverted;
                                    } else if (i == 29) {
                                        acc30 += sumPure / sumSecure;
                                        acci30 += sumPure / sumInverted;
                                    } /*else if(i == 39) {
                                        acc40 += sumPure/sumSecure;
                                        acci40 += sumPure/sumInverted;
                                    } else if(i == 49) {
                                        acc50 += sumPure/sumSecure;
                                        acci50 += sumPure/sumInverted;
                                    }*/
                                }
                            }

                            ++lineNumber;

                            System.out.print(".");
                        }

                        System.out.println("\n\nThe average accuracy of selected index are " + bowDistanceAvgAccuracy / lineNumber);
                        if (invertedIndexBuilt) {
                            System.out.println("\n\nThe average accuracy of secure index and inverted index " + invertedIndexAccuracy / lineNumber);

                            System.out.println("\n\t\tFor top k = 5, our accuracy is " + acc5 / lineNumber);
                            System.out.println("\t\tFor top k = 10, our accuracy is " + acc10 / lineNumber);
                            System.out.println("\t\tFor top k = 15, our accuracy is " + acc15 / lineNumber);
                            System.out.println("\t\tFor top k = 20, our accuracy is " + acc20 / lineNumber);
                            System.out.println("\t\tFor top k = 30, our accuracy is " + acc30 / lineNumber);
                            //System.out.println("\t\tFor top k = 40, our accuracy is " + acc40 / lineNumber);
                            //System.out.println("\t\tFor top k = 50, our accuracy is " + acc50 / lineNumber);

                            System.out.println("\n\n\t\tFor top k = 5, inverted accuracy is " + acci5 / lineNumber);
                            System.out.println("\t\tFor top k = 10, inverted accuracy is " + acci10 / lineNumber);
                            System.out.println("\t\tFor top k = 15, inverted accuracy is " + acci15 / lineNumber);
                            System.out.println("\t\tFor top k = 20, inverted accuracy is " + acci20 / lineNumber);
                            System.out.println("\t\tFor top k = 30, inverted accuracy is " + acci30 / lineNumber);
                            //System.out.println("\t\tFor top k = 40, inverted accuracy is " + acci40 / lineNumber);
                            //System.out.println("\t\tFor top k = 50, inverted accuracy is " + acci50 / lineNumber);
                        }
                    } catch (InputMismatchException ime) {
                        //ime.printStackTrace();
                        System.out.println("Error: please input a float value!");
                    } catch (FileNotFoundException e) {
                        System.out.println("Error: file can not be found!");
                    } catch (IOException e) {
                        System.out.println("Error: file can not be opened!");
                    }
                }

            } else if (operationType == Constant.OPERATION_TEST_RECALL_RATIO) {

                if (!invertedIndexBuilt) {
                    System.out.println("\nError: please build inverted index first!");
                } else {

                    System.out.println("Model: test recall ratio based on selected index.");
                    try {

                        System.out.print("Now, processing ");
                        reader = new BufferedReader(new FileReader(sampleIndexFileName));

                        String tempString;
                        double recallRatio = 0.0;

                        int lineNumber = 0;

                        // read util null
                        while ((tempString = reader.readLine()) != null) {

                            int idx = Integer.parseInt(tempString);

                            ArrayList<LSHVector> searchResult = secureIndex.searchForTruePositive(secureIndex.lshVectors[BaseTool.mapIndex(idx, loopSize)], key1, key2);

                            HashSet<Integer> invertedIndexIdSet = null;

                            invertedIndexIdSet = invertedIndex.search(secureIndex.lshVectors[BaseTool.mapIndex(idx, loopSize)]);

                            recallRatio += (double) searchResult.size() / (double) invertedIndexIdSet.size();

                            ++lineNumber;

                            System.out.print(".");
                        }

                        System.out.println("\n\nThe average recall ratio is " + recallRatio / lineNumber);

                    } catch (InputMismatchException ime) {
                        //ime.printStackTrace();
                        System.out.println("Error: please input a float value!");
                    } catch (FileNotFoundException e) {
                        System.out.println("Error: file can not be found!");
                    } catch (IOException e) {
                        System.out.println("Error: file can not be opened!");
                    }
                }

            } else if (operationType == Constant.OPERATION_TEST_ERROR_RATIO) {
                if (!bowLoaded) {
                    System.out.println("\nError: please load bow file first!");
                } else {

                    System.out.println("Model: test error ratio based on selected index.");
                    try {

                        System.out.print("Now, processing ");
                        reader = new BufferedReader(new FileReader(sampleIndexFileName));

                        String tempString;

                        double errorRatio = 0.0;

                        int lineNumber = 0;

                        int totalNum = 0;

                        // read util null
                        while ((tempString = reader.readLine()) != null) {

                            int idx = Integer.parseInt(tempString);

                            ArrayList<LSHVector> searchResult = secureIndex.searchForTruePositive(secureIndex.lshVectors[BaseTool.mapIndex(idx, loopSize)], key1, key2);

                            ArrayList<Double> distOfSearch = calculateDistanceForAnQuery(secureIndex.lshVectors[BaseTool.mapIndex(idx, loopSize)], searchResult, bowStrings);

                            int topK = distOfSearch.size();

                            ArrayList<Double> distOfPure = calculateTopKDistanceForAll(secureIndex.lshVectors[BaseTool.mapIndex(idx, loopSize)], topK, limit, loopSize, bowStrings);


                            for (int i = 0; i < topK; ++i) {
                                errorRatio += distOfSearch.get(i) / distOfPure.get(i);
                            }

                            totalNum += topK;
                            ++lineNumber;

                            System.out.print(".");
                        }

                        System.out.println("\n\nThe average error ratio is " + errorRatio / totalNum);

                    } catch (InputMismatchException ime) {
                        //ime.printStackTrace();
                        System.out.println("Error: please input a float value!");
                    } catch (FileNotFoundException e) {
                        System.out.println("Error: file can not be found!");
                    } catch (IOException e) {
                        System.out.println("Error: file can not be opened!");
                    }
                }
            } else if (operationType == Constant.OPERATION_SUMMARIZE_INVERTED_INDEX) {

                if (!invertedIndexBuilt) {
                    System.out.println("\nError: please build inverted index first! ");
                } else {

                    try {
                        System.out.println("Please indicate the summary step, e.g., 1000:");
                        Scanner scan = new Scanner(System.in);
                        int step = scan.nextInt();

                        System.out.print("Now, processing ");

                        TreeMap<Integer, Long> sumMap = new TreeMap<Integer, Long>();

                        for (int i = 0; i < l; ++i) {

                            Iterator<Long> it = invertedIndex.getIndexTable().get(i).keySet().iterator();
                            while (it.hasNext()) {

                                long key = it.next();

                                int id = invertedIndex.getIndexTable().get(i).get(key).size() / step;

                                if (sumMap.containsKey(id)) {

                                    sumMap.put(id, sumMap.get(id) + 1);
                                } else {
                                    sumMap.put(id, new Long(1));
                                }
                            }
                        }

                        System.out.println("\nThe following is the summary of inverted index:");

                        Iterator<Integer> it = sumMap.keySet().iterator();
                        while (it.hasNext()) {

                            int key = it.next();

                            System.out.println("\tRange from " + key * step + " to " + (key + 1) * step + " : " + sumMap.get(key));
                        }


                    } catch (InputMismatchException ime) {
                        //ime.printStackTrace();
                        System.out.println("Error: please input a float value!");
                    }
                }

                System.out.println("Done.");
            }
        }
        try {
            br.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void loadBOWString(String fileName, ArrayList<String> bowStrings, int limit) {

        File file = new File(fileName);
        BufferedReader reader = null;

        try {
            System.out.print("\nStart reading BoW by line...");

            reader = new BufferedReader(new FileReader(file));

            String tempString;

            int lineNumber = 0;

            long startTime = System.currentTimeMillis();
            // read util null
            while ((tempString = reader.readLine()) != null) {

                bowStrings.add(tempString);

                ++lineNumber;
                //System.out.println(lineNumber);
                if (lineNumber == limit) {
                    break;
                }
            }

            System.out.println("   done, cost time:" + (System.currentTimeMillis() - startTime) + " ms");

            reader.close();

        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
            }
        }
    }

    public static ArrayList<BOWVector> loadBOW(String fileName, int dimension, int limit) {

        ArrayList<BOWVector> bows = new ArrayList<BOWVector>(limit);

        File file = new File(fileName);
        BufferedReader reader = null;

        try {
            System.out.print("\nStart reading BoW by line...");

            reader = new BufferedReader(new FileReader(file));

            String tempString;

            int lineNumber = 0;

            long startTime = System.currentTimeMillis();
            // read util null
            while ((tempString = reader.readLine()) != null) {

                bows.add(new BOWVector(lineNumber, tempString.replace("\n", ""), dimension));

                //System.out.println("Insert " + line + ", result type: " + insertResult.getType() + ", kick-away: " + insertResult.getNumOfKick());

                ++lineNumber;
                //System.out.println(lineNumber);
                if (lineNumber == limit) {
                    break;
                }
            }

            System.out.println("   done, cost time:" + (System.currentTimeMillis() - startTime) + " ms");

            reader.close();

        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
            }
        }

        return bows;
    }

    public static double calculateDistance(BOWVector bow1, BOWVector bow2) {

        double distance = 0.0;

        HashSet<Integer> keys = new HashSet<Integer>();
        Iterator<Integer> it = bow1.getValuesMap().keySet().iterator();

        while (it.hasNext()) {
            keys.add(it.next());
        }

        it = bow2.getValuesMap().keySet().iterator();

        while (it.hasNext()) {
            keys.add(it.next());
        }

        for (Integer index : keys) {

            if (bow1.getValuesMap().containsKey(index) && bow2.getValuesMap().containsKey(index)) {
                distance += Math.pow((bow1.getValuesMap().get(index) - bow2.getValuesMap().get(index)), 2);
            } else if (bow1.getValuesMap().containsKey(index)) {
                distance += Math.pow(bow1.getValuesMap().get(index), 2);
            } else if (bow2.getValuesMap().containsKey(index)) {
                distance += Math.pow(bow2.getValuesMap().get(index), 2);
            }
        }

        return Math.pow(distance, 0.5);
    }

    public static ArrayList<Double> calculateGroundTruth(LSHVector query, HashSet<LSHVector> secureList, HashSet<Integer> invertedList, ArrayList<String> bowStrings, int limit, int loopSize, int topK) {

        ArrayList<Double> result = new ArrayList<Double>(2);

        ArrayList<Double> secureDisList = new ArrayList<Double>(topK);

        ArrayList<Double> invertedDisList = new ArrayList<Double>(topK);

        // Step 1: sort distance in secure index
        for (LSHVector oneItem : secureList) {

            double e2Distance = MathTool.calculateDistance(query.getId() - 1, bowStrings.get(query.getId() - 1), oneItem.getId() - 1, bowStrings.get(oneItem.getId() - 1), 10000);

            sortInsert(e2Distance, secureDisList);
        }

        // Step 2: sort distance in inverted index
        for (Integer oneItem : invertedList) {

            double e2Distance = MathTool.calculateDistance(query.getId() - 1, bowStrings.get(query.getId() - 1), oneItem - 1, bowStrings.get(oneItem - 1), 10000);

            sortInsert(e2Distance, invertedDisList);
        }


        ArrayList<Double> distOfPure = calculateTopKDistanceForAll(query, topK, limit, loopSize, bowStrings);

        double sumSecure = 0.0;
        double sumInverted = 0.0;
        double sumPure = 0.0;

        //int limit = (secureDisList.size() < invertedDisList.size() ? secureDisList.size() : invertedDisList.size());

        for (int i = 0; i < topK; ++i) {

            sumSecure += secureDisList.get(i);
            sumInverted += invertedDisList.get(i);
            sumPure += distOfPure.get(i);
        }

        result.add(sumPure / sumSecure);
        result.add(sumPure / sumInverted);
        return result;
    }

    public static ArrayList<Double> calculateDistanceForAnQuery(LSHVector query, ArrayList<LSHVector> searchResult, ArrayList<String> bowStrings) {

        ArrayList<Double> result = new ArrayList<Double>(searchResult.size());

        TreeMap<Double, Integer> distMap = new TreeMap<Double, Integer>();

        for (Iterator<LSHVector> it = searchResult.iterator(); it.hasNext(); ) {

            LSHVector oneItem = it.next();

            double bowDist = MathTool.calculateDistance(query.getId() - 1, bowStrings.get(query.getId() - 1), oneItem.getId() - 1, bowStrings.get(oneItem.getId() - 1), 10000);

            if (bowDist == 0.0) {
                continue;
            }

            if (!distMap.containsKey(bowDist)) {

                distMap.put(bowDist, 1);
            } else {
                distMap.put(bowDist, distMap.get(bowDist) + 1);
            }
        }

        Iterator<Double> it = distMap.keySet().iterator();
        while (it.hasNext()) {

            double key = it.next();

            int times = distMap.get(key);
            for (int i = 0; i < times; ++i) {

                result.add(key);
            }
        }

        return result;
    }

    public static ArrayList<Double> calculateTopKDistanceForAll(LSHVector query, int k, int limit, int loopSize, ArrayList<String> bowStrings) {

        ArrayList<Double> result = new ArrayList<Double>(k);

        TreeMap<Double, Integer> distMap = new TreeMap<Double, Integer>();

        int loopNum = limit / loopSize;

        for (int i = 1; i <= loopSize; ++i) {

            double bowDist = MathTool.calculateDistance(query.getId(), bowStrings.get(query.getId()), BaseTool.mapIndex(i, loopSize), bowStrings.get(BaseTool.mapIndex(i, loopSize)), 10000);

            if (bowDist == 0.0) {
                continue;
            }

            if (!distMap.containsKey(bowDist)) {

                distMap.put(bowDist, 1);
            } else {
                distMap.put(bowDist, distMap.get(bowDist) + 1);
            }
        }

        Iterator<Double> it = distMap.keySet().iterator();
        while (it.hasNext()) {

            double key = it.next();

            int times = distMap.get(key) * loopSize;
            for (int i = 0; i < times; ++i) {

                result.add(key);
            }

            if (result.size() >= k) {
                break;
            }
        }
        return result;
    }

    private static void sortInsert(double value, ArrayList<Double> dataList) {

        boolean flag = false;

        if (dataList.size() == 0) {
            dataList.add(value);
            flag = true;
        } else {

            for (int i = 0; i < dataList.size(); ++i) {

                if (value < dataList.get(i)) {
                    dataList.add(i, value);
                    flag = true;
                    break;
                }
            }
        }

        if (!flag) {
            dataList.add(dataList.size(), value);
        }
    }
}
