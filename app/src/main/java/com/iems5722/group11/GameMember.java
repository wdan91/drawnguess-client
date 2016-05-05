package com.iems5722.group11;


import java.io.Serializable;

/**
 * Created by WD-MAC on 16/4/18.
 */
public class GameMember implements Serializable {
    private String userId;
    private String roomId;
    private boolean curHost;
    private boolean preHost;
    private boolean nextHost;


    public String getUserId(){
        return userId;
    }
    public String getRoomId(){
        return roomId;
    }
    public boolean isCurHost(){
        return curHost;
    }
    public boolean isPreHost(){
        return preHost;
    }
    public boolean isNextHost(){
        return nextHost;
    }
    public void setCurHost(boolean b){
        curHost = b;
    }
    public void setPreHost(boolean b){
        preHost = b;
    }
    public void setNextHost(boolean b){
        nextHost = b;
    }
    public GameMember(){
        userId = null;
        roomId = null;
        curHost = false;
        preHost = false;
        nextHost = false;
    }
    public GameMember(String user, String room, boolean cur, boolean pre, boolean next){
        userId = user;
        roomId = room;
        curHost = cur;
        preHost = pre;
        nextHost = next;
    }
    public boolean isEqual(GameMember b){
        if (this.userId.equals(b.userId) && this.roomId.equals(b.roomId)){
            return true;
        }
        else return false;
    }

}
