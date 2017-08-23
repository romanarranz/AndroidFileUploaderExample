package com.github.romanarranz.androidfileuploaderexample.events;

import android.app.NotificationManager;
import android.content.Context;
import android.support.v4.app.NotificationCompat;

/**
 * Created by romanarranzguerrero on 23/8/17.
 */

public class NotificationProgress {

    private static final int NOTIFICATION_ID = 4006;

    private Thread mNotificationThread;
    private int mCurrentProgress = 0, mMaxProgress;
    private boolean mStop = false;

    private NotificationManager mNM;
    private NotificationCompat.Builder mNBuilder;
    private String mProgressText, mEndText;

    public NotificationProgress(Context context, String title, int iconId) {
        mNM = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        mNBuilder = new NotificationCompat.Builder(context);

        mNBuilder
                .setContentTitle(title)
                .setSmallIcon(iconId);
    }

    public void start() {
        if (mNotificationThread == null) {
            createThead();
        }

        mNotificationThread.start();
    }

    public void stop() {
        mStop = true;
    }

    public void setMax(int max) {
        mMaxProgress = max;
    }

    public void update(int current) {
        mCurrentProgress = current;
    }

    public void setProgressText(String text) {
        mProgressText = text;
    }

    public void setFinishText(String text) {
        mEndText = text;
    }

    private void createThead() {
        mNotificationThread = new Thread(new Runnable() {
            @Override
            public void run() {
                while (!mStop) {
                    // establecer el indicador de proceso
                    int max = 100;
                    int current = Math.round((mCurrentProgress/mMaxProgress) * 100);
                    mNBuilder
                            .setContentText(mProgressText)
                            .setProgress(max, current, false);
                    mNM.notify(NOTIFICATION_ID, mNBuilder.build());

                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }

                // informar de fin
                mNBuilder
                        .setContentText(mEndText)
                        .setProgress(0, 0, false); // elimina la barra de progreso
                mNM.notify(NOTIFICATION_ID, mNBuilder.build());
            }
        });
    }
}
