package com.midcon.chatapp;


import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import java.util.HashMap;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;


/**
 * A simple {@link Fragment} subclass.
 */
public class RequestFragment extends Fragment {

    private RecyclerView requestList;
    private DatabaseReference mRootRef;
    private FirebaseAuth mAuth;
    private static String currentUserID;
    private View mView;
    private LinearLayoutManager linearLayoutManager;
    private static String req_status;
    private static String otherUserID;
    private Query queryRef;
    private static TextView tempView;
    public RequestFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
          mView = inflater.inflate(R.layout.fragment_request, container, false);

         requestList = (RecyclerView) mView.findViewById(R.id.recycler_request);
         //requestList.setHasFixedSize(true);

        tempView = (TextView) mView.findViewById(R.id.no_req);

        mRootRef = FirebaseDatabase.getInstance().getReference();
         mAuth = FirebaseAuth.getInstance();
         currentUserID = mAuth.getCurrentUser().getUid();

         linearLayoutManager = new LinearLayoutManager(getContext());
         requestList.setLayoutManager(linearLayoutManager);
         return mView;
    }

    @Override
    public void onStart() {
        super.onStart();

        queryRef = mRootRef.child("friend_request").child(currentUserID).orderByChild("timestamp").limitToLast(20);

        final FirebaseRecyclerAdapter<Users, RequestViewHolder> requestViewAdapter =
                new FirebaseRecyclerAdapter<Users, RequestViewHolder>(
                        Users.class,
                        R.layout.friend_req_view,
                        RequestViewHolder.class,
                        queryRef
                ) {
            @Override
            protected void populateViewHolder(final RequestViewHolder viewHolder, Users model, int position) {

                otherUserID = getRef(position).getKey();

                mRootRef.child("users").child(otherUserID).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {

                        final String name = (String) dataSnapshot.child("name").getValue();
                        final String thumb_image = dataSnapshot.child("thumb_image").getValue().toString();

                        viewHolder.setName(name);
                        viewHolder.setImage(thumb_image);
                        viewHolder.itemView.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {

                                Intent i = new Intent(getContext(),UserProfile.class);
                                i.putExtra("user_id",otherUserID);
                                startActivity(i);
                            }
                        });
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });

            }
        };
        requestList.setAdapter(requestViewAdapter);
    }

    public static class RequestViewHolder extends RecyclerView.ViewHolder {

        private DatabaseReference mRef;

        public RequestViewHolder(final View itemView) {
            super(itemView);
            mRef = FirebaseDatabase.getInstance().getReference().child("friend_request").child(currentUserID);
            mRef.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {

                    if (dataSnapshot.hasChild(otherUserID)){

                        req_status = dataSnapshot.child(otherUserID).child("request_type").getValue().toString();
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
        }


        public void setName(String name) {
            TextView nameView = (TextView) itemView.findViewById(R.id.req_view_text);
            ImageView frnd_icon = (ImageView) itemView.findViewById(R.id.imageView2);
            TextView click_text = (TextView) itemView.findViewById(R.id.textView2);
            View line = itemView.findViewById(R.id.lineView);

            if (req_status.equals("received")) {

                tempView.setVisibility(View.GONE);
                frnd_icon.setVisibility(View.VISIBLE);
                click_text.setVisibility(View.VISIBLE);
                nameView.setVisibility(View.VISIBLE);
                nameView.setText(name + " has sent you a friend request");
                line.setVisibility(View.VISIBLE);
            }
            else tempView.setVisibility(View.VISIBLE);
        }

        public void setImage(String thumb_image){

            CircleImageView imgView = (CircleImageView) itemView.findViewById(R.id.req_view_img);
            if (req_status.equals("received")){

                imgView.setVisibility(View.VISIBLE);
                Picasso.with(itemView.getContext()).load(thumb_image).networkPolicy(NetworkPolicy.OFFLINE).placeholder(R.drawable.avatar)
                    .into(imgView);
            }

        }
    }

}
