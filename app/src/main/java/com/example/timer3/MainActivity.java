package com.example.timer3;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.preference.PreferenceManager;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity
        implements SharedPreferences.OnSharedPreferenceChangeListener {

    private SeekBar seekBar;
    private TextView timeTextView;
    private ImageView startStopImageView;
    private ImageView plusButton, minusButton;
    private SharedPreferences sharedPreferences;
    private CountDownTimer countDownTimer;
    private boolean timerIsGoing = false;
    private boolean countingTimeWasGot = false;
    private int countingTime;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initFields();

        initSeekBar();

        sharedPreferences.registerOnSharedPreferenceChangeListener(this);
    }

    private void initFields() {
        seekBar = findViewById(R.id.seekBar);
        timeTextView = findViewById(R.id.timeTextView);
        plusButton = findViewById(R.id.plusImageView);
        minusButton = findViewById(R.id.minusImageView);
        startStopImageView = findViewById(R.id.startStopImageView);
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
    }

    private void initSeekBar() {
        seekBar.setMax(Integer.parseInt(sharedPreferences.getString("maximal_interval", "600")));
        setDefaultIntervalFromSharedPreferences();
        seekBar.setOnSeekBarChangeListener(createOnSeekBarChangeListener());
    }

    private void initCountDownTimer() {
        countDownTimer = new CountDownTimer(seekBar.getProgress()*1000, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                int secsUntilFinished = (int) (millisUntilFinished/1000);
                updateTimeTextView(secsUntilFinished);
                seekBar.setProgress(secsUntilFinished);
                if (sharedPreferences.getBoolean("enable_half_time_notification", false) &&
                        sharedPreferences.getBoolean("enable_sound", true) &&
                        secsUntilFinished == countingTime/2) {
                    MediaPlayer.create(getApplicationContext(), R.raw.half_time_notification).start();
                }
            }

            @Override
            public void onFinish() {
                timerIsGoing = false;
                if (sharedPreferences.getBoolean("enable_sound", true)) {
                    startEndingMelody();
                }
                refreshTimer(null);
            }
        };
    }


    public void startOrStopTimer(View view) {
        if (!timerIsGoing) {
            initCountDownTimer();
            countDownTimer.start();
            setCountingTimeDisabledToChange();
            startStopImageView.setImageResource(R.drawable.ic_baseline_pause_24);
            timerIsGoing = true;
            if (!countingTimeWasGot) {
                countingTime = seekBar.getProgress();
                countingTimeWasGot = true;
            }
        } else {
            countDownTimer.cancel();
            startStopImageView.setImageResource(R.drawable.ic_baseline_play_arrow_24);
            timerIsGoing = false;
        }
    }

    public void refreshTimer(View view) {
        setDefaultIntervalFromSharedPreferences();
        setCountingTimeEnabledToChange();
        if (countDownTimer != null) {
            countDownTimer.cancel();
        }
        startStopImageView.setImageResource(R.drawable.ic_baseline_play_arrow_24);
        countingTimeWasGot = false;
        timerIsGoing = false;
    }


    public void plusOneSecondToTime(View view) {
        if (!timerIsGoing) {
            seekBar.setProgress(seekBar.getProgress()+1);
            updateTimeTextView(seekBar.getProgress());
        }
    }

    public void minusOneSecondFromTime(View view) {
        if (!timerIsGoing) {
            seekBar.setProgress(seekBar.getProgress()-1);
            updateTimeTextView(seekBar.getProgress());
        }
    }


    private void setDefaultIntervalFromSharedPreferences() {
        seekBar.setProgress(Integer.parseInt(sharedPreferences.getString("default_interval", "60")));
        updateTimeTextView(seekBar.getProgress());
    }

    private void updateTimeTextView(int secsUntilFinished) {
        if (!(sharedPreferences.getBoolean("enable_hours", false))) {
            int minutes = secsUntilFinished/60;
            int seconds = secsUntilFinished - minutes*60;
            timeTextView.setText(createTextToTimeTextViewIfHoursDisabled(minutes, seconds));
        } else {
            int hours = secsUntilFinished/3600;
            int minutes = (secsUntilFinished - hours*3600)/60;
            int seconds = secsUntilFinished - hours*3600 - minutes*60;
            timeTextView.setText(createTextToTimeTextViewIfHoursEnabled(hours, minutes, seconds));
        }
    }

    private String createTextToTimeTextViewIfHoursDisabled(int minutes, int seconds) {
        return convertIntToStringAddingZeroForwardIfIntLesserTen(minutes) + ":"
                + convertIntToStringAddingZeroForwardIfIntLesserTen(seconds);
    }

    private String createTextToTimeTextViewIfHoursEnabled(int hours, int minutes, int seconds) {
        return convertIntToStringAddingZeroForwardIfIntLesserTen(hours) + ":"
                + convertIntToStringAddingZeroForwardIfIntLesserTen(minutes) + ":"
                + convertIntToStringAddingZeroForwardIfIntLesserTen(seconds);
    }

    private String convertIntToStringAddingZeroForwardIfIntLesserTen(int integer) {
        String string;
        if (integer < 10) {
            string = "0" + integer;
        } else {
            string = "" + integer;
        }
        return string;
    }

    private SeekBar.OnSeekBarChangeListener createOnSeekBarChangeListener() {
        return new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                updateTimeTextView(progress);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        };
    }

    private void startEndingMelody() {
        MediaPlayer mediaPlayer;
        switch(sharedPreferences.getString("melody", "Bell")) {
            case "Alarm Siren":
                mediaPlayer = MediaPlayer.create(getApplicationContext(), R.raw.alarm_siren_sound);
                break;
            case "Bip":
                mediaPlayer = MediaPlayer.create(getApplicationContext(), R.raw.bip_sound);
                break;
            case "Cowboy Sound":
                mediaPlayer = MediaPlayer.create(getApplicationContext(), R.raw.cowboy_music);
                break;
            default:
                mediaPlayer = MediaPlayer.create(getApplicationContext(), R.raw.bell_sound);
        }
        mediaPlayer.start();
    }

    @SuppressLint("ResourceAsColor")
    private void setCountingTimeDisabledToChange() {
        seekBar.setEnabled(false);
        plusButton.setEnabled(false);
        minusButton.setEnabled(false);
        plusButton.setImageResource(R.drawable.ic_baseline_add_gray_24);
        minusButton.setImageResource(R.drawable.ic_baseline_remove_gray_24);
    }

    @SuppressLint("ResourceAsColor")
    private void setCountingTimeEnabledToChange() {
        seekBar.setEnabled(true);
        plusButton.setEnabled(true);
        minusButton.setEnabled(true);
        plusButton.setImageResource(R.drawable.ic_baseline_add_white_24);
        minusButton.setImageResource(R.drawable.ic_baseline_remove_white_24);
    }


    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if (key.equals("maximal_interval")) {
            seekBar.setMax(Integer.parseInt(sharedPreferences.getString("maximal_interval", "600")));
        } else if (key.equals("enable_hours")) {
            updateTimeTextView(seekBar.getProgress());
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.timer_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        Intent intent;
        if (id == R.id.settings_menu_item) {
            intent = new Intent(this, SettingsActivity.class);
        } else {
            intent = new Intent(this, AboutActivity.class);
        }
        startActivity(intent);
        return true;
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        sharedPreferences.unregisterOnSharedPreferenceChangeListener(this);
    }
}