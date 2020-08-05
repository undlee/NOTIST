package org.techtown.direcord;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

public class SignUpActivity extends AppCompatActivity {
    private EditText email_join;
    private EditText pwd_join;
    private EditText name_join;
    private EditText pwd_check;
    private Button btn;
    FirebaseAuth firebaseAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        Button sBtn = (Button)findViewById(R.id.signupButton);
        Intent intent = getIntent();

        name_join = (EditText) findViewById(R.id.nameInput);
        email_join = (EditText) findViewById(R.id.emailInput);
        pwd_join = (EditText) findViewById(R.id.passwordInput);
        pwd_check = (EditText) findViewById(R.id.passwordCheck);
        btn = (Button) findViewById(R.id.signUp_Check);
        firebaseAuth = FirebaseAuth.getInstance();

        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String name = name_join.getText().toString().trim();
                String email = email_join.getText().toString().trim();
                String pwd1 = pwd_join.getText().toString().trim();
                String pwd2 = pwd_check.getText().toString().trim();

                if(pwd1.equals(pwd2)) {
                    //패스워드 일치
                    firebaseAuth.createUserWithEmailAndPassword(email, pwd1)
                            .addOnCompleteListener(SignUpActivity.this, new OnCompleteListener<AuthResult>() {
                                @Override
                                public void onComplete(@NonNull Task<AuthResult> task) {
                                    if (task.isSuccessful()) {
                                        Intent intent = new Intent(SignUpActivity.this, MainActivity.class);
                                        startActivity(intent);
                                    } else {
                                        Toast.makeText(SignUpActivity.this, "회원가입 불가", Toast.LENGTH_SHORT).show();
                                        return;
                                    }
                                }
                            });
                }
                else {
                    //패스워드 불일치
                    Toast.makeText(SignUpActivity.this, "패스워드 불일치", Toast.LENGTH_SHORT).show();
                }
            }
        });


    }
}