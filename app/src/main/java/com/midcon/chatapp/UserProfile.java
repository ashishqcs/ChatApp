package com.midcon.chatapp;

import android.content.Intent;
import android.icu.text.DateFormat;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import java.sql.Timestamp;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;


public class UserProfile extends AppCompatActivity {

    private TextView tv_name,tv_status,tv_friends;
    private Button btn_sendMsg,btn_sendRequest;
    private ImageView img_profile;

    private static String name;

    String otherUserID, currentUserId, reqStatus;

    private DatabaseReference mRootRef;

    private FirebaseAuth mfirebaseAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_profile);

        tv_name = (TextView) findViewById(R.id.userProfileName );
        tv_friends = (TextView) findViewById(R.id.totalFriends );
        tv_status = (TextView) findViewById(R.id.userProfileStatus );
        btn_sendMsg = (Button) findViewById(R.id.sendMessage);
        btn_sendRequest = (Button) findViewById(R.id.sendRequest);
        img_profile = (ImageView) findViewById(R.id.imageView);

        otherUserID = getIntent().getStringExtra("user_id");
        reqStatus ="not_friends";

        mfirebaseAuth = FirebaseAuth.getInstance();
        mRootRef = FirebaseDatabase.getInstance().getReference();
        mRootRef.keepSynced(true);

        currentUserId = mfirebaseAuth.getCurrentUser().getUid();
        btn_sendMsg.setVisibility(View.INVISIBLE);
        btn_sendMsg.setEnabled(false);


        ///////////////////////////////////FRIEND REQ STATUS///////////////////////////////////////////////

        mRootRef.child("users").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                if (dataSnapshot.hasChild(otherUserID)) {

                    name = dataSnapshot.child(otherUserID).child("name").getValue().toString();
                    String status = dataSnapshot.child(currentUserId).child("status").getValue().toString();
                    final String image = dataSnapshot.child(otherUserID).child("image").getValue().toString();

                    tv_name.setText(name);
                    tv_status.setText(status);
                    Picasso.with(UserProfile.this).load(image).networkPolicy(NetworkPolicy.OFFLINE).placeholder(R.drawable.ravatar).into(img_profile);

                    //--------------- FRIEND LIST / REQUEST FEATURE------------------------

                    mRootRef.child("friend_request").child(currentUserId).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {

                            if (dataSnapshot.hasChild(otherUserID)) {

                                String req_type = dataSnapshot.child(otherUserID).child("request_type").getValue().toString();

                                if (req_type.equals("received")) {

                                    reqStatus = "request_received";
                                    btn_sendRequest.setText("ACCEPT FRIEND    REQUEST");
                                    btn_sendRequest.setEnabled(true);
                                    btn_sendMsg.setEnabled(true);
                                    btn_sendMsg.setVisibility(View.VISIBLE);
                                    btn_sendMsg.setText("Reject Request");

                                } else if (req_type.equals("sent")) {

                                    reqStatus = "request_sent";
                                    btn_sendRequest.setText("CANCEL FRIEND     REQUEST");
                                    btn_sendRequest.setEnabled(true);

                                }
                            }
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });

                    mRootRef.child("friends").child(currentUserId).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {

                            if (dataSnapshot.hasChild(otherUserID)) {

                                reqStatus = "friends";
                                btn_sendRequest.setText("UNFRIEND");
                                btn_sendRequest.setEnabled(true);
                                btn_sendMsg.setVisibility(View.VISIBLE);
                                btn_sendMsg.setEnabled(true);

                            }
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();

        mRootRef.child("users").child(currentUserId).child("online").setValue(Variables.online);


        btn_sendRequest.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                // ------------------ NOT FRIENDS STATE ----------------------------------

                btn_sendRequest.setEnabled(false);
                if (reqStatus.equals("not_friends")) {

                    btn_sendRequest.setEnabled(false);

                    Map requestMap = new HashMap();
                    requestMap.put("friend_request/"+currentUserId+"/"+otherUserID+"/request_type","sent");
                    requestMap.put("friend_request/"+currentUserId+"/"+otherUserID+"/timestamp", ServerValue.TIMESTAMP);
                    requestMap.put("friend_request/"+otherUserID+"/"+currentUserId+"/request_type","received");
                    requestMap.put("friend_request/"+otherUserID+"/"+currentUserId+"/timestamp", ServerValue.TIMESTAMP);

                    DatabaseReference newNotificationRef = mRootRef.child("notifications").child(otherUserID).push();
                    String newNotificationId = newNotificationRef.getKey();
                    HashMap notificationData = new HashMap<>();
                    notificationData.put("from",currentUserId);
                    notificationData.put("type","request");

                    requestMap.put("notifications/"+otherUserID+"/"+newNotificationId,notificationData);

                    mRootRef.updateChildren(requestMap, new DatabaseReference.CompletionListener() {
                        @Override
                        public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                            if(databaseError == null){
                                reqStatus = "request_sent";
                                btn_sendRequest.setEnabled(true);
                                btn_sendRequest.setText("CANCEL FRIEND     REQUEST");
                            }
                            else{

                                String error = databaseError.getMessage();
                                Toast.makeText(UserProfile.this, error , Toast.LENGTH_SHORT).show();
                            }

                        }

                    });

                }

                //-------------------------- CANCEL REQUEST STATE ---------------------------------

                if (reqStatus.equals("request_sent")){

                    btn_sendRequest.setEnabled(false);

                    Map cancelReqMap = new HashMap();
                    cancelReqMap.put("friend_request/"+currentUserId+"/"+otherUserID, null);
                    cancelReqMap.put("friend_request/"+otherUserID+"/"+currentUserId, null);

                    mRootRef.updateChildren(cancelReqMap, new DatabaseReference.CompletionListener() {
                        @Override
                        public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {

                            if(databaseError == null){

                                reqStatus = "not_friends";
                                btn_sendRequest.setText("SEND FRIEND     REQUEST");
                                btn_sendRequest.setEnabled(true);
                            }
                            else{

                                String error = databaseError.getMessage();
                                Toast.makeText(UserProfile.this,error, Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
                }

                //------------------ REQUEST RECEIVED STATE // ACCEPT FRIEND REQUEST---------------------

                if(reqStatus.equals("request_received")){

                    btn_sendRequest.setEnabled(false);

                    //String currentDate = DateFormat.getDateTimeInstance().format(new Date());

                    Map friendsMap = new HashMap();
                    friendsMap.put("friends/"+currentUserId+"/"+otherUserID+"/date",ServerValue.TIMESTAMP);
                    friendsMap.put("friends/"+otherUserID+"/"+currentUserId+"/date",ServerValue.TIMESTAMP);
                    friendsMap.put("friends/"+otherUserID+"/"+currentUserId+"/chatting","false");
                    friendsMap.put("friends/"+currentUserId+"/"+otherUserID+"/chatting","false");

                    friendsMap.put("friend_request/"+currentUserId+"/"+otherUserID , null);
                    friendsMap.put("friend_request/"+otherUserID+"/"+currentUserId , null);

                    mRootRef.updateChildren(friendsMap, new DatabaseReference.CompletionListener() {
                        @Override
                        public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {

                            if(databaseError == null){

                                reqStatus = "friends";
                                btn_sendRequest.setText("UNFRIEND");
                                btn_sendRequest.setEnabled(true);
                                btn_sendMsg.setVisibility(View.VISIBLE);
                                btn_sendMsg.setText("Send Message");
                                btn_sendMsg.setEnabled(true);
                            }
                            else{

                                String error = databaseError.getMessage();
                                Toast.makeText(UserProfile.this, error , Toast.LENGTH_SHORT).show();
                            }

                        }
                    });
                }

                // ------------ ---------UNFRIEND FEATURE ---------------

                if(reqStatus.equals("friends")){

                    btn_sendRequest.setEnabled(false);

                    Map unfriendMap = new HashMap();
                    unfriendMap.put("friends/"+currentUserId+"/"+otherUserID , null);
                    unfriendMap.put("friends/"+otherUserID+"/"+currentUserId , null);

                    mRootRef.updateChildren(unfriendMap, new DatabaseReference.CompletionListener() {
                        @Override
                        public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {

                            if(databaseError == null){

                                reqStatus = "not_friends";
                                btn_sendRequest.setText("SEND FRIEND REQUEST");

                                btn_sendMsg.setEnabled(false);
                                btn_sendMsg.setVisibility(View.INVISIBLE);
                            }
                            else {

                                String error = databaseError.getMessage();
                                Toast.makeText(UserProfile.this, error, Toast.LENGTH_SHORT).show();
                            }
                            btn_sendRequest.setEnabled(true);
                        }
                    });
                }

            }
        });

        btn_sendMsg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if(reqStatus.equals("request_received")){

                    btn_sendMsg.setEnabled(false);

                    Map rejectReqMap = new HashMap();
                    rejectReqMap.put("friend_request/"+currentUserId+"/"+otherUserID, null);
                    rejectReqMap.put("friend_request/"+otherUserID+"/"+currentUserId, null);

                    mRootRef.updateChildren(rejectReqMap, new DatabaseReference.CompletionListener() {
                        @Override
                        public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {

                            if(databaseError == null){

                                btn_sendMsg.setVisibility(View.INVISIBLE);
                                reqStatus = "not_friends";
                                btn_sendRequest.setText("SEND FRIEND     REQUEST");
                                btn_sendRequest.setEnabled(true);
                            }
                            else{

                                String error = databaseError.getMessage();
                                Toast.makeText(UserProfile.this,error, Toast.LENGTH_SHORT).show();
                            }
                        }
                    });

                    return;
                }

                else {
                    Intent chatIntent = new Intent(UserProfile.this,ChatActivity.class);
                    chatIntent.putExtra("user_id",otherUserID);
                    chatIntent.putExtra("user_name",name);
                    startActivity(chatIntent);
                    return;
                }




            }
        });
    }

    @Override
    protected void onStop() {
        super.onStop();

            //mRootRef.child("users").child(currentUserId).child("online").setValue(ServerValue.TIMESTAMP);
    }
}
