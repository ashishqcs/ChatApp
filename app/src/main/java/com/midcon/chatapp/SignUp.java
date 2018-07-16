package com.midcon.chatapp;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.iid.FirebaseInstanceId;

import java.util.HashMap;
import java.util.Map;

public class SignUp extends AppCompatActivity {

    private EditText et_email,et_password,et_name,et_mobile;
    private Button btn_register;

    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);


        et_email = (EditText) findViewById(R.id.email);
        et_password = (EditText) findViewById(R.id.password);
        et_name = (EditText) findViewById(R.id.name);
        et_mobile = (EditText) findViewById(R.id.mobile);
        btn_register = (Button) findViewById(R.id.register1);

        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference().child("users");

        btn_register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (et_email.getText().toString().equals("") || et_password.getText().toString().equals(""))
                {
                    Toast.makeText(SignUp.this, "No Field can be left Blank", Toast.LENGTH_SHORT).show();
                }
                else
                {
                    addUser();
                }
            }
        });

    }

    private void addUser()
    {
        final String email = et_email.getText().toString().trim();
        final String password = et_password.getText().toString().trim();
        final String name = et_name.getText().toString().trim();
        final String mobile = et_mobile.getText().toString().trim();

        mAuth.createUserWithEmailAndPassword(email,password).addOnCompleteListener(SignUp.this, new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {

                if(task.isSuccessful())
                {
                    String user_id = mAuth.getCurrentUser().getUid();
                    Map signUp = new HashMap();
                    signUp.put("name",name);
                    signUp.put("mobile",mobile);
                    signUp.put("status","Hi I am using ChatApp now!");
                    signUp.put("image","default");
                    signUp.put("thumb_image","default");
                    signUp.put("device_token",null);
                    signUp.put("online", ServerValue.TIMESTAMP);

                    mDatabase.child(user_id).updateChildren(signUp, new DatabaseReference.CompletionListener() {
                        @Override
                        public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                            if(databaseError == null){

                                Toast.makeText(SignUp.this, "Registration Successful", Toast.LENGTH_SHORT).show();

                                if(!mAuth.getCurrentUser().equals(null)) {

                                    mDatabase.child("device_token").setValue("no_token");
                                    mAuth.signOut();
                                    Toast.makeText(SignUp.this, "Logout Success", Toast.LENGTH_SHORT).show();
                                    Intent i1 = new Intent(SignUp.this,LoginActivity.class);
                                    startActivity(i1);
                                    finish();
                                }

                                Intent intent = new Intent(SignUp.this,MainActivity.class);
                                startActivity(intent);


                            }
                            else{
                                String error = databaseError.getMessage();
                                Toast.makeText(SignUp.this,error, Toast.LENGTH_SHORT).show();
                            }
                        }
                    });

                    et_mobile.setText("");
                    et_email.setText("");
                    et_name.setText("");
                    et_password.setText("");

                }

                else
                {
                    Toast.makeText(SignUp.this, "User Signup Failed", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}
