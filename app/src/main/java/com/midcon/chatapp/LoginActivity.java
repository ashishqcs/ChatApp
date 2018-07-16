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
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.iid.FirebaseInstanceId;

public class LoginActivity extends AppCompatActivity {

    private Button btn_login,btn_register;
    private EditText et_email,et_password;
    private FirebaseAuth mAuth;
    private DatabaseReference mfirebaseDatabase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        mAuth = FirebaseAuth.getInstance();

        et_email = (EditText) findViewById(R.id.loginEmail);
        et_password = (EditText) findViewById(R.id.loginPswd);
        btn_login = (Button) findViewById(R.id.loginBtn);
        btn_register = (Button) findViewById(R.id.registerBtn);

        mAuth = FirebaseAuth.getInstance();
        mfirebaseDatabase = FirebaseDatabase.getInstance().getReference().child("users");
    }

    @Override
    protected void onStart() {
        super.onStart();

        btn_login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (et_email.getText().toString().equals("") || et_password.getText().toString().equals(""))
                {
                    Toast.makeText(LoginActivity.this, "No Field can be left Blank", Toast.LENGTH_SHORT).show();
                    et_email.setText("");
                    et_password.setText("");
                }
                else
                {
                    loginUser();
                }
            }
        });

        btn_register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent i = new Intent(LoginActivity.this,SignUp.class);
                startActivity(i);
            }
        });
    }

    private void loginUser()
    {

        final String email = et_email.getText().toString().trim();
        final String password = et_password.getText().toString().trim();

        mAuth.signInWithEmailAndPassword(email,password).addOnCompleteListener(LoginActivity.this, new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {

                if(task.isSuccessful())
                {

                    final String user = mAuth.getCurrentUser().getUid();
                    String deviceToken = FirebaseInstanceId.getInstance().getToken();
                    mfirebaseDatabase.child(user).child("device_token").setValue(deviceToken).addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            Intent i = new Intent(LoginActivity.this, UserHome.class);
                            startActivity(i);
                            finish();
                        }
                    });
                }

                else
                {
                    Toast.makeText(LoginActivity.this,"Invalid Email or Password",Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}
