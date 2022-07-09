package iss.ca.memgame;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Locale;
import java.util.Timer;

public class GameActivity extends AppCompatActivity {

    TextView timerText;
    Button btn;
    Timer timer;
    int seconds = 0;
    boolean running;

    InputStream in = null;
    private Thread bkgdThread;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);

        running=true;
        runTimer();

        btn = findViewById(R.id.finishBtn);

        btn.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {

                running = false;
                seconds = 0;
                finish();
            }
        });

        System.out.println("start memory game");
        Intent intent = getIntent();

        Resources r = getResources();
        String name = getPackageName();
        ArrayList<String> numbersList = (ArrayList<String>) getIntent().getSerializableExtra("imgList");
        if (bkgdThread != null) {
            bkgdThread.interrupt();
            for(int i=1; i<=12; i++)
            {
                ImageView img = findViewById(r.getIdentifier("img" + i, "id", name));
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        img.setImageResource(R.drawable.cross);
                    }
                });
            }
        }
        bkgdThread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    //loop through list of image URLs and setting bitmap images to ImageView in layout grid
                    for (int i = 1; i <= 6; i++) {
                        if (Thread.interrupted())
                            return;
                        URL url = new URL(numbersList.get(i - 1));
                        URLConnection urlConn = url.openConnection();
                        HttpURLConnection httpConn = (HttpURLConnection) urlConn;
                        httpConn.connect();

                        //raw data from image URL
                        in = httpConn.getInputStream();

                        //converted to bitmap
                        Bitmap bmpimg = BitmapFactory.decodeStream(in);

                        //taking advantage of simple naming convention of ImageView to allow for looping
                        ImageView img = findViewById(r.getIdentifier("img" + i, "id", name));
                        ImageView img1 = findViewById(r.getIdentifier("img" + (i+6), "id", name));

                        //UI thread started to allow setting of images
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                img.setImageBitmap(bmpimg);
                                img1.setImageBitmap(bmpimg);
                            }
                        });
                        Thread.sleep(500);
                    }
                }
                catch (
                        MalformedURLException e)
                {
                    e.printStackTrace();
                }
                catch (IOException e)
                {
                    e.printStackTrace();
                }
                catch (Exception e)
                {
                    e.printStackTrace();
                }
            }
        });bkgdThread.start();



    }

    private void runTimer() {
        timerText = findViewById(R.id.txtTime);
        final Handler handler = new Handler();
        handler.post(new Runnable() {
            @Override
            public void run() {
                int hours = seconds / 3600;
                int minutes = (seconds % 3600) / 60;
                int secs = seconds % 60;
                String time = String.format(Locale.getDefault(),"%d:%02d:%02d", hours, minutes, secs);
                timerText.setText(time);
                if (running) {
                    seconds++;
                }
                handler.postDelayed(this, 1000);
            }
        });
    }

    //Match image
    //if selected1==selected2
    //MediaPlayer correctSound = MediaPlayer.create(Game.this, R.raw.correct);
    //				correctSound.start();

}