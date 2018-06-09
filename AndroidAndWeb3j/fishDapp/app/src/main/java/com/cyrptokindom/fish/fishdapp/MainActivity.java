package com.cyrptokindom.fish.fishdapp;


import android.app.Activity;
import android.os.Bundle;
import android.widget.TextView;

import com.cyrptokindom.fish.fishdapp.util.*;


public class MainActivity extends Activity {




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        TextView text = (TextView)findViewById(R.id.textview1);
        text.setText("j");

    }





}
