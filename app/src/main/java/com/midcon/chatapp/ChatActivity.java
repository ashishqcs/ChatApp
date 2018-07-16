package com.midcon.chatapp;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;
public class ChatActivity extends AppCompatActivity {

    Toolbar toolbar;
    public String mChatUserID;
    private String mChatUserName;

    private EditText et_msgType;
    private ImageButton imgBtn_send,imgBtn_add;
    private TextView tv_name,tv_last_seen;
    private CircleImageView iv_image;
    private DatabaseReference mRootRef;
    private FirebaseUser user;

    private RecyclerView recyclerView;
    private final List<MessageGetSet> messageList = new ArrayList();
    private MessageAdapter messageAdapter;
    private String image;
    private static long count;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.setContentView(R.layout.activity_chat);

        mRootRef = FirebaseDatabase.getInstance().getReference();
        user = FirebaseAuth.getInstance().getCurrentUser();

        et_msgType = (EditText) findViewById(R.id.editText_message);
        imgBtn_send = (ImageButton) findViewById(R.id.sendButton);
        imgBtn_add = (ImageButton) findViewById(R.id.addButton);

        mChatUserID = getIntent().getStringExtra("user_id");
        mChatUserName = getIntent().getStringExtra("user_name");

        toolbar = (Toolbar) findViewById(R.id.chat_app_bar);
        setSupportActionBar(toolbar);

        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayShowCustomEnabled(true);

        LayoutInflater inflater = (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View action_bar_view = inflater.inflate(R.layout.chat_custom_bar,null);

        actionBar.setCustomView(action_bar_view);

        mRootRef.child("chat").child(user.getUid()).child(mChatUserID).child("count").setValue(0);
        mRootRef.child("chat").child(user.getUid()).child(mChatUserID).child("seen").setValue("true");
        //-----------/////////////////-custom bar items----//////////-------------------------------//////////

        tv_name = (TextView) findViewById(R.id.nameActionBar);
        tv_last_seen = (TextView) findViewById(R.id.lastSeen);
        iv_image = (CircleImageView) findViewById(R.id.imgActionBar);

        tv_name.setText(mChatUserName);

        messageAdapter = new MessageAdapter(messageList);
        recyclerView = (RecyclerView) findViewById(R.id.recycler_chat);

        recyclerView.setHasFixedSize(true);
        final LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setStackFromEnd(true);

        recyclerView.setLayoutManager(linearLayoutManager);
        recyclerView.setAdapter(messageAdapter);

        showMessages();

        recyclerView.addOnLayoutChangeListener(new View.OnLayoutChangeListener() {
            @Override
            public void onLayoutChange(View v, int left, int top, int right, int bottom, int oldLeft, int oldTop, int oldRight, int oldBottom) {
                recyclerView.scrollToPosition(messageList.size()-1);
            }
        });

    }

