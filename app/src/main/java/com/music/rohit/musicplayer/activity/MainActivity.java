package com.music.rohit.musicplayer.activity;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Intent;
import android.os.RemoteException;
import android.provider.ContactsContract;
import android.support.v4.media.MediaBrowserCompat;
import android.support.v4.media.session.MediaControllerCompat;
import android.support.v4.media.session.PlaybackStateCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.RemoteViews;

import com.music.rohit.musicplayer.R;
import com.music.rohit.musicplayer.service.AudioService;

import java.util.Random;

import static android.media.session.PlaybackState.STATE_PAUSED;
import static android.media.session.PlaybackState.STATE_PLAYING;
import static com.music.rohit.musicplayer.R.*;
import static com.music.rohit.musicplayer.R.drawable.ic_pause_black_36dp;

public class MainActivity extends AppCompatActivity {

    private static final int STATE_PAUSED = 0;
    private static final int STATE_PLAYING = 1;

    private ImageView mPlay,mSkipNext,mSkipPrevious;

    private MediaControllerCompat mMediaControllerCompat;

    private MediaBrowserCompat mMediaBrowserCompat;

    private int mCurrentState;

    private android.support.v4.app.NotificationCompat.Builder builder;
    private RemoteViews remoteViews;

    private ImageView mRemotePlay;

    private MediaBrowserCompat.ConnectionCallback mMediaBrowserCompatConnectionCallback = new MediaBrowserCompat.ConnectionCallback() {

            @Override
            public void onConnected() {
                super.onConnected();
                try {
                    mMediaControllerCompat = new MediaControllerCompat(MainActivity.this, mMediaBrowserCompat.getSessionToken());
                    mMediaControllerCompat.registerCallback(mMediaControllerCompatCallback);
                    setSupportMediaController(mMediaControllerCompat);
                    getSupportMediaController().getTransportControls().playFromMediaId(String.valueOf(raw.demons), null);


                } catch( RemoteException e ) {

                }
            }
        };


    //Listen to changes
    private MediaControllerCompat.Callback mMediaControllerCompatCallback = new MediaControllerCompat.Callback() {

        @Override
        public void onPlaybackStateChanged(PlaybackStateCompat state) {
            super.onPlaybackStateChanged(state);
            if( state == null ) {
                return;
            }

            switch( state.getState() ) {
                case PlaybackStateCompat.STATE_PLAYING: {
                    mCurrentState = STATE_PLAYING;
                    mPlay.setImageResource(R.drawable.ic_pause_black_36dp);
                    /*mRemotePlay.setImageResource(android.R.drawable.ic_media_pause);*/
                    break;
                }
                case PlaybackStateCompat.STATE_PAUSED: {
                    mCurrentState = STATE_PAUSED;
                    mPlay.setImageResource(drawable.ic_play_arrow_black_36dp);
                    /*mRemotePlay.setImageResource(android.R.drawable.ic_media_play);*/
                    break;
                }
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(layout.activity_main);

        mPlay = (ImageView)findViewById(id.imageView_play);
        mSkipNext = (ImageView)findViewById(id.imageView_next);
        mSkipPrevious = (ImageView)findViewById(id.imageView_previous);

        mMediaBrowserCompat = new MediaBrowserCompat(this, new ComponentName(this, AudioService.class),
                mMediaBrowserCompatConnectionCallback, getIntent().getExtras());

        mMediaBrowserCompat.connect();
        myNotification();
        onClickListeners();
    }

    private void onClickListeners() {

        mPlay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
               // mMediaBrowserCompat = new MediaBrowserCompat(this, ,MediaControllerCompat.Callback,null);
                if( mCurrentState == STATE_PAUSED ) {
                    getSupportMediaController().getTransportControls().play();
                    mCurrentState = STATE_PLAYING;
                } else {
                    if( getSupportMediaController().getPlaybackState().getState() == PlaybackStateCompat.STATE_PLAYING ) {
                        getSupportMediaController().getTransportControls().pause();
                    }

                    mCurrentState = STATE_PAUSED;
                }

               /* getSupportMediaController().getTransportControls().play();
                mCurrentState = STATE_PLAYING;*/
            }
        });

        mSkipPrevious.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

            }
        });

        mSkipNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                getSupportMediaController().getTransportControls().seekTo(30);
            }
        });

    }


    public void myNotification() {

        remoteViews = new RemoteViews(getPackageName(),
                R.layout.my_notification);

        //mRemotePlay = (ImageView) findViewById(id.remote_play);

        /*mRemotePlay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if( mCurrentState == STATE_PAUSED ) {
                    getSupportMediaController().getTransportControls().play();
                    mCurrentState = STATE_PLAYING;
                } else {
                    if( getSupportMediaController().getPlaybackState().getState() == PlaybackStateCompat.STATE_PLAYING ) {
                        getSupportMediaController().getTransportControls().pause();
                    }

                    mCurrentState = STATE_PAUSED;
                }
            }
        });*/

        /*remoteViews.setTextViewText(R.id.text, messageBody);
        remoteViews.setTextViewText(R.id.title, title);*/
        // Open NotificationView.java Activity
        Intent i = new Intent(getApplicationContext(),AudioService.class);
        PendingIntent pIntent = PendingIntent.getActivity(this, 0, i,
                PendingIntent.FLAG_UPDATE_CURRENT);

        builder = new android.support.v4.app.NotificationCompat.Builder(getApplicationContext())
                // Set Icon
                .setSmallIcon(R.mipmap.ic_launcher)
                // Set Ticker Message
                .setTicker("New Notification")
                // Dismiss Notification
                .setAutoCancel(false)
                // Set PendingIntent into Notification
                .setContentIntent(pIntent)
                // Set RemoteViews into Notification
                .setContent(remoteViews)
                //Sticky Notification
                .setOngoing(true);


        // Locate and set the Image into customnotificationtext.xml ImageViews
        remoteViews.setImageViewResource(R.id.imagenotileft, R.mipmap.ic_launcher);
        builder.setPriority(2);

        NotificationManager notificationmanager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        // Build Notification with Notification Manager

        Random random = new Random();
        int m = random.nextInt(9999 - 1000) + 1000;

        notificationmanager.notify(m, builder.build());
    }


    @Override
    protected void onDestroy() {
        builder.setAutoCancel(true);
        super.onDestroy();
    }
}
