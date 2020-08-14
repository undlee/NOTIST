package org.techtown.direcord;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GetTokenResult;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.kakao.auth.AuthType;
import com.kakao.auth.ISessionCallback;
import com.kakao.auth.Session;
import com.kakao.network.ErrorResult;
import com.kakao.usermgmt.UserManagement;
import com.kakao.usermgmt.callback.LogoutResponseCallback;
import com.kakao.usermgmt.callback.MeResponseCallback;
import com.kakao.usermgmt.callback.MeV2ResponseCallback;
import com.kakao.usermgmt.response.MeV2Response;
import com.kakao.usermgmt.response.model.UserProfile;
import com.kakao.util.exception.KakaoException;

import android.util.Log;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.File;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "";
    private Button sign_up;
    private Button login;
    private Button search;
    private Button kakao;
    private Button google;
    private EditText email_login;
    private EditText pwd_login;
    //    private SignInButton buttonGoogle;
    private static final int RC_SIGN_IN = 900;
    private GoogleSignInClient googleSignInClient;
    FirebaseAuth firebaseAuth;
    public static FirebaseStorage firebaseStorage;
    SharedPreferences pref;

    private static String uid = "unkown-uid";

    public static String getUid(){
        return uid;
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //세션 콜백 등록
        Session.getCurrentSession().addCallback(sessionCallback);

        sign_up = (Button) findViewById(R.id.signupButton);
        login = (Button) findViewById(R.id.loginButton);
        kakao = (Button) findViewById(R.id.KakaologinButton);

        search = (Button) findViewById(R.id.searchButton);
        email_login = (EditText) findViewById(R.id.emailInput);
        pwd_login = (EditText) findViewById(R.id.passwordInput);

        firebaseAuth = firebaseAuth.getInstance();
        firebaseStorage = FirebaseStorage.getInstance();

        //로그인
        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = email_login.getText().toString().trim();
                String pwd = pwd_login.getText().toString().trim();

                firebaseAuth.signInWithEmailAndPassword(email, pwd)
                        .addOnCompleteListener(MainActivity.this, new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                if (task.isSuccessful()) {
                                    Intent intent = new Intent(MainActivity.this, BottomMainActivity.class);
                                    startActivity(intent);
                                } else {
                                    Toast.makeText(MainActivity.this, "로그인 오류", Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
            }
        });

        //구글로그인
        firebaseAuth = FirebaseAuth.getInstance();
        google = findViewById(R.id.GoogleloginButton);

        // Google 로그인을 앱에 통합
        // GoogleSignInOptions 개체를 구성할 때 requestIdToken을 호출
        GoogleSignInOptions googleSignInOptions = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();

        googleSignInClient = GoogleSignIn.getClient(this, googleSignInOptions);

        google.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent signInIntent = googleSignInClient.getSignInIntent();
                startActivityForResult(signInIntent, RC_SIGN_IN);
            }
        });

        //카카오 로그인
        kakao = (Button) findViewById(R.id.KakaologinButton);
        kakao.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Session session = Session.getCurrentSession();
//                session.addCallback(new ISessionCallback());
                session.open(AuthType.KAKAO_LOGIN_ALL, MainActivity.this);
            }
        });

        //로그아웃

        //아이디, 비번 찾기
        search.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, SearchActivity.class);
                startActivity(intent);
            }
        });

        //회원가입
        sign_up.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, SignUpActivity.class);
                startActivity(intent);
            }
        });

    }

    private ISessionCallback sessionCallback;

    ISessionCallback callback = new ISessionCallback() {
        @Override
        public void onSessionOpened() {
            requestMe();
            Log.d(TAG, "세션오픈 <성공>");
        }

        @Override
        public void onSessionOpenFailed(KakaoException exception) {
            setContentView(R.layout.activity_main);
            Log.d(TAG, "세션오픈 <실패>");
        }
    };

    private void requestMe() {
        UserManagement.getInstance().me(new MeV2ResponseCallback() {
            @Override
            public void onSessionClosed(ErrorResult errorResult) {
                Log.d(TAG, "카카오 세션 Close!");
            }

            @Override
            public void onSuccess(MeV2Response result) {
                String url = result.getProfileImagePath(); //밑줄 그어진 거 = 곧 사라질 method임. 안 쓰는 게 좋음
                String id = result.getNickname();
                long token = result.getId();

                Intent intent = new Intent(MainActivity.this, MainActivity.class);
                intent.putExtra("profileImage", url);
                intent.putExtra("id", id);
                intent.putExtra("token", token);
                startActivity(intent);
            }
        });

    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // 구글로그인 버튼 응답
        if (requestCode == RC_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                // 구글 로그인 성공
                GoogleSignInAccount account = task.getResult(ApiException.class);
                firebaseAuthWithGoogle(account);
            } catch (ApiException e) {

            }
        }

        //카카오 간편로그인 실행 결과를 받아서 SDK로 전달
