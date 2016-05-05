package com.iems5722.group11;


import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

import io.socket.client.IO;
import io.socket.client.Socket;
import io.socket.emitter.Emitter;

public class ViewingActivity extends AppCompatActivity {
    private final static String TAG = "Viewing Activity";

    private DrawingView drawView;
    private Button sendBtn;
    private EditText messageBox;

    Socket socket;
    private String myRoomId;
    private String myUserId;
    private Puzzle puzzle;

    private LinearLayout viewLinear;
    private LinearLayout readyView;
    private LinearLayout finishView;
    private TextView finishInfo;
    private TextView readyInfo;

    //for timer
    private TextView timerView;
    private Timer timer;
    private TimerTask timerTask;

    private int count = 59;
    private static int delay = 1000;  //1s
    private static int period = 1000;  //1s

    private static final int UPDATE_TEXTVIEW = 0;

    private Handler timerHandler;

    private Activity thisActivity;


    //for mssageshowing
    private ListView chatMessageLV;
    private ArrayList<String> chatMessages;
    private MessageAdapter chatMessageAdapter;

    private TextView puzzleTV;

    private int width;

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus) {
            //width = drawView.getWidth();
            Log.i(TAG, "width=" + width);
        }
        //Toast.makeText(getApplicationContext(),"width = "+drawView.getWidth()+" height = "+drawView.getHeight(),Toast.LENGTH_LONG).show();
        //Here you can get the size!
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_viewing);
        //      ActionBar actionBar = getSupportActionBar();
        //     actionBar.setDisplayHomeAsUpEnabled(true);
        width  = getWindowManager().getDefaultDisplay().getWidth();       // 屏幕宽（像素，如：480px）
        myRoomId = getIntent().getStringExtra("myRoomId");
        myUserId = getIntent().getStringExtra("myUserId");
        puzzle = new Puzzle("a","b","c");
        puzzle = (Puzzle) getIntent().getSerializableExtra("puzzle");
        chatMessages = new ArrayList<>();
        thisActivity = this;


        findViews();
        chatMessageAdapter = new MessageAdapter(this, chatMessages);

