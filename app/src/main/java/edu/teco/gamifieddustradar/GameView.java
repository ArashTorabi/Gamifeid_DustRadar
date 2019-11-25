
package edu.teco.gamifieddustradar;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.util.DisplayMetrics;
import android.view.MotionEvent;
import android.view.View;

import edu.teco.gamifieddustradar.bluetooth.BLEService;

public class GameView extends View {
    
    private Bitmap duster[];
    private int dusterX = 10;
    private int dusterY;
    private int dusterSpeed;
    
    private int canvasWidth, canvasHeight;
    
    private int greenParticleX, greenParticleY, greenParticleSpeed = 14;
    private int blueParticleX, blueParticleY, blueParticleSpeed = 22;
    private int blackParticleX, blackParticleY, blackParticleSpeed = 17;
    private int bombX = 1500, bombY=500 , bombSpeed = 10;
    private Bitmap particles[];
    
    private int firstCloudX, firstCloudY, firstCloudSpeed = 4;
    private int secondCloudX, secondCloudY, secondCloudSpeed = 3;
    private int thirdCloudX, thirdCloudY, thirdCloudSpeed = 4;
    private int fourthCloudX, fourthCloudY, fourthCloudSpeed = 5;
    private Bitmap clouds[];
    
    private int score, lifeCounter, levelCounter;
    
    private boolean touch = false;
    private Bitmap backgroundImageLevel1;
    private Bitmap backgroundImageLevel2;
    private Bitmap backgroundImageLevel3;
    private Paint scorePaint;
    private Paint levelCounterPaint;
    private Bitmap[] life;
    private Integer screenWidth;
    private Integer screenHeight;
    
    private Bitmap levelBarIconBitmap;
    private Bitmap levelBarBackBitmap;
    private float levelBarIconBitmapX = 0 ;
    private float levelBarColorEndLineX = 0 ;
    private Paint linePaint;
    
    
    
    private GameStartActivity theGameActivity = new GameStartActivity() ;
    private boolean isBomb = false;
    
