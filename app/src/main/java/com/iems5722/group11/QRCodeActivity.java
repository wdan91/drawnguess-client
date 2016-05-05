package com.iems5722.group11;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;


import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;
import com.google.zxing.qrcode.QRCodeWriter;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;


public class QRCodeActivity extends AppCompatActivity {
    Button genCode, readCode;//, enterGame;
    // ImageView QRCode;
    Activity myActivity;
    TextView codeResult;

    String myUserId;
    String myRoomId;

    // static final String ACTION_SCAN = "com.google.zxing.client.android.SCAN";
    static String TAG = "QR code scanner";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_qrcode);

        myUserId = getIntent().getStringExtra("myUserId");
        Log.i(TAG, "UserId: " + myUserId);



//        myRoomId = "Andy";
        new getRoomId().execute(getString(R.string.server_ip)+getString(R.string.room_id_api),myUserId);
        Log.i(TAG, "room id ="+ myRoomId);

        findViews();
        // enterGame.setVisibility(View.GONE);



        myActivity = this;

        readCode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                IntentIntegrator integrator = new IntentIntegrator(myActivity);
                integrator.setPrompt(getString(R.string.scan)); //底部的提示文字，设为""可以置空
                integrator.setBeepEnabled(true); //扫描成功的「哔哔」声，默认开启
                integrator.setCaptureActivity(ScanActivity.class);
                integrator.initiateScan();
            }
        });

        //QRCode.setImageBitmap(bm);
        //enterGame.setVisibility(View.VISIBLE);
        //QRCodeDialog.Builder dialogBuilder= new QRCodeDialog.Builder(v.getContext());
        //dialogBuilder.setImage(bm);
        //QRCodeDialog qrCodeDialog = dialogBuilder.create();
        //qrCodeDialog.findViewById(R.id.qr_cancel_btn);

        // qrCodeDialog.setCanceledOnTouchOutside(true);
        //qrCodeDialog.show();
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setPositiveButton(getString(R.string.enter_game), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Intent intent = new Intent(QRCodeActivity.this, WaitingActivity.class);
                intent.putExtra("myUserId",myUserId);
                intent.putExtra("myRoomId",myRoomId);
                intent.putExtra("isHost",true);
                Log.i(TAG, "before start waiting userid: " + myUserId);
                startActivity(intent);
                myActivity.finish();
            }
        }).setNegativeButton(getString(R.string.exit_dialog_cancel), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
            }
        });
        final AlertDialog dialog = builder.create();
        LayoutInflater inflater = getLayoutInflater();
        View dialogLayout = inflater.inflate(R.layout.qrcode_dialog, null);
        dialog.setView(dialogLayout);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);


        dialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface d) {
                ImageView image = (ImageView) dialog.findViewById(R.id.qr_img_dialog);
                float imageWidthInPX = (float)image.getWidth();
                Bitmap bm;
                while (myRoomId==null) {
                    try{
                        Thread.sleep(10);

                    } catch (Exception e){
                        e.printStackTrace();
                    }

                }
                bm = generateQRCode(myRoomId);
                LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(Math.round(imageWidthInPX),
                        Math.round(imageWidthInPX * (float)bm.getHeight() / (float)bm.getWidth()));
                image.setLayoutParams(layoutParams);
                image.setImageBitmap(bm);


            }
        });

        genCode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // TODO: 16/4/18 get roomid from server
                dialog.show();
                //QRCode.setImageBitmap(bm);

            }
        });
        /*enterGame.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(QRCodeActivity.this, WaitingActivity.class);
                intent.putExtra("myUserId",myUserId);
                intent.putExtra("myRoomId",myRoomId);
                intent.putExtra("isHost",true);
                Log.i(TAG, "before start waiting userid: " + myUserId);
                startActivity(intent);
                myActivity.finish();
            }
        });*/


    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, intent);
        if(result != null) {
            if(result.getContents() == null) {
                Log.d(TAG, "Cancelled");
                Toast.makeText(this, "Cancelled", Toast.LENGTH_LONG).show();
            } else {
                Log.d(TAG, "Scanned: " + result.getContents());
                //codeResult.setText(result.getContents());
                Intent newintent = new Intent(QRCodeActivity.this, WaitingActivity.class);
                newintent.putExtra("myUserId",myUserId);
                newintent.putExtra("myRoomId",result.getContents());
                newintent.putExtra("isHost",false);
                startActivity(newintent);
                myActivity.finish();

            }
        }
    }

    private void findViews(){
        genCode = (Button)findViewById(R.id.qr_gen_code_btn);
        readCode = (Button)findViewById(R.id.qr_read_code_btn);
        //enterGame = (Button)findViewById(R.id.qr_enter_game_btn);


        // QRCode = (ImageView)findViewById(R.id.qr_img);
        // codeResult = (TextView)findViewById(R.id.code_result);
    }

    private Bitmap generateQRCode(String qrCodeString){
        Bitmap bmp = null;    //二维码图片
        QRCodeWriter writer = new QRCodeWriter();
        try {
            BitMatrix bitMatrix = writer.encode(qrCodeString, BarcodeFormat.QR_CODE, 300, 300); //参数分别表示为: 条码文本内容，条码格式，宽，高
            int width = bitMatrix.getWidth();
            int height = bitMatrix.getHeight();
            bmp = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565);

            //绘制每个像素
            for (int x = 0; x < width; x++) {
                for (int y = 0; y < height; y++) {
                    bmp.setPixel(x, y, bitMatrix.get(x, y) ? Color.BLACK : Color.WHITE);
                }
            }

        } catch (WriterException e) {
            e.printStackTrace();
        }

        return bmp;
    }
    class getRoomId extends AsyncTask<String, Void, String>{
        @Override
        protected String doInBackground(String... params) {
            String sendResult =  "";
            String line;

            try{
                URL url = new URL(params[0]);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setReadTimeout(10000);
                conn.setConnectTimeout(15000);
                conn.setRequestMethod("POST");
                conn.setDoInput(true);
                conn.setDoOutput(true);

                OutputStream os = conn.getOutputStream();
                BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(os, "UTF-8"));

                Uri.Builder builder = new Uri.Builder();
                //      builder.appendQueryParameter(getString(R.string.sendmessage_param1),mChatRoomId);
                builder.appendQueryParameter("user_id",params[1]);
                String query = builder.build().getEncodedQuery();

                writer.write(query);
                writer.flush();
                writer.close();
                os.close();

                int response = conn.getResponseCode();
                if (response != 200){
                    Toast.makeText(getApplicationContext(),"Server Error",Toast.LENGTH_SHORT).show();
                    return null;

                }

                Log.d("Get roomid", "response = " + response);
                InputStream is = conn.getInputStream();

                BufferedReader br = new BufferedReader(new InputStreamReader(is));
                while ((line = br.readLine()) != null){
                    sendResult += line;
                }


            } catch (Exception e){
               // Toast.makeText(getApplicationContext(),"Server Error",Toast.LENGTH_SHORT).show();
                e.printStackTrace();
                return  null;
            }
            return sendResult;
        }

        @Override
        protected void onPostExecute(String s) {
            if (s != null) {

                try {
                    JSONObject json = new JSONObject(s);
                    String status = json.getString("status");
                    Log.d(TAG, "status=" + status);
                    if (!status.equals("OK")) {
                        Toast.makeText(getApplicationContext(), "Server error", Toast.LENGTH_SHORT).show();
                      //  Log.d("ChatActivity", "not ok");
                    } else {

                        //Log.d("ChatActivity",s);
                        JSONObject data = json.getJSONObject("data");
                        myRoomId = data.getString("room_id");
                        Log.i(TAG, "roomId="+myRoomId);

//                    if(array!=null) {


                        /*if (mChatMessages.size() >= 5) {
                            mListview.setSelection(5);
                            Log.d("ChatActivity", "set selection to 5");
                        }*/
                        //mListview.smoothScrollToPosition(0);
                        //mListview.smoothScrollToPositionFromTop(0,-10);
                    }


                } catch (Exception e) {
                    e.printStackTrace();
                   // Toast.makeText(getApplicationContext(), getString(R.string.servererror), Toast.LENGTH_SHORT).show();
                    return;
                }
            }
            super.onPostExecute(s);
        }
    }


}
