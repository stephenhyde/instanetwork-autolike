/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mycompany.instagramautolike;
import java.util.*;
public class Instanetwork {
    private static final int NUM_ARGS = 12;
    private Instanetwork() {
        InitiateTest();
    }
    private Instanetwork(String[] a, List<String> l) {
        InitiateInstanetwork(a, l);
    }
    private void InitiateTest() {
        List<String> htags = new ArrayList<>();
        htags.add("appreciation");
        htags.add("bodybuilding");
        htags.add("books");
        htags.add("boss");
        htags.add("compliments");
        htags.add("diet");
        htags.add("f4f");
        htags.add("fashion");
        htags.add("fitness");
        htags.add("fitnessmodel");
        htags.add("focus");
        htags.add("gametime");
        htags.add("goals");
        htags.add("gold");
        htags.add("good");
        htags.add("l4l");
        htags.add("likeaboss");
        htags.add("makeupartist");
        htags.add("mealprep");
        htags.add("model");
        htags.add("motivation");
        htags.add("motivational");
        htags.add("mua");
        htags.add("photographer");
        htags.add("refreshing");
        htags.add("shreded");
        htags.add("truth");
        htags.add("vip");
        new InstaLikeNonGUI("167.114.103.96","8080","none", "none", "hydnova", "T3lephone","stevehyde797@gmail.com", 50, 15, 15, 5, 5, htags);
    }

    private void InitiateInstanetwork(String[] a, List<String> l) {
        new InstaLikeNonGUI(a[0], a[1], a[2], a[3], a[4], a[5], a[6], Integer.valueOf(a[7]), Integer.valueOf(a[8]), Integer.valueOf(a[9]), Integer.valueOf(a[10]), Integer.valueOf(a[11]), l);
    }

    public static void main(String[] args) {
        List<String> htags = new ArrayList<>();
        if (args.length == 0) {
            new Instanetwork();
        } else if (args.length > NUM_ARGS) {
            for (int i = NUM_ARGS; i < args.length; i++) {
                htags.add(args[i]);
                System.out.println("Hashtag " + args[i]);
            }
            new Instanetwork(args, htags);
        } else {
            System.out.println("Jar not initialized properly");
        }
    }
}