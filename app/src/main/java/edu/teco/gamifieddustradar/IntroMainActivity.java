package edu.teco.gamifieddustradar;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;


import edu.teco.gamifieddustradar.blebridge.BLEBridge;

public class IntroMainActivity extends AppCompatActivity {
    
    private Button startGame;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_intro);
        
        
        startGame = (Button) findViewById(R.id.StartButton);
        startGame.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // BLE Bridge
                Intent intent = new Intent(IntroMainActivity.this, BLEBridge.class);
                startActivity(intent);
            }
        });
    }
}
