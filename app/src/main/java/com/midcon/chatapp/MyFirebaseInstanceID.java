package com.midcon.chatapp;

import android.util.Log;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.FirebaseInstanceIdService;

import static android.content.ContentValues.TAG;

/**
 * Created by ASHISH SINGH on 13/03/2018.
 */

public class MyFirebaseInstanceID extends FirebaseInstanceIdService {

    private DatabaseReference tokenRef;
    private FirebaseAuth mAuth;

    @Override
    public void onTokenRefresh() {
        super.onTokenRefresh();

            mAuth = FirebaseAuth.getInstance();
            tokenRef = FirebaseDatabase.getInstance().getReference().child("user");
            String token = FirebaseInstanceId.getInstance().getToken();
            Log.d(TAG, "Token: " +token);

            tokenRef.child(mAuth.getCurrentUser().getUid()).child("device_token").setValue(token);
    }
}
