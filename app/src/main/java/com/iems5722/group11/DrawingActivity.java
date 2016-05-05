package com.iems5722.group11;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

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
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Timer;
import java.util.TimerTask;

import io.socket.client.IO;
import io.socket.client.Socket;
import io.socket.emitter.Emitter;

public class DrawingActivity extends AppCompatActivity {

    private final static String TAG = "Drawing Activity";
    private DrawingView drawView;
    private ImageButton currPaint, drawBtn, eraseBtn;
    private LinearLayout paintLayout;
    private SeekBar brushSizeBar;
    private SeekBar eraseSizeBar;

    Socket socket;


    private float brushSize;
    private float eraseSize;

    private String myRoomId;
    private String myUserId;
    private ArrayList<GameMember> gameMembers;
    private Puzzle puzzle, newPuzzle;

    private LinearLayout drawLinear;
    private LinearLayout readyView;
    private LinearLayout finishView;
    private TextView finishInfo;
    private TextView readyInfo;

    private TextView puzzleTV;

    //for timer
    private TextView timerView;
    private Timer timer;
    private TimerTask timerTask;

    private int count = 59;
    private static int delay = 1000;  //1s
    private static int period = 1000;  //1s

    private static final int UPDATE_TEXTVIEW = 0;

    private Handler timerHandler;

    //for answer checking
    private int totalNum;
    private int curNum;
    private int corrctNum;

    private Activity thisActivity;

    private boolean started;

    private int width;
    /**
     * ATTENTION: This was auto-generated to implement the App Indexing API.
     * See https://g.co/AppIndexing/AndroidStudio for more information.
     */
    //private GoogleApiClient client;

