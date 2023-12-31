package com.vapesimulation.cloud;

import static android.content.Context.MODE_PRIVATE;

import android.content.Context;
import android.content.SharedPreferences;
import android.media.MediaPlayer;

public class Constant {

    public static int FlavourSize = 1000;
    public static int FlavourProgress = 1000;

    public static int coins = 100;
    public static int CurrentBattery = 0;
    public static int CurrentFlavour = 0;
    public static float VOLUME = 100;


    // THIS VARIABLE STORE HOW MANY TIMES VAPE BUTTON PRESS
    public static int VapeCount = 0;

    public static Boolean focusRelease = false;
    public static Boolean isRefillDialogVisible = false;


    public static int [] EquipFlavour = new int[] {1,0,0,0};
    public static int [] EquipBattery = new int[] {1,0};



    public static void inhaleSound(Context context){

        MediaPlayer inhalePlayer = MediaPlayer.create(context, R.raw.inhale);
        inhalePlayer.start();
    }

    public static void saveData(Context context,String KEY,int value){

        SharedPreferences sharedPreferences = context.getSharedPreferences(KEY, MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putInt(KEY, value);
        editor.apply();
    }

    public static int getData(Context context,String KEY){

        SharedPreferences sharedPreferences = context.getSharedPreferences(KEY, MODE_PRIVATE);
        return sharedPreferences.getInt(KEY, 0);
    }

    public static int getFlavourData(Context context,String KEY){

        SharedPreferences sharedPreferences = context.getSharedPreferences(KEY, MODE_PRIVATE);
        return sharedPreferences.getInt(KEY, -1);
    }
}
