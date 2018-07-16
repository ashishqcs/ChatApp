package com.midcon.chatapp;

import android.content.Context;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
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

import de.hdodenhof.circleimageview.CircleImageView;

public class AllUsers extends AppCompatActivity {

    private RecyclerView recyclerList;

    private DatabaseReference mdatabaseReference;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_all_users);

        recyclerList = (RecyclerView) findViewById(R.id.recycler);
        recyclerList.setHasFixedSize(true);
        recyclerList.setLayoutManager(new LinearLayoutManager(this));
        recyclerList.getBaseline();
        recyclerList.setTop(View.SCROLL_INDICATOR_TOP);


        mdatabaseReference = FirebaseDatabase.getInstance().getReference().child("users");
        mAuth = FirebaseAuth.getInstance();

        mdatabaseReference.keepSynced(true);
    }

    @Override
    protected void onStart() {
        super.onStart();

        mdatabaseReference.child(mAuth.getCurrentUser().getUid()).child("online").setValue(Variables.online);

        FirebaseRecyclerAdapter<Users,UsersViewHolder> firebaseRecyclerAdapter = new FirebaseRecyclerAdapter<Users, UsersViewHolder>(
                Users.class,
                R.layout.user_view,
                UsersViewHolder.class,
                mdatabaseReference
        ) {
            @Override
            protected void populateViewHolder(UsersViewHolder viewHolder, final Users model, int position) {

                final String user_id = getRef(position).getKey();

                viewHolder.setDisplayName(model.getName());
                viewHolder.setDisplayStatus(model.getStatus());
                viewHolder.setDisplayImage(model.getImage(),getApplicationContext());

                if(user_id.equals(mAuth.getCurrentUser().getUid())){

                    viewHolder.mView.setVisibility(View.GONE);
                    viewHolder.mView.setLayoutParams(new RecyclerView.LayoutParams(0,0));
                }
                viewHolder.mView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        Intent i = new Intent(AllUsers.this,UserProfile.class);
                        i.putExtra("user_id",user_id);
                        startActivity(i);
                    }
                });

            }
        };

        recyclerList.setAdapter(firebaseRecyclerAdapter);
    }

    public static class UsersViewHolder extends RecyclerView.ViewHolder {

        View mView;
        public UsersViewHolder(View itemView) {
            super(itemView);

            mView = itemView;

        }

        public void setDisplayName(String name)
        {
            TextView userNameView = (TextView) mView.findViewById(R.id.viewName);
            userNameView.setText(name);
        }
        public void setDisplayStatus(String status)
        {
            TextView userStatusView = (TextView) mView.findViewById(R.id.viewStatus);
            userStatusView.setText(status);
        }

        public void setDisplayImage(final String thumb_image, final Context applicationContext)
        {
            final CircleImageView userImageView = (CircleImageView) mView.findViewById(R.id.circleImageView2);
            Picasso.with(applicationContext).load(thumb_image).networkPolicy(NetworkPolicy.OFFLINE)
                    .placeholder(R.drawable.avatar).into(userImageView, new Callback() {
                @Override
                public void onSuccess() {

                }

                @Override
                public void onError() {
                    Picasso.with(applicationContext).load(thumb_image).placeholder(R.drawable.avatar).into(userImageView);

                }
            });
        }

    }

    @Override
    protected void onPause() {
        super.onPause();
        mdatabaseReference.child(mAuth.getCurrentUser().getUid()).child("online").setValue(ServerValue.TIMESTAMP);
    }
}
