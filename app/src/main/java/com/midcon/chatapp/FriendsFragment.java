package com.midcon.chatapp;


import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import java.text.DateFormat;
import java.text.SimpleDateFormat;

import de.hdodenhof.circleimageview.CircleImageView;


/**
 * A simple {@link Fragment} subclass.
 */
public class FriendsFragment extends Fragment {


    private RecyclerView mFriendsList;
    private DatabaseReference mfriendsReference;
    private DatabaseReference mRootRef;
    private FirebaseAuth mAuth;
    private static TextView noFrndView;

    private String currentUser_id;

    private View mMainview;

    public FriendsFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment

        mMainview = inflater.inflate(R.layout.fragment_friends, container, false);

        mFriendsList = (RecyclerView) mMainview.findViewById(R.id.recycler_friends);
        mAuth = FirebaseAuth.getInstance();

        currentUser_id = mAuth.getCurrentUser().getUid();
        noFrndView = (TextView) mMainview.findViewById(R.id.no_frnd);

        mfriendsReference = FirebaseDatabase.getInstance().getReference().child("friends").child(currentUser_id);
        mRootRef = FirebaseDatabase.getInstance().getReference();

        mfriendsReference.keepSynced(true);
        mRootRef.keepSynced(true);

        mFriendsList.setHasFixedSize(true);
        mFriendsList.setLayoutManager(new LinearLayoutManager(getContext()));

        return mMainview;
    }

    @Override
    public void onStart() {
        super.onStart();
        final FirebaseRecyclerAdapter<Friends , FriendsViewHolder> friendsViewAdapter =
                new FirebaseRecyclerAdapter<Friends, FriendsViewHolder>(
                Friends.class,
                R.layout.user_view,
                FriendsViewHolder.class,
                mfriendsReference
        ) {
            @Override
            protected void populateViewHolder(final FriendsViewHolder viewHolder, final Friends model, int position) {

                DateFormat df = new SimpleDateFormat("dd-MM-yyy hh:mm a");
                String dateTime = df.format(model.getDate());

                viewHolder.setDate(dateTime);
                final String list_user_id = getRef(position).getKey();

                mRootRef.child("users").child(list_user_id).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {

                        final String userName = dataSnapshot.child("name").getValue().toString();
                        final String userThumbImg = dataSnapshot.child("thumb_image").getValue().toString();
                        final String onlineStatusString = dataSnapshot.child("online").getValue().toString();
                        final Long onlineStatus = Long.parseLong(onlineStatusString);

                        viewHolder.setName(userName);
                        viewHolder.setThumbImage(userThumbImg,getContext());
                        viewHolder.setOnline(onlineStatus);

                        viewHolder.mView.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {

                                CharSequence options[] = new CharSequence[]{"Open Profile","Send Message"};

                                AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                                builder.setTitle("Select Options");
                                builder.setItems(options, new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {

                                       switch(which){

                                           case 0 : Intent profileIntent = new Intent(getContext(),UserProfile.class);
                                               profileIntent.putExtra("user_id",list_user_id);
                                               profileIntent.putExtra("user_name",userName);
                                               startActivity(profileIntent);
                                               break;
                                           case 1 : Intent chatIntent = new Intent(getContext(),ChatActivity.class);
                                               chatIntent.putExtra("user_id",list_user_id);
                                               chatIntent.putExtra("user_name",userName);
                                               startActivity(chatIntent);
                                               break;
                                           default:
                                               Toast.makeText(getContext(), "Select valid Option", Toast.LENGTH_SHORT).show();
                                       }
                                    }
                                });
                                builder.show();

                            }
                        });

                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
            }
        };
        mFriendsList.setAdapter(friendsViewAdapter);
    }

   public static class FriendsViewHolder extends RecyclerView.ViewHolder {

       View mView;


       public FriendsViewHolder(View itemView) {
           super(itemView);
           mView = itemView;
       }

       public void setDate(String date){

           TextView userStatusView = (TextView) mView.findViewById(R.id.viewStatus);
           userStatusView.setText(date);
       }

       public void setName(String name){

           noFrndView.setVisibility(View.GONE);
           TextView userNameView = (TextView) mView.findViewById(R.id.viewName);
           userNameView.setText(name);
       }

       public void setOnline(Long onlineStatus){

           ImageView onlineView = (ImageView) mView.findViewById(R.id.onlineView);
           if(onlineStatus.equals(Variables.online)){

               onlineView.setVisibility(View.VISIBLE);
           }
           else {

               onlineView.setVisibility(View.INVISIBLE);
           }
       }

       public void setThumbImage(final String image , final Context applicationContext){

           final CircleImageView imgView = (CircleImageView) mView.findViewById(R.id.circleImageView2);

           Picasso.with(mView.getContext()).load(image).networkPolicy(NetworkPolicy.OFFLINE).placeholder(R.drawable.avatar)
                   .into(imgView, new Callback() {
               @Override
               public void onSuccess() {

               }

               @Override
               public void onError() {
                   Picasso.with(applicationContext).load(image).placeholder(R.drawable.avatar).into(imgView);
               }
           });
       }
   }
}
