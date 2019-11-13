package edu.teco.gamifieddustradar;

import android.support.v7.app.AppCompatActivity;

//import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import edu.teco.gamifieddustradar.blebridge.BLEBridge;

public class GameOverActivity extends AppCompatActivity {
    
    private Button startGameAgain, exitGame;
    private TextView displayScore;
    private TextView displayHighScore;
    private String score;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game_over);
        
        score = getIntent().getExtras().get("score").toString();
        
        startGameAgain = (Button) findViewById(R.id.play_again_btn);
        exitGame = (Button) findViewById(R.id.exit_game_btn);
        displayScore = (TextView) findViewById(R.id.displayScore);
        displayHighScore = (TextView) findViewById(R.id.displayHighScore);
        
        
        
        
        startGameAgain.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // BLE Bridge
                Intent intent = new Intent(GameOverActivity.this, BLEBridge.class);
                getApplicationContext().startActivity(intent);
            }
        });
        exitGame.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
                System.exit(0);
            }
        });
        
        displayScore.setText("Score = " + score);
        displayHighScore.setText("Highscore = " + readHighscore());
    }
    public int readHighscore() {
        SharedPreferences highScoreSharedPref = getSharedPreferences("GAMEHIGHSCORE", Context.MODE_PRIVATE);
        return highScoreSharedPref.getInt("HIGHSCORE", 0);
    }
}


