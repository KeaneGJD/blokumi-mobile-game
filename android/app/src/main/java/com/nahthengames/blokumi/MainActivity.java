package com.nahthengames.blokumi;

import android.graphics.Color;
import android.os.Bundle;

import com.getcapacitor.BridgeActivity;

public class MainActivity extends BridgeActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        registerPlugin(BlokumiSharePlugin.class);
        super.onCreate(savedInstanceState);

        // Match the WebView to Blokumi's navy background while HTML loads.
        bridge.getWebView().setBackgroundColor(Color.rgb(17, 24, 39));
    }
}