//        if (Session.getCurrentSession().handleActivityResult(requestCode, resultCode, data)) {
//            return;
//        }

        super.onActivityResult(requestCode, resultCode, data);
    }

    // 사용자가 정상적으로 로그인한 후에 GoogleSignInAccount 개체에서 ID 토큰을 가져와서
    // Firebase 사용자 인증 정보로 교환하고 Firebase 사용자 인증 정보를 사용해 Firebase에 인증합니다.
    private void firebaseAuthWithGoogle(GoogleSignInAccount acct) {

        AuthCredential credential = GoogleAuthProvider.getCredential(acct.getIdToken(), null);
        firebaseAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            final FirebaseUser mUser = firebaseAuth.getCurrentUser();
                            mUser.getIdToken(true).addOnCompleteListener(new OnCompleteListener<GetTokenResult>() {
                                public void onComplete(@NonNull Task<GetTokenResult> task) {
                                    if (task.isSuccessful()) {
                                        // TODO 당장은 필요없지만 우리 서버와 idToken으로 통신해야한다.
                                        String idToken = task.getResult().getToken();
                                        uid = mUser.getUid();
                                    } else {
                                        // TODO fail process
                                    }
                                }
                            });
                            // 로그인 성공
                            Intent intent = new Intent(MainActivity.this, BottomMainActivity.class);
                            startActivity(intent);
                        } else {
                            // 로그인 실패
                        }

                    }
                });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        // 세션 콜백 삭제
        Session.getCurrentSession().removeCallback(sessionCallback);
    }

    private File createFile() {
        String dirPath = getFilesDir().getAbsolutePath();
        File file = new File(dirPath); // 일치하는 폴더가 없으면 생성
        if (!file.exists()) {
            file.mkdirs();
            Toast.makeText(this, "Success", Toast.LENGTH_SHORT).show();
        }

        // txt 파일 생성
        String testStr = "ABCDEFGHIJK...";
        File savefile = new File(dirPath + "/test.txt");
        try {
            FileOutputStream fos = new FileOutputStream(savefile);
            fos.write(testStr.getBytes());
            fos.close();
            Toast.makeText(this, "Save Success", Toast.LENGTH_SHORT).show();
        } catch (IOException e) {
        }
        return savefile;

    }


//    public void checkLogin() {
//
//        new AsyncTask<Void, Void, Void>() {
//            @Override
//            protected void onPreExecute() {
//                super.onPreExecute();
//
//            }
//
//            @Override
//            protected Void doInBackground(Void... voids) {
//                try {
//                    Url = new URL(https://direcord-283711.dt.r.appspot.com/login/check/?idToken=); // URL화 한다.
//                    HttpURLConnection conn = (HttpURLConnection) Url.openConnection(); // URL을 연결한 객체 생성.
//                    conn.setRequestMethod("GET"); // get방식 통신
//                    conn.setDoOutput(true); // 쓰기모드 지정
//                    conn.setDoInput(true); // 읽기모드 지정
//                    conn.setUseCaches(false); // 캐싱데이터를 받을지 안받을지
//                    conn.setDefaultUseCaches(false); // 캐싱데이터 디폴트 값 설정
//
//                    strCookie = conn.getHeaderField("Set-Cookie"); //쿠키데이터 보관
//
//                    InputStream is = conn.getInputStream(); //input스트림 개방
//
//                    StringBuilder builder = new StringBuilder(); //문자열을 담기 위한 객체
//                    BufferedReader reader = new BufferedReader(new InputStreamReader(is, "UTF-8")); //문자열 셋 세팅
//                    String line;
//
//                    while ((line = reader.readLine()) != null) {
//                        builder.append(line + "\n");
//                    }
//
//                    result = builder.toString();
//
//                } catch (MalformedURLException | ProtocolException exception) {
//                    exception.printStackTrace();
//                } catch (IOException io) {
//                    io.printStackTrace();
//                }
//                return null;
//            }
//
//            @Override
//            protected void onPostExecute(Void aVoid) {
//                super.onPostExecute(aVoid);
//                System.out.println(result);
//            }
//        }.execute();
//    }
}

