package com.vapesimulation.cloud;

import static com.vapesimulation.cloud.Constant.CurrentFlavour;
import static com.vapesimulation.cloud.Constant.CurrentBattery;
import static com.vapesimulation.cloud.Constant.EquipBattery;
import static com.vapesimulation.cloud.Constant.EquipFlavour;
import static com.vapesimulation.cloud.Constant.VOLUME;
import static com.vapesimulation.cloud.Constant.VapeCount;
import static com.vapesimulation.cloud.Constant.coins;
import static com.vapesimulation.cloud.Constant.FlavourSize;
import static com.vapesimulation.cloud.Constant.isRefillDialogVisible;
import static com.vapesimulation.cloud.Constant.getData;
import static com.vapesimulation.cloud.Constant.FlavourProgress;
import static com.vapesimulation.cloud.Constant.getFlavourData;
import static com.vapesimulation.cloud.Acitvities.MySplashActivity.appStartInterstitial;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.FullScreenContentCallback;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.OnUserEarnedRewardListener;
import com.google.android.gms.ads.initialization.InitializationStatus;
import com.google.android.gms.ads.initialization.OnInitializationCompleteListener;
import com.google.android.gms.ads.interstitial.InterstitialAd;
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback;
import com.google.android.gms.ads.rewarded.RewardItem;
import com.google.android.gms.ads.rewarded.RewardedAd;
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback;
import com.xw.repo.BubbleSeekBar;

import pl.droidsonroids.gif.GifImageView;

public class HomeActivity extends AppCompatActivity {

    int exit = 0;
    MediaPlayer backgroundMusic;
    RelativeLayout rl_market,rl_battery,rl_sound,rl_flavour;
    ImageView iv_vapeBattery,iv_earnDialog,iv_market,iv_soundSetting;
    BubbleSeekBar music_seekbar;
    LinearLayout ll_exit;
    public static Button bt_vape;
    public static ProgressBar inhaleProgress, flavourProgress;
    public static MediaPlayer inhalePlayer,coughPlayer;
    public static TextView tv_coin,tv_points;
    public static GifImageView smoke;


    public static RewardedAd rewardedAd;
    public static InterstitialAd interstitialAd;
    InterstitialAd interstitialAdForEachItem;
    AdView mAdView,mAdView1;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        onWindowFocusChanged(true);

        // calling App start Add

        if(appStartInterstitial != null){
            appStartInterstitial.show(HomeActivity.this);
        }

        // initializing ids

        tv_coin = findViewById(R.id.tv_coin);
        music_seekbar = findViewById(R.id.music_seekbar);
        flavourProgress = findViewById(R.id.flavour_progressBar);
        inhaleProgress = findViewById(R.id.progressBar);
        rl_flavour = findViewById(R.id.rl_flavour);
        rl_sound = findViewById(R.id.rl_sound);
        iv_vapeBattery = findViewById(R.id.iv_vapeBattery);
        rl_market = findViewById(R.id.rl_market);
        rl_battery = findViewById(R.id.rl_battery);
        tv_coin.setText(String.valueOf(coins));
        bt_vape = findViewById(R.id.bt_vape);
        tv_points = findViewById(R.id.tv_points);
        smoke = findViewById(R.id.smoke);
        ll_exit = findViewById(R.id.ll_exit);
        iv_earnDialog = findViewById(R.id.iv_earnDialog);
        iv_market = findViewById(R.id.iv_market);
        iv_soundSetting = findViewById(R.id.iv_soundSetting);

        // fetching old data

        loadData();
        setData();

        // setup music

        playBackgroundMusic();
        playInhaleMusic();
        playCoughMusic();

        // load ads

        MobileAds.initialize(HomeActivity.this, new OnInitializationCompleteListener() {
            @Override
            public void onInitializationComplete(InitializationStatus initializationStatus) {
            }
        });

        loadBanner();
        loadInter(this);
        loadInterForEachItem();
        loadRewardAd();

        // other setup

        music_seekbar.setOnProgressChangedListener(new BubbleSeekBar.OnProgressChangedListener() {
            @Override
            public void onProgressChanged(BubbleSeekBar bubbleSeekBar, int progress, float progressFloat, boolean fromUser) {

                backgroundMusic.setVolume((float)progress/100,(float)progress/100);
            }

            @Override
            public void getProgressOnActionUp(BubbleSeekBar bubbleSeekBar, int progress, float progressFloat) {

            }

            @Override
            public void getProgressOnFinally(BubbleSeekBar bubbleSeekBar, int progress, float progressFloat, boolean fromUser) {

            }
        });

