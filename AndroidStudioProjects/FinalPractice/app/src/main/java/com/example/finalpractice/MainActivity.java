package com.example.finalpractice;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import android.os.Handler;
import android.os.Message;
import android.view.View;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;

public class MainActivity extends AppCompatActivity {
    EditText editText;
    Button btn1, btn2;
    TextView textView;
    String txtToTranslate; //번역할 문자열 저장용 변수
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        editText = (EditText)findViewById(R.id.et1);
        textView = (TextView)findViewById(R.id.tv1);
        btn1 = (Button) findViewById(R.id.button1);
        btn2 = (Button) findViewById(R.id.button2);


        btn1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                // Thread로 웹서버에 접속해야 Exception 없음.
                new Thread() {
                    public void run() {
                        String target = editText.getText().toString();
                        if (target != null) {
                            String naverHtml = getResult(target);
                            Bundle bun = new Bundle();
                            bun.putString("NAVER_HTML", naverHtml);
                            Message msg = handler.obtainMessage();
                            msg.setData(bun);
                            handler.sendMessage(msg);
                        }
                    }
                }.start();
            }
        });

       btn2.setOnClickListener(new View.OnClickListener() {
           @Override
           public void onClick(View view) {

           }
          }
    }
    private String getResult(String s) {
        String clientId = "tA0Z9ODHFgLw9ZzCqE2X";//애플리케이션 클라이언트 아이디값";
        String clientSecret = "RGT1vLmpFV";//애플리케이션 클라이언트 시크릿값";
        StringBuffer response = null;
        try {
            Log.v("태그1",s);
            String text = URLEncoder.encode(s, "UTF-8");
            String apiURL = "https://openapi.naver.com/v1/papago/n2mt";
            URL url = new URL(apiURL);
            HttpURLConnection con = (HttpURLConnection) url.openConnection();
            con.setRequestMethod("POST");
            con.setRequestProperty("X-Naver-Client-Id", clientId);
            con.setRequestProperty("X-Naver-Client-Secret", clientSecret);
            // post request
            String postParams = "source=en&target=ko&text=" + text;
            con.setDoOutput(true);
            DataOutputStream wr = new DataOutputStream(con.getOutputStream());
            wr.writeBytes(postParams);
            wr.flush();
            wr.close();
            int responseCode = con.getResponseCode();
            BufferedReader br;
            if (responseCode == 200) { // 정상 호출
                br = new BufferedReader(new InputStreamReader(con.getInputStream()));
            } else {  // 에러 발생
                br = new BufferedReader(new InputStreamReader(con.getErrorStream()));
            }
            String inputLine;
            response = new StringBuffer();   ////////////////////
            while ((inputLine = br.readLine()) != null) {
                response.append(inputLine);
            }
            br.close();
            //text1.setText(response.toString());
        } catch (Exception e) {
            e.printStackTrace();
        }
        if(response != null) {
            Gson gson = new GsonBuilder().create();
            JsonParser parser = new JsonParser();
            Log.d("태그2",response.toString());
            JsonElement rootObj = parser.parse(response.toString()).getAsJsonObject().get("message").getAsJsonObject().get("result");
            TranslatedItem item = gson.fromJson(rootObj.toString(), TranslatedItem.class);

            String result = item.getTranslatedItem(rootObj.toString()).substring(57,item.getTranslatedItem(rootObj.toString()).length()-2); //104번줄에서 다 처리하지 못한 JSON형식 문자열 처리.
            return result;

        }
        else{
            System.out.println(response);
            return response.toString();
        }

    }



    private class TranslatedItem {
        String TranslatedText;

        public String getTranslatedItem(String s) {
            TranslatedText = s;
            return TranslatedText;
        }
    }

    Handler handler = new Handler() {
        public void handleMessage(Message msg) {
            Bundle bun = msg.getData();
            String naverHtml = bun.getString("NAVER_HTML");

            textView.setText(naverHtml);
        }
    };

}

