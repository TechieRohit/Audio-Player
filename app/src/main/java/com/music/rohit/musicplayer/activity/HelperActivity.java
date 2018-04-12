package com.music.rohit.musicplayer.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

public class HelperActivity extends Activity {

    private HelperActivity ctx;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Toast.makeText(HelperActivity.this,"in helper class",Toast.LENGTH_LONG).show();

        ctx = this;
        String action = (String) getIntent().getExtras().get("DO");
        if (action.equals("play")) {
            Toast.makeText(ctx,"ok",Toast.LENGTH_LONG).show();
        }
    }
}
