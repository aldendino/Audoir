package com.aldendino.audoir;

import android.graphics.Point;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Display;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;


public class MainActivity extends ActionBarActivity {

    private final int xValueRange = 20;
    private final int yValueRange = 9;
    private int screenWidth;
    private int screenHeight;

    private Thread thread;
    private int sampleRate = 44100;
    private double frequency = 440;

    private boolean isRunning = true;
    private boolean isTouching = false;

    private int xOffset;
    private int yOffset;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Display display = getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        screenWidth = size.x;
        screenHeight = size.y;

        thread = new Thread() {
            public void run() {
                Log.d("thread","thread started");
                setPriority(Thread.MAX_PRIORITY);
                int bufferSize = AudioTrack.getMinBufferSize(sampleRate,
                        AudioFormat.CHANNEL_OUT_MONO, AudioFormat.ENCODING_PCM_16BIT);
                AudioTrack audioTrack = new AudioTrack(AudioManager.STREAM_MUSIC,
                        sampleRate, AudioFormat.CHANNEL_OUT_MONO,
                        AudioFormat.ENCODING_PCM_16BIT, bufferSize,
                        AudioTrack.MODE_STREAM);

                short samples[] = new short[bufferSize];
                int amplitude = 10000;
                double twopi = 8.*Math.atan(1.);
                double phase = 0.0;

                int sample = 0;

                audioTrack.play();

                while(isRunning){
                    while(isTouching) {
                        //frequency = 110 * xOffset;
                        frequency = 55 + 10 * xOffset;
                        for (int i = 0; i < bufferSize; i++) {
                            //samples[i] = (short) (amplitude * Math.sin(phase));
                            //phase += twopi * frequency / sampleRate;
                            samples[i] = computeSawWave(amplitude, frequency, sample, yOffset * yOffset);
                            sample ++;
                            if (sample >= sampleRate) sample = 0;
                        }
                        audioTrack.write(samples, 0, bufferSize);
                    }
                }
                audioTrack.stop();
                audioTrack.release();
                Log.d("thread","thread stopped");
            }
        };
        thread.start();
    }

    private short computeWave(int amplitude, double frequency, int sample) {
        return (short) (amplitude * Math.sin(2 * Math.PI * frequency * (double) sample / sampleRate));
    }

    private short computeSquareWave(int amplitude, double frequency, int sample, int squareness) {
        short wave = 0;
        for (int i = 0; i < squareness; i++) {
            int change = 1 + (2 * i);
            wave += computeWave((int) ((double) amplitude / change), frequency * (double) change, sample);
        }
        return wave;
    }

    private short computeEvenWave(int amplitude, double frequency, int sample, int squareness) {
        short wave = 0;
        for (int i = 0; i < squareness; i++) {
            int change = 2 * i - 1;
            wave += computeWave((int) ((double) amplitude / change), frequency * (double) change, sample);
        }
        return wave;
    }

    private short computeSawWave(int amplitude, double frequency, int sample, int squareness) {
        short wave = 0;
        for (int i = 0; i < squareness; i++) {
            int change = 1 + i;
            wave += computeWave((int) ((double) amplitude / change), frequency * (double) change, sample);
        }
        return wave;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        int action = event.getAction();
        if(action == MotionEvent.ACTION_DOWN || action == MotionEvent.ACTION_MOVE) {
            if(!isTouching) isTouching = true;
            int x = (int) event.getX();
            int y = (int) event.getY();
            xOffset = inRange(0, screenWidth, x, 0, xValueRange);
            yOffset = inRange(0, screenHeight, y, 0, yValueRange);
            //Log.d("touch",""+frequency);
            Log.d("touch", "" + yOffset);
        }
        if(action == MotionEvent.ACTION_UP) {
            if(isTouching) isTouching = false;
        }
        return true;
    }

    public int inRange(int fromInit, int toInit, int value, int fromFinal, int toFinal) {
        if(fromInit < toInit && fromFinal < toFinal && value >= fromInit && value <= toInit) {
            int fromRange = toInit - fromInit;
            int toRange = toFinal - fromFinal;
            return (int) (value * ((double) toRange / (double) fromRange));
        }
        return -1;
    }

    public void onDestroy(){
        super.onDestroy();
        isRunning = false;
        try {
            thread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        thread = null;
    }

    /*@Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }*/

    /*@Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }*/
}
