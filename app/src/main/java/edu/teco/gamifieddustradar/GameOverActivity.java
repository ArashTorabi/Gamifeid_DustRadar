package edu.teco.gamifieddustradar;

import android.annotation.SuppressLint;
import android.os.Handler;
import android.os.Vibrator;
import android.support.v7.app.AppCompatActivity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import edu.teco.gamifieddustradar.blebridge.BLEBridge;

public class GameOverActivity extends AppCompatActivity {
    
    private Button startGameAgain, exitGame;
    private ImageView gameOverIcon;
    private TextView displayScore;
    private TextView displayHighScore;
    private String score;
    boolean doubleBackToExitPressedOnce = false;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game_over);
        
        score = getIntent().getExtras().get("score").toString();
        
        startGameAgain = (Button) findViewById(R.id.play_again_btn);
        exitGame = (Button) findViewById(R.id.exit_game_btn);
        displayScore = (TextView) findViewById(R.id.displayScore);
        displayHighScore = (TextView) findViewById(R.id.displayHighScore);
        gameOverIcon = findViewById(R.id.gameOverIcon);
        
        
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
        
        
        // hidden Button to reset HighScore - fast DoubleClick on displayHighScore Text
          displayHighScore.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                
                if (doubleBackToExitPressedOnce) {
                    SharedPreferences highScorePref = getSharedPreferences("GAMEHIGHSCORE", Context.MODE_PRIVATE);
                    SharedPreferences.Editor editor = highScorePref.edit();
                    editor.putInt("HIGHSCORE", 1920);
                    editor.commit();
                    displayHighScore.setText("Highscore = " + readHighscore());
                    return;
                }
    
                doubleBackToExitPressedOnce = true;
    
                new Handler().postDelayed(new Runnable() {
        
                    @Override
                    public void run() {
                        doubleBackToExitPressedOnce=false;
                    }
                }, 200);
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


