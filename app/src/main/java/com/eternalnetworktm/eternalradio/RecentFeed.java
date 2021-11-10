package com.eternalnetworktm.eternalradio;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.ArrayAdapter;

import androidx.appcompat.app.AppCompatActivity;

import com.eternalnetworktm.eternalradio.databinding.ActivityRecentFeedBinding;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RecentFeed extends AppCompatActivity {

    //https://youtu.be/5lNQLR53UtY

    ActivityRecentFeedBinding binding;
    ArrayList<String> recentList;
    ArrayAdapter<String> recentAdapter;
    Handler mainHandler = new Handler();
    ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityRecentFeedBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        initializeRecentlist();
        new fetchData().start();
        binding.fetchDataBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new fetchData().start();
            }
        });
    }

    private void initializeRecentlist() {
        recentList = new ArrayList<>();
        recentAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, recentList);
        binding.recentList.setAdapter(recentAdapter);
    }

    class fetchData extends Thread {

        String data = "";

        @Override
        public void run() {
            super.run();

            mainHandler.post(new Runnable() {
                @Override
                public void run() {
                    progressDialog = new ProgressDialog(RecentFeed.this);
                    progressDialog.setMessage(getString(R.string.updating_data));
                    progressDialog.setCancelable(false);
                    progressDialog.show();
                }
            });

            try {
                URL url = new URL("https://radiocp.novahost.bg:2199/recentfeed/mnikolov/json/");
                HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
                InputStream inputStream = httpURLConnection.getInputStream();
                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));

                String line;

                while ((line = bufferedReader.readLine()) != null) {
                    data = data + line;
                }

                if (!data.isEmpty()) {
                    JSONObject jsonObject = new JSONObject(data);
                    JSONArray items = jsonObject.getJSONArray("items");
                    recentList.clear();

                    for (int i = 0; i < items.length(); i++) {

                        JSONObject titles = items.getJSONObject(i);
                        String title = titles.getString("title");
                        Pattern pattern = Pattern.compile("^(.*?)(?:\\ *?\\[.*\\])?$");

                        Matcher matcher = pattern.matcher(title);

                        while (matcher.find()) {
                            title = matcher.group();
                        }

                        recentList.add(title);
                    }
                }

            } catch (JSONException | IOException e) {
                e.printStackTrace();
            }

            mainHandler.post(new Runnable() {
                @Override
                public void run() {
                    if (progressDialog.isShowing())
                        progressDialog.dismiss();
                    recentAdapter.notifyDataSetChanged();
                }
            });
        }
    }
}