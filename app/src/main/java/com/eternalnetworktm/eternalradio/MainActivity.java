package com.eternalnetworktm.eternalradio;

import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.Objects;

public class MainActivity extends AppCompatActivity {
    //StreamingPart
    Button Play;
    String stream = "https://radio.jump.bg/proxy/mnikolov/stream";
    MediaPlayer mediaPlayer;
    boolean prepared = false;
    boolean started = false;

    private TextView mTextViewResult;
    private RequestQueue mQueue;

    private SensorManager mSensorManager;
    private float mAccel;
    private float mAccelCurrent;
    private float mAccelLast;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);
        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        Objects.requireNonNull(mSensorManager).registerListener(mSensorListener, mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER),
                SensorManager.SENSOR_DELAY_NORMAL);
        mAccel = 10f;
        mAccelCurrent = SensorManager.GRAVITY_EARTH;
        mAccelLast = SensorManager.GRAVITY_EARTH;

        setContentView(R.layout.activity_main);
        Play = findViewById(R.id.Radio);
        Play.setEnabled(false);
        Play.setText(R.string.loadgin_pls_wait);

        mediaPlayer = new MediaPlayer();
        mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
        new PlayTask().execute(stream);

        Play.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (started) {
                    started = false;
                    mediaPlayer.pause();
                    Play.setText(R.string.play);
                } else {
                    started = true;
                    mediaPlayer.start();
                    Play.setText(R.string.pause);
                }
            }
        });

        //JSON Parser
        mTextViewResult = findViewById(R.id.text_view_result);
        Button buttonParse = findViewById(R.id.button_parse);

        mQueue = Volley.newRequestQueue(this);

        jsonParse();

        buttonParse.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                jsonParse();
            }
        });

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
                Toast.makeText(getApplicationContext(), "Shake event detected", Toast.LENGTH_SHORT).show();
            }
        }
        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) {
        }
    };

    @Override
    protected void onPause() {
        mSensorManager.unregisterListener(mSensorListener);
        super.onPause();
        if (started) {
            mediaPlayer.pause();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (started) {
            mediaPlayer.start();
        }
    }

    @Override
    protected void onResume() {
        mSensorManager.registerListener(mSensorListener, mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER),
                SensorManager.SENSOR_DELAY_NORMAL);
        super.onResume();
        if (prepared) {
            mediaPlayer.release();
        }
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

        String url = "https://radiocp.novahost.bg:2199/rpc/mnikolov/streaminfo.get";

        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, url, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            JSONArray jsonArray = response.getJSONArray("data");

                            for (int i = 0; i < jsonArray.length(); i++) {
                                JSONObject data = jsonArray.getJSONObject(i);

                                String song = data.getString("song");
                                String bitrate = data.getString("bitrate");
                                String listeners = data.getString("listeners");
                                String maxlisteners = data.getString("maxlisteners");
                                String date = data.getString("date");
                                String time = data.getString("time");

                                mTextViewResult.append("\nSong: " + song + ",\nBitrate: " + bitrate + ",\nListeners: " + listeners + " / " + maxlisteners + ",\n" + date + " " + time + "\n\n");
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                error.printStackTrace();
            }
        });

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