    public GameView(Context context) {
        super(context);
        
        theGameActivity = (GameStartActivity) context;
        
        
        particles = new Bitmap[5];
        particles[0]= BitmapFactory.decodeResource(getResources(),R.drawable.greenparticle);
        particles[1]= BitmapFactory.decodeResource(getResources(),R.drawable.blueparticle);
        particles[2]= BitmapFactory.decodeResource(getResources(),R.drawable.blackparticle);
        particles[3]= BitmapFactory.decodeResource(getResources(),R.drawable.bomb);
        particles[4]= BitmapFactory.decodeResource(getResources(),R.drawable.bombx);
        
        duster = new Bitmap[2];
        duster[0] = BitmapFactory.decodeResource(getResources(), R.drawable.duster_down);
        duster[1] = BitmapFactory.decodeResource(getResources(), R.drawable.duster_up);
        
        DisplayMetrics metrics = context.getResources().getDisplayMetrics();
        screenWidth = metrics.widthPixels;
        screenHeight = metrics.heightPixels;
        
        // Initialize a new Paint instance to draw the line
        linePaint = new Paint();
        // Line color
        linePaint.setColor(Color.RED);
        linePaint.setStyle(Paint.Style.STROKE);
        // Line width in pixels
        linePaint.setStrokeWidth(80);
        linePaint.setAntiAlias(true);
        
        clouds = new Bitmap[4];
        clouds[0] = BitmapFactory.decodeResource(getResources(),R.drawable.cloud1);
        clouds[1] = BitmapFactory.decodeResource(getResources(),R.drawable.cloud2);
        clouds[2] = Bitmap.createScaledBitmap(clouds[0],clouds[0].getWidth()/2, clouds[0].getHeight()/2,false);
        clouds[3] = Bitmap.createScaledBitmap(clouds[1],clouds[1].getWidth()/2, clouds[1].getHeight()/2,false);
        
        backgroundImageLevel1 = BitmapFactory.decodeResource(getResources(), R.drawable.game_background_level_1);
        backgroundImageLevel1 = Bitmap.createScaledBitmap(backgroundImageLevel1, screenWidth, screenHeight -80, false);
        
        backgroundImageLevel2 = BitmapFactory.decodeResource(getResources(), R.drawable.game_background_level_2);
        backgroundImageLevel2 = Bitmap.createScaledBitmap(backgroundImageLevel2, screenWidth, screenHeight -80, false);
        
        backgroundImageLevel3 = BitmapFactory.decodeResource(getResources(), R.drawable.game_background_level_3);
        backgroundImageLevel3 = Bitmap.createScaledBitmap(backgroundImageLevel3, screenWidth, screenHeight -80, false);
        
        
        scorePaint = new Paint();
        scorePaint.setColor(Color.DKGRAY);
        scorePaint.setTextSize(75);
        scorePaint.setTypeface(Typeface.DEFAULT_BOLD);
        scorePaint.setAntiAlias(true);
        
        levelCounterPaint = new Paint();
        levelCounterPaint.setColor(Color.DKGRAY);
        levelCounterPaint.setTextSize(75);
        levelCounterPaint.setTypeface(Typeface.DEFAULT_BOLD);
        levelCounterPaint.setAntiAlias(true);
        
        life = new Bitmap[2];
        life[0] = BitmapFactory.decodeResource(getResources(), R.drawable.clothes_color);
        life[1] = BitmapFactory.decodeResource(getResources(), R.drawable.clothes);
        
        dusterY = 1830;
        score = 0;
        levelCounter = 1;
        lifeCounter = 3;
        
        levelBarIconBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.levelbar_icon);
        levelBarBackBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.levelbar_flag);
        
        levelBarBackBitmap = Bitmap.createScaledBitmap(levelBarBackBitmap, screenWidth-40, screenHeight*7/100, false );
        levelBarIconBitmap = Bitmap.createScaledBitmap(levelBarIconBitmap, levelBarIconBitmap.getWidth()*45/100, levelBarIconBitmap.getHeight()*45/100, false );
    }
    
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        
        canvasWidth = canvas.getWidth();
        canvasHeight = canvas.getHeight();
        
        // select Background images corresponding to  3 levels
        switch (levelCounter){
            case 1: canvas.drawBitmap(backgroundImageLevel1, 0,0,null);
                break;
            case 2: canvas.drawBitmap(backgroundImageLevel2, 0,0,null);
                break;
            case 3: canvas.drawBitmap(backgroundImageLevel3, 0,0,null);
                levelCounter=1;
                break;
            default:
        }
        
        // Draw level bar
        if (levelBarIconBitmapX < canvasWidth-180){
            levelBarIconBitmapX = (levelBarIconBitmapX + (float)canvasWidth/(2200*2)); // about 2 minute
            levelBarColorEndLineX = (levelBarColorEndLineX + (float)canvasWidth/(2200*2)); // about 2 minute
        }else {
            levelBarColorEndLineX = 0;
            levelBarIconBitmapX = 0;
            ++levelCounter;
            score = score + 100;
            lifeCounter = 3;
            theGameActivity.toastText.setText("+100P  "+"Level= "+levelCounter );
            theGameActivity.gameToast.show();
        }
        
        // Draw a line on
        canvas.drawLine(
                canvasWidth/24, // startX
                canvasHeight-canvasHeight/13, // startY
                canvasWidth/19 + levelBarColorEndLineX, // stopX
                canvasHeight-canvasHeight/13, // stopY
                linePaint // Paint
        );
        canvas.drawBitmap(levelBarBackBitmap, canvasWidth/60, canvasHeight-canvasHeight/9, null);
        canvas.drawBitmap(levelBarIconBitmap, canvasWidth/30 + levelBarIconBitmapX, canvasHeight-canvasHeight/10, null);
        
        
        int minDusterY = duster[0].getHeight() - 200 ;
        int maxDusterY = canvasHeight -  duster[0].getHeight() - 200 ;
        dusterY = dusterY + dusterSpeed;
        
        if(dusterY < minDusterY ) {
            dusterY = minDusterY ;
        }
        if(dusterY > maxDusterY) {
            dusterY = maxDusterY ;
        }
        
        dusterSpeed = dusterSpeed + 2;
        
        if (touch){
            canvas.drawBitmap(duster[1], dusterX, dusterY, null);
            touch = false;
        }
        else {
            canvas.drawBitmap(duster[0], dusterX, dusterY,null);
        }
        
        //Draw Clouds
        firstCloudX= firstCloudX - firstCloudSpeed;
        secondCloudX= secondCloudX - secondCloudSpeed;
        thirdCloudX= thirdCloudX - thirdCloudSpeed;
        fourthCloudX= fourthCloudX - fourthCloudSpeed;
        
        if(firstCloudX < 0 - clouds[0].getWidth()){
            firstCloudX = canvasWidth + clouds[0].getWidth();
            firstCloudY = (int) Math.floor(Math.random() * (maxDusterY - minDusterY))  ;
        }
        
        if(secondCloudX < 0 - clouds[1].getWidth()){
            secondCloudX = canvasWidth + clouds[1].getWidth();
            secondCloudY = (int) Math.floor(Math.random() * (maxDusterY - minDusterY)) ;
        }
        
        if(thirdCloudX < 0 - clouds[2].getWidth()){
            thirdCloudX = canvasWidth + clouds[2].getWidth();
            thirdCloudY = (int) Math.floor(Math.random() * (maxDusterY - minDusterY) );
        }
        if(fourthCloudX < 0 - clouds[3].getWidth()){
            fourthCloudX = canvasWidth + clouds[3].getWidth();
            fourthCloudY = (int) Math.floor(Math.random() * (maxDusterY - minDusterY)) ;
        }
        
        canvas.drawBitmap(clouds[0],firstCloudX, firstCloudY,null);
        canvas.drawBitmap(clouds[1],secondCloudX, secondCloudY,null);
        canvas.drawBitmap(clouds[2],thirdCloudX, thirdCloudY,null);
        canvas.drawBitmap(clouds[3],fourthCloudX, fourthCloudY,null);
        
        // Green Particle
        greenParticleX = greenParticleX - greenParticleSpeed;
        if(hitParticleChecker(greenParticleX,greenParticleY)){
            theGameActivity.toastText.setText("+20P" );
            theGameActivity.gameToast.show();
            score = score + 20;
            greenParticleX = -90;
        }
        if(greenParticleX < 0 ){
            greenParticleX = canvasWidth + 19;
            greenParticleY = (int) Math.floor(Math.random() * (maxDusterY - minDusterY)) + minDusterY + 100;
        }
        canvas.drawBitmap(particles[0],greenParticleX, greenParticleY,null);
        
        // Blue Particle
        blueParticleX = blueParticleX - blueParticleSpeed;
        if(hitParticleChecker(blueParticleX,blueParticleY)){
            theGameActivity.toastText.setText("+10P" );
            theGameActivity.gameToast.show();
            score = score + 10;
            blueParticleX = -90;
        }
        if(blueParticleX < 0 ){
            blueParticleX = canvasWidth + 19;
            blueParticleY = (int) Math.floor(Math.random() * (maxDusterY - minDusterY)) + minDusterY + 100;
        }
        canvas.drawBitmap(particles[1],blueParticleX, blueParticleY,null);
        
        // Black Particle
        
        if(score < 100){
            blackParticleX = blackParticleX - blackParticleSpeed;
            
        }else if (score < 200){
            blackParticleX = blackParticleX - blackParticleSpeed - 7;
        }
        else if (score < 400){
            blackParticleX = blackParticleX - blackParticleSpeed - 14;
            
        }else {
            blackParticleX = blackParticleX - blackParticleSpeed - 18;
        }
        
        if(hitParticleChecker(blackParticleX,blackParticleY)){
            blackParticleX = -100;
            theGameActivity.toastText.setText("Oops!!");
            theGameActivity.gameToast.show();
            lifeCounter--;
            theGameActivity.vibrator(80);
    
            if(lifeCounter == 0){
                //Toast.makeText(getContext(), "Game Over", Toast.LENGTH_SHORT).show();
                theGameActivity.onGameOver();
            }
        }
        if(blackParticleX < 0 ){
            blackParticleX = canvasWidth + 2000;
            blackParticleY = (int) Math.floor(Math.random() * (maxDusterY - minDusterY)) + minDusterY + 100;
        }
        canvas.drawBitmap(particles[2],blackParticleX, blackParticleY,null);
    
    
        // Bomb
        bombX = bombX - bombSpeed;
        
     /*   if(hitParticleChecker(bombX,bombY)){
            blackParticleX = -90;
                //Toast.makeText(getContext(), "Game Over", Toast.LENGTH_SHORT).show();
                theGameActivity.onGameOver();
        }*/
        if(  bombX < dusterX  ){
            theGameActivity.vibrator(120);
            canvas.drawBitmap(particles[4],bombX, bombY,null);
            canvas.drawBitmap(particles[4],dusterX, dusterY,null);
            canvas.drawBitmap(particles[4],dusterX, dusterY + 300,null);
            canvas.drawBitmap(particles[4],dusterX, dusterY+ 600,null);
            canvas.drawBitmap(particles[4],dusterX, dusterY+ 900,null);
            canvas.drawBitmap(particles[4],dusterX, dusterY+ 1200,null);
            canvas.drawBitmap(particles[4],dusterX, dusterY+ 1500,null);
            canvas.drawBitmap(particles[4],dusterX, dusterY+ 1800,null);
            canvas.drawBitmap(particles[4],dusterX, dusterY- 300,null);
            canvas.drawBitmap(particles[4],dusterX, dusterY- 600,null);
            canvas.drawBitmap(particles[4],dusterX, dusterY- 900,null);
            canvas.drawBitmap(particles[4],dusterX, dusterY- 1200,null);
            canvas.drawBitmap(particles[4],dusterX, dusterY- 1500,null);
            canvas.drawBitmap(particles[4],dusterX, dusterY- 1800,null);
             theGameActivity.onGameOver();
    
            // bombX = canvasWidth + 700;
            //bombY = (int) Math.floor(Math.random() * (maxDusterY - minDusterY)) + minDusterY + 100;
        }
         if( BLEService.buttonPress == true ){
            BLEService.buttonPress = false;
            if (bombX<canvasWidth){
                canvas.drawBitmap(particles[4],bombX, bombY,null);
                theGameActivity.toastText.setText("+5P" );
                theGameActivity.gameToast.show();
                score = score + 5;
                bombX = canvasWidth + 700;
                bombY = (int) Math.floor(Math.random() * (maxDusterY - minDusterY)) + minDusterY + 100;
                if(isBomb){
                    theGameActivity.vibrator(50);
                    isBomb = false;
                }
            }
         }
        
        canvas.drawBitmap(particles[3],bombX, bombY,null);
        if (bombX<canvasWidth) {
            isBomb = true;
        }
        canvas.drawText("Level : " + levelCounter,50,130, levelCounterPaint);
        canvas.drawText("Score : " + score,470,130, scorePaint);
        
        // Draw 3 Clothes as 3 life Symbol
        for (int i = 0; i < 3; i++) {
            int x = (int) (1000 + life[0].getWidth() * 1.1 * i );
            int y = 40;
            if(i < lifeCounter){
                canvas.drawBitmap(life[0], x, y, null );
            }
            else{
                canvas.drawBitmap(life[1], x, y, null );
            }
        }
    }
    
    public boolean hitParticleChecker(int x , int y){
        if(dusterX < x && x < (dusterX + duster[0].getWidth()) && dusterY < y && y < (dusterY + duster[0].getHeight())){
            return  true;
        }
        return false;
    }
    
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            touch = true;
            dusterSpeed = -30;
        }
        return true;
    }
    
    public int getScore() {
        return this.score;
    }
   
    
}
