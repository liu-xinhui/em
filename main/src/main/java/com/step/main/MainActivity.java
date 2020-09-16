package com.step.main;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.powershare.etm.ui.EmMainActivity;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        TextView textView = findViewById(R.id.test);
        textView.setOnClickListener(view -> {
            Intent intent = new Intent(MainActivity.this, EmMainActivity.class);
            //启动
            startActivity(intent);
        });
    }
}