        bt_vape.setOnTouchListener(new RepeatListener(this, getParent(),30, 30, new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if(flavourProgress.getProgress() == 0){

                    if(!isRefillDialogVisible){
                        refill_dialog();
                    }

                }else{
                    inhalePlayer.setVolume(1,1);

                    if(inhaleProgress.getProgress() == 100){

                        inhalePlayer.setVolume(0,0);
                        coughPlayer.setVolume(1,1);

                    }else{
                        inhaleProgress.setProgress(inhaleProgress.getProgress()+1);
                        flavourProgress.setProgress(flavourProgress.getProgress()-1);
                    }
                }
            }
        }));

    }

    void loadBanner(){

        mAdView = findViewById(R.id.adView);
        mAdView1 = findViewById(R.id.adView1);
        AdRequest adRequest = new AdRequest.Builder().build();
        mAdView.loadAd(adRequest);
        mAdView1.loadAd(adRequest);

    }

    public static void loadInter(Context context){

        FullScreenContentCallback fullScreenContentCallback = new FullScreenContentCallback() {
            @Override
            public void onAdDismissedFullScreenContent() {
                interstitialAd = null;
                loadInter(context);

            }
        };

        interstitialAd.load(context, context.getString(R.string.admob_interstitial_ad), new AdRequest.Builder().build(), new InterstitialAdLoadCallback() {
            @Override
            public void onAdFailedToLoad(@NonNull LoadAdError loadAdError) {
                interstitialAd = null;
                loadInter(context);
            }

            @Override
            public void onAdLoaded(@NonNull InterstitialAd minterstitialAd) {
                minterstitialAd.setFullScreenContentCallback(fullScreenContentCallback);
                interstitialAd = minterstitialAd;
            }
        });

    }

    void loadInterForEachItem(){

        FullScreenContentCallback fullScreenContentCallback = new FullScreenContentCallback() {
            @Override
            public void onAdDismissedFullScreenContent() {
                interstitialAdForEachItem = null;
                loadInterForEachItem();

            }
        };

        interstitialAdForEachItem.load(HomeActivity.this, getString(R.string.admob_interstitial_ad), new AdRequest.Builder().build(), new InterstitialAdLoadCallback() {
            @Override
            public void onAdFailedToLoad(@NonNull LoadAdError loadAdError) {
                interstitialAdForEachItem = null;
                loadInterForEachItem();
            }

            @Override
            public void onAdLoaded(@NonNull InterstitialAd interstitialAd) {
                interstitialAd.setFullScreenContentCallback(fullScreenContentCallback);
                interstitialAdForEachItem = interstitialAd;
            }
        });

    }

    public static void showRewardVideo(Activity activity){

        if(rewardedAd != null){

            rewardedAd.show(activity,new OnUserEarnedRewardListener() {
                @Override
                public void onUserEarnedReward(@NonNull RewardItem rewardItem) {
                }
            });
        }
    }

    public void loadRewardAd(){

        FullScreenContentCallback fullScreenContentCallback =
                new FullScreenContentCallback() {
                    @Override
                    public void onAdShowedFullScreenContent() {
                        // Code to be invoked when the ad showed full screen content.
                    }

                    @Override
                    public void onAdDismissedFullScreenContent() {
                        rewardedAd = null;
                        VapeCount = 0;
                        loadRewardAd();
                        // Code to be invoked when the ad dismissed full screen content.
                    }
                };

        rewardedAd.load(
                this,
                getString(R.string.admob_reward_ad),
                new AdRequest.Builder().build(),
                new RewardedAdLoadCallback() {
                    @Override
                    public void onAdLoaded(RewardedAd ad) {
                        rewardedAd = ad;
                        rewardedAd.setFullScreenContentCallback(fullScreenContentCallback);
                    }
                });
    }

    void setData(){

        // set last coins

        tv_coin.setText(String.valueOf(coins));

        // set last flavour

        flavourProgress.setProgress(FlavourProgress);

        switch (CurrentFlavour){
            case 0:
                flavourProgress.setProgressDrawable(getResources().getDrawable(R.drawable.blue_flavour_progress_bar));
                break;
            case 1:
                flavourProgress.setProgressDrawable(getResources().getDrawable(R.drawable.red_flavour_progress_bar));
                break;
            case 2:
                flavourProgress.setProgressDrawable(getResources().getDrawable(R.drawable.orange_flavour_progress_bar));
                break;
            case 3:
                flavourProgress.setProgressDrawable(getResources().getDrawable(R.drawable.sky_flavour_progress_bar));
                break;
        }

        // set last battery

        switch (CurrentBattery){
            case 0:
                iv_vapeBattery.setImageResource(R.mipmap.holder_1);
                break;
            case 1:
                iv_vapeBattery.setImageResource(R.mipmap.holder_2);
                break;
        }
    }

    void loadData(){

        if(getData(getApplicationContext(),"coins") == 0){
            coins = 100;
        }else{
            coins = getData(getApplicationContext(),"coins");
        }

        if(getFlavourData(getApplicationContext(),"FlavourProgress") == -1){
            FlavourProgress = 1000;
        }else{
            FlavourProgress = getFlavourData(getApplicationContext(),"FlavourProgress");
        }

        CurrentBattery = getData(getApplicationContext(),"CurrentBattery");
        CurrentFlavour = getData(getApplicationContext(),"CurrentFlavour");

        EquipFlavour[0] = getData(getApplicationContext(),"EquipFlavour0");
        EquipFlavour[1] = getData(getApplicationContext(),"EquipFlavour1");
        EquipFlavour[2] = getData(getApplicationContext(),"EquipFlavour2");
        EquipFlavour[3] = getData(getApplicationContext(),"EquipFlavour3");

        EquipBattery[0] = getData(getApplicationContext(),"EquipBattery0");
        EquipBattery[1] = getData(getApplicationContext(),"EquipBattery1");

    }

    void flavourInhale(){

        switch (CurrentFlavour){
            case 0:
                flavourProgress.setProgressDrawable(getResources().getDrawable(R.drawable.blue_flavour_progress_bar));
                break;
            case 1:
                flavourProgress.setProgressDrawable(getResources().getDrawable(R.drawable.red_flavour_progress_bar));
                break;
            case 2:
                flavourProgress.setProgressDrawable(getResources().getDrawable(R.drawable.orange_flavour_progress_bar));
                break;
            case 3:
                flavourProgress.setProgressDrawable(getResources().getDrawable(R.drawable.sky_flavour_progress_bar));
                break;
        }

        flavourProgress.setProgress(FlavourSize);
    }

    // Sounds

    void clickSound(){
        MediaPlayer mediaPlayer = MediaPlayer.create(getApplicationContext(), R.raw.click);
        mediaPlayer.start();

        if(interstitialAdForEachItem != null){
            interstitialAdForEachItem.show(this);
        }

    }

    void clickSoundNoAd(){
        MediaPlayer mediaPlayer = MediaPlayer.create(getApplicationContext(), R.raw.click);
        mediaPlayer.start();
    }

    void playCoughMusic(){

        coughPlayer = MediaPlayer.create(getApplicationContext(), R.raw.cough);
        coughPlayer.setLooping(true);
        coughPlayer.setVolume(0,0);
        coughPlayer.start();
    }

    void playInhaleMusic(){

        inhalePlayer = MediaPlayer.create(getApplicationContext(), R.raw.inhale);
        inhalePlayer.setLooping(true);
        inhalePlayer.setVolume(0,0);
        inhalePlayer.start();
    }

    void playBackgroundMusic(){

        backgroundMusic = MediaPlayer.create(getApplicationContext(), R.raw.music);
        backgroundMusic.setVolume(VOLUME,VOLUME);
        backgroundMusic.start();
        backgroundMusic.setLooping(true);
    }

    // Dialog

    public void earn_dialog(View view) {

        clickSound();

        // custom dialog
        Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.earn_doalog);

        LinearLayout dialogDismissButton = dialog.findViewById(R.id.dialog_cancel);

        dialogDismissButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
                clickSoundNoAd();
            }
        });

        LinearLayout dialogWatchButton = dialog.findViewById(R.id.dialog_watch);

        dialogWatchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
                clickSoundNoAd();

                if(rewardedAd != null){

                    rewardedAd.show(getParent(), new OnUserEarnedRewardListener() {
                        @Override
                        public void onUserEarnedReward(@NonNull RewardItem rewardItem) {
                            Toast.makeText(HomeActivity.this, "You Have Earned 500$ !", Toast.LENGTH_SHORT).show();
                            coins += 500;
                            tv_coin.setText(coins);
                        }
                    });
                }else{

                    Toast.makeText(HomeActivity.this, "Reward Video isn't available , Try Later !", Toast.LENGTH_SHORT).show();
                    loadRewardAd();
                }

            }
        });
        dialog.show();
    }

    public void refill_dialog() {

        // custom dialog
        Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.refill_doalog);

        LinearLayout cancelButton = dialog.findViewById(R.id.dialog_cancel);

        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
                clickSoundNoAd();
                isRefillDialogVisible = false;
            }
        });

        LinearLayout ShopButton = dialog.findViewById(R.id.dialog_juiceShop);

        ShopButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
                clickSoundNoAd();
                rl_flavour.setVisibility(View.VISIBLE);
                isRefillDialogVisible = false;
            }
        });

        isRefillDialogVisible = true;
        dialog.show();
    }

    // Close Methods

    public void close_market(View view) {
        clickSoundNoAd();
        EnableBackgroundControl();
        rl_market.setVisibility(View.INVISIBLE);
    }

    public void close_battery(View view) {
        clickSoundNoAd();
        EnableBackgroundControl();
        rl_battery.setVisibility(View.INVISIBLE);
    }

    public void close_music(View view) {
        clickSoundNoAd();

        EnableBackgroundControl();

        rl_sound.setVisibility(View.INVISIBLE);
    }

    public void close_flavour(View view) {
        clickSoundNoAd();
        EnableBackgroundControl();
        rl_flavour.setVisibility(View.INVISIBLE);
    }

    // Market Layout

    public void market(View view) {

        clickSound();
        disableBackgroundControl();
        rl_market.setVisibility(View.VISIBLE);
    }

    public void battery(View view) {

        clickSound();
        disableBackgroundControl();
        rl_market.setVisibility(View.INVISIBLE);
        rl_battery.setVisibility(View.VISIBLE);
    }

    public void flavour(View view) {

        clickSound();
        disableBackgroundControl();
        rl_market.setVisibility(View.INVISIBLE);
        rl_flavour.setVisibility(View.VISIBLE);
    }

    // Battery child

    public void battery1(View view) {

        clickSound();

        if(EquipBattery[0] ==  1){

            CurrentBattery = 0;
            iv_vapeBattery.setImageResource(R.mipmap.holder_1);
            Toast.makeText(this, "Battery 1 Equipped !", Toast.LENGTH_SHORT).show();

        }else{

            if(coins >= 10000){
                coins = coins - 10000;
                Toast.makeText(this, "Wow !!! Battery 1 Unlocked !", Toast.LENGTH_SHORT).show();
                tv_coin.setText(String.valueOf(coins));
                EquipBattery[0] = 1 ;
                CurrentBattery = 0;
                iv_vapeBattery.setImageResource(R.mipmap.holder_1);
                Toast.makeText(this, "Battery 1 Equipped !", Toast.LENGTH_SHORT).show();
            }else{
                Toast.makeText(this, "You don't have enough coins !", Toast.LENGTH_SHORT).show();
            }
        }
    }

    public void battery2(View view) {

        clickSound();

        if(EquipBattery[1] ==  1){

            CurrentBattery = 1;
            iv_vapeBattery.setImageResource(R.mipmap.holder_2);
            Toast.makeText(this, "Battery 2 Equipped !", Toast.LENGTH_SHORT).show();

        }else{

            if(coins >= 20000){
                coins = coins - 20000;
                Toast.makeText(this, "Wow !!! Battery 2 Unlocked !", Toast.LENGTH_SHORT).show();
                tv_coin.setText(String.valueOf(coins));
                EquipBattery[1] = 1 ;
                CurrentBattery = 1;
                iv_vapeBattery.setImageResource(R.mipmap.holder_2);
                Toast.makeText(this, "Battery 2 Equipped !", Toast.LENGTH_SHORT).show();
            }else{
                Toast.makeText(this, "You don't have enough coins !", Toast.LENGTH_SHORT).show();
            }
        }
    }

    // flavour child

    public void blue(View view) {

        clickSound();

        if(EquipFlavour[0] ==  1){

            CurrentFlavour = 0;
            flavourInhale();
            Toast.makeText(this, "Blue Flavour Equipped !", Toast.LENGTH_SHORT).show();

        }else{

            EquipFlavour[0] = 1 ;
            CurrentFlavour = 0;
            flavourInhale();
            Toast.makeText(this, "Blue Flavour Equipped !", Toast.LENGTH_SHORT).show();

        }

    }

    public void red(View view) {

        clickSound();

        if(EquipFlavour[1] ==  1){

            CurrentFlavour = 1;
            flavourInhale();
            Toast.makeText(this, "Red Flavour Equipped !", Toast.LENGTH_SHORT).show();
        }else{

            if(coins >= 2000){
                coins = coins - 2000;
                EquipFlavour[1] = 1;
                Toast.makeText(this, "Wow !!! Red Flavour Unlocked !", Toast.LENGTH_SHORT).show();
                tv_coin.setText(String.valueOf(coins));
                CurrentFlavour = 1;
                flavourInhale();
                Toast.makeText(this, "Red Flavour Equipped !", Toast.LENGTH_SHORT).show();
            }else{
                Toast.makeText(this, "You don't have enough coins !", Toast.LENGTH_SHORT).show();
            }
        }
    }

    public void orange(View view) {

        clickSound();

        if(EquipFlavour[2] ==  1){
            CurrentFlavour = 2;
            flavourInhale();
            Toast.makeText(this, "Orange Flavour Equipped !", Toast.LENGTH_SHORT).show();
        }else{

            if(coins >= 3000){
                coins = coins -3000;
                Toast.makeText(this, "Wow !!! Orange Flavour Unlocked !", Toast.LENGTH_SHORT).show();
                tv_coin.setText(String.valueOf(coins));
                EquipFlavour[2] = 1 ;
                CurrentFlavour = 2;
                flavourInhale();
                Toast.makeText(this, "Orange Flavour Equipped !", Toast.LENGTH_SHORT).show();
            }else{
                Toast.makeText(this, "You don't have enough coins !", Toast.LENGTH_SHORT).show();
            }
        }

    }

    public void sky(View view){

        clickSound();

        if(EquipFlavour[3] ==  1){
            CurrentFlavour = 3;
            flavourInhale();
            Toast.makeText(this, "Sky Flavour Equipped !", Toast.LENGTH_SHORT).show();
        }else{

            if(coins >= 4000){
                coins = coins -4000;
                Toast.makeText(this, "Wow !!! Sky Flavour Unlocked !", Toast.LENGTH_SHORT).show();
                tv_coin.setText(String.valueOf(coins));
                EquipFlavour[3] = 1 ;
                CurrentFlavour = 3;
                flavourInhale();
                Toast.makeText(this, "Sky Flavour Equipped !", Toast.LENGTH_SHORT).show();
            }else{
                Toast.makeText(this, "You don't have enough coins !", Toast.LENGTH_SHORT).show();
            }
        }

    }

    // Adjust sound layout

    public void sound(View view) {

        clickSound();

        disableBackgroundControl();
        rl_sound.setVisibility(View.VISIBLE);
    }

    // Other

    void disableBackgroundControl(){

        ll_exit.setClickable(false);
        iv_market.setClickable(false);
        iv_earnDialog.setClickable(false);
        iv_soundSetting.setClickable(false);
        bt_vape.setVisibility(View.GONE);
        bt_vape.setVisibility(View.INVISIBLE);
        bt_vape.setClickable(false);
    }

    void EnableBackgroundControl(){

        ll_exit.setClickable(true);
        iv_market.setClickable(true);
        iv_earnDialog.setClickable(true);
        iv_soundSetting.setClickable(true);
        bt_vape.setVisibility(View.VISIBLE);
        bt_vape.setClickable(true);
    }

    @SuppressLint("NewApi")
    @Override
    public void onWindowFocusChanged(boolean hasFocus)
    {
        super.onWindowFocusChanged(hasFocus);

        if( android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT && hasFocus){

            getWindow().getDecorView().setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                            | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                            | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                            | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                            | View.SYSTEM_UI_FLAG_FULLSCREEN
                            | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);

        }
    }

    @Override
    protected void onPause() {
        backgroundMusic.setVolume(0,0);
        backgroundMusic.stop();
        backgroundMusic.release();
        super.onPause();
    }
    @Override
    protected void onResume() {
        playBackgroundMusic();
        super.onResume();
    }

    public void close_app(View view) {

        moveTaskToBack(true);
        android.os.Process.killProcess(android.os.Process.myPid());
        finish();
        super.onBackPressed();
    }

    @Override
    public void onBackPressed() {

        if(exit == 0){
            Toast.makeText(this, "Press Again To Exit", Toast.LENGTH_SHORT).show();
            exit = 1;
        }else{
            moveTaskToBack(true);
            android.os.Process.killProcess(android.os.Process.myPid());
            finish();
        }

    }
}