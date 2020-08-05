package org.techtown.direcord;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

public class SearchPwdActivity extends AppCompatActivity {
    private EditText name;
    private Button btn;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_pwd);

        name = (EditText) findViewById(R.id.idSearch_name);
        btn = (Button) findViewById(R.id.idSearch_btn);
    }

}
