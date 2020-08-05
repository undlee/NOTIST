package org.techtown.direcord;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

public class SearchIdActivity extends AppCompatActivity {
    private EditText name;
    private EditText email;
    private Button btn;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_id);

        name = (EditText) findViewById(R.id.pwdSearch_name);
        email = (EditText) findViewById(R.id.pwdSearch_email);
        btn = (Button) findViewById(R.id.pwdSearch_btn);
    }
}
