package com.music.rohit.musicplayer.activity;

import android.content.ComponentName;
import android.content.Intent;
import android.os.RemoteException;
import android.support.v4.media.MediaBrowserCompat;
import android.support.v4.media.session.MediaControllerCompat;
import android.support.v4.media.session.PlaybackStateCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.RemoteViews;
import android.widget.Toast;

import com.music.rohit.musicplayer.R;
import com.music.rohit.musicplayer.receiver.MusicState;
import com.music.rohit.musicplayer.service.AudioService;

import static com.music.rohit.musicplayer.R.*;

public class MainActivity extends AppCompatActivity {

    private static final int STATE_PAUSED = 0;
    private static final int STATE_PLAYING = 1;

    private ImageView mPlay,mSkipNext,mSkipPrevious;

    private MediaControllerCompat mMediaControllerCompat;

    private MediaBrowserCompat mMediaBrowserCompat;

    public static int mCurrentState;

    private android.support.v4.app.NotificationCompat.Builder builder;
    public static RemoteViews remoteViews;

    public static ImageView mRemotePlay;

    private MediaBrowserCompat.ConnectionCallback mMediaBrowserCompatConnectionCallback =
            new MediaBrowserCompat.ConnectionCallback() {

            @Override
            public void onConnected() {
                super.onConnected();
                try {
                    mMediaControllerCompat = new MediaControllerCompat(MainActivity.this,
                            mMediaBrowserCompat.getSessionToken());
                    mMediaControllerCompat.registerCallback(mMediaControllerCompatCallback);
                    setSupportMediaController(mMediaControllerCompat);
                    getSupportMediaController().getTransportControls().playFromMediaId(String.valueOf(raw.demons), null);


                } catch( RemoteException e ) {

                }
            }
        };


    //Listen to changes
    private MediaControllerCompat.Callback mMediaControllerCompatCallback =
            new MediaControllerCompat.Callback() {

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
                    /*remoteViews.setImageViewResource
                            (R.id.my_notification_remote_play,android.R.drawable.ic_media_pause);*/
                    break;
                }
                case PlaybackStateCompat.STATE_PAUSED: {
                    mCurrentState = STATE_PAUSED;
                    mPlay.setImageResource(drawable.ic_play_arrow_black_36dp);
                    /*remoteViews.setImageViewResource
                            (R.id.my_notification_remote_play,android.R.drawable.ic_media_play);*/
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

        mMediaBrowserCompat = new MediaBrowserCompat(this, new ComponentName(this,
                AudioService.class),
                mMediaBrowserCompatConnectionCallback, getIntent().getExtras());

        mMediaBrowserCompat.connect();
        /*myNotification();*/

        onClickListeners();
    }

    private void onClickListeners() {

        mPlay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if( mCurrentState == STATE_PAUSED ) {
                    playMusic();
                } else {
                    pauseMusic();
                }

            }
        });

        mSkipPrevious.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getSupportMediaController().getTransportControls().seekTo(-30000);
            }
        });

        mSkipNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                getSupportMediaController().getTransportControls().seekTo(30000);
            }
        });

    }

    public void playMusic() {
        getSupportMediaController().getTransportControls().play();
                    mCurrentState = STATE_PLAYING;
    }

    public void pauseMusic() {
        if( getSupportMediaController().getPlaybackState().getState() ==
                PlaybackStateCompat.STATE_PLAYING ) {
            getSupportMediaController().getTransportControls().pause();
        }

        mCurrentState = STATE_PAUSED;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if( getSupportMediaController().getPlaybackState().getState() == PlaybackStateCompat.STATE_PLAYING ) {
            getSupportMediaController().getTransportControls().pause();
        }

        mMediaBrowserCompat.disconnect();
    }

    /*@Override
    public void currentState(int state) {
        if (state == STATE_PAUSED) {
            playMusic();
        }else {
            pauseMusic();
        }
    }*/

    @Override
    protected void onNewIntent(Intent intent) {
        // TODO Auto-generated method stub
        super.onNewIntent(intent);
        setIntent(intent);
        Log.i("onNewIntent", intent.toString());    // DEBUG - very useful


        if (intent.getExtras() != null) {   // As the Intent we send back has extras, if it don't, it is a different Intent. it is possible to use TRY {} CATCH{} for this as well to find if Extras is NULL.
            String tmp;
            tmp = intent.getExtras().getString("DO");
            if (tmp != null) {
                if (tmp.equals("play")){
                }
            } else {
                Log.i("onNewIntent", "No new Intent");
            }
        } else {
            Toast.makeText(MainActivity.this,"No new intent",Toast.LENGTH_LONG).show();
        }
    }

}
