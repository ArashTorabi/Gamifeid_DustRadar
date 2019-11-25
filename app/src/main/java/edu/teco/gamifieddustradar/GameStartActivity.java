package edu.teco.gamifieddustradar;

import android.content.BroadcastReceiver;
import android.content.IntentFilter;
import android.os.Vibrator;
import android.support.v7.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Timer;
import java.util.TimerTask;

import edu.teco.gamifieddustradar.blebridge.KeepAliveManager;
import edu.teco.gamifieddustradar.bluetooth.BLEService;
import edu.teco.gamifieddustradar.data.DataService;
import edu.teco.gamifieddustradar.gps.GPSService;
import edu.teco.gamifieddustradar.http.HTTPIntent;
import edu.teco.gamifieddustradar.http.HTTPService;

import static edu.teco.gamifieddustradar.blebridge.BLEBridgeHandler.*;

public class GameStartActivity extends AppCompatActivity {
    
    private GameView myGameView;
    private Handler handler = new Handler();
    private final  static long Interval = 32;
    boolean doubleBackToExitPressedOnce = false;
    private String deviceAddress;
    private Vibrator myVibrator;
     View layout;
     Toast gameToast;
     TextView toastText;
    
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    
        deviceAddress = getIntent().getExtras().getString(DataService.EXTRA_DATASERVICE_ADDRESS);
        Intent startRecordingIntent = new Intent(DataService.BROADCAST_DATASERVICE_START_RECORDING);
        startRecordingIntent.putExtra(DataService.EXTRA_DATASERVICE_ADDRESS, deviceAddress);
        GameStartActivity.this.sendBroadcast(startRecordingIntent);
    
        Intent startTransmitIntent = new Intent(HTTPService.BROADCAST_HTTPSERVICE_START_TRANSMIT);
        startTransmitIntent.putExtra(DataService.EXTRA_DATASERVICE_ADDRESS, deviceAddress);
        GameStartActivity.this.sendBroadcast(startTransmitIntent);
        
