package com.midcon.chatapp;

import android.app.ProgressDialog;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class StatusUpdate extends AppCompatActivity {

    private EditText et_status;
    private Button btn_addStatus;
    private ProgressDialog progDialog;

    private DatabaseReference mdatabaseReference;
    private FirebaseUser mfirebaseUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_status_update);

        et_status = (EditText) findViewById(R.id.statusWrite);
        btn_addStatus = (Button) findViewById(R.id.updateStatus);

        mfirebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        String user_id = mfirebaseUser.getUid();
        mdatabaseReference = FirebaseDatabase.getInstance().getReference().child("users").child(user_id);

        btn_addStatus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String status = et_status.getText().toString().trim();

                if(status.equals(""))
                {
                    Toast.makeText(StatusUpdate.this, "Enter Status First", Toast.LENGTH_SHORT).show();
                }
                else
                {
                    progDialog = new ProgressDialog(StatusUpdate.this);

                    progDialog.setTitle("Change Status");
                    progDialog.setMessage("Updating Status...");
                    progDialog.show();


                    mdatabaseReference.child("status").setValue(status).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {

                            if(task.isSuccessful())
                            {
                                progDialog.dismiss();
                                Toast.makeText(StatusUpdate.this, "Status Updated", Toast.LENGTH_SHORT).show();

                            }
                            else {
                                Toast.makeText(StatusUpdate.this, "Status Update Failed ! Please Try Again", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });

                    mdatabaseReference.addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {

                            String statusUpdate = dataSnapshot.child("status").getValue().toString();
                            et_status.setText(statusUpdate);
                            finish();
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });

                }
            }
        });
    }
}
