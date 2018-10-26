package com.example.finalpractice;

import android.content.Intent;
import android.graphics.Bitmap;

import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;


import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;


import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;

/**
 * 프로젝트명: AI Image Translator (AiTrans)
 * 국방부2018년 공개SW캠프 app과정
 * 개발자: 상병 김정후
 *
 * 개요:
 * 본 프로젝트는 인공신경망 기반 인공지(NMT)을 활용한 이미지 문자인식 번역기 앱입니다.
 * 현 시점까지 주류로 존재하는 문장 입력형의 번역기에서 더 나아가 사진 등의 이미지에서
 * 문자를 인식하고 추출하여 번역하는 기능을 추가해봤습니다.
 * 또한 번역률에 있어서도 NMT는 단순히 단어를 치환하는 방식의 기존의 번역이 아닌
 * 단어 전후의 맥락까지 고려하기 때문에 현격한 차이를 보이는 것이 특징입니다.
 * 이 앱을 통해 해외 도서 등을 언어장벽을 구애받지 않고 읽을 수 있게 되기를 바라며 만들었습니다.
 * 현 시점에서는 영한 번역만이 구현되어있으나 차후 다른 언어 및 언어 자동 감지 기능도 추가될 예정입니다.
 *
 * 사용된 API:
 * 네이버 NMT번역 API
 * Tesseract API (Apache 라이센스 사용)
*/

public class MainActivity extends AppCompatActivity {
    EditText editText;
    Button btn1, btn2, btn3;
    TextView textView;
    ImageView imageView;
    String txtToTranslate; //번역할 문자열 저장용 변수
    private int PICK_IMAGE_REQUEST = 1;
    Bitmap bitmap = null;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        editText = (EditText)findViewById(R.id.et1);
        textView = (TextView)findViewById(R.id.tv1);
        imageView = (ImageView)findViewById(R.id.iv1) ;
        btn1 = (Button) findViewById(R.id.button1);
        btn2 = (Button) findViewById(R.id.button2);
        btn3 = (Button) findViewById(R.id.button3);

        //번역실행 버튼 이벤트 처리
        btn1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try{
                // Thread로 웹서버에 접속해야 Exception 없음.
                new Thread() {
                    public void run() {

                        String target = editText.getText().toString();
                        //editText뷰에 문자열이 있고 이미지가 없을 경우
                        if (target != null && bitmap == null) {
                            String naverHtml = getResult(target);
                            Bundle bun = new Bundle();
                            bun.putString("NAVER_HTML", naverHtml);
                            Message msg = handler.obtainMessage();
                            msg.setData(bun);
                            handler.sendMessage(msg);
                        }
                        //imageView에 이미지를 업로드 되어있는 경우
                        else if (bitmap != null){
                            Log.d("여기 타나요?",txtToTranslate);
                            target = txtToTranslate;
                            String naverHtml = getResult(target);
                            Bundle bun = new Bundle();
                            bun.putString("NAVER_HTML", naverHtml);
                            Message msg = handler.obtainMessage();
                            msg.setData(bun);
                            handler.sendMessage(msg);
                        }
                        else{

                        }

                    }
                }.start();}
                catch(NullPointerException e){
                    e.printStackTrace();
                }
            }
        });

        //이미지 업로드 버튼 이벤트 처리
       btn2.setOnClickListener(new View.OnClickListener() {
           @Override
           public void onClick(View view) {
                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                intent.setType("image/*");
               startActivityForResult(Intent.createChooser(intent, "이미지를 선택해주세요"), PICK_IMAGE_REQUEST);
           }
          });

       //이미지가 업로드 되어있을때 이미지를 터치하는 것으로 imageView를 초기화
       imageView.setOnClickListener(new View.OnClickListener() {

           @Override
           public void onClick(View view) {
               if(bitmap != null) {
                   imageView.setImageDrawable(null);
                   bitmap = null;
                   Toast.makeText(getApplicationContext(), "이미지를 지웠습니다.", Toast.LENGTH_LONG).show();
               }
               else{

               }
       }});



       //저장하기 버튼 이벤트 처리
       btn3.setOnClickListener(new View.OnClickListener(){
           @Override
            public void onClick(View view) {
               textView.buildDrawingCache();
               Bitmap captureView = textView.getDrawingCache();
               FileOutputStream fos;

               try {
                   File directory = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
                   File file = new File(directory, "/" + System.currentTimeMillis() + ".jpeg");
                   fos = new FileOutputStream(file);
                   captureView.compress(Bitmap.CompressFormat.JPEG, 100, fos);
                   fos.flush();
                   fos.getFD().sync();
                   fos.close();

                   //sdcard안의 Pictures 폴더로 번역 결과를 JPEG형태로 저장
                   MediaStore.Images.Media.insertImage(getContentResolver(), captureView, file.getName(), "");

               } catch (FileNotFoundException e) {
                   e.printStackTrace();
               } catch (IOException o) {
                   o.printStackTrace();
               }
                Toast.makeText(getApplicationContext(), "저장했습니다.", Toast.LENGTH_LONG).show();
        }
    });
    }

    @Override  //이미지 선택작업을 후의 결과 처리
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {

            Uri uri = data.getData();  //data에서 절대경로로 이미지를 가져옴

            try {
                bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), uri);
                // Log.d(TAG, String.valueOf(bitmap));

                ImageView imageView = (ImageView) findViewById(R.id.iv1);
                imageView.setImageBitmap(bitmap);
