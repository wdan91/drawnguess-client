package com.iems5722.group11;

import java.io.Serializable;

/**
 * Created by WD-MAC on 16/4/19.
 */
public class Puzzle implements Serializable {
    private String answer;
    private String hintA;
    private String hintB;

    public Puzzle(String a, String b, String c){
        answer = a;
        hintA = b;
        hintB = c;
    }
    public String getAnswer(){
        return answer;
    }
    public String getHintA(){
        return hintA;
    }
    public String getHintB(){
        return hintB;
    }

}
