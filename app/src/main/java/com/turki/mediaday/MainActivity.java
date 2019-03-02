package com.turki.mediaday;

import android.Manifest;
import android.animation.Animator;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Toast;

import com.lovejjfg.shadowcircle.CircleImageView;

import java.io.IOException;

public class MainActivity extends AppCompatActivity {

    private CircleImageView imageView;
    private ImageButton recorderButton;
    private Button generator;

    private static final int REQUEST_CAPTURE = 283;
    private static final int PERMISSION_RECORD = 907;

    private MediaPlayer player;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        imageView = findViewById(R.id.image);
        recorderButton = findViewById(R.id.recorderButton);
        generator = findViewById(R.id.generateButton);

        //Image
        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                startActivityForResult(intent, REQUEST_CAPTURE);
            }
        });

        //Recording
        recorderButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(Build.VERSION.SDK_INT >= 23){
                    if(checkSelfPermission(Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED){
                        //granted
                        if(!isRecording)
                            startRecording();
                        else
                            stopRecording();
                    }else{//you don't have the permission, request it
                        requestPermissions(new String[]{Manifest.permission.RECORD_AUDIO}, PERMISSION_RECORD);
                        //this codes sends you to OnPermissionResult
                    }
                }else{//granted (older devices)
                    if(!isRecording)
                        startRecording();
                    else
                        stopRecording();
                }
            }
        });

        generator.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    if(fileName != null) {
                        player = new MediaPlayer();
                        player.setDataSource(fileName);
                        player.prepare();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }

                ObjectAnimator animator = ObjectAnimator.ofFloat(imageView, "rotationY", 0f, 360f);
                animator.setDuration(3000);
                animator.setRepeatCount(ValueAnimator.INFINITE);
                animator.setInterpolator(new AccelerateDecelerateInterpolator());

                animator.addListener(new Animator.AnimatorListener() {
                    @Override
                    public void onAnimationStart(Animator animation) {
                        if(player != null)
                            player.start();
                    }

                    @Override
                    public void onAnimationEnd(Animator animation) {

                    }

                    @Override
                    public void onAnimationCancel(Animator animation) {

                    }

                    @Override
                    public void onAnimationRepeat(Animator animation) {
                        if(player != null){
                            player.seekTo(0);
                            player.start();
                        }
                    }
                });

                animator.start();
            }
        });

    }

    private void stopRecording(){
        if(recorder != null){
            recorder.stop();
            isRecording = false;
            recorderButton.setImageResource(R.drawable.ic_mic_black);
        }
    }

    private boolean isRecording = false;

    private MediaRecorder recorder;
    private String fileName;
    private void startRecording(){
        fileName = getFilesDir().getAbsolutePath()+"/sound.3gp";
        recorder = new MediaRecorder();

        recorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        recorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
        recorder.setOutputFile(fileName);

        recorder.setAudioEncoder(MediaRecorder.AudioEncoder.DEFAULT);

        try {
            recorder.prepare();
        } catch (IOException e) {
            e.printStackTrace();
        }

        recorder.start();
        isRecording = true;
        recorderButton.setImageResource(R.drawable.ic_mic_red);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if(requestCode == PERMISSION_RECORD){
            if(grantResults[0] == PackageManager.PERMISSION_GRANTED){
                //granted permission
                startRecording();
            }else{//PERMISSION DENIED
                Toast.makeText(this, "You have denied the permission of recording",Toast.LENGTH_SHORT)
                        .show();
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == REQUEST_CAPTURE){
            //image got!!
            if(resultCode == RESULT_OK){
                //set image to imageview
                try {
                    Bitmap bitmap = (Bitmap)data.getExtras().get("data");

                    imageView.setBackgroundColor(Color.TRANSPARENT);//remove background
                    imageView.setImageBitmap(bitmap);
                }catch (NullPointerException e){
                    e.printStackTrace();
                }
            }else{
                Toast.makeText(this, "Image capture failed", Toast.LENGTH_SHORT)
                        .show();
            }
        }

    }

    @Override
    protected void onStop() {
        super.onStop();
        if(player != null){
            player.release();
            player = null;
        }

        if(recorder != null){
            recorder.release();
            recorder = null;
        }
    }
}
