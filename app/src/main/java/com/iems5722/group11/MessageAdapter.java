package com.iems5722.group11;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * Created by WD-MAC on 16/4/21.
 */
public class MessageAdapter  extends ArrayAdapter<String> {
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        String message = getItem(position);
        if(convertView == null){
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.chat_message_item, parent, false);
        }
        TextView memberTV = (TextView) convertView.findViewById(R.id.message_tv);
        memberTV.setText(message);
        return convertView;    }

    public MessageAdapter(Context context, ArrayList<String> message){
        super(context, 0, message);

    }

}
