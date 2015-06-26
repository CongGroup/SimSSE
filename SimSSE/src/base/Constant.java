package base;

/**
 * Created by HarryC on 8/5/14.
 *
 * This class is used to define all the constant values.
 */
public class Constant {

    // Error
    public static final int ERROR_ARGUMENTS = 2;


    // Insert result
    public static final short INSERT_DIRECT = 1;

    public static final short INSERT_KICK = 2;

    public static final short INSERT_COUNTER = 3;

    public static final short INSERT_FAIL = 0;


    //
    public static final int OPERATION_LOAD_QUERY_FILE = 2;
    public static final int OPERATION_LOAD_BOW_FILE = 2;

    public static final int OPERATION_QUERY = 3;

    public static final int OPERATION_QUERY_USER_DEFINED = 4;

    public static final int OPERATION_QUERY_TEST_DIST = 5;

    public static final int OPERATION_DYNAMIC_ADD = 6;

    public static final int OPERATION_DYNAMIC_DELETE = 7;

    public static final int OPERATION_RANDOM_SAMPLE = 4;

    public static final int OPERATION_FIND_GOOD_LSH_POINT = 5;

    public static final int OPERATION_FIND_GOOD_BOW_POINT = 6;

    public static final int OPERATION_BUILD_INVERTED_INDEX = 7;

    public static final int OPERATION_TEST_AVERAGE_BOW_ACCUARY = 8;

    public static final int OPERATION_INSERT = 9;

    public static final int OPERATION_DELETE = 10;

    public static final int OPERATION_INSERT_BATCH = 11;

    public static final int OPERATION_DELETE_BATCH = 12;

    public static final int OPERATION_TEST_RECALL_RATIO = 13;

    public static final int OPERATION_TEST_ERROR_RATIO = 14;

    public static final int OPERATION_SUMMARIZE_INVERTED_INDEX = 15;
}
