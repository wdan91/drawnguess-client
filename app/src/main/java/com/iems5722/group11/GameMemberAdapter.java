package com.iems5722.group11;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * Created by WD-MAC on 16/4/19.
 */
public class GameMemberAdapter extends ArrayAdapter<GameMember>{
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        GameMember member = getItem(position);
        if(convertView == null){
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.game_member_item, parent, false);
        }
        TextView memberTV = (TextView) convertView.findViewById(R.id.game_member_tv);
        memberTV.setText((member.getUserId()));
        return convertView;    }

    public GameMemberAdapter(Context context, ArrayList<GameMember> members){
        super(context, 0, members);

    }

}
