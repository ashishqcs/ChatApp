package com.midcon.chatapp;


import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.List;
import de.hdodenhof.circleimageview.CircleImageView;

/**
 * Created by ASHISH SINGH on 09/02/2018.
 */

public class MessageAdapter extends RecyclerView.Adapter {

    private static final int MSG_TYPE_SENT = 1;
    private static final int MSG_TYPE_RECEIVED = 2;
    private DatabaseReference mRootRef;

    private List<MessageGetSet> mMessageList;
    public MessageAdapter(List<MessageGetSet> messageList){

        mMessageList = messageList;
        mRootRef = FirebaseDatabase.getInstance().getReference();
    }



    @Override
    public int getItemViewType(int position) {

        if (mMessageList.get(position).getSenderID().equals(FirebaseAuth.getInstance().getCurrentUser().getUid())){

            return MSG_TYPE_SENT;
        }
        else return MSG_TYPE_RECEIVED;

    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view;

        if(viewType == MSG_TYPE_SENT){

                view = LayoutInflater.from(parent.getContext()).inflate(R.layout.msg_sent_layout,parent,false);
                return new SentMessageHolder(view);
        }
        else if(viewType == MSG_TYPE_RECEIVED){

            view = LayoutInflater.from(parent.getContext()).inflate(R.layout.msg_receive_layout,parent,false);
            return new ReceivedMessageHolder(view);
        }


        return null;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {

        MessageGetSet message = mMessageList.get(position);

        switch(holder.getItemViewType()){

            case MSG_TYPE_SENT :
                ((SentMessageHolder) holder).bind(message);
                break;

            case MSG_TYPE_RECEIVED :
                ((ReceivedMessageHolder) holder).bind(message);
                break;
        }
    }


    @Override
    public int getItemCount() {
        return mMessageList.size();
    }


    public class ReceivedMessageHolder extends RecyclerView.ViewHolder{

        private TextView msgContent;
        private TextView timeView;
        View mView;

        public ReceivedMessageHolder(View itemView) {
            super(itemView);
            mView = itemView;
        }

        void bind(MessageGetSet message){

            msgContent = (TextView) itemView.findViewById(R.id.msg_rec_View);
            timeView = (TextView) itemView.findViewById(R.id.msg_rec_timeView);
            DateFormat df = new SimpleDateFormat("hh:mm a");
            String time = df.format(message.getTime());

            timeView.setText(time);
            msgContent.setText(message.getMessage());
        }
    }

    private class SentMessageHolder extends RecyclerView.ViewHolder {

        private TextView messageView;
        private TextView timeView;


        public SentMessageHolder(View view) {
            super(view);

            messageView = (TextView) view.findViewById(R.id.msg_sent_View);
            timeView = (TextView) view.findViewById(R.id.msg_sent_timeView);
        }

        public void bind(MessageGetSet message) {

            DateFormat df = new SimpleDateFormat("hh:mm a");
            String time = df.format(message.getTime());

            timeView.setText(time);
            messageView.setText(message.getMessage());
        }
    }
}
