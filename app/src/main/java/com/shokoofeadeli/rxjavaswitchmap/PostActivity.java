package com.shokoofeadeli.rxjavaswitchmap;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.widget.TextView;

import io.reactivex.annotations.Nullable;

public class PostActivity extends AppCompatActivity {
    private static final String TAG = "ViewPostActivity";

    private TextView text;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post);
        text = findViewById(R.id.text);

        getIncomingIntent();
    }

    private void getIncomingIntent(){
        if(getIntent().hasExtra("post")){
            Post post = getIntent().getParcelableExtra("post");
            text.setText(post.getTitle());
        }
    }
}