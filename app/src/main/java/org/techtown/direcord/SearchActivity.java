package org.techtown.direcord;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;

public class SearchActivity extends AppCompatActivity {
    private Button id;
    private Button pwd;
    FirebaseAuth firebaseAuth;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        id = (Button) findViewById(R.id.searchID);
        pwd = (Button) findViewById(R.id.searchPW);

        //id찾기
        id.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(SearchActivity.this, SearchIdActivity.class);
                startActivity(intent);
            }
        });
        //pwd 찾기
        pwd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(SearchActivity.this, SearchPwdActivity.class);
                startActivity(intent);
            }
        });
    }
}
