# SimSSE

Searchable symmetric encryption (SSE) has been studied extensively for its full potential in enabling exact-match queries on encrypted records. Yet, situations for similarity queries remain to be fully explored. SimSSE is a high performance encrypted index that supports privacy-assured similarity search over millions of encrypted high-dimensional records with millisecond latency. It employs locality-sensitive hashing (LSH) and SSE, and leverages a set of advanced hash-based algorithms including multiple-choice hashing, open addressing, and cuckoo hashing. 

- Setup
  - JDK: 1.7+
  - IDE: Eclipse, IntelliJ IDEA
  
- Arguments: 
  - [lsh file path] [bow file path] [L] [D] [R] [loadFactor] [thresholdOfKick] [counterLimit] [LIMIT] [key1] [key2] [times]
    - "L": the LSH parameter;
    - "D": the probe step;
    - "R": the radius of a cluster; 
    - "thresholdOfKick": the threshold for cuckoo-kick operations;
    - "counterLimit": the maximum probe step;
    - "LIMIT": the number of records that would be inserted;
    - "times": the number of copies of each record (for testing);

  - E.g., "\lsh-L10R005-sample.txt \bow-u-100w-sample.txt 10 5 0.05 0.8 10 1000 1000 hongkong harry 1"

- Software Interface:
````
----------------------- Root Menu -----------------------
Please select an operation:

[1]  ...
[2]  load BOW file;
[3]  query test;
[4]  random sample test;
[5]  find good LSH points;
[6]  find good BOW points;
[7]  build inverted index of LSH;
[8]  test good points;
[9]  insert;
[10] delete;
[11] batch insert;
[12] batch delete;
[QUIT] quit system.

--->

````

- Current Maintainer
  - Helei Cui, City University of Hong Kong, helei.cui@my.cityu.edu.hk
  - Xingliang Yuan, City University of Hong Kong, xl.y@my.cityu.edu.hk