//                if (bitmap != null) {
//                    Log.d("이미지 있습니다", String.valueOf(bitmap));
//                }
            } catch (IOException e) {
                e.printStackTrace();
            }
           doOCR ocr = new doOCR(this);//여기에 OCR메소드를 넣겠습니다.
           txtToTranslate = ocr.processImage();
        }
    }

    Bitmap getBitmap() {
        return bitmap;
    }




    //네이버 NMT번역 API
    private String getResult(String s) {
        String clientId = "tA0Z9ODHFgLw9ZzCqE2X";//애플리케이션 클라이언트 아이디값";
        String clientSecret = "RGT1vLmpFV";//애플리케이션 클라이언트 시크릿값";
        StringBuffer response = null;
        try {
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
            response = new StringBuffer();
            while ((inputLine = br.readLine()) != null) {
                response.append(inputLine);
            }
            br.close();

        //response에는 JSON형태로 번역결과가 저장되어 있으므로 파싱할 필요가 있음
        if(response != null) {
            Gson gson = new GsonBuilder().create();
            JsonParser parser = new JsonParser();
            JsonElement rootObj = parser.parse(response.toString()).getAsJsonObject().get("message").getAsJsonObject().get("result");
            TranslatedItem item = gson.fromJson(rootObj.toString(), TranslatedItem.class);

            String result = item.getTranslatedItem(rootObj.toString()).substring(57,item.getTranslatedItem(rootObj.toString()).length()-2); //104번줄에서 다 처리하지 못한 JSON형식 문자열 처리.
            return result;

        }
        else{
            return response.toString();
        }
        } catch (Exception e) {
            Toast.makeText(getApplicationContext(), "번역할 문장을 찾지 못했습니다.", Toast.LENGTH_LONG).show();
            e.printStackTrace();
            return "여기에 결과가 출력됩니다.";
        }
    }



    private class TranslatedItem {
        String TranslatedText;

        public String getTranslatedItem(String s) {
            TranslatedText = s;
            return TranslatedText;
        }
    }

    //맨위에 스레드에서 getResult(target)이후의 구문을 처리함. textView에 결과가 출력됨
    Handler handler = new Handler() {
        public void handleMessage(Message msg) {
            Bundle bun = msg.getData();
            String naverHtml = bun.getString("NAVER_HTML");

            textView.setText(naverHtml);
        }
    };

}