    //for mssageshowing
    private ListView chatMessageLV;
    private ArrayList<String> chatMessages;
    private MessageAdapter chatMessageAdapter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_drawing);
        //   ActionBar actionBar = getSupportActionBar();
        //  actionBar.setDisplayHomeAsUpEnabled(true);

        width  = getWindowManager().getDefaultDisplay().getWidth();       // 屏幕宽（像素，如：480px）
        myRoomId = getIntent().getStringExtra("myRoomId");
        myUserId = getIntent().getStringExtra("myUserId");
        //mySelf = new GameMember();
        //mySelf = (GameMember) getIntent().getSerializableExtra("mySelf");
        Log.i(TAG, "myRoomId: " + myRoomId);
        Log.i(TAG, "myUserId:" + myUserId);
        started = false;

        gameMembers = new ArrayList<>();
        gameMembers = (ArrayList<GameMember>) getIntent().getSerializableExtra("gameMembers");
        puzzle = (Puzzle) getIntent().getSerializableExtra("puzzle");
        for (GameMember member : gameMembers) {
            Log.i(TAG, "new incomer");
            Log.i(TAG, "RoomId: " + member.getRoomId());
            Log.i(TAG, "UserId:" + member.getUserId());
            Log.i(TAG, "isHost" + member.isCurHost());
            Log.i(TAG, "isNextHost" + member.isNextHost());
            Log.i(TAG, "isPreHost" + member.isPreHost());

        }
        new getPuzzle().execute(getString(R.string.server_ip) + getString(R.string.puzzle_id_api), myUserId);
        totalNum = gameMembers.size();

        // TODO: 16/4/22 have to be deleted!!!!!
        corrctNum = 1;
        curNum = 0;
        Log.i(TAG, "total number: " + totalNum);
        Log.i(TAG, "Puzzle: "+puzzle.getAnswer()+puzzle.getHintA()+puzzle.getHintB());
        thisActivity = this;


        //  myRoomId = "8888";
        // myUserId = "diane";


        findViews();

        puzzleTV.setText(getString(R.string.answer) + "\n" + puzzle.getAnswer());

        chatMessages = new ArrayList<>();
        chatMessageAdapter = new MessageAdapter(this, chatMessages);
      //  chatMessageAdapter = new ArrayAdapter(this, android.R.layout.simple_expandable_list_item_2, chatMessages);
        chatMessageLV.setAdapter(chatMessageAdapter);

        initTimer();
        initDrawView();
        initLoadingView();
        //  Log.i(TAG, "before finish view");
        //  initFinishView(); //// TODO: 16/4/20 for testing


        connectSocket();
        socketJoinRoom();

        //finishThisRound();
        //timer.schedule(timerTask, delay, period);

        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        //client = new GoogleApiClient.Builder(this).addApi(AppIndex.API).build();
    }

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
    //    socket.emit("leave_room", json);
        socket.disconnect();
        socket.off();
       //socket.off("leave_broadcasting",onLeaveBroadcasting);
        stopTimer();
        timer = null;
        timerTask = null;

        Log.i(TAG,"finished");

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.i(TAG, "destroyed");





    }


    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        //width = drawView.getWidth();
        //Toast.makeText(getApplicationContext(),"width = "+drawView.getWidth()+" height = "+drawView.getHeight(),Toast.LENGTH_LONG).show();
        //Here you can get the size!
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

    public void paintClicked(View view) {
        if (view != currPaint) {
            ImageButton imgView = (ImageButton) view;
            String color = view.getTag().toString();
            drawView.setColor(color);
            imgView.setImageDrawable(getResources().getDrawable(R.drawable.paint_pressed));
            currPaint.setImageDrawable(getResources().getDrawable(R.drawable.paint));
            currPaint = (ImageButton) view;
        }
        //use chosen color
    }


    private Emitter.Listener onConnectSuccess = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            Log.i(TAG, "on connect success");
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Log.i(TAG, "connected");
                }
            });
        }
    };

    private Emitter.Listener onReadyBroadcasting = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            Log.i(TAG, "on ready broadcasting");
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
    private Emitter.Listener onJoinBroadcasting = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            Log.i(TAG, "on join broadcasting");
            if(!started) {
                try {
                    JSONObject emitdata = (JSONObject) args[0];
                    Log.i(TAG, "emitdata: " + emitdata.toString());
                    String userId = emitdata.getString("user_id");
                    String roomId = emitdata.getString("room_id");
                    Log.i(TAG, "user_id " + userId + " has joined room " + roomId);
                    JSONObject sdata = emitdata.getJSONObject("sdata");
                    JSONArray userList = sdata.getJSONArray("user_list");

                    //userIds.add(userId);
                    curNum = userList.length(); // TODO: 16/4/19 修改curnum的计算方法
                    Log.i(TAG, "totalNum=" + totalNum + " curNum=" + curNum);
                    if (curNum == totalNum) {
                        started = true;
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                //cancelLoadingView();
                                JSONObject json = new JSONObject();
                                try {
                                    json.put("user_id", myUserId);
                                    json.put("room_id", myRoomId);
                                    socket.emit("game_ready", json);
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }

                                //   puzzleTV.setText(puzzle.getAnswer());

                            }
                        });
                        //startTimer();
                    }


                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    };

    private Emitter.Listener onMessageBroadcasting = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            Log.i(TAG, "on message braodcasting");
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
                String isCorrect = data.getString("is_correct");
                if (isCorrect.equals("true")) {
                    Log.i(TAG, userId + " is correct");
                    corrctNum++;
                    if (corrctNum == totalNum) {
                        finishThisRound();
                    }
                } else {
                    Log.i(TAG, userId + ": " + message);
                }

            } catch (Exception e) {
                e.printStackTrace();
            }

        }
    };

    private Emitter.Listener onFinishBroadcasting = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            Log.i(TAG, "finish this round");
            Intent intent = new Intent(DrawingActivity.this, ViewingActivity.class);
            intent.putExtra("myUserId", myUserId);
            intent.putExtra("myRoomId", myRoomId);
            intent.putExtra("puzzle", newPuzzle);
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    initFinishView();
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
            thisActivity.finish();
        }
    };

    private Emitter.Listener onQuitBroadcasting = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            Log.i(TAG, "on quit bradcasting");
            //Intent intent = new Intent(DrawingActivity.this, MainActivity.class);
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    initFinishView();
                }
            });
            try{
                Thread.sleep(3000);
            } catch (Exception e){
                e.printStackTrace();
            }
            //startActivity(intent);
            thisActivity.finish();
            // TODO: 16/4/20  jump to last activity
        }
    };
    private Emitter.Listener onLeaveBroadcasting = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            Log.i(TAG,"on leave broadcasting");
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(getApplicationContext(), R.string.sb_left, Toast.LENGTH_LONG);
                }
            });
            thisActivity.finish();

        }
    };


    private void finishThisRound() {
        Iterator<GameMember> iterator = gameMembers.iterator();
        stopTimer();
        while (iterator.hasNext()) {
            GameMember member = iterator.next();
            if (member.getUserId().equals(myUserId)) {
                member.setCurHost(false);
                member.setPreHost(true);
                break;
            }
        }
        boolean finished = true;
        iterator = gameMembers.iterator();
        while (iterator.hasNext()) {
            GameMember member = iterator.next();
            if (!member.isPreHost()) {
                member.setCurHost(true);
                finished = false;
                break;
            }
        }
        if (finished) {
            socketQuitGame();
        } else {
            socketFinishGame();
            // thisActivity.finish();

        }

    }

    private boolean updatePos(MotionEvent event) {
        try {
            JSONObject json = new JSONObject();
            JSONObject data = new JSONObject();
            data.put("brush_size", drawView.getDrawPaint().getStrokeWidth());
            // data.put("brush_size",dra)
            //data.put("brush_size",drawView.getDrawPaint().getStrokeWidth());
            data.put("paint_color", drawView.getDrawPaint().getColor());
            data.put("is_erase", drawView.getErase());
            data.put("action", event.getAction());
            data.put("pos_x", event.getX()/width);
            data.put("pos_y", event.getY()/width);
            json.put("user_id", myUserId);
            json.put("room_id", myRoomId);
            json.put("data", data);
            Log.i(TAG, json.toString());
            // Log.d(TAG,"brushsize: "+ brushSize);
            socket.emit("pos_update", json);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }


    private void updateTimer() {
        timerView.setText(String.valueOf(count));
    }

    private void startTimer() {
        timer.schedule(timerTask, delay, period);
    }

    private void stopTimer() {
        Log.i(TAG, "timer stopped");
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
                        //stopTimer();
                        finishThisRound();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }

            }
        };

    }

    private void findViews() {
        drawView = (DrawingView) findViewById(R.id.drawing);
        drawLinear = (LinearLayout) findViewById(R.id.draw_linear);
        readyView = (LinearLayout) findViewById(R.id.draw_ready);
        finishView = (LinearLayout) findViewById(R.id.draw_finish);
        finishInfo = (TextView) findViewById(R.id.draw_result);
        timerView = (TextView) findViewById(R.id.draw_timer);
        brushSizeBar = (SeekBar) findViewById(R.id.brush_size);
        eraseSizeBar = (SeekBar) findViewById(R.id.erase_size);
        drawBtn = (ImageButton) findViewById(R.id.draw_btn);
        paintLayout = (LinearLayout) findViewById(R.id.paint_colors);
        eraseBtn = (ImageButton) findViewById(R.id.erase_btn);
        chatMessageLV = (ListView) findViewById(R.id.draw_chat);
        puzzleTV = (TextView)findViewById(R.id.draw_puzzle);

        readyInfo = (TextView)findViewById(R.id.draw_ready_tv);



    }

    private void cancelLoadingView() {
        drawLinear.setVisibility(View.VISIBLE);
        readyView.setVisibility(View.GONE);
        Log.d(TAG, "click ready view");
    }

    private void initLoadingView() {
        finishView.setVisibility(View.GONE);
        drawLinear.setVisibility(View.GONE);
        readyView.setVisibility(View.VISIBLE);
        readyInfo.setText(getString(R.string.ready_info_draw));
  /*      try {
            Thread.sleep(2000);
        } catch (Exception e){
            e.printStackTrace();
        }*/

        // TODO: 16/4/19 only for testing, need to be deleted
       /* readyView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                drawLinear.setVisibility(View.VISIBLE);
                readyView.setVisibility(View.GONE);
                Log.d(TAG, "click ready view");
                startTimer();

            }
        });*/
    }
    private void initFinishView(){
        finishInfo.setText(getString(R.string.finish_answer) +" "+ puzzle.getAnswer() + "\n" + getString(R.string.correct_num) +" "+ (corrctNum-1));
        readyView.setVisibility(View.GONE);
        drawLinear.setVisibility(View.GONE);
        finishView.setVisibility(View.VISIBLE);

    }

    private void connectSocket() {

        try {
            socket = IO.socket(getString(R.string.server_ip));
            socket.on(Socket.EVENT_CONNECT, onConnectSuccess);
            socket.on("join_broadcasting", onJoinBroadcasting);
            socket.on("game_ready_broadcasting",onReadyBroadcasting);
            socket.on("game_finish_broadcasting",onFinishBroadcasting);
            socket.on("message_send_broadcasting",onMessageBroadcasting);
            socket.on("game_quit_broadcasting",onQuitBroadcasting);
           // socket.on("leave_broadcasting", onLeaveBroadcasting);
            //socket.on("pos_broadcasting",onPosBroadcasting);
            socket.connect();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void socketJoinRoom() {

        try {
            JSONObject json = new JSONObject();
            json.put("room_id", myRoomId);
            json.put("user_id", myUserId);
            Log.i(TAG, json.toString());
            socket.emit("join_room", json);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void initDrawView() {
        drawView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                drawView.setPath(event.getAction(), event.getX(), event.getY());
                return updatePos(event);

            }
        });

        drawView.setDrawer(true);


        currPaint = (ImageButton) paintLayout.getChildAt(0);
        currPaint.setImageDrawable(getResources().getDrawable(R.drawable.paint_pressed));
        drawBtn.setImageDrawable(getResources().getDrawable(R.drawable.brush_pressed));


        eraseSizeBar.setEnabled(false);
        drawBtn.setOnClickListener(new ImageButton.OnClickListener() {
            @Override
            public void onClick(View v) {
                drawView.setErase(false);
                drawView.setBrushSize(brushSize);
                drawBtn.setImageDrawable(getResources().getDrawable(R.drawable.brush_pressed));
                eraseBtn.setImageDrawable(getResources().getDrawable(R.drawable.eraser));
                brushSizeBar.setEnabled(true);
                eraseSizeBar.setEnabled(false);
            }
        });
        eraseBtn.setOnClickListener(new ImageButton.OnClickListener() {
            @Override
            public void onClick(View v) {
                drawView.setErase(true);
                drawView.setBrushSize(eraseSize);
                drawBtn.setImageDrawable(getResources().getDrawable(R.drawable.brush));
                eraseBtn.setImageDrawable(getResources().getDrawable(R.drawable.eraser_pressed));

                eraseSizeBar.setEnabled(true);
                brushSizeBar.setEnabled(false);

            }
        });


        brushSizeBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            private int size = 0;

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                size = progress;

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                brushSize = size / 3.0f + 5;
                drawView.setBrushSize(brushSize);

            }
        });

        eraseSizeBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            private int size = 0;

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                size = progress;

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                eraseSize = size / 3.0f + 5;
                drawView.setBrushSize(eraseSize);

                // drawView.setBrushSize(eraseSize);

            }
        });

    }

    private void socketFinishGame() {

        try {
            JSONObject json = new JSONObject();
            json.put("room_id", myRoomId);
            json.put("user_id", myUserId);
            JSONObject data = new JSONObject();
            Iterator<GameMember> iterator = gameMembers.iterator();
            JSONArray jsonMembers = new JSONArray();

            //ArrayList<String> listMembers = new ArrayList<>();
            while (iterator.hasNext()){
                GameMember member = iterator.next();
                JSONObject tmp = new JSONObject();
                tmp.put("user_id",member.getUserId());
                tmp.put("room_id",member.getRoomId());
                tmp.put("is_cur_host",member.isCurHost());
                tmp.put("is_pre_host",member.isPreHost());
                tmp.put("is_next_host", member.isNextHost());
                jsonMembers.put(tmp);
                //listMembers.add(tmp);
            }
            data.put("members",jsonMembers);
            JSONObject puzzleJson = new JSONObject();
            puzzleJson.put("answer",newPuzzle.getAnswer() );
            puzzleJson.put("hint_a",newPuzzle.getHintA());
            puzzleJson.put("hint_b",newPuzzle.getHintB());
            data.put("puzzle", puzzleJson);
            data.put("right", corrctNum);
            json.put("data", data);
            Log.i(TAG, "emit json: " + json.toString());
            socket.off(Socket.EVENT_CONNECT, onConnectSuccess);
            socket.off("join_broadcasting", onJoinBroadcasting);
            socket.off("game_ready_broadcasting", onReadyBroadcasting);
            socket.off("message_send_broadcasting", onMessageBroadcasting);
            socket.off("game_quit_broadcasting", onQuitBroadcasting);
           // socket.off("leave_broadcasting", onLeaveBroadcasting);
            socket.emit("game_finish",json);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void socketQuitGame() {

        try {
            JSONObject json = new JSONObject();
            json.put("room_id", myRoomId);
            json.put("user_id", myUserId);
            JSONObject data = new JSONObject();
            data.put("right", corrctNum);
            json.put("data", data);
            //data.put("puzzle",nextPuzzle);
            Log.i(TAG, "game is going to finish");
            socket.off(Socket.EVENT_CONNECT, onConnectSuccess);
            socket.off("join_broadcasting", onJoinBroadcasting);
            socket.off("game_ready_broadcasting", onReadyBroadcasting);
            socket.off("game_finish_broadcasting", onFinishBroadcasting);
            socket.off("message_send_broadcasting", onMessageBroadcasting);
           // socket.off("leave_broadcasting", onLeaveBroadcasting);
            socket.emit("game_quit", json);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    class getPuzzle extends AsyncTask<String, Void, String> {
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
                Toast.makeText(getApplicationContext(),"Server Error",Toast.LENGTH_SHORT).show();
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
                        //  JSONObject puzzle  = json.getJSONObject("")
                        newPuzzle = new Puzzle(data.getString("answer"),data.getString("hint_a"),data.getString("hint_b"));
                        //   myRoomId = data.getString("room_id");
                        Log.i(TAG, "puzzle="+newPuzzle.getAnswer());

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

       /*drawFrame =(FrameLayout)findViewById(R.id.draw_frame);
        final RelativeLayout relativeLayout = new RelativeLayout(getApplicationContext());
        relativeLayout.setLayoutParams(new RelativeLayout.LayoutParams(ViewGroup.LayoutParams
                .MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        relativeLayout.setBackgroundColor(Color.DKGRAY);
        relativeLayout.setAlpha(0.7f);

        RelativeLayout.LayoutParams params;
        // Place 1st 30x40 ImageView at (50,60) coordinates
        params = new RelativeLayout.LayoutParams(100, 100);
        params.leftMargin = 20;
        params.topMargin = 50;
        final ImageView imageView1 = new ImageView(getApplicationContext());
        imageView1.setImageDrawable(getResources().getDrawable(R.drawable.brush));
        // Add 1st imageview to relative layout
        relativeLayout.addView(imageView1, params);

        // Place 2nd 30x40 ImageView at (100,60) coordinates
        params = new RelativeLayout.LayoutParams(120, 120);
        params.leftMargin = 800;
        params.topMargin = 450;
        final ImageView imageView2 = new ImageView(getApplicationContext());
        imageView2.setImageDrawable(getResources().getDrawable(R.drawable.brush));
        // Add 2nd imageview to relative layout
        relativeLayout.addView(imageView2, params);

        // finally add it ot the framelayout
        drawFrame.addView(relativeLayout);*/