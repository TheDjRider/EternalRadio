package com.eternalnetworktm.eternalradio;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.Objects;

public class MainActivity extends BaseActivity {
    //StreamingPart
    Button Play;
    String stream = "https://radio.jump.bg/proxy/mnikolov/stream";
    MediaPlayer mediaPlayer;
    boolean prepared = false;
    boolean started = false;

    private TextView mTextViewResult;
    private RequestQueue mQueue;

    //Shake Detection Part
    private SensorManager mSensorManager;
    private float mAccel;
    private float mAccelCurrent;
    private float mAccelLast;

    //Notification Part

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        //Shake Start
        setContentView(R.layout.activity_main);
        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        Objects.requireNonNull(mSensorManager).registerListener(mSensorListener, mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER),
                SensorManager.SENSOR_DELAY_NORMAL);
        mAccel = 10f;
        mAccelCurrent = SensorManager.GRAVITY_EARTH;
        mAccelLast = SensorManager.GRAVITY_EARTH;
        //Shake End

        setContentView(R.layout.activity_main);
        Play = findViewById(R.id.Radio);
        Play.setEnabled(false);
        Play.setText(R.string.loading_pls_wait);

        mediaPlayer = new MediaPlayer();
        mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
        new PlayTask().execute(stream);

        //Notification Start
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel("Eternal Radio", "Eternal Radio", NotificationManager.IMPORTANCE_DEFAULT);
            NotificationManager manager = getSystemService(NotificationManager.class);
            manager.createNotificationChannel(channel);
        }

        Play.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                handlePlayButtonClick();
            }
        });

        //JSON Parser
        mTextViewResult = findViewById(R.id.text_view_result);
        Button buttonParse = findViewById(R.id.button_parse);

        mQueue = Volley.newRequestQueue(this);

        jsonParse();

        buttonParse.setOnClickListener(v -> {
            mTextViewResult.setText(""); // Clear the previous text
            jsonParse(); // Parse and display the new data
        });


    }

    private void handlePlayButtonClick() {
        if (mediaPlayer.isPlaying()) {
            mediaPlayer.pause();
            Play.setText(R.string.play);
        } else {
            mediaPlayer.reset();
            mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
            new PlayTask().execute(stream);
            mediaPlayer.setOnPreparedListener(mp -> {
                mediaPlayer.start();
                Play.setText(R.string.pause);
                sendNotification(
                        getString(R.string.app_name),
                        "Eternal Radio is now playing");
            });
        }
    }

    //notification
    private void sendNotification(String title, String message) {
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, "Eternal Radio")
                .setSmallIcon(R.mipmap.ic_launcher_foreground)
                .setContentTitle(title)
                .setContentText(message)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setAutoCancel(true);

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);
        if (ActivityCompat.checkSelfPermission(this, "android.permission.POST_NOTIFICATIONS") != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        notificationManager.notify(1, builder.build());
    }

    private final SensorEventListener mSensorListener = new SensorEventListener() {
        @Override
        public void onSensorChanged(SensorEvent event) {
            float x = event.values[0];
            float y = event.values[1];
            float z = event.values[2];
            mAccelLast = mAccelCurrent;
            mAccelCurrent = (float) Math.sqrt((double) (x * x + y * y + z * z));
            float delta = mAccelCurrent - mAccelLast;
            mAccel = mAccel * 0.9f + delta;
            if (mAccel > 12) {
                Toast.makeText(
                        getApplicationContext(),
                        getString(R.string.shake_event_detected),
                        Toast.LENGTH_SHORT).show();
                if (mediaPlayer.isPlaying()) {
                    mediaPlayer.stop();
                }
                handlePlayButtonClick();
            }
        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) {
        }
    };

    @Override
    protected void onPause() {
        super.onPause();
        mSensorManager.unregisterListener(mSensorListener);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mSensorManager.unregisterListener(mSensorListener);
    }

    @Override
    protected void onResume() {
        super.onResume();
        mSensorManager.registerListener(mSensorListener, mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER),
                SensorManager.SENSOR_DELAY_NORMAL);
    }

    private class PlayTask extends AsyncTask<String, Void, Boolean> {
        @Override
        protected Boolean doInBackground(String... strings) {
            try {
                mediaPlayer.setDataSource(strings[0]);
                mediaPlayer.prepare();
                prepared = true;
            } catch (IOException e) {
                e.printStackTrace();
            }
            return prepared;
        }

        @Override
        protected void onPostExecute(Boolean aBoolean) {
            super.onPostExecute(aBoolean);
            Play.setEnabled(true);
            Play.setText(R.string.play);
        }
    }

    private void jsonParse() {

        String url = "https://radio.jump.bg:2199/rpc/mnikolov/streaminfo.get";

        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, url, null,
                response -> {
                    try {
                        JSONArray jsonArray = response.getJSONArray("data");

                        for (int i = 0; i < jsonArray.length(); i++) {
                            JSONObject data = jsonArray.getJSONObject(i);

                            String song = data.getString("song");
                            String bitrate = data.getString("bitrate");
                            String listeners = data.getString("listeners");
                            String maxlisteners = data.getString("maxlisteners");

                            mTextViewResult.append(
                                    getString(R.string.song) + ": " + song + "\n" +
                                            getString(R.string.listeners) + ": " + listeners + " / " + maxlisteners + "\n" +
                                            getString(R.string.bitrate) + ": " + bitrate + "\n");
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }, error -> error.printStackTrace());

        mQueue.add(request);
    }

    //AboutUs Section
    public void AboutUs(View view) {
        Intent i = new Intent(this, AboutUs.class);
        startActivity(i);
    }

    //RecentFeed Section
    public void RecentFeed(View view) {
        Intent i = new Intent(this, RecentFeed.class);
        startActivity(i);
    }
}
