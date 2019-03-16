package com.adampolt.plants;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.format.DateUtils;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Date;

public class MainActivity extends AppCompatActivity {
    // Minimum length of time, in milliseconds, before user can water the plant
    private long MIN_TIME = 1000 * 60 * 60; //One Hour

    // Length of time, in milliseconds, that the plant can go without water
    private long MAX_TIME = 1000 * 60 * 60 * 24; //One Day

    // Shared Preferences for saving plant data
    private SharedPreferences sharedPrefs;

    // The last time the user watered their plant
    private long lastWateredTime;

    // The time this plant was first watered
    private long firstWateredTime = 0;

    // The views that we're going to update often
    private TextView statusTextView;
    private TextView scoreTextView;
    private ImageView plantImageView;
    private Button waterButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Save references to our Views
        statusTextView = findViewById(R.id.status);
        plantImageView = findViewById(R.id.plantImage);
        waterButton = findViewById(R.id.waterButton);
        scoreTextView = findViewById(R.id.score);

        // Get the Shared preferences file for our plant
        sharedPrefs = getSharedPreferences("plant", 0);

        // Get first watered time and last watered time from Shared Preferences
        lastWateredTime = sharedPrefs.getLong("lastWatered", 0);
        firstWateredTime = sharedPrefs.getLong("firstWatered", 0);

        // Add an on-click action to the Water button
        waterButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                waterPlant();
            }
        });

        if(lastWateredTime == 0) {
            // If the plant hasn't been watered before, show New Plant text
            newPlant();
        } else {
            // If the plant has been watered before, update its water status
            // For now we'll just do this check when the Activity is first created
            // But if there's time next week I'll show you how to improve this
            checkWater();
        }
    }

    private void waterPlant() {
        long now = new Date().getTime();

        if(now - lastWateredTime < MIN_TIME) {
            // The plant can't be watered yet, so let the user know
            Toast.makeText(this, R.string.already_watered, Toast.LENGTH_LONG).show();

            updateScore();
        } else {
            // Save the last watered time to shared preferences
            lastWateredTime = now;
            sharedPrefs.edit().putLong("lastWatered", lastWateredTime).apply();

            // if this is the first time the plant has been watered, save the time
            if(firstWateredTime == 0) {
                firstWateredTime = now;
                sharedPrefs.edit().putLong("firstWatered", now).apply();
            }

            checkWater();
        }
    }

    private void checkWater() {
        //Check if the plant can be watered or is dead
        long now = new Date().getTime();
        long timeSinceLastWater = now - lastWateredTime;

        if(timeSinceLastWater > MAX_TIME) {
            //The plant is dead
            gameOver();
        } else if(timeSinceLastWater > MIN_TIME) {
            //The plant needs water
            plantNeedsWater();
        } else {
            //The plant is blooming
            plantIsBlooming();
        }
    }

    // Update the views to show that the plant is brand-new
    private void newPlant() {
        statusTextView.setText(R.string.you_got_a_new_plant_water_it);
        plantImageView.setImageResource(R.drawable.ic_plant_ok);
        scoreTextView.setText(R.string.new_plant);
    }

    // Update the views to show that the plant can be watered
    private void plantNeedsWater() {
        statusTextView.setText(R.string.plant_needs_water);
        plantImageView.setImageResource(R.drawable.ic_plant_ok);
        updateScore();
    }

    // Update the views to show that the plant died and reset the score
    private void gameOver() {
        statusTextView.setText(R.string.plant_died);
        plantImageView.setImageResource(R.drawable.ic_plant_bad);


        // Reset the plant's first water time
        firstWateredTime = 0;
        sharedPrefs.edit().putLong("firstWatered", 0).apply();

        scoreTextView.setText(R.string.plant_score_died);
    }

    // Update the views to show that the plant is well-watered
    private void plantIsBlooming() {
        statusTextView.setText(R.string.plant_blooming);
        plantImageView.setImageResource(R.drawable.ic_plant_good);
        updateScore();
    }

    // Show how long you've kept the plant alive
    // We didn't get around to this in class, but I thought it would be a good
    // way to show how to get relative time strings
    private void updateScore() {
        long now = new Date().getTime();
        String timeAlive = DateUtils.getRelativeTimeSpanString(firstWateredTime, now, 0).toString();
        scoreTextView.setText("Planted " + timeAlive);
    }
}
