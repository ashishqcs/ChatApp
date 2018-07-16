package com.midcon.chatapp;

import android.content.Intent;
import android.support.design.widget.TabLayout;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;

public class UserHome extends AppCompatActivity {

    private FirebaseUser user;

    private DatabaseReference mUserRef;

    private ViewPager viewPager;
    private SectionPageAdapter sectionPageAdapter;

    private TabLayout tabLayout;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_home);

        user = FirebaseAuth.getInstance().getCurrentUser();

        if(!user.equals(null))
        mUserRef = FirebaseDatabase.getInstance().getReference().child("users").child(user.getUid());

        //////----tabs-----------//////////////////////////////////////
        viewPager = (ViewPager) findViewById(R.id.mainPager);
        sectionPageAdapter = new SectionPageAdapter(getSupportFragmentManager());
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.addToBackStack(null);
        viewPager.setAdapter(sectionPageAdapter);
        tabLayout = (TabLayout) findViewById(R.id.main_tabs);
        tabLayout.setupWithViewPager(viewPager);

        if(!user.equals(null))
            mUserRef.child("online").setValue(Variables.online);

    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        getMenuInflater().inflate(R.menu.main,menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){

            case R.id.users :
                Intent i2 = new Intent(UserHome.this,AllUsers.class);
                startActivity(i2);
                return true;

            case R.id.settings :
                Intent i = new Intent(UserHome.this,Settings.class);
                startActivity(i);
                return true;

            case R.id.logout :
                signOut();
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public void signOut(){

       // mUserRef.child("online").setValue(ServerValue.TIMESTAMP);
        mUserRef.child("device_token").setValue("no_token");
        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        mAuth.signOut();
        Toast.makeText(this, "Logout Success", Toast.LENGTH_SHORT).show();
        Intent i1 = new Intent(UserHome.this,LoginActivity.class);
        startActivity(i1);
        finish();
    }

    @Override
    protected void onRestart() {
        super.onRestart();

        mUserRef.child("online").setValue(Variables.online);

    }

    @Override
    protected void onResume() {
        super.onResume();

        mUserRef.child("online").setValue(Variables.online);

    }

    @Override
    protected void onPause() {
        super.onPause();
        mUserRef.child("online").setValue(ServerValue.TIMESTAMP);

    }
}
