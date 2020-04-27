package com.example.langagetraduction;

import android.app.Service;
import android.content.ClipData;
import android.content.ClipDescription;
import android.content.ClipboardManager;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.Nullable;

import java.util.concurrent.ExecutionException;

public class ClipboardService extends Service {
    protected String targetLanguage;
    private static final String TAG = "ClipboardService";

    // 서비스 생명주기
    @Override
    public void onCreate() {
        super.onCreate();

        Log.d(TAG, "onCreate()");
        targetLanguage = "ko";
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "onStartCommand()");

        if (intent == null) {
            return START_STICKY;
        } else {
            String text = readFromClipboard();
            if (text != null) {
                PapagoNetworkTask papago = new PapagoNetworkTask();
                String finalText = null;
                try {
                    finalText = papago.execute(text).get();
                } catch (ExecutionException e) {
                    e.printStackTrace();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                Log.d(TAG, "파파고 클래스에서 넘어온 결과 : " + finalText);

                if (finalText != null) {
                    Intent showIntent = new Intent(getApplicationContext(), MainActivity.class);
                    showIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK |
                                        Intent.FLAG_ACTIVITY_SINGLE_TOP |
                                        Intent.FLAG_ACTIVITY_CLEAR_TOP);

                    showIntent.putExtra("text", finalText);
                    startActivity(showIntent);
                }
            }
        }

        return super.onStartCommand(intent, flags, startId);
    }
    @Override
    public void onDestroy() {
        super.onDestroy();

        Log.d(TAG, "onDestroy()");
    }

    // 클립보드의 데이터를 읽어오는 메소드
    // api 사용량 한계로 100자 이상 넘으면 동작 안 하도록 함
    // 복사가 아닌 잘라내기, 붙여넣기를 하여도 인식함
    private String readFromClipboard() {
        StringBuffer data = new StringBuffer();
        ClipboardManager clipboardManager = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);

        if (clipboardManager != null) {
            if (clipboardManager.hasPrimaryClip()) {
                ClipData clipData = clipboardManager.getPrimaryClip();
                if (clipboardManager.getPrimaryClipDescription().hasMimeType(ClipDescription.MIMETYPE_TEXT_PLAIN)) { // 단순 문자열
                    Log.d(TAG, "TEXT_PLAIN");
                    data.append(clipData.getItemAt(0).getText());
                } else if (clipboardManager.getPrimaryClipDescription().hasMimeType(ClipDescription.MIMETYPE_TEXT_HTML)) { // HTML 타입
                    Log.d(TAG, "TEXT_HTML");
                    data.append(clipData.getItemAt(0).getText());
                }
            }
        } else {
            Log.d(TAG, "클립보드 매니저가 null입니다.");
            return null;
        }

        if (data.toString().length() >= 100) {
            Toast.makeText(this, "번역할 문장이 100자를 넘습니다.", Toast.LENGTH_LONG).show();
            Log.d(TAG, "Text length over 50");
            return null;
        }

        return data.toString();
    }
}