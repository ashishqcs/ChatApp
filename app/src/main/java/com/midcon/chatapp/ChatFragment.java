package com.midcon.chatapp;


import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import de.hdodenhof.circleimageview.CircleImageView;


/**
 * A simple {@link Fragment} subclass.
 */
public class ChatFragment extends Fragment {

    private RecyclerView mChatrecycler;
    private View mMainView;
    private DatabaseReference mRootRef;
    private DatabaseReference mChatRef;
    private FirebaseAuth mAuth;
    private LinearLayoutManager linearLayoutManager;
    private Query queryRef;
    private static TextView noTextView;

    private static String currentUserID;
    private static String sender;

    public ChatFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
       mMainView = inflater.inflate(R.layout.fragment_chat, container, false);

       mChatrecycler = (RecyclerView) mMainView.findViewById(R.id.chatRecycler);
       mRootRef = FirebaseDatabase.getInstance().getReference();
       mAuth = FirebaseAuth.getInstance();
       currentUserID = mAuth.getCurrentUser().getUid();

        mChatRef =FirebaseDatabase.getInstance().getReference().child("chat").child(currentUserID);

        linearLayoutManager = new LinearLayoutManager(getContext());
        linearLayoutManager.setReverseLayout(true);
        linearLayoutManager.setStackFromEnd(true);
        mChatrecycler.setHasFixedSize(true);
        mChatrecycler.setLayoutManager(linearLayoutManager);
        noTextView = (TextView) mMainView.findViewById(R.id.no_chat);

        return mMainView;
    }

    @Override
    public void onStart() {
        super.onStart();

        queryRef = mChatRef.orderByChild("timestamp").limitToLast(20);
        final FirebaseRecyclerAdapter<MessageGetSet ,ChatViewHolder> chatViewAdapter =
                new FirebaseRecyclerAdapter<MessageGetSet, ChatViewHolder>(
                MessageGetSet.class,
                R.layout.user_view,
                ChatViewHolder.class,
                queryRef
        ) {
            @Override
            protected void populateViewHolder(final ChatViewHolder viewHolder, final MessageGetSet model, int position) {

                final String list_user_id = getRef(position).getKey();

                mRootRef.child("users").child(list_user_id).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {

                        final String name = dataSnapshot.child("name").getValue().toString();
                        final String thumb_image = dataSnapshot.child("thumb_image").getValue().toString();

                        viewHolder.setName(name);
                        viewHolder.setThumb_image(thumb_image);
                        viewHolder.itemView.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {

                                Intent intent = new Intent(getContext(),ChatActivity.class);
                                intent.putExtra("user_id",list_user_id);
                                intent.putExtra("user_name", name);
                                startActivity(intent);

                            }
                        });
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });

                mChatRef.child(list_user_id).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {

                        if (dataSnapshot.hasChild("message")){

                            final String lastMsg = dataSnapshot.child("message").getValue().toString();
                            sender = dataSnapshot.child("sender").getValue().toString();
                            final String count = dataSnapshot.child("count").getValue().toString();
                            final long timeStamp = (long) dataSnapshot.child("timestamp").getValue();

                            GetTimeAgo getTimeAgo = new GetTimeAgo();
                            String time = getTimeAgo.chatTimeAgo(timeStamp);
                            viewHolder.setStatus(lastMsg);
                            viewHolder.extraViews(count,sender,time);
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });

                mRootRef.child(list_user_id).child(currentUserID).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        if (dataSnapshot.hasChild("seen")){

                            String seen = dataSnapshot.child("seen").getValue().toString();

                            if (sender.equals(currentUserID) && seen.equals("true")){

                                viewHolder.seenMsg();
                            }
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });

            }
        };
        mChatrecycler.setAdapter(chatViewAdapter);


    }

    public static class ChatViewHolder extends RecyclerView.ViewHolder {
        public ChatViewHolder(View itemView ) {
            super(itemView);
        }

        public void setName(String name) {

            noTextView.setVisibility(View.GONE);
            TextView nameView = (TextView) itemView.findViewById(R.id.viewName);
            nameView.setText(name);
        }

        public void setThumb_image(String thumb_image){

            CircleImageView imgView = (CircleImageView) itemView.findViewById(R.id.circleImageView2);
            Picasso.with(itemView.getContext()).load(thumb_image).networkPolicy(NetworkPolicy.OFFLINE)
                    .placeholder(R.drawable.avatar).into(imgView);
        }

        public void setStatus(String lastMsg) {

            TextView status = (TextView) itemView.findViewById(R.id.viewStatus);
            status.setText(lastMsg);
            status.setTextSize(17);

        }

        public void extraViews(String count, String sender, String time) {

            TextView timeView = (TextView) itemView.findViewById(R.id.timeText);
            timeView.setVisibility(View.VISIBLE);
            timeView.setText(time);

            if(sender.equals(currentUserID)){

                ImageView iv_done = (ImageView) itemView.findViewById(R.id.imgDone);

                iv_done.setVisibility(View.VISIBLE);

            }
            else{

                TextView countView = (TextView) itemView.findViewById(R.id.unseenMsgView);
                if (!count.equals("0")){
                    timeView.setTextColor(itemView.getResources().getColor(R.color.alertGreen));
                    countView.setText(count);
                    countView.setVisibility(View.VISIBLE);
                }
            }

        }

        public void seenMsg() {

            ImageView iv_done = (ImageView) itemView.findViewById(R.id.imgDone);
            iv_done.setVisibility(View.INVISIBLE);
            ImageView msgSeen = (ImageView) itemView.findViewById(R.id.imgAllDone);
            msgSeen.setVisibility(View.VISIBLE);
        }
    }

}
