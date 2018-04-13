package com.music.rohit.musicplayer.utils;

import android.app.Activity;
import android.os.Bundle;
import android.widget.Toast;

public class HelperActivity extends Activity {

    private HelperActivity ctx;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ctx = this;
        String action = (String) getIntent().getExtras().get("DO");
        if (action.equals("play")) {
            Toast.makeText(ctx,"playing",Toast.LENGTH_LONG).show();
        }
    }
}