    private void showMessages() {

        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        mRootRef.child("messages").child(mAuth.getCurrentUser().getUid()).child(mChatUserID)
                .addChildEventListener(new ChildEventListener() {
                    @Override
                    public void onChildAdded(DataSnapshot dataSnapshot, String s) {

                        MessageGetSet  messages = dataSnapshot.getValue(MessageGetSet.class);
                        messageList.add(messages);
                        recyclerView.scrollToPosition(messageList.size() -1);
                        messageAdapter.notifyDataSetChanged();
                    }

                    @Override
                    public void onChildChanged(DataSnapshot dataSnapshot, String s) {

                    }

                    @Override
                    public void onChildRemoved(DataSnapshot dataSnapshot) {

                    }

                    @Override
                    public void onChildMoved(DataSnapshot dataSnapshot, String s) {

                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
    }

    public void setImage(final String image){

        Picasso.with(getApplicationContext()).load(image).networkPolicy(NetworkPolicy.OFFLINE)
                .into(iv_image, new Callback() {
            @Override
            public void onSuccess() {

            }

            @Override
            public void onError() {

                Picasso.with(getApplicationContext()).load(image).placeholder(R.drawable.avatar).into(iv_image);

            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();

        mRootRef.child("friends").child(user.getUid()).child(mChatUserID).child("chatting").setValue("true");
        mRootRef.child("users").child(user.getUid()).child("online").setValue(Variables.online);
        //////////////////////////////////////TIME AGO FEATURE//////////////////////////////////////////////////////

        mRootRef.child("users").child(mChatUserID).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                image = dataSnapshot.child("thumb_image").getValue().toString();
                String online = dataSnapshot.child("online").getValue().toString();

                setImage(image); ////////setImage method

                GetTimeAgo getTimeAgo = new GetTimeAgo();

                Long lastTime = Long.parseLong(online);
                String lastSeen = getTimeAgo.getTimeAgo(lastTime , getApplicationContext());

                tv_last_seen.setText(lastSeen);

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        /////////////////////////CREATING OBJECT OF ChatMESSAGING CLASS WE CREATED///////////////////////////////////

           //final ChatMessaging chatMessaging = new ChatMessaging();

           //chatMessaging.chatAddFeature(mChatUserID);

           /////////////////////////////SEND BUTTON ONCLICK LISTNER/////////////////////////////////

        imgBtn_send.setOnClickListener(new View.OnClickListener() {
               @Override
               public void onClick(View v) {

                   mRootRef.child("friends").child(user.getUid()).addValueEventListener(new ValueEventListener() {
                       @Override
                       public void onDataChange(DataSnapshot dataSnapshot) {

                           if(dataSnapshot.hasChild(mChatUserID)){

                               final String message = et_msgType.getText().toString().trim();

                               if(!TextUtils.isEmpty(message)){

                                   /*final ImageView messageSent = (ImageView) findViewById(R.id.messageSent);
                                   final ImageView pendingMsg = (ImageView) findViewById(R.id.pendingMsg);

                                   pendingMsg.setVisibility(View.VISIBLE);
                                   messageSent.setVisibility(View.INVISIBLE)*/;

                                   //chatMessaging.sendButton(message,mChatUserID,image);
                                   //////////////////////////////////// CREATING CHAT CHILD IN DATABASE //////////////////////

                                   final String currentUserID = user.getUid();
                                   mRootRef.child("chat").child(currentUserID)
                                           .addValueEventListener(new ValueEventListener() {
                                       @Override
                                       public void onDataChange(DataSnapshot dataSnapshot) {

                                           if(!dataSnapshot.hasChild(mChatUserID)){

                                               Map chatAddMap = new HashMap();
                                               chatAddMap.put("seen","false");
                                               chatAddMap.put("timestamp", ServerValue.TIMESTAMP);
                                               chatAddMap.put("message",message);
                                               chatAddMap.put("sender",currentUserID);

                                               Map chatUserMap = new HashMap();
                                               chatUserMap.put("chat/"+currentUserID+"/"+mChatUserID,chatAddMap);
                                               chatUserMap.put("chat/"+mChatUserID+"/"+currentUserID,chatAddMap);

                                               mRootRef.updateChildren(chatUserMap, new DatabaseReference.CompletionListener() {
                                                   @Override
                                                   public void onComplete(DatabaseError databaseError,
                                                                          DatabaseReference databaseReference) {

                                                       if(databaseError != null){

                                                           Log.d( "CHAT LOG",databaseError.getMessage().toString());
                                                       }
                                                   }
                                               });

                                               mRootRef.child(mChatUserID).child(currentUserID).child("count").setValue(1);

                                           }
                                       }

                                       @Override
                                       public void onCancelled(DatabaseError databaseError) {

                                       }
                                   });

                                   ////////////////////////////////////SEND MESSAGE FUNCTIONALITY/////////////////////////////////

                                   DatabaseReference user_message_push = FirebaseDatabase.getInstance().getReference()
                                           .child("messages").child(currentUserID).child(mChatUserID).push();

                                   final String push_id =user_message_push.getKey();
                                   String current_user_ref = "messages/"+currentUserID+"/"+mChatUserID;
                                   String chat_user_ref = "messages/"+mChatUserID+"/"+currentUserID;


                                   Map messageMap = new HashMap();
                                   messageMap.put("message",message);
                                   messageMap.put("type", "text");
                                   messageMap.put("time",ServerValue.TIMESTAMP);
                                   messageMap.put("senderID",currentUserID);
                                   messageMap.put("receiverID",mChatUserID);
                                   messageMap.put("image_thumb",image);

                                   Map messageUserMap = new HashMap();
                                   messageUserMap.put(current_user_ref+"/"+push_id,messageMap);
                                   messageUserMap.put(chat_user_ref+"/"+push_id,messageMap);

                                   mRootRef.updateChildren(messageUserMap).addOnCompleteListener( new OnCompleteListener() {
                                       @Override
                                       public void onComplete(@NonNull Task task) {

                                           if (task.isSuccessful()){

                                               //pendingMsg.setVisibility(View.INVISIBLE);
                                               //messageSent.setVisibility(View.VISIBLE);

                                               Map notificationData = new HashMap();
                                               notificationData.put("from",user.getUid());
                                               notificationData.put("type","chat");
                                               notificationData.put("message",message);

                                               Map notificationMap = new HashMap();
                                               notificationMap.put("notifications/"+mChatUserID+"/"+push_id,notificationData);
                                               mRootRef.updateChildren(notificationMap);

                                           }
                                           else {

                                               ImageView msgFail = (ImageView) findViewById(R.id.sendingFailed);
                                               msgFail.setVisibility(View.VISIBLE);
                                           }
                                       }
                                   });


                                   Map lastMsgMap = new HashMap();
                                   lastMsgMap.put("seen","false");
                                   lastMsgMap.put("timestamp", ServerValue.TIMESTAMP);
                                   lastMsgMap.put("message",message);
                                   lastMsgMap.put("count",0);
                                   lastMsgMap.put("sender",currentUserID);

                                   Map chatUserMap = new HashMap();
                                   chatUserMap.put("chat/"+currentUserID+"/"+mChatUserID,lastMsgMap);
                                   chatUserMap.put("chat/"+mChatUserID+"/"+currentUserID,lastMsgMap);

                                   mRootRef.updateChildren(chatUserMap);

                                   ///////////////////...//COUNT////////////////////////////////////////
                                   mRootRef.child("chat").child(mChatUserID).addValueEventListener(new ValueEventListener() {
                                       @Override
                                       public void onDataChange(DataSnapshot dataSnapshot) {

                                           if(dataSnapshot.hasChild(currentUserID)){

                                               String st = dataSnapshot.child(currentUserID).child("count").getValue().toString();
                                               count = Long.parseLong(st);
                                           }
                                       }

                                       @Override
                                       public void onCancelled(DatabaseError databaseError) {

                                       }
                                   });
                                   count = count +1;
                                   mRootRef.child("chat").child(mChatUserID).child(currentUserID).child("count").setValue(count);

                                   ///////////////////////////////////////////////////////////////////////////////////////

                                   et_msgType.getText().clear();
                               }
                           }
                           else Toast.makeText(ChatActivity.this, "You are no longer Friends", Toast.LENGTH_SHORT).show();
                       }

                       @Override
                       public void onCancelled(DatabaseError databaseError) {

                       }
                   });

               }
           });
    }

    @Override
    protected void onStop() {
        super.onStop();

        if(!user.equals(null))
            mRootRef.child("users").child(user.getUid()).child("online").setValue(Variables.online);

        mRootRef.child("friends").child(user.getUid()).child(mChatUserID).child("chatting").setValue("false");
        mRootRef.child("chat").child(user.getUid()).child(mChatUserID).child("count").setValue(0);
    }


}
