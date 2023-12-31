package com.vapesimulation.cloud;

import static com.vapesimulation.cloud.Constant.CurrentBattery;
import static com.vapesimulation.cloud.Constant.CurrentFlavour;
import static com.vapesimulation.cloud.Constant.EquipBattery;
import static com.vapesimulation.cloud.Constant.EquipFlavour;
import static com.vapesimulation.cloud.Constant.FlavourProgress;
import static com.vapesimulation.cloud.Constant.VapeCount;
import static com.vapesimulation.cloud.Constant.coins;
import static com.vapesimulation.cloud.Constant.focusRelease;
import static com.vapesimulation.cloud.Constant.saveData;
import static com.vapesimulation.cloud.HomeActivity.bt_vape;
import static com.vapesimulation.cloud.HomeActivity.coughPlayer;
import static com.vapesimulation.cloud.HomeActivity.flavourProgress;
import static com.vapesimulation.cloud.HomeActivity.inhalePlayer;
import static com.vapesimulation.cloud.HomeActivity.inhaleProgress;
import static com.vapesimulation.cloud.HomeActivity.interstitialAd;
import static com.vapesimulation.cloud.HomeActivity.showRewardVideo;
import static com.vapesimulation.cloud.HomeActivity.smoke;
import static com.vapesimulation.cloud.HomeActivity.tv_coin;
import static com.vapesimulation.cloud.HomeActivity.tv_points;

import android.app.Activity;
import android.content.Context;
import android.os.Handler;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;

public class RepeatListener implements View.OnTouchListener {

    private Handler handler = new Handler();
    private Handler handlerForDown = new Handler();

    private int initialInterval;
    private final int normalInterval;
    private final View.OnClickListener clickListener;
    private View touchedView;
    Context context;
    Activity activity;

    private Runnable handlerRunnable = new Runnable() {
        @Override
        public void run() {
            if(touchedView.isEnabled()) {
                handler.postDelayed(this, normalInterval);
                clickListener.onClick(touchedView);
            } else {
                // if the view was disabled by the clickListener, remove the callback
                handler.removeCallbacks(handlerRunnable);
                touchedView.setPressed(false);
                touchedView = null;
            }
        }
    };

    private Runnable handlerForReverse = new Runnable() {
        @Override
        public void run() {

            handlerForDown.postDelayed(this, 30);

            if(inhaleProgress.getProgress() > 0){
                inhaleProgress.setProgress(inhaleProgress.getProgress()-1);
                bt_vape.setVisibility(View.INVISIBLE);
            }

            if(inhaleProgress.getProgress() == 0){
                smoke.setVisibility(View.GONE);
                bt_vape.setVisibility(View.VISIBLE);
            }

            if(inhaleProgress.getProgress() == 1){

                if(interstitialAd != null){
                    interstitialAd.show(activity);
                }

            }
        }
    };

    /**
     * @param initialInterval The interval after first click event
     * @param normalInterval The interval after second and subsequent click
     *       events
     * @param clickListener The OnClickListener, that will be called
     *       periodically
     */

    public RepeatListener(Context context, Activity activity, int initialInterval, int normalInterval,
                          View.OnClickListener clickListener) {
        if (clickListener == null)
            throw new IllegalArgumentException("null runnable");
        if (initialInterval < 0 || normalInterval < 0)
            throw new IllegalArgumentException("negative interval");


        this.context = context;
        this.initialInterval = initialInterval;
        this.normalInterval = normalInterval;
        this.clickListener = clickListener;
        this.activity = activity;
    }

    public boolean onTouch(View view, MotionEvent motionEvent) {

        switch (motionEvent.getAction()) {

            case MotionEvent.ACTION_DOWN:

                handlerForDown.removeCallbacks(handlerForReverse);
                handler.removeCallbacks(handlerRunnable);
                handler.postDelayed(handlerRunnable, initialInterval);
                touchedView = view;
                touchedView.setPressed(true);
                clickListener.onClick(view);
                focusRelease = false;

                return true;

            case MotionEvent.ACTION_UP:

            case MotionEvent.ACTION_CANCEL:

                inhalePlayer.setVolume(0,0);
                coughPlayer.setVolume(0,0);
                smoke.setVisibility(View.VISIBLE);


                updateScore();
                displayPoints();

                handlerForReverse.run();
                handler.removeCallbacks(handlerRunnable);
                touchedView.setPressed(false);
                touchedView = null;
                focusRelease = true;

                if(VapeCount < 4 && flavourProgress.getProgress() != 0){
                    VapeCount++;
                }else if (VapeCount >= 4){
                    showRewardVideo(activity);
                }

                return true;
        }

        return false;
    }

    void updateScore(){

        coins += inhaleProgress.getProgress();
        FlavourProgress = flavourProgress.getProgress();

        tv_coin.setText(String.valueOf(coins));

        // Save Data

        saveData(context,"coins",coins);
        saveData(context,"CurrentBattery",CurrentBattery);
        saveData(context,"CurrentFlavour",CurrentFlavour);
        saveData(context,"FlavourProgress",FlavourProgress);

        saveData(context,"EquipFlavour0",EquipFlavour[0]);
        saveData(context,"EquipFlavour1",EquipFlavour[1]);
        saveData(context,"EquipFlavour2",EquipFlavour[2]);
        saveData(context,"EquipFlavour3",EquipFlavour[3]);

        saveData(context,"EquipBattery0",EquipBattery[0]);
        saveData(context,"EquipBattery1",EquipBattery[1]);

    }

    void displayPoints(){

        if(inhaleProgress.getProgress() > 0){

            tv_points.setVisibility(View.VISIBLE);
            tv_points.setText("+"+String.valueOf(inhaleProgress.getProgress()));
            Animation slideUpAnim = AnimationUtils.loadAnimation(context,R.anim.slide_down);
            tv_points.setAnimation(slideUpAnim);

            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    Animation fadeOutAnim = AnimationUtils.loadAnimation(context,R.anim.fade_out);
                    tv_points.setAnimation(fadeOutAnim);
                    tv_points.setVisibility(View.GONE);
                }
            }, 3000);

        }

    }

}