//        chatMessageAdapter = new ArrayAdapter(this, android.R.layout.simple_expandable_list_item_1, chatMessages);
        chatMessageLV.setAdapter(chatMessageAdapter);


        drawView.setDrawer(false);
        puzzleTV.setText(getString(R.string.hint)+"\n"+puzzle.getHintA()+", "+puzzle.getHintB());
        //myRoomId = "8888";
        //myUserId = "diane_fake";



        try{
            socket = IO.socket(getString(R.string.server_ip));
            socket.on(Socket.EVENT_CONNECT,onConnectSuccess);
            socket.on("join_broadcasting",onJoinBroadcasting);
            socket.on("pos_broadcasting",onPosBroadcasting);
            socket.on("game_ready_broadcasting", onReadyBroadcasting);
            socket.on("game_finish_broadcasting",onFinishBroadcasting);
            socket.on("message_send_broadcasting",onMessageBroadcasting);
            socket.on("game_quit_broadcasting",onQuitBroadcasting);
           // socket.on("leave_broadcasting", onLeaveBroadcasting);

            socket.connect();

        }catch (Exception e){
            e.printStackTrace();
        }

        try {
            JSONObject json = new JSONObject();
            json.put("room_id", myRoomId);
            json.put("user_id", myUserId);
            Log.i(TAG, json.toString());
            socket.emit("join_room", json);
        } catch (Exception e) {
                e.printStackTrace();
        }

        sendBtn.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View v) {
                String myMessage = messageBox.getText().toString();
                if (myMessage.equals("")) {
                    return;
                } else if (myMessage.trim().equals("")) {
                    messageBox.setText("");
                    return;
                }
                if (myMessage.toLowerCase().equals(puzzle.getAnswer().toLowerCase())){
                    String messageSend = myUserId+getString(R.string.view_correct);
                    try {
                        JSONObject json = new JSONObject();
                        JSONObject data = new JSONObject();
                        json.put("room_id", myRoomId);
                        json.put("user_id", myUserId);
                        data.put("is_correct","true");
                        data.put("message",messageSend);
                        json.put("data",data);
                        Log.i(TAG, json.toString());
                        Log.i(TAG, "send message:" + json.toString());
                        socket.emit("message_send",json);
                    } catch (Exception e){
                        e.printStackTrace();
                    }
                }
                else{
                    String messageSend = myUserId+": "+myMessage;
                    try {
                        JSONObject json = new JSONObject();
                        JSONObject data = new JSONObject();
                        json.put("room_id", myRoomId);
                        json.put("user_id", myUserId);
                        data.put("is_currect",false);
                        data.put("message",messageSend);
                        json.put("data",data);
                        Log.i(TAG, "send message:" + json.toString());
                        socket.emit("message_send", json);
                    } catch (Exception e){
                        e.printStackTrace();
                    }
                }

                messageBox.setText("");
                // TODO: 16/4/19 for testing need to be deleted
                // chatMessages.add(myMessage);
                //chatMessageAdapter.notifyDataSetChanged();
                //    messageField.setText(myMessage);
                //    messageField.setSelected(true);


            }
        });
        initTimer();
        initLoadingView();



    }
    @Override
    public void onDestroy() {
        super.onDestroy();

    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        //   return super.onKeyDown(keyCode, event);
        if (keyCode == KeyEvent.KEYCODE_BACK )
        {
            // 创建退出对话框
            AlertDialog isExit = new AlertDialog.Builder(this).create();
            // 设置对话框标题
            isExit.setTitle(getString(R.string.exit_dialog_title));
            // 设置对话框消息
            isExit.setMessage(getString(R.string.exit_dialog_content));
            // 添加选择按钮并注册监听
            isExit.setButton(getString(R.string.exit_dialog_yes), dialogListener);
            isExit.setButton2(getString(R.string.exit_dialog_cancel), dialogListener);
            // 显示对话框
            isExit.show();

        }
        return false;
    }
    /**监听对话框里面的button点击事件*/
    DialogInterface.OnClickListener dialogListener = new DialogInterface.OnClickListener()
    {
        public void onClick(DialogInterface dialog, int which)
        {
            switch (which)
            {
                case AlertDialog.BUTTON_POSITIVE:// "确认"按钮退出程序
                    finish();
                    stopTimer();
                    break;
                case AlertDialog.BUTTON_NEGATIVE:// "取消"第二个按钮取消对话框
                    break;
                default:
                    break;
            }
        }
    };

    @Override
    protected void onStop() {
        super.onStop();
        JSONObject json = new JSONObject();
        try{
            json.put("user_id", myUserId);
            json.put("room_id", myRoomId);


        } catch (Exception e){
            e.printStackTrace();
        }
        socket.emit("leave_room", json);
        socket.disconnect();
        socket.off();
       /* socket.off(Socket.EVENT_CONNECT, onConnectSuccess);
        socket.off("join_broadcasting", onJoinBroadcasting);
        socket.off("pos_broadcasting", onPosBroadcasting);
        socket.off("game_ready_broadcasting", onReadyBroadcasting);
        socket.off("game_finish_broadcasting", onFinishBroadcasting);
        socket.off("message_send_broadcasting", onMessageBroadcasting);
        socket.off("game_quit_broadcasting", onQuitBroadcasting);
        socket.disconnect();*/
        // socket.off("leave_broadcasting", onLeaveBroadcasting);
        stopTimer();
        timer = null;
        timerTask = null;

    }

    private void findViews(){
        drawView = (DrawingView)findViewById(R.id.viewing);
        sendBtn = (Button)findViewById(R.id.send_btn);
        chatMessageLV = (ListView)findViewById(R.id.message_field);
        viewLinear = (LinearLayout)findViewById(R.id.view_linear);
        readyView = (LinearLayout)findViewById(R.id.view_ready);
        timerView = (TextView) findViewById(R.id.view_timer);
        puzzleTV = (TextView)findViewById(R.id.view_puzzle);
        messageBox = (EditText)findViewById(R.id.view_message);
        readyInfo = (TextView)findViewById(R.id.view_ready_tv);

        finishInfo = (TextView)findViewById(R.id.view_result);
        finishView = (LinearLayout)findViewById(R.id.view_finish);




    }
    private void updateTimer() {
        timerView.setText(String.valueOf(count));
    }

    private void startTimer() {
        timer.schedule(timerTask, delay, period);
    }

    private void stopTimer() {
        timer.cancel();
        timerTask.cancel();
    }

    public void sendTimerMessage(int id) {
        if (timerHandler != null) {
            Message message = Message.obtain(timerHandler, id);
            timerHandler.sendMessage(message);
        }
    }


    private void initTimer() {
        timerHandler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                switch (msg.what) {
                    case UPDATE_TEXTVIEW:
                        updateTimer();
                        break;
                    default:
                        break;

                }
            }
        };
        timer = new Timer();
        timerTask = new TimerTask() {
            @Override
            public void run() {
                Log.i(TAG, "Timer: count: " + String.valueOf(count));
                sendTimerMessage(UPDATE_TEXTVIEW);
                try {
                    Log.i(TAG, "Sleep(1000)...");
                    Thread.sleep(1000);
                    count--;
                    if (count <= 0) {
                        stopTimer();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }

            }
        };

    }
    private Emitter.Listener onJoinBroadcasting = new Emitter.Listener(){
        @Override
        public void call(Object... args) {
            try {
                JSONObject emitdata = (JSONObject) args[0];
                Log.i(TAG, "emitdata: " + emitdata.toString());
                String userId = emitdata.getString("user_id");
                String roomId = emitdata.getString("room_id");
                Log.i(TAG, "user_id " + userId + " has joined room " + roomId);
                //userIds.add(userId);


            } catch (Exception e ){
                e.printStackTrace();
            }
        }
    };

    private Emitter.Listener onConnectSuccess = new Emitter.Listener(){
        @Override
        public void call(Object... args) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Log.i(TAG, "connected");
                }
            });
        }
    };

    private Emitter.Listener onPosBroadcasting = new Emitter.Listener(){
        @Override
        public void call(Object... args) {
            try{
                JSONObject emitdata = (JSONObject) args[0];
                Log.i(TAG,"emitdata: "+ emitdata.toString());
                JSONObject data = emitdata.getJSONObject("data");
                final boolean isErase = data.getBoolean("is_erase");
                final float brushSize = Float.parseFloat(data.getString("brush_size"));
                final int paintColor = data.getInt("paint_color");

                final float posX = Float.parseFloat(data.getString("pos_x"));

                final float posY = Float.parseFloat(data.getString("pos_y"));
                Log.d(TAG,"brushsize: "+ brushSize);


                final int action = data.getInt("action");
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        drawView.getDrawPaint().setColor(paintColor);
                        drawView.getDrawPaint().setStrokeWidth(brushSize);
                        drawView.setErase(isErase);
                        // drawView.setBrushSetting(brushSetting);
                        drawView.setPath(action,posX*width,posY*width);
                    }
                });

            } catch (Exception e){
                e.printStackTrace();
            }
        }
    };

    private void initLoadingView() {
        finishView.setVisibility(View.GONE);
        viewLinear.setVisibility(View.GONE);
        readyView.setVisibility(View.VISIBLE);
        readyInfo.setText(R.string.ready_info_view);

       /* readyView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                viewLinear.setVisibility(View.VISIBLE);
                readyView.setVisibility(View.GONE);
                Log.d(TAG, "click ready view");
                startTimer();

            }
        });*/
    }
    private Emitter.Listener onReadyBroadcasting = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            Log.i(TAG,"on ready broadcasting");
            try {
                Thread.sleep(2000);
            }catch ( Exception e){
                e.printStackTrace();
            }
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    cancelLoadingView();
                }
            });
            startTimer();
        }
    };
    private Emitter.Listener onMessageBroadcasting = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            try {
                JSONObject emitdata = (JSONObject) args[0];
                Log.i(TAG, "receive: " + emitdata.toString());
                String userId = emitdata.getString("user_id");
                String roomId = emitdata.getString("room_id");
                JSONObject data = emitdata.getJSONObject("data");
                final String message = data.getString("message");
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        chatMessages.add(message);
                        chatMessageAdapter.notifyDataSetChanged();
                    }
                });

            } catch (Exception e) {
                e.printStackTrace();
            }

        }
    };
    private Emitter.Listener onLeaveBroadcasting = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(getApplicationContext(), R.string.sb_left, Toast.LENGTH_LONG);
                }
            });
            thisActivity.finish();

        }
    };

    private Emitter.Listener onFinishBroadcasting = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    stopTimer();
                }
            });
            try{
                JSONObject emitdata = (JSONObject) args[0];
                Log.i(TAG,"finish this round");
                Log.i(TAG,emitdata.toString());
                JSONObject data = emitdata.getJSONObject("data");
                JSONArray members = data.getJSONArray("members");
                JSONObject puzzleJson = data.getJSONObject("puzzle");
                String answer = puzzleJson.getString("answer");
                String hintA = puzzleJson.getString("hint_a");
                String hintB = puzzleJson.getString("hint_b");
                final Integer right = data.getInt("right");
                // TODO: 16/4/20 generate game over view
                Puzzle newPuzzle = new Puzzle(answer, hintA, hintB);
                ArrayList<GameMember> gameMembers = new ArrayList<>();
                int length = members.length();
                boolean isDraw = false;
                for (int i = 0 ; i < length; i++){
                    JSONObject jo = members.getJSONObject(i);
                    GameMember gameMember = new GameMember(jo.getString("user_id"),jo.getString("room_id"),
                            jo.getBoolean("is_cur_host"),jo.getBoolean("is_pre_host"),jo.getBoolean("is_next_host"));
                    gameMembers.add(gameMember);
                    if (jo.getString("user_id").equals(myUserId) && jo.getBoolean("is_cur_host") ){
                        isDraw = true;
                    }
                }
                if (isDraw){
                    Intent intent = new Intent(ViewingActivity.this, DrawingActivity.class);
                    intent.putExtra("gameMembers",gameMembers);
                    intent.putExtra("myUserId",myUserId);
                    intent.putExtra("myRoomId", myRoomId);
                    intent.putExtra("puzzle", newPuzzle);
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            initFinishView(right);

                        }
                    });
                    try{
                        Thread.sleep(3000);
                    } catch (Exception e){
                        e.printStackTrace();
                    }
                    socket.disconnect();
                    socket.off();
                    startActivity(intent);
                 /*   socket.off("join_broadcasting", onJoinBroadcasting);
                    socket.off("pos_broadcasting", onPosBroadcasting);
                    socket.off("game_ready_broadcasting", onReadyBroadcasting);
                    socket.off("message_send_broadcasting", onMessageBroadcasting);
                    socket.off("game_quit_broadcasting", onQuitBroadcasting);*/
                 //   socket.off("leave_broadcasting", onLeaveBroadcasting);

                    thisActivity.finish();
                }
                else{
                    Intent intent = new Intent(ViewingActivity.this, ViewingActivity.class);
                    intent.putExtra("myUserId",myUserId);
                    intent.putExtra("myRoomId", myRoomId);
                    intent.putExtra("puzzle", newPuzzle);
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            initFinishView(right);

                        }
                    });
                    try{
                        Thread.sleep(3000);
                    } catch (Exception e){
                        e.printStackTrace();
                    }
                    socket.disconnect();
                    socket.off();
                    startActivity(intent);
                    /*socket.off("join_broadcasting", onJoinBroadcasting);
                    socket.off("pos_broadcasting", onPosBroadcasting);
                    socket.off("game_ready_broadcasting", onReadyBroadcasting);
                    socket.off("message_send_broadcasting", onMessageBroadcasting);
                    socket.off("game_quit_broadcasting", onQuitBroadcasting);*/
               //     socket.off("leave_broadcasting", onLeaveBroadcasting);

                    thisActivity.finish();
                }


            } catch (Exception e){
                e.printStackTrace();
            }
        }
    };

    private void cancelLoadingView() {
        viewLinear.setVisibility(View.VISIBLE);
        readyView.setVisibility(View.GONE);
        Log.d(TAG, "click ready view");
    }
    private Emitter.Listener onQuitBroadcasting = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            //  Intent intent = new Intent(ViewingActivity.this, MainActivity.class);
            //  startActivity(intent);
            try {
                JSONObject emitdata = (JSONObject) args[0];
                Log.i(TAG, "finish this round");
                Log.i(TAG, emitdata.toString());
                JSONObject data = emitdata.getJSONObject("data");
                final Integer right = data.getInt("right");
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        initFinishView(right);
                    }
                });
                try{
                    Thread.sleep(3000);
                } catch (Exception e){
                    e.printStackTrace();
                }
            }catch (Exception e){
                e.printStackTrace();
            }

            socket.off("join_broadcasting", onJoinBroadcasting);
            socket.off("pos_broadcasting", onPosBroadcasting);
            socket.off("game_ready_broadcasting", onReadyBroadcasting);
            socket.off("message_send_broadcasting", onMessageBroadcasting);
            socket.off("game_finish_broadcasting", onFinishBroadcasting);

            thisActivity.finish();
            // TODO: 16/4/20  jump to last activity
        }
    };

    private void initFinishView(Integer right){
        finishInfo.setText(getString(R.string.finish_answer) + " "+puzzle.getAnswer() + "\n" + getString(R.string.correct_num) + " " +(right-1));
        readyView.setVisibility(View.GONE);
        viewLinear.setVisibility(View.GONE);
        finishView.setVisibility(View.VISIBLE);

    }

}
