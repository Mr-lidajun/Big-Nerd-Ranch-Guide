package com.bignerdranch.android.criminalintent;

/**
 * @author lidajun
 * @email solidajun@gmail.com
 * @date 16/6/26 10:36.
 * @desc: 定义数据表字段
 */
public class CrimeDbSchema {
    public static final class CrimeTable {
        public static final String NAME = "crimes";

        public static final class Cols {
            public static final String UUID = "uuid";
            public static final String TITLE = "title";
            public static final String DATE = "date";
            public static final String SOLVED = "solved";
        }
    }
}
