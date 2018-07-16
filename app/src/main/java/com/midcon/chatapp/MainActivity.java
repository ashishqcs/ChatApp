package com.midcon.chatapp;

import android.app.Activity;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.iid.FirebaseInstanceId;

public class MainActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Splash splash = new Splash();
        splash.start();
    }

    private class Splash extends Thread{

        @Override
        public void run() {
            super.run();

            try {
                sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            } finally {

                FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

                if(user != null){

                    Intent intent = new Intent(MainActivity.this,UserHome.class);
                    startActivity(intent);
                    finish();

                }
                else {
                    Intent intent = new Intent(MainActivity.this,LoginActivity.class);
                    startActivity(intent);
                    finish();
                }
            }
        };
    }
}
