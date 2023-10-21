package com.example.imageeditor;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    Button button;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        button = findViewById(R.id.startBtn);

        button.setOnClickListener(view -> {
            Intent intent = new Intent(getApplicationContext(),OpenActivity.class);
            startActivity(intent);
        });
    }
}