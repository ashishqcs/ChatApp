package com.midcon.chatapp;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;
import id.zelory.compressor.Compressor;

public class Settings extends Activity {

    private CircleImageView imageView;
    private TextView tv_name,tv_status;
    private Button btn_status,btn_image;
    private ProgressDialog progDialog;

    private DatabaseReference mdatabaseReference;
    private FirebaseUser mfirebaseUser;
    private StorageReference mStorageRef;

    String user_id;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        imageView = (CircleImageView) findViewById(R.id.circleImageView);
        tv_name = (TextView) findViewById(R.id.displayName);
        tv_status = (TextView) findViewById(R.id.status);
        btn_status = (Button) findViewById(R.id.changeStatus);
        btn_image = (Button) findViewById(R.id.changeDP);

        mfirebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        String uid = mfirebaseUser.getUid();
        user_id = uid.trim();

        mdatabaseReference = FirebaseDatabase.getInstance().getReference().child("users").child(user_id);
        mStorageRef = FirebaseStorage.getInstance().getReference();

       mdatabaseReference.keepSynced(true);

        mdatabaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                String name = dataSnapshot.child("name").getValue().toString();
                final String image = dataSnapshot.child("image").getValue().toString();
                String status = dataSnapshot.child("status").getValue().toString();

                tv_name.setText(name);
                tv_status.setText(status);
                if(!image.equals("default")) {

                    //Picasso.with(Settings.this).load(image).placeholder(R.drawable.avatar).into(imageView);
                    Picasso.with(Settings.this).load(image).networkPolicy(NetworkPolicy.OFFLINE)
                            .placeholder(R.drawable.avatar).into(imageView, new Callback() {
                        @Override
                        public void onSuccess() {

                        }

                        @Override
                        public void onError() {

                            Picasso.with(Settings.this).load(image).placeholder(R.drawable.avatar).into(imageView);
                        }
                    });
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        btn_status.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent i =new Intent(Settings.this,StatusUpdate.class);
                startActivity(i);
                i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            }
        });

        btn_image.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                CropImage.activity()
                        .setGuidelines(CropImageView.Guidelines.ON).setAspectRatio(16,9).start(Settings.this);
            }
        });



    }

    @Override
    protected void onStart() {
        super.onStart();

        mdatabaseReference.child("online").setValue(Variables.online);
    }

    @Override
    protected void onPause() {
        super.onPause();
        mdatabaseReference.child("online").setValue(ServerValue.TIMESTAMP);

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {

            CropImage.ActivityResult result = CropImage.getActivityResult(data);

            if (resultCode == RESULT_OK) {

                Uri resultUri = result.getUri();

                final File thumb_filePath = new File(resultUri.getPath());

                Bitmap thumbBitmap = null;
                try {
                    thumbBitmap = new Compressor(this).setMaxHeight(200).setMaxWidth(200).setQuality(60)
                            .compressToBitmap(thumb_filePath);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    thumbBitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
                    final byte[] thumb_byte = baos.toByteArray();




                StorageReference imagePath = mStorageRef.child("profile_images").child(user_id+".jpg");
                final StorageReference thumbImagePath = mStorageRef.child("profile_images").child("thumbs").child(user_id+".jpg");

                progDialog = new ProgressDialog(Settings.this);

                progDialog.setTitle("Change Profile pic");
                progDialog.setMessage("Uploading Image...");
                progDialog.setCanceledOnTouchOutside(false);
                progDialog.show();

                imagePath.putFile(resultUri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {

                        if(task.isSuccessful()){

                            final String image_url = task.getResult().getDownloadUrl().toString();

                            final UploadTask uploadTask = thumbImagePath.putBytes(thumb_byte);
                            uploadTask.addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                                @Override
                                public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> thumb_task) {

                                    String thumb_url = thumb_task.getResult().getDownloadUrl().toString();
                                    if(thumb_task.isSuccessful()){

                                        Map uploadURLs = new HashMap<>();
                                        uploadURLs.put("image",image_url);
                                        uploadURLs.put("thumb_image",thumb_url);

                                        mdatabaseReference.updateChildren(uploadURLs).addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {

                                                progDialog.dismiss();

                                                if(task.isSuccessful()){

                                                    Toast.makeText(Settings.this, "Image Uploaded", Toast.LENGTH_SHORT).show();

                                                }
                                                else {

                                                    Toast.makeText(Settings.this, "Error in uploading..", Toast.LENGTH_SHORT).show();
                                                }
                                            }
                                        });
                                    }
                                }
                            });


                        }
                        else{
                            progDialog.dismiss();

                            Toast.makeText(Settings.this, "Error in uploading..", Toast.LENGTH_SHORT).show();
                        }
                    }
                });

            }
            else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {

                Exception error = result.getError();
                String err = error.toString();
                Toast.makeText(this, err, Toast.LENGTH_SHORT).show();
            }
        }
    }

}
