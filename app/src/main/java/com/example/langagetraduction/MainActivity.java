package com.example.langagetraduction;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Spinner;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {
    private TextView translatedTextView;
    private static final String TAG = "Main";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        translatedTextView = findViewById(R.id.translatedTextView);

        ClipboardManager clipboardManager = (ClipboardManager) getSystemService(getApplicationContext().CLIPBOARD_SERVICE);
        clipboardManager.addPrimaryClipChangedListener(new ClipboardManager.OnPrimaryClipChangedListener() {
            @Override
            public void onPrimaryClipChanged() {
                Intent intent = new Intent(getApplicationContext(), ClipboardService.class);
                startService(intent);
            }
        });

        Spinner spinner = findViewById(R.id.spinner);
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {

            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);

        if (intent != null) {
            String finalText = intent.getStringExtra("text");
            if (finalText == null) {
                Log.d(TAG, "finalText가 null입니다.");
            } else {
                Log.d(TAG, "전달받은 데이터 : " + finalText);
                translatedTextView.setText(finalText);
            }
        } else {
            Log.d(TAG, "클립보드 서비스로부터 넘어온 intent가 null 입니다.");
        }
    }

    // 알림을 누르면 앱으로 이동하여 전체 문장을 볼 수 있게끔 하자
    @RequiresApi(api = Build.VERSION_CODES.O)
    private void showResultNotifBar(String s) { // 이걸 메인 액티비티로 옮기자...? 옮겨야 하나?
        NotificationCompat.Builder builder = new NotificationCompat.Builder(getApplicationContext(), "default");

        builder.setSmallIcon(R.mipmap.ic_launcher);
        builder.setContentTitle("번역된 결과");
        builder.setContentText(s);
        builder.setColor(Color.GRAY);
        builder.setAutoCancel(true); // 사용자가 알림을 탭하면 자동으로 제거

        // 알림 표시
        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) // sdk 버전이 오레오 이상일 시 Notification Channel 을 생성해주어야 함
            notificationManager.createNotificationChannel(new NotificationChannel("default", "기본 채널", NotificationManager.IMPORTANCE_NONE));

        notificationManager.notify(1, builder.build());
    }
}
