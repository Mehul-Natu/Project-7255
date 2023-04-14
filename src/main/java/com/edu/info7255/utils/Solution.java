package com.edu.info7255.utils;

public class Solution {


    private static final UpgradedUnionSet org = new UpgradedUnionSet();


    public static void main (String[] args) {

//        int n = 8;
//        int[] array = new int[n];
//        split(1, n, array, 1, 0);
//
//
//        Arrays.stream(array).forEach(p -> System.out.print(p + ","));

        org.setPeer(4, 5);
        org.setPeer(6, 7);
        org.setPeer(8, 9);
        org.setManager(2, 1);
        org.setManager(3, 1);
        org.setManager(5, 2);
        org.setManager(7, 3);
        System.out.println(org.isInManagementChain(1, 8));
        org.setPeer(9, 4);
        System.out.println(org.isInManagementChain(1, 8));
    }





    public static void work(int a0, int n, int[] array, int diff, int start) {
        if (n == 2) {
            array[start] = a0;
            array[start + 1] = a0 + diff;
        } else if (n == 1) {
            array[start] = a0;
        } else {
            split(a0, n, array, diff, start);
        }
    }

    public static void split(int a0, int n, int[] array, int diff, int start) {
        work(a0, (n + 1)/2, array, diff*2, start);
        work(a0 + diff, n/2, array, diff*2, start + (n + 1)/2);
    }

    // 1 2 3 4 5 6 7
    //0, 4
    //


    public static void workAndSplit(int from, int to, int[] array) {
        if (to - from <= 2) {
            return;
        } else if (to - from == 3) {
            int temp = array[from];
            array[from] = array[from + 1];
            array[from + 1] = temp;
        }

        int ap1 = from;
        int ap2 = from + 1;

        int diffAp1 = array[from + 2] - array[from];
        int diffAp2 = array[from + 3] - array[from + 1];

        int ap1Length = (to - from + 2)/2;
        int ap2Length = (to - from + 1)/2;



    }
}
