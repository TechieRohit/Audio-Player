package com.music.rohit.musicplayer.service;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.res.AssetFileDescriptor;
import android.graphics.BitmapFactory;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.PowerManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.media.MediaBrowserCompat;
import android.support.v4.media.MediaBrowserServiceCompat;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.MediaButtonReceiver;
import android.support.v4.media.session.MediaSessionCompat;
import android.support.v4.media.session.PlaybackStateCompat;
import android.text.TextUtils;
import android.widget.RemoteViews;

import com.music.rohit.musicplayer.R;

import java.io.IOException;
import java.util.List;
import java.util.Random;

/**
 * Created by ROHIT on 4/11/2018.
 */

public class AudioService extends MediaBrowserServiceCompat implements MediaPlayer.OnCompletionListener,
        AudioManager.OnAudioFocusChangeListener {

    //MediaPlayer for actual playback
    private MediaPlayer mMediaPlayer;
    //Manage metadata and playback controls/states.
    private MediaSessionCompat mMediaSessionCompat;

    private android.support.v4.app.NotificationCompat.Builder builder;
    private RemoteViews remoteViews;
    @Override
    public void onCreate() {
        super.onCreate();

        //Initializing MediaPlayer and request partial wake lock
        /*mMediaPlayer = new MediaPlayer();
        mMediaPlayer.setWakeMode(getApplicationContext(), PowerManager.PARTIAL_WAKE_LOCK);
        mMediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);*/
        //mMediaPlayer.setVolume(1.0f,1.0f);
        initializeMediaPlayer();
        initializeMediaSession();
        createNotification();

        /*new MyNotification(AudioService.this);*/
    }

    private void createNotification() {

        builder = new android.support.v4.app.NotificationCompat.Builder(getApplicationContext())
                // Set Icon
                .setSmallIcon(R.mipmap.ic_launcher)
                // Set Ticker Message
                .setTicker("New Notification")
                // Dismiss Notification
                .setAutoCancel(false)
                // Set PendingIntent into Notification
                //.setContentIntent(pIntent)
                // Set RemoteViews into Notification
                //Sticky Notification
                .setOngoing(true);

        remoteViews = new RemoteViews(getPackageName(),
                R.layout.my_notification);

        Intent i = new Intent(getApplicationContext(),AudioService.class);
        i.putExtra("DO","play");

        PendingIntent pIntent = PendingIntent.getActivity(this, 0, i,
                0);

        /*Intent switchIntent = new Intent("com.example.app.ACTION_PLAY");
        PendingIntent pendingSwitchIntent = PendingIntent.getBroadcast(this, 100,
                switchIntent, 0);*/

        remoteViews.setOnClickPendingIntent(R.id.my_notification_remote_play,pIntent);

        builder.setContent(remoteViews);

        NotificationManager notificationmanager = (NotificationManager)
                getSystemService(NOTIFICATION_SERVICE);
        // Build Notification with Notification Manager

        Random random = new Random();
        int m = random.nextInt(9999 - 1000) + 1000;

        notificationmanager.notify(m, builder.build());
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        MediaButtonReceiver.handleIntent(mMediaSessionCompat,intent);
        return super.onStartCommand(intent, flags, startId);
    }

    @Nullable
    @Override
    public BrowserRoot onGetRoot(@NonNull String clientPackageName, int clientUid, @Nullable Bundle rootHints) {
        if(TextUtils.equals(clientPackageName, getPackageName())) {
            return new BrowserRoot(getString(R.string.app_name), null);
        }

        return null;
    }

    @Override
    public void onLoadChildren(@NonNull String parentId, @NonNull Result<List<MediaBrowserCompat.MediaItem>> result) {
        result.sendResult(null);
    }

    //Audio focus change callback
    @Override
    public void onAudioFocusChange(int audioCurrentFocus) {

        switch( audioCurrentFocus ) {
            //Another App has requested the focus
            case AudioManager.AUDIOFOCUS_LOSS: {
                if( mMediaPlayer.isPlaying() ) {
                    mMediaPlayer.stop();
                }
                break;
            }
            //Another app need audio focus for a very short period
            case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT: {
                mMediaPlayer.pause();
                break;
            }
            //Another app is showing the notification you need to slow your volume down
            case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK: {
                if( mMediaPlayer != null ) {
                    mMediaPlayer.setVolume(0.3f, 0.3f);
                }
                break;
            }
            //The process of the previous app of showing the notification is done you can now resume to your original volume
            case AudioManager.AUDIOFOCUS_GAIN: {
                if( mMediaPlayer != null ) {
                    if( !mMediaPlayer.isPlaying() ) {
                        mMediaPlayer.start();
                    }
                    mMediaPlayer.setVolume(1.0f, 1.0f);
                }
                break;
            }
        }
    }

    @Override
    public void onCompletion(MediaPlayer mediaPlayer) {

        if( mMediaPlayer != null ) {
            mMediaPlayer.release();
        }
    }


    //Used for handling playback state when media session actions occur.
    private MediaSessionCompat.Callback mCallback = new MediaSessionCompat.Callback() {

        @Override
        public void onPlay() {
            super.onPlay();
            if (!fetchAudioFocus()) {
                return;
            }

            //Setting media session compat active if the audio focus is granted
            mMediaSessionCompat.setActive(true);
            setMediaPlaybackState(PlaybackStateCompat.STATE_PLAYING);

            /*showPlayNotification();*/
            /*myNotification();*/
            mMediaPlayer.start();
            remoteViews.setImageViewResource
                    (R.id.my_notification_remote_play,android.R.drawable.ic_media_pause);
        }

        @Override
        public void onPause() {
            super.onPause();

            if( mMediaPlayer.isPlaying() ) {
                mMediaPlayer.pause();
                setMediaPlaybackState(PlaybackStateCompat.STATE_PAUSED);
                /*showPausedNotification();*/
                remoteViews.setImageViewResource
                        (R.id.my_notification_remote_play,android.R.drawable.ic_media_play);
            }
        }

        @Override
        public void onPlayFromMediaId(String mediaId, Bundle extras) {
            super.onPlayFromMediaId(mediaId, extras);

            try {
                AssetFileDescriptor afd = getResources().openRawResourceFd(Integer.valueOf(mediaId));
                if( afd == null ) {
                    return;
                }

                try {
                    mMediaPlayer.setDataSource(afd.getFileDescriptor(), afd.getStartOffset(), afd.getLength());

                } catch( IllegalStateException e ) {
                    mMediaPlayer.release();
                    initializeMediaPlayer();
                    mMediaPlayer.setDataSource(afd.getFileDescriptor(), afd.getStartOffset(), afd.getLength());
                }

                afd.close();
                initMediaSessionMetadata();

            } catch (IOException e) {
                return;
            }

            try {
                mMediaPlayer.prepare();
            } catch (IOException e) {}
        }

        @Override
        public void onSeekTo(long pos) {
            super.onSeekTo(pos);
            mMediaPlayer.getDuration();

            mMediaPlayer.seekTo((int) (mMediaPlayer.getCurrentPosition() + pos));
        }

    };


    //setMediaPlaybackState() method above is a helper method that creates a PlaybackStateCompat.Builder object
    // and gives it the proper actions and state, and then builds and associates a PlaybackStateCompat with your
    // MediaSessionCompat object.
    private void setMediaPlaybackState(int state) {
        PlaybackStateCompat.Builder playbackstateBuilder = new PlaybackStateCompat.Builder();
        if( state == PlaybackStateCompat.STATE_PLAYING ) {
            playbackstateBuilder.setActions(PlaybackStateCompat.ACTION_PLAY_PAUSE | PlaybackStateCompat.ACTION_PAUSE);
        } else {
            playbackstateBuilder.setActions(PlaybackStateCompat.ACTION_PLAY_PAUSE | PlaybackStateCompat.ACTION_PLAY);
        }
        playbackstateBuilder.setState(state, PlaybackStateCompat.PLAYBACK_POSITION_UNKNOWN, 0);
        mMediaSessionCompat.setPlaybackState(playbackstateBuilder.build());
    }

    //Here we initialize the MediaSessionCompat object and connect it to the media buttons and control methods that allow
    // us to handle playback and user input. This method starts by creating a ComponentName object that points to the
    // Android support library's MediaButtonReceiver class, and uses that to create a new MediaSessionCompat. We then pass
    // the MediaSession.Callback object that we created earlier to it, and set the flags necessary for receiving media button
    // inputs and control signals.
    private void initializeMediaSession() {
        ComponentName mediaButtonReceiver = new ComponentName(getApplicationContext(), MediaButtonReceiver.class);
        mMediaSessionCompat = new MediaSessionCompat(getApplicationContext(), "Tag", mediaButtonReceiver, null);

        mMediaSessionCompat.setCallback(mCallback);
        mMediaSessionCompat.setFlags( MediaSessionCompat.FLAG_HANDLES_MEDIA_BUTTONS | MediaSessionCompat.FLAG_HANDLES_TRANSPORT_CONTROLS );

        Intent mediaButtonIntent = new Intent(Intent.ACTION_MEDIA_BUTTON);
        mediaButtonIntent.setClass(this, MediaButtonReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(this, 0, mediaButtonIntent, 0);
        mMediaSessionCompat.setMediaButtonReceiver(pendingIntent);

        setSessionToken(mMediaSessionCompat.getSessionToken());
    }

    //Requesting audio focus
    private boolean fetchAudioFocus() {
        AudioManager audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);

        //The result is associated with onAudioFocusChangeListener implemented above
        int result = audioManager.requestAudioFocus(this,
                AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN);

        return result == AudioManager.AUDIOFOCUS_GAIN;
    }

    private void initializeMediaPlayer() {
        //Initializing Media Player
        mMediaPlayer = new MediaPlayer();
        mMediaPlayer.setWakeMode(getApplicationContext(), PowerManager.PARTIAL_WAKE_LOCK);
        mMediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
    }

    private void initMediaSessionMetadata() {
        MediaMetadataCompat.Builder metadataBuilder = new MediaMetadataCompat.Builder();
//Notification icon in card
        metadataBuilder.putBitmap(MediaMetadataCompat.METADATA_KEY_DISPLAY_ICON, BitmapFactory.decodeResource(getResources(), R.mipmap.ic_launcher));
        metadataBuilder.putBitmap(MediaMetadataCompat.METADATA_KEY_ALBUM_ART, BitmapFactory.decodeResource(getResources(), R.mipmap.ic_launcher));

//lock screen icon for pre lollipop
        metadataBuilder.putBitmap(MediaMetadataCompat.METADATA_KEY_ART, BitmapFactory.decodeResource(getResources(), R.mipmap.ic_launcher));
        metadataBuilder.putString(MediaMetadataCompat.METADATA_KEY_DISPLAY_TITLE, "Display Title");
        metadataBuilder.putString(MediaMetadataCompat.METADATA_KEY_DISPLAY_SUBTITLE, "Display Subtitle");
        metadataBuilder.putLong(MediaMetadataCompat.METADATA_KEY_TRACK_NUMBER, 1);
        metadataBuilder.putLong(MediaMetadataCompat.METADATA_KEY_NUM_TRACKS, 1);

        mMediaSessionCompat.setMetadata(metadataBuilder.build());
    }

   /* private void lockScreenControls() {

        // Use the media button APIs (if available) to register ourselves for media button
        // events

        MediaButtonHelper.registerMediaButtonEventReceiverCompat(AudioManager,
                mMediaButtonReceiverComponent);
        // Use the remote control APIs (if available) to set the playback state
        if (mRemoteControlClientCompat == null) {
            Intent intent = new Intent(Intent.ACTION_MEDIA_BUTTON);
            intent.setComponent(mMediaButtonReceiverComponent);
            mRemoteControlClientCompat = new RemoteControlClientCompat(PendingIntent.getBroadcast(this *//*context*//*,0 *//*requestCode, ignored*//*, intent *//*intent*//*, 0 *//*flags*//*));
            RemoteControlHelper.registerRemoteControlClient(mAudioManager,mRemoteControlClientCompat);
        }
        mRemoteControlClientCompat.setPlaybackState(RemoteControlClient.PLAYSTATE_PLAYING);
        mRemoteControlClientCompat.setTransportControlFlags(
                RemoteControlClient.FLAG_KEY_MEDIA_PAUSE |
                        RemoteControlClient.FLAG_KEY_MEDIA_PREVIOUS |
                        RemoteControlClient.FLAG_KEY_MEDIA_NEXT |
                        RemoteControlClient.FLAG_KEY_MEDIA_STOP);

        //update remote controls
        mRemoteControlClientCompat.editMetadata(true)
                .putString(MediaMetadataRetriever.METADATA_KEY_ARTIST, "NombreArtista")
                .putString(MediaMetadataRetriever.METADATA_KEY_ALBUM, "Titulo Album")
                //.putLong(MediaMetadataRetriever.METADATA_KEY_DURATION,playingItem.getDuration())
                // TODO: fetch real item artwork
                .putBitmap(RemoteControlClientCompat.MetadataEditorCompat.METADATA_KEY_ARTWORK, getAlbumArt())
                .apply();
    }*/
}