        myGameView = new GameView(this);
        setContentView(myGameView);
        
        Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        myGameView.invalidate();
                    }
                });
            }
        }, 0,Interval);
    
        LayoutInflater inflater = getLayoutInflater();
        layout = inflater.inflate(R.layout.custom_toast,
                (ViewGroup) findViewById(R.id.custom_toast_container));
        toastText = (TextView) layout.findViewById(R.id.text);
        //toastText.setText("UPS!");
    
        gameToast = new Toast(getApplicationContext());
        gameToast.setGravity(Gravity.CENTER_VERTICAL, 0, 0);
        gameToast.setDuration(Toast.LENGTH_SHORT);
        gameToast.setView(layout);
    
        //gameToast.show();
        
    }
    
    @Override
    public void onBackPressed() {
        Log.i("GameActivity", "GameStartActivity: onBackPressed()");
        
  
        if (doubleBackToExitPressedOnce) {
            onDoubleBack();
            stopServices();
            super.onBackPressed();
            return;
        }
    
        this.doubleBackToExitPressedOnce = true;
        Toast.makeText(this, "Tap twice and fast to exit", Toast.LENGTH_SHORT).show();
    
        new Handler().postDelayed(new Runnable() {
        
            @Override
            public void run() {
                doubleBackToExitPressedOnce=false;
            }
        }, 200);
       
    }
    @Override
    public void onPause() {
        Log.i(deviceAddress, "onPause");
        unregisterHandlerReceiver();
        
        Intent stopRecordingIntent = new Intent(DataService.BROADCAST_DATASERVICE_STOP_RECORDING);
        stopRecordingIntent.putExtra(DataService.EXTRA_DATASERVICE_ADDRESS, deviceAddress);
        GameStartActivity.this.getApplicationContext().sendBroadcast(stopRecordingIntent);
    
        Intent stopTransmitIntent = new Intent(HTTPService.BROADCAST_HTTPSERVICE_STOP_TRANSMIT);
        stopTransmitIntent.putExtra(DataService.EXTRA_DATASERVICE_ADDRESS, deviceAddress);
        GameStartActivity.this.getApplicationContext().sendBroadcast(stopTransmitIntent);
        super.onPause();
    }
    
    
    @Override
    public void onResume() {
        super.onResume();
        Log.i(deviceAddress, "onResume");
    
        registerHandlerReceiver();
    
    
        Intent startRecordingIntent = new Intent(DataService.BROADCAST_DATASERVICE_START_RECORDING);
        startRecordingIntent.putExtra(DataService.EXTRA_DATASERVICE_ADDRESS, deviceAddress);
        GameStartActivity.this.sendBroadcast(startRecordingIntent);
    
        Intent startTransmitIntent = new Intent(HTTPService.BROADCAST_HTTPSERVICE_START_TRANSMIT);
        startTransmitIntent.putExtra(DataService.EXTRA_DATASERVICE_ADDRESS, deviceAddress);
        GameStartActivity.this.sendBroadcast(startTransmitIntent);
    }
    
    public void onGameOver(){
        compareScore();
        Intent gameOverIntent = new Intent(getApplicationContext(),GameOverActivity.class);
        gameOverIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        gameOverIntent.putExtra("score",myGameView.getScore());
        getApplicationContext().startActivity(gameOverIntent);
    }
    public void onDoubleBack(){
        Intent gameResetIntent = new Intent(getApplicationContext(), IntroMainActivity.class);
        gameResetIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        getApplicationContext().startActivity(gameResetIntent);
    }
    
    public int readHighScore() {
        SharedPreferences pref = getSharedPreferences("GAMEHIGHSCORE", Context.MODE_PRIVATE);
        return pref.getInt("HIGHSCORE", 0);
    }
    
    public void writeHighScore(int highScore) {
        SharedPreferences highScorePref = getSharedPreferences("GAMEHIGHSCORE", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = highScorePref.edit();
        editor.putInt("HIGHSCORE", highScore);
        editor.commit();
    }
    
    public void compareScore() {
        if (myGameView.getScore() > readHighScore()) {
            writeHighScore(myGameView.getScore());
        }
    }
    
    public void vibrator(long myDuration) {
        myVibrator = (Vibrator)getSystemService(Context.VIBRATOR_SERVICE);
        myVibrator.vibrate(myDuration);
    }
    
    private void stopServices() {
        KeepAliveManager.stopService(this);
        
        DataService.stopService(this);
        BLEService.stopService(this);
        GPSService.stopService(this);
        HTTPService.stopService(this);
        isTransmitting = false;
    }
    
    // BroadcastReceivers
    
    private final BroadcastReceiver mHandlerReceiver = (new BroadcastReceiver() {
        
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            
            if (DataService.BROADCAST_DATASERVICE_DATA_STORED.equals(action)) {
                //updateView();
                return;
            }
            
            if (BLEService.BROADCAST_BLESERVICE_DATA_AVAILABLE.equals(action)) {
                String address = intent.getStringExtra(BLEService.EXTRA_BLESERVICE_ADDRESS);
                if (deviceAddress.equals(address)) {
                    //lastData = intent.getStringExtra(BLEService.EXTRA_BLESERVICE_DATA);
                    //bleConnectionStatus = "Connected";
                    //updateView();
                }
                return;
            }
            
            if (BLEService.BROADCAST_BLESERVICE_GATT_CONNECTED.equals(action)) {
                String address = intent.getStringExtra(BLEService.EXTRA_BLESERVICE_ADDRESS);
                if (deviceAddress.equals(address)) {
                    // bleConnectionStatus = "Connected";
                    //updateView();
                }
                return;
            }
            
            if (BLEService.BROADCAST_BLESERVICE_GATT_DISCONNECTED.equals(action)) {
                String address = intent.getStringExtra(BLEService.EXTRA_BLESERVICE_ADDRESS);
                if (deviceAddress.equals(address)) {
                    // bleConnectionStatus = "Disconnected";
                    // updateView();
                }
                return;
            }
            
            if (GPSService.BROADCAST_GPSSERVICE_LOCATION_AVAILABLE.equals(action)) {
                //  gpsConnectionStatus = "Available";
                //updateView();
                return;
            }
            
            if (HTTPIntent.BROADCAST_HTTPINTENT_POST_SUCCESS.equals(action)) {
                //updateView();
                return;
            }
            
            if (HTTPService.BROADCAST_HTTPSERVICE_TIMEOUT.equals(action)) {
                //updateView();
                return;
            }
            
            if (HTTPService.BROADCAST_HTTPSERVICE_NOTHING_TO_TRANSMIT.equals(action)) {
               // lastViewUpdate = 0;
               // updateView();
                return;
            }
        }
    });
    
    private void registerHandlerReceiver() {
        IntentFilter filter = new IntentFilter();
        
        filter.addAction(DataService.BROADCAST_DATASERVICE_DATA_STORED);
        filter.addAction(BLEService.BROADCAST_BLESERVICE_DATA_AVAILABLE);
        filter.addAction(BLEService.BROADCAST_BLESERVICE_GATT_CONNECTED);
        filter.addAction(BLEService.BROADCAST_BLESERVICE_GATT_DISCONNECTED);
        filter.addAction(GPSService.BROADCAST_GPSSERVICE_LOCATION_AVAILABLE);
        filter.addAction(HTTPIntent.BROADCAST_HTTPINTENT_POST_SUCCESS);
        filter.addAction(HTTPService.BROADCAST_HTTPSERVICE_TIMEOUT);
        filter.addAction(HTTPService.BROADCAST_HTTPSERVICE_NOTHING_TO_TRANSMIT);
    
        GameStartActivity.this.registerReceiver(mHandlerReceiver, filter);
    }
    
    private void unregisterHandlerReceiver() {
        try {
            GameStartActivity.this.unregisterReceiver(mHandlerReceiver);
        }
        catch (IllegalArgumentException e) {
            e.printStackTrace();
        }
    }

}
