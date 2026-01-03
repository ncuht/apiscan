package com.github.apiscan.entity;

import com.alibaba.fastjson2.JSON;
import com.github.apiscan.util.ObjectParser;

import java.util.List;

import static com.github.apiscan.util.ObjectParser.parseObject;

public class Test<T extends Test> {
    private byte b;

    private String str;

    private String[] strArr;

    private int[] iArr;

    private int[][][] iArrArr;

    private List<String> ls;

    private List<String[]> lsa;

    private List<T>[] lsaa;

    private List<List<T>>[] lsaaa;

    private T[] ta;

    private List<? extends List<String>> lt;

    private List<? super SubTest> st;
    private List<? super SubTest> sst;

    private List<? extends SubTest> et;

    private List<T> listt;

    private List list;

    private List<List<String>> lls;

    private List<List<List<List<String>>>> lllls;

    private List<SubTest> la;



    public static void main(String[] args) {
        System.out.println(JSON.toJSONString(ObjectParser.parseObject(Test.class)));
    }
}

class SubTest {
    private String str;

    private SubSubTest subSubTest;

    private SubTest subTest;

    private List<SubTest> subTests;

    private List<List<SubTest>> subTestss;

    private List<List<List<SubTest>>> subTestsss;
}

class SubSubTest {
    private String str;

    private SubTest parent;
}
