/*
언어감지 API 해당 언어

1. ko: 한국어
2. ja: 일본어
3. zh-cn: 중국어 간체
4. zh-tw: 중국어 번체
5. hi: 힌디어
6. en: 영어
7. es: 스페인어
8. fr: 프랑스어
9. de: 독일어
10. pt: 포르투갈어
11. vi: 베트남어
12. id: 인도네시아어
13. fa: 페르시아어
14. ar: 아랍어
15. mm: 미얀마어
16. th: 태국어
17. ru: 러시아어
18. it: 이탈리아어
19. unk: 알 수 없음
 */

package com.example.langagetraduction;

import android.os.AsyncTask;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;

public class PapagoNetworkTask extends AsyncTask<String, Void, String> {
    private String detectLangURL = "https://openapi.naver.com/v1/papago/detectLangs"; // 언어감지 url
    private String NMTURL = "https://openapi.naver.com/v1/papago/n2mt";// 파파고 NMT 번역 url
    String clientID = "IFXp8eF1YC1GMK6Erox7";
    String clientSecret = "udrEsbFCZA";
    private String targetLanguage = "ko";
    private static final int SUCCESS_CODE = 200;
    private static final String TAG = "Papago";

    @Override
    protected String doInBackground(String... params) {
        String targetText = params[0];
        String finalText = translateProcess(targetText);

        if (finalText == null) {
            Log.d(TAG, "nmtProcess가 null을 반환했습니다.");
        } else {
            return finalText;
        }
        return null;
    }

    @Override
    protected void onPostExecute(String s) {
        super.onPostExecute(s);
    }

    private String translateProcess(String targetText) {
        String sourceLanguage = detectLanguageProcess(targetText);

        if (sourceLanguage == null) {
            Log.d(TAG, "detectLanguageProcess가 null을 반환했습니다.");
        } else {
            String translatedText = nmtProcess(sourceLanguage, targetText);
            return translatedText;
        }
        return null;
    }

    //-----------------------------------------------------------//
    //------------------------ 언어감지 API ----------------------//
    private String detectLanguageProcess(String targetText) {
        try {
            String encodedText = URLEncoder.encode(targetText, "UTF-8"); // 언어감지 및 번역할 텍스트를 인코딩
            URL detectLang_url = new URL(detectLangURL);
            HttpURLConnection detectLang_con = (HttpURLConnection) detectLang_url.openConnection();

            detectLang_con.setRequestMethod("POST");
            detectLang_con.setRequestProperty("X-Naver-Client-Id", clientID);
            detectLang_con.setRequestProperty("X-Naver-Client-Secret", clientSecret);

            // post request
            String DLApiPostParams = "query=" + encodedText; // Detect Language API Post Parameters
            detectLang_con.setDoOutput(true);
            DataOutputStream wr = new DataOutputStream(detectLang_con.getOutputStream());
            wr.writeBytes(DLApiPostParams);
            wr.flush();
            wr.close();

            int responseCode = detectLang_con.getResponseCode();
            BufferedReader br;
            if (responseCode == SUCCESS_CODE)
                br = new BufferedReader(new InputStreamReader(detectLang_con.getInputStream()));
            else
                br = new BufferedReader(new InputStreamReader(detectLang_con.getErrorStream()));

            String inputLine;
            StringBuffer response = new StringBuffer();
            while ((inputLine = br.readLine()) != null)
                response.append(inputLine);
            br.close();

            // 응답예시
            // { "langCode" : "ko" }
            String resp = response.toString();
            JSONObject json = new JSONObject(resp);
            String sourceLanguage = json.getString("langCode");
            Log.d(TAG, "langCode : " + sourceLanguage);

            return sourceLanguage;
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }

    //-----------------------------------------------------------//
    //-------------------------- NMT API ------------------------//
    private String nmtProcess(String sourceLanguage, String targetText) {
        try {
            String encodedText = URLEncoder.encode(targetText, "UTF-8"); // 언어감지 및 번역할 텍스트를 인코딩
            URL NMT_url = new URL(NMTURL);
            HttpURLConnection NMT_con = (HttpURLConnection) NMT_url.openConnection();

            NMT_con.setRequestMethod("POST");
            NMT_con.setRequestProperty("X-Naver-Client-Id", clientID);
            NMT_con.setRequestProperty("X-Naver-Client-Secret", clientSecret);

            // post request
            String NTApiPostParams = "source=" + sourceLanguage + "&target=" + targetLanguage + "&text=" + encodedText; // NMT Translate API Post Parameters
            NMT_con.setDoOutput(true);
            DataOutputStream wr = new DataOutputStream(NMT_con.getOutputStream());
            wr.writeBytes(NTApiPostParams);
            wr.flush();
            wr.close();

            int responseCode = NMT_con.getResponseCode();
            BufferedReader br;
            if (responseCode == SUCCESS_CODE)
                br = new BufferedReader(new InputStreamReader(NMT_con.getInputStream()));
            else
                br = new BufferedReader(new InputStreamReader(NMT_con.getErrorStream()));

            String inputLine;
            StringBuffer response = new StringBuffer();
            while ((inputLine = br.readLine()) != null)
                response.append(inputLine);
            br.close();

            String resp = response.toString();
            String translatedText = getTranslatedText(resp);
            Log.d(TAG, "번역 결과 : " + translatedText);

            return translatedText;
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }

    /*
    응답예시
    {
    "message": {
        "@type": "response",
        "@service": "naverservice.labs.api",
        "@version": "1.0.0",
        "result": {
            "translatedText": "tea"
        }
    }
     */
    private String getTranslatedText(String s) throws JSONException {
        JSONObject json = new JSONObject(s);
        JSONObject jsonMessage = json.getJSONObject("message");
        JSONObject jsonResult = jsonMessage.getJSONObject("result");

        String str = jsonResult.getString("translatedText");
        return str;
    }
}
