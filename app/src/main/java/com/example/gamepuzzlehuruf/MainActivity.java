package com.example.gamepuzzlehuruf;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.drawable.DrawableCompat;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.example.gamepuzzlehuruf.databinding.ActivityMainBinding;

import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;

    private int x = 3;
    private int y = 3;
    private Button[][] btns;
    private int[] tiles;
    private long stepCount = 0;
    private Timer timer;
    private long timeCount;

    MenuItem acak;
    MenuItem about;
    MenuItem keluar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        getSupportActionBar().setTitle("Game Puzzle Huruf");
        getSupportActionBar().setBackgroundDrawable(new ColorDrawable(getResources().getColor(R.color.primary)));
        loadActivity();
        setNumbers();
        loadData();
        setupListener();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu, menu);
        acak = menu.findItem(R.id.menuAcak);
        about = menu.findItem(R.id.menuAbout);
        keluar = menu.findItem(R.id.menuKeluar);
        playGame(false);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.menuAcak) {
            generateLetters();
            loadData();
        }
        else if (id == R.id.menuAbout) startActivity(new Intent(getApplicationContext(), AboutActivity.class));
        else if (id == R.id.menuKeluar) {
            AlertDialog.Builder alert = new AlertDialog.Builder(this)
                    .setTitle("Keluar")
                    .setMessage("Ingin keluar dari aplikasi ini?")
                    .setCancelable(false)
                    .setPositiveButton("Iya", (x, y) -> this.finish())
                    .setNegativeButton("Tidak", (x, y) -> {});
            alert.show();
        }
        return super.onOptionsItemSelected(item);
    }

    private void setupListener() {
        binding.btnStartAndPause.setOnClickListener(s -> {
            if (binding.btnStartAndPause.getText().equals("Start")) {
                binding.btnStartAndPause.setText("Pause");
                backgroundTint(binding.btnStartAndPause);
                playGame(true);
                loadGame();
            } else {
                timer.cancel();
                playGame(false);
                binding.btnStartAndPause.setVisibility(View.GONE);
                binding.inPause.setVisibility(View.VISIBLE);
            }
        });
        binding.btnContinue.setOnClickListener(s -> {
            binding.btnStartAndPause.setVisibility(View.VISIBLE);
            binding.inPause.setVisibility(View.GONE);
            playGame(true);
            loadTimer();
        });

        binding.btnRestart.setOnClickListener(s -> {
            AlertDialog.Builder alert = new AlertDialog.Builder(this)
                    .setTitle("Restart")
                    .setMessage("Ingin mengulang permainan ini?")
                    .setCancelable(false)
                    .setPositiveButton("Iya", (x, y) -> {
                        binding.btnStartAndPause.setVisibility(View.VISIBLE);
                        binding.inPause.setVisibility(View.GONE);
                        playGame(true);
                        loadGame();
                    })
                    .setNegativeButton("Tidak", (x, y) -> {});
            alert.show();
        });

        binding.btnStop.setOnClickListener(s -> {
            AlertDialog.Builder alert = new AlertDialog.Builder(this)
                    .setTitle("Stop")
                    .setMessage("Ingin menghentikan permainan ini?")
                    .setCancelable(false)
                    .setPositiveButton("Iya", (x, y) -> {
                        stopGame();
                    })
                    .setNegativeButton("Tidak", (x, y) -> {});
            alert.show();
        });
    }

    private void playGame(boolean play) {
        for (int i = 0; i < binding.group.getChildCount(); i++) btns[i / 4][i % 4].setClickable(play);
        acak.setEnabled(play);
        about.setEnabled(!play);
        keluar.setEnabled(!play);
    }

    private void stopGame() {
        timer.cancel();
        binding.btnStartAndPause.setVisibility(View.VISIBLE);
        binding.inPause.setVisibility(View.GONE);
        binding.btnStartAndPause.setText("Start");
        backgroundTint(binding.btnStartAndPause, R.color.start);
        playGame(false);
        stepCount = 0;
        timeCount = 0;
        setNumbers();
        loadData();
        setTime(timeCount);
        binding.step.setText(String.valueOf(stepCount));
    }

    private void loadGame() {
        stepCount = 0;
        timeCount = 0;
        generateLetters();
        loadData();
        loadTimer();
        binding.step.setText(String.valueOf(stepCount));
        setTime(timeCount);
    }

    public void btnClick(View view) {
        Button btn = (Button) view;
        int btnX = btn.getTag().toString().charAt(0)-'0';
        int btnY = btn.getTag().toString().charAt(1)-'0';

        if ((Math.abs(x-btnX) == 1 && y == btnY) || (Math.abs(btnY-y) == 1 && btnX == x)) {
            btns[x][y].setText(btn.getText().toString());
            backgroundTint(btns[x][y]);
            btn.setText("");
            backgroundTint(btn, R.color.empty);
            x = btnX; y = btnY;
            stepCount++;
            binding.step.setText(String.valueOf(stepCount));
            winCondition();
        }
    }

    private void loadTimer() {
        timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                timeCount++;
                setTime(timeCount);
            }
        }, 1000, 1000);
    }

    private void setTime(long time) {
        long second = time % 60;
        long hour = time / 3600;
        long minute = (time - hour * 3600) / 60;
        runOnUiThread(() -> binding.time.setText(String.format("%02d:%02d:%02d", hour, minute, second)));
    }

    private void winCondition() {
        boolean isWin = false;
        if (x == 3 && y == 3) {
            for (int i = 0; i < binding.group.getChildCount() - 1; i++) {
                if (btns[i / 4][i % 4].getText().toString().equals(String.valueOf((char)(i + 65)))) isWin = true;
                else {
                    isWin = false;
                    break;
                }
            }
        }

        if (isWin) {
            timer.cancel();
            AlertDialog.Builder alert = new AlertDialog.Builder(this)
                    .setTitle("You Win it!")
                    .setMessage("Total Langkah: " + stepCount + "\nWaktu: " + binding.time.getText() + "\nIngin mengulang permainan ini?")
                    .setCancelable(false)
                    .setPositiveButton("Iya", (x, y) -> {
                        binding.btnStartAndPause.setVisibility(View.VISIBLE);
                        binding.inPause.setVisibility(View.GONE);
                        playGame(true);
                        loadGame();
                    })
                    .setNegativeButton("Tidak", (x, y) -> stopGame());
            alert.show();
        }
    }

    private void loadData() {
        x = 3; y = 3;
        for (int i = 0; i < binding.group.getChildCount() - 1; i++) {
            btns[i / 4][i % 4].setText(String.valueOf((char)(tiles[i] + 64)));
            backgroundTint(btns[i / 4][i % 4]);
        }
        btns[x][y].setText("");
        backgroundTint(btns[x][y], R.color.empty);
    }

    private void loadActivity() {
        btns = new Button[4][4];
        for (int i = 0; i < binding.group.getChildCount(); i++) btns[i / 4][i % 4] = (Button) binding.group.getChildAt(i);
    }

    private void setNumbers() {
        tiles = new int[16];
        for (int i = 0; i < binding.group.getChildCount() - 1; i++) tiles[i] = i + 1;
    }

    private void generateLetters() {
        int n = 15;
        while (n > 1) {
            int num = new Random().nextInt(n--);
            int temp = tiles[num];
            tiles[num] = tiles[n];
            tiles[n] = temp;
        }
        if (!isSame()) generateLetters();
    }

    private boolean isSame(){
        int count = 0;
        for (int i = 0; i < 15; i++) for (int j = 0; j < i; j++) if (tiles[j] > tiles[i]) count++;
        return count % 2 == 0;
    }

    private void backgroundTint(Button btn) {
        backgroundTint(btn, R.color.primary);
    }

    private void backgroundTint(Button btn, int color) {
        Drawable draw = btn.getBackground();
        draw = DrawableCompat.wrap(draw);
        DrawableCompat.setTint(draw, getResources().getColor(color));
        btn.setBackground(draw);
    }

}