package com.nuraycelik.bookiread;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toast.makeText(this,"Welcome",Toast.LENGTH_LONG).show();

    }
    public void save(View view)
    {
        Intent myIntent = new Intent(MainActivity.this, Main1Activity.class);
        MainActivity.this.startActivity(myIntent);
    }
}