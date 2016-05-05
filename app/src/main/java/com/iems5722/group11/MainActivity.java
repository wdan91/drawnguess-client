package com.iems5722.group11;


import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

public class MainActivity extends AppCompatActivity {

    EditText userIdET;
    String myUserId;
    Button enterGameBtn;
    private final static String TAG = "main Activity";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        userIdET = (EditText)findViewById(R.id.main_userid_et);
        enterGameBtn = (Button)findViewById(R.id.main_entergame_btn);
        enterGameBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                myUserId = userIdET.getText().toString();
                // TODO: 16/4/18 handle illegal input
                if (myUserId.equals("")) {
                    return;
                } else if (myUserId.trim().equals("")) {
                    userIdET.setText("");
                    return;
                }
                Intent intent = new Intent(MainActivity.this, QRCodeActivity.class);
                Log.i(TAG, "UserId: "+myUserId);
                intent.putExtra("myUserId",myUserId);
                startActivity(intent);
            }
        });


    }




    public void toDrawingActivity(View view){
        Intent intent = new Intent(MainActivity.this,DrawingActivity.class);
        startActivity(intent);
    }
    public void toViewingActivity(View view){
        Intent intent = new Intent(MainActivity.this,ViewingActivity.class);
        startActivity(intent);
    }
    public void toQRCodeActivity(View view){
        //     Intent intent = new Intent(MainActivity.this,GalleryActivity.class);
        //     startActivity(intent);
    }
}
