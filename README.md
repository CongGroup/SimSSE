# SimSSE

- Abstract

This is a prototype project.

- Setup
  - JDK: 1.7+
  - IDE: Eclipse, IntelliJ IDEA
  
- Arguments: 
  - [lsh file path] [bow file path] [L] [D] [R] [loadFactor] [thresholdOfKick] [counterLimit] [LIMIT] [key1] [key2] [times]
    - "L": LSH parameter, L;
    - "D": probe step;
    - "R": selected radius;
    - "LIMIT": the number of records that would be inserted;
    - "times": the number of copies of each record, which is used to expand the dataset in test;

  - E.g., "\lsh-L10R005-sample.txt \bow-u-100w-sample.txt 10 5 0.05 0.8 10 1000 1000 hongkong harry 1"
  
