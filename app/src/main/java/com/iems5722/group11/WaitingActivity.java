package com.iems5722.group11;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

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

import io.socket.client.IO;
import io.socket.client.Socket;
import io.socket.emitter.Emitter;

public class WaitingActivity extends AppCompatActivity {

    private final static String TAG = "Waiting Activity";



    ArrayList<GameMember> gameMembers;
    Puzzle puzzle;
    GameMember mySelf;
    ListView gameMemberLV;
    GameMemberAdapter gameMemberAdapter;
    Button startGameBtn;

    String myUserId;
    String myRoomId;
    boolean isHost;

    Socket socket;

    Activity myActivity;

    //for test

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_waiting);

        myRoomId = getIntent().getStringExtra("myRoomId");
        myUserId = getIntent().getStringExtra("myUserId");
        isHost = getIntent().getBooleanExtra("isHost", false);

        Log.i(TAG, myUserId+" "+myRoomId+" "+isHost);

        findViews();

        setTitle(getString(R.string.waiting));
        myActivity = this;

        mySelf = new GameMember(myUserId, myRoomId, isHost, false, false);
        gameMembers = new ArrayList<>();
        gameMembers.add(mySelf);

        try {
            socket = IO.socket(getString(R.string.server_ip));
            socket.on(Socket.EVENT_CONNECT, onConnectSuccess);
            //socket.on("join_broadcasting", onJoinBroadcasting);
            socket.on("join_waiting_room_broadcasting",onJoinBroadcasting);
            socket.on("member_update_broadcasting", onMemberUpdateBroadcasting);
            socket.on("game_join_broadcasting",onStartBroadcasting);
            socket.on("leave_waiting_room_broadcasting",onLeaveBroadcasting);
            socket.on("dismiss_waiting_room_broadcasting",onDismissRoomBroadcasting);
            socket.connect();

        } catch (Exception e) {
            e.printStackTrace();
        }
        JSONObject json = new JSONObject();
        try{
            json.put("user_id", myUserId);
            json.put("room_id", myRoomId);


        } catch (Exception e){
            e.printStackTrace();
        }
        socket.emit("join_waiting_room", json);
        Log.i(TAG, "before I joined room");

       // puzzle = new Puzzle("Apple","5 letters","fruit");

        if(isHost){
            startGameBtn.setVisibility(View.VISIBLE);
            new getPuzzle().execute(getString(R.string.server_ip) + getString(R.string.puzzle_id_api), myUserId);

        }else{
            startGameBtn.setVisibility(View.GONE);
        }

        startGameBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

              /*  Intent intent = new Intent(WaitingActivity.this, DrawingActivity.class);
                intent.putExtra("gameMembers",gameMembers);
                intent.putExtra("myUserId",myUserId);
                intent.putExtra("myRoomId", myRoomId);
                intent.putExtra("puzzle", puzzle);*/
                JSONObject json = new JSONObject();
                JSONObject data = new JSONObject();
                JSONObject puzzleJson = new JSONObject();
                try{

                    json.put("user_id",myUserId);
                    json.put("room_id",myRoomId);
                    puzzleJson.put("answer", puzzle.getAnswer());
                    puzzleJson.put("hint_a",puzzle.getHintA());
                    puzzleJson.put("hint_b",puzzle.getHintB());
                    data.put("puzzle",puzzleJson);
                    json.put("data", data);
                } catch (Exception e){
                    e.printStackTrace();
                }
             /*   socket.off(Socket.EVENT_CONNECT, onConnectSuccess);
                socket.off("join_waiting_room_broadcasting", onJoinBroadcasting);
                socket.off("member_update_broadcasting", onMemberUpdateBroadcasting);
                socket.off("game_join_broadcasting",onStartBroadcasting);
                socket.off("dismiss_waiting_room_broadcasting", onDismissRoomBroadcasting);
                socket.off("leave_waiting_room_broadcasting",onLeaveBroadcasting);
                startActivity(intent);

                myActivity.finish();*/
                socket.emit("game_join", json);

                //    intent.putExtra("m")
            }
        });

        gameMemberAdapter = new GameMemberAdapter(this, gameMembers);
        gameMemberLV.setAdapter(gameMemberAdapter);
       /* for (int i =0; i<5; i++){
            GameMember member = new GameMember("Alice"+i,myRoomId,false,false,false);
            gameMembers.add(member);
            gameMemberAdapter.notifyDataSetChanged();
        }*/


        // TODO: 16/4/19 for testing, need to be deleted
        /*Intent intent = new Intent(WaitingActivity.this, ViewingActivity.class);
        intent.putExtra("myUserId",myUserId);
        intent.putExtra("myRoomId",myRoomId);
        intent.putExtra("puzzle", puzzle);
        startActivity(intent);
        myActivity.finish();*/
    }
    private void findViews(){
        gameMemberLV = (ListView)findViewById(R.id.wait_lv);
        startGameBtn = (Button)findViewById(R.id.wait_start_btn);

    }

    // TODO: 16/4/19 socket start game call back function
    private Emitter.Listener onStartBroadcasting = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            if (!isHost) {
                try {
                    JSONObject emitdata = (JSONObject) args[0];
                    Log.i(TAG, "on start: " + emitdata.toString());
                    String userId = emitdata.getString("user_id");
                    String roomId = emitdata.getString("room_id");
                    JSONObject data = emitdata.getJSONObject("data");
                    JSONObject puzzleJson = data.getJSONObject("puzzle");
                    String answer = puzzleJson.getString("answer");
                    String hintA = puzzleJson.getString("hint_a");
                    String hintB = puzzleJson.getString("hint_b");
                    // TODO: 16/4/20 get puzzle from

                    Intent intent = new Intent(WaitingActivity.this, ViewingActivity.class);
                    intent.putExtra("myUserId", myUserId);
                    intent.putExtra("myRoomId", myRoomId);
                    Puzzle myPuzzle = new Puzzle(answer,hintA,hintB);
                    intent.putExtra("puzzle", myPuzzle);
                    socket.disconnect();
                    socket.off(Socket.EVENT_CONNECT, onConnectSuccess);
                    socket.off("join_waiting_room_broadcasting", onJoinBroadcasting);
                    socket.off("member_update_broadcasting", onMemberUpdateBroadcasting);
                    socket.off("game_join_broadcasting", onStartBroadcasting);
                    socket.off("dismiss_waiting_room_broadcasting", onDismissRoomBroadcasting);
                    socket.off("leave_waiting_room_broadcasting", onLeaveBroadcasting);
                    myActivity.finish();

                    startActivity(intent);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            else {
                Intent intent = new Intent(WaitingActivity.this, DrawingActivity.class);
                intent.putExtra("gameMembers",gameMembers);
                intent.putExtra("myUserId",myUserId);
                intent.putExtra("myRoomId", myRoomId);
                intent.putExtra("puzzle", puzzle);
                socket.disconnect();
                socket.off(Socket.EVENT_CONNECT, onConnectSuccess);
                socket.off("join_waiting_room_broadcasting", onJoinBroadcasting);
                socket.off("member_update_broadcasting", onMemberUpdateBroadcasting);
                socket.off("game_join_broadcasting", onStartBroadcasting);
                socket.off("dismiss_waiting_room_broadcasting", onDismissRoomBroadcasting);
                socket.off("leave_waiting_room_broadcasting", onLeaveBroadcasting);
                myActivity.finish();
                startActivity(intent);

            }
        }
    };

    private Emitter.Listener onLeaveBroadcasting = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            Log.i(TAG, "on leave braodcasting");
            try {
                JSONObject emitdata = (JSONObject) args[0];
                Log.i(TAG, "emitdata: " + emitdata.toString());
                String userId = emitdata.getString("user_id");
                String roomId = emitdata.getString("room_id");
                GameMember newMember = new GameMember(userId, roomId, false, false, false);
                Iterator<GameMember> iterator = gameMembers.iterator();
                while (iterator.hasNext()){
                    GameMember member = iterator.next();
                    if (newMember.isEqual(member)){
                        iterator.remove();
                        Log.i(TAG, "delete member");

                        break;
                    }
                }
                //gameMembers.add(newMember);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        gameMemberAdapter.notifyDataSetChanged();
                    }
                });
            }catch (Exception e){
                e.printStackTrace();
            }
        }
    };
    private Emitter.Listener onDismissRoomBroadcasting = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            Toast.makeText(getApplicationContext(),R.string.host_left,Toast.LENGTH_LONG);
            myActivity.finish();
        }
    };
    private Emitter.Listener onJoinBroadcasting = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            Log.i(TAG,"I joined room: before parse json");
            Log.i(TAG,args[0].toString());

            try {
                JSONObject json = new JSONObject();
                json.put("user_id",myUserId);
                json.put("room_id",myRoomId);
                socket.emit("member_update",json);
                Log.i(TAG,"I joined room");
                //JSONObject emitdata = (JSONObject) args[0];
                //Log.i(TAG, "emitdata: " + emitdata.toString());
                //String userId = emitdata.getString("user_id");
                //String roomId = emitdata.getString("room_id");
                //GameMember newMember = new GameMember(userId, roomId, false, false, false);
                //Log.i(TAG, "user_id " + userId + " has joined room " + roomId);
                //userIds.add(userId);



            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    };
    private Emitter.Listener onMemberUpdateBroadcasting = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            try {
                JSONObject emitdata = (JSONObject) args[0];
                Log.i(TAG, "emitdata: " + emitdata.toString());
                String userId = emitdata.getString("user_id");
                String roomId = emitdata.getString("room_id");
                GameMember newMember = new GameMember(userId, roomId, false, false, false);
                for (GameMember g: gameMembers){
                    if (g.isEqual(newMember)){
                        return;
                    }
                }
                gameMembers.add(newMember);
                Log.i(TAG, "update member");
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        gameMemberAdapter.notifyDataSetChanged();
                    }
                });
            }catch (Exception e){
                e.printStackTrace();
            }
        }
    };

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        JSONObject json = new JSONObject();
        try {
            json.put("user_id", myUserId);
            json.put("room_id", myRoomId);


        } catch (Exception e){
            e.printStackTrace();
        }
        Log.i(TAG, "press button");
        socket.emit("leave_waiting_room", json);

        myActivity.finish();

    }



    @Override
    protected void onStop() {
        Log.i(TAG,"leave room");

        super.onStop();
        // TODO: 16/4/19 close socket
        JSONObject json = new JSONObject();
        try{
            json.put("user_id", myUserId);
            json.put("room_id", myRoomId);


        } catch (Exception e){
            e.printStackTrace();
        }

        //socket.emit("leave_room",json);
        socket.disconnect();
        socket.off(Socket.EVENT_CONNECT, onConnectSuccess);
        socket.off("join_waiting_room_broadcasting", onJoinBroadcasting);
        socket.off("member_update_broadcasting", onMemberUpdateBroadcasting);
        socket.off("game_join_broadcasting", onStartBroadcasting);
        socket.off("dismiss_waiting_room_broadcasting", onDismissRoomBroadcasting);
        socket.off("leave_waiting_room_broadcasting", onLeaveBroadcasting);
        Log.i(TAG, "stop activity");

    }

    public void onDestroy() {
        super.onDestroy();



    }

    private Emitter.Listener onConnectSuccess = new Emitter.Listener() {
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
                        puzzle = new Puzzle(data.getString("answer"),data.getString("hint_a"),data.getString("hint_b"));
                     //   myRoomId = data.getString("room_id");
                        Log.i(TAG, "puzzle="+puzzle.getAnswer());

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