//package org.techtown.direcord;
//
//import android.app.Activity;
//import android.content.Context;
//import android.content.Intent;
//import android.content.SharedPreferences;
//import android.content.pm.PackageInfo;
//import android.content.pm.PackageManager;
//import android.content.pm.Signature;
//import android.net.Uri;
//import android.os.AsyncTask;
//import android.os.Bundle;
//import android.os.Environment;
//import android.util.Base64;
//import android.util.Log;
//import android.view.View;
//import android.view.WindowManager;
//import android.widget.Button;
//import android.widget.CheckBox;
//import android.widget.CompoundButton;
//import android.widget.EditText;
//import android.widget.Toast;
//
//import androidx.annotation.NonNull;
//import androidx.annotation.Nullable;
//import androidx.appcompat.app.AppCompatActivity;
//
//import com.google.android.gms.auth.api.signin.GoogleSignIn;
//import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
//import com.google.android.gms.auth.api.signin.GoogleSignInClient;
//import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
//import com.google.android.gms.common.SignInButton;
//import com.google.android.gms.common.api.ApiException;
//import com.google.android.gms.tasks.OnCompleteListener;
//import com.google.android.gms.tasks.Task;
//import com.google.firebase.auth.AuthCredential;
//import com.google.firebase.auth.AuthResult;
//import com.google.firebase.auth.FirebaseAuth;
//import com.google.firebase.auth.FirebaseUser;
//import com.google.firebase.auth.GetTokenResult;
//import com.google.firebase.auth.GoogleAuthProvider;
//import com.google.firebase.firestore.FirebaseFirestore;
//import com.kakao.auth.AuthType;
//import com.kakao.auth.ISessionCallback;
//import com.kakao.auth.Session;
//import com.kakao.network.ErrorResult;
//import com.kakao.usermgmt.UserManagement;
//import com.kakao.usermgmt.callback.LogoutResponseCallback;
//import com.kakao.usermgmt.callback.MeResponseCallback;
//import com.kakao.usermgmt.callback.MeV2ResponseCallback;
//import com.kakao.usermgmt.response.MeV2Response;
//import com.kakao.usermgmt.response.model.UserProfile;
//import com.kakao.util.exception.KakaoException;
//
//import android.util.Log;
//
//import java.io.BufferedReader;
//import java.io.File;
//import java.io.FileNotFoundException;
//import java.io.IOException;
//import java.io.InputStream;
//import java.io.InputStreamReader;
//import java.net.HttpURLConnection;
//import java.net.MalformedURLException;
//import java.net.ProtocolException;
//import java.net.URL;
//import java.security.MessageDigest;
//import java.security.NoSuchAlgorithmException;
//
//import java.io.FileInputStream;
//import java.io.FileOutputStream;
//import java.io.File;
//
//public class MainActivity extends AppCompatActivity {
//    private static final String TAG = "";
//    private Button sign_up;
//    private Button login;
//    private Button search;
//    private Button kakao;
//    private Button google;
//    private EditText email_login;
//    private EditText pwd_login;
//    //    private SignInButton buttonGoogle;
//    private static final int RC_SIGN_IN = 900;
//    private GoogleSignInClient googleSignInClient;
//    FirebaseAuth firebaseAuth;
//    SharedPreferences pref;
//
//
//    @Override
//    protected void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_main);
//
//        //세션 콜백 등록
//        Session.getCurrentSession().addCallback(sessionCallback);
//
//        sign_up = (Button) findViewById(R.id.signupButton);
//        login = (Button) findViewById(R.id.loginButton);
//        kakao = (Button) findViewById(R.id.KakaologinButton);
//
//        search = (Button) findViewById(R.id.searchButton);
//        email_login = (EditText) findViewById(R.id.emailInput);
//        pwd_login = (EditText) findViewById(R.id.passwordInput);
//
//        firebaseAuth = firebaseAuth.getInstance();
//
//        //로그인
//        login.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                String email = email_login.getText().toString().trim();
//                String pwd = pwd_login.getText().toString().trim();
//
//                firebaseAuth.signInWithEmailAndPassword(email, pwd)
//                        .addOnCompleteListener(MainActivity.this, new OnCompleteListener<AuthResult>() {
//                            @Override
//                            public void onComplete(@NonNull Task<AuthResult> task) {
//                                if (task.isSuccessful()) {
//                                    Intent intent = new Intent(MainActivity.this, BottomMainActivity.class);
//                                    startActivity(intent);
//                                } else {
//                                    Toast.makeText(MainActivity.this, "로그인 오류", Toast.LENGTH_SHORT).show();
//                                }
//                            }
//                        });
//            }
//        });
//
//        //구글로그인
//        firebaseAuth = FirebaseAuth.getInstance();
//        google = findViewById(R.id.GoogleloginButton);
//
//        // Google 로그인을 앱에 통합
//        // GoogleSignInOptions 개체를 구성할 때 requestIdToken을 호출
//        GoogleSignInOptions googleSignInOptions = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
//                .requestIdToken(getString(R.string.default_web_client_id))
//                .requestEmail()
//                .build();
//
//        googleSignInClient = GoogleSignIn.getClient(this, googleSignInOptions);
//
//        google.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                Intent signInIntent = googleSignInClient.getSignInIntent();
//                startActivityForResult(signInIntent, RC_SIGN_IN);
//            }
//        });
//
//        //카카오 로그인
//        kakao = (Button) findViewById(R.id.KakaologinButton);
//        kakao.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                Session session = Session.getCurrentSession();
////                session.addCallback(new ISessionCallback());
//                session.open(AuthType.KAKAO_LOGIN_ALL, MainActivity.this);
//            }
//        });
//
//        //로그아웃
//
//        //아이디, 비번 찾기
//        search.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                Intent intent = new Intent(MainActivity.this, SearchActivity.class);
//                startActivity(intent);
//            }
//        });
//
//        //회원가입
//        sign_up.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                Intent intent = new Intent(MainActivity.this, SignUpActivity.class);
//                startActivity(intent);
//            }
//        });
//
//    }
//
//    private ISessionCallback sessionCallback;
//
//    ISessionCallback callback = new ISessionCallback() {
//        @Override
//        public void onSessionOpened() {
//            requestMe();
//            Log.d(TAG, "세션오픈 <성공>");
//        }
//
//        @Override
//        public void onSessionOpenFailed(KakaoException exception) {
//            setContentView(R.layout.activity_main);
//            Log.d(TAG, "세션오픈 <실패>");
//        }
//    };
//
//    private void requestMe() {
//        UserManagement.getInstance().me(new MeV2ResponseCallback() {
//            @Override
//            public void onSessionClosed(ErrorResult errorResult) {
//                Log.d(TAG, "카카오 세션 Close!");
//            }
//
//            @Override
//            public void onSuccess(MeV2Response result) {
//                String url = result.getProfileImagePath(); //밑줄 그어진 거 = 곧 사라질 method임. 안 쓰는 게 좋음
//                String id = result.getNickname();
//                long token = result.getId();
//
//                Intent intent = new Intent(MainActivity.this, MainActivity.class);
//                intent.putExtra("profileImage", url);
//                intent.putExtra("id", id);
//                intent.putExtra("token", token);
//                startActivity(intent);
//            }
//        });
//
//    }
//
//    public void onActivityResult(int requestCode, int resultCode, Intent data) {
//        super.onActivityResult(requestCode, resultCode, data);
//
//        // 구글로그인 버튼 응답
//        if (requestCode == RC_SIGN_IN) {
//            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
//            try {
//                // 구글 로그인 성공
//                GoogleSignInAccount account = task.getResult(ApiException.class);
//                firebaseAuthWithGoogle(account);
//            } catch (ApiException e) {
//
//            }
//        }
//
//        //카카오 간편로그인 실행 결과를 받아서 SDK로 전달
////        if (Session.getCurrentSession().handleActivityResult(requestCode, resultCode, data)) {
////            return;
////        }
//
//        super.onActivityResult(requestCode, resultCode, data);
//    }
//
//    // 사용자가 정상적으로 로그인한 후에 GoogleSignInAccount 개체에서 ID 토큰을 가져와서
//    // Firebase 사용자 인증 정보로 교환하고 Firebase 사용자 인증 정보를 사용해 Firebase에 인증합니다.
//    private void firebaseAuthWithGoogle(GoogleSignInAccount acct) {
//
//        AuthCredential credential = GoogleAuthProvider.getCredential(acct.getIdToken(), null);
//        firebaseAuth.signInWithCredential(credential)
//                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
//                    @Override
//                    public void onComplete(@NonNull Task<AuthResult> task) {
//                        if (task.isSuccessful()) {
//                            final FirebaseUser mUser = firebaseAuth.getCurrentUser();
//                            Log.d("mUser is null. ", (mUser == null) + "");
//                            mUser.getIdToken(true).addOnCompleteListener(new OnCompleteListener<GetTokenResult>() {
//                                public void onComplete(@NonNull Task<GetTokenResult> task) {
//                                    if (task.isSuccessful()) {
//                                        String idToken = task.getResult().getToken();
//                                        Log.d("idToken  ", idToken);
//                                        String url = "https://direcord-283711.dt.r.appspot.com/login/login/";
//                                        url += idToken;
//                                        Http auth = new Http(url);
//                                        try {
//                                            auth.executeGet();
//                                            Log.d("result ", auth.getResponse());
//                                            //파일
//                                            FirebaseFirestore db = FirebaseFirestore.getInstance();
//                                            Log.d("db ", (db == null) + "");
//                                            String uid = "ABCDEFG";
//                                            String uploadPath = FileManager.getInstance().upload(uid, createFile());
//                                            Log.d("uri ", uploadPath);
//                                        } catch (InterruptedException e) {
//                                            e.printStackTrace();
//                                        }
//
//                                    } else {
//                                        // TODO fail process
//                                    }
//                                }
//                            });
//                            // 로그인 성공
//                            Intent intent = new Intent(MainActivity.this, BottomMainActivity.class);
//                            startActivity(intent);
//                        } else {
//                            // 로그인 실패
//                        }
//
//                    }
//                });
//    }
//
//    @Override
//    protected void onDestroy() {
//        super.onDestroy();
//
//        // 세션 콜백 삭제
//        Session.getCurrentSession().removeCallback(sessionCallback);
//    }
//
//    private File createFile() {
//        String dirPath = getFilesDir().getAbsolutePath();
//        File file = new File(dirPath); // 일치하는 폴더가 없으면 생성
//        if (!file.exists()) {
//            file.mkdirs();
//            Toast.makeText(this, "Success", Toast.LENGTH_SHORT).show();
//        }
//
//        // txt 파일 생성
//        String testStr = "ABCDEFGHIJK...";
//        File savefile = new File(dirPath + "/test.txt");
//        try {
//            FileOutputStream fos = new FileOutputStream(savefile);
//            fos.write(testStr.getBytes());
//            fos.close();
//            Toast.makeText(this, "Save Success", Toast.LENGTH_SHORT).show();
//        } catch (IOException e) {
//        }
//        return savefile;
//
//    }
//
//
////    public void checkLogin() {
////
////        new AsyncTask<Void, Void, Void>() {
////            @Override
////            protected void onPreExecute() {
////                super.onPreExecute();
////
////            }
////
////            @Override
////            protected Void doInBackground(Void... voids) {
////                try {
////                    Url = new URL(https://direcord-283711.dt.r.appspot.com/login/check/?idToken=); // URL화 한다.
////                    HttpURLConnection conn = (HttpURLConnection) Url.openConnection(); // URL을 연결한 객체 생성.
////                    conn.setRequestMethod("GET"); // get방식 통신
////                    conn.setDoOutput(true); // 쓰기모드 지정
////                    conn.setDoInput(true); // 읽기모드 지정
////                    conn.setUseCaches(false); // 캐싱데이터를 받을지 안받을지
////                    conn.setDefaultUseCaches(false); // 캐싱데이터 디폴트 값 설정
////
////                    strCookie = conn.getHeaderField("Set-Cookie"); //쿠키데이터 보관
////
////                    InputStream is = conn.getInputStream(); //input스트림 개방
////
////                    StringBuilder builder = new StringBuilder(); //문자열을 담기 위한 객체
////                    BufferedReader reader = new BufferedReader(new InputStreamReader(is, "UTF-8")); //문자열 셋 세팅
////                    String line;
////
////                    while ((line = reader.readLine()) != null) {
////                        builder.append(line + "\n");
////                    }
////
////                    result = builder.toString();
////
////                } catch (MalformedURLException | ProtocolException exception) {
////                    exception.printStackTrace();
////                } catch (IOException io) {
////                    io.printStackTrace();
////                }
////                return null;
////            }
////
////            @Override
////            protected void onPostExecute(Void aVoid) {
////                super.onPostExecute(aVoid);
////                System.out.println(result);
////            }
////        }.execute();
////    }
//}
//
