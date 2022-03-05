package com.example.hjy.patent;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.SignInButton;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class Login extends AppCompatActivity {

    private EditText editText_email, editText_password;
    private CheckBox checkBox_autologin;
    private Button button_login;
    private TextView textView_findID, textView_findPW, textView_signup;

    private FirebaseAuth firebaseAuth;
    private FirebaseAuth.AuthStateListener authStateListener;

    SharedPreferences prof;
    SharedPreferences.Editor editor;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login);

        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN); ;//FullScreen
        firebaseAuth = FirebaseAuth.getInstance();

        prof =  getSharedPreferences("login", 0);
        editor = prof.edit(); //id, password

        editText_email = (EditText) findViewById(R.id.login_edittext_email);
        editText_password = (EditText) findViewById(R.id.login_edittext_password);
        checkBox_autologin = (CheckBox)findViewById(R.id.login_checkbox);
        button_login = (Button) findViewById(R.id.login_button_login);
        textView_findID=(TextView)findViewById(R.id.login_textview_findID);
        textView_findPW=(TextView)findViewById(R.id.login_textview_findPW);
        textView_signup=(TextView)findViewById(R.id.login_textview_signup);

        if(prof.getBoolean("auto_save", false)==true) {  //불러올때
            editText_email.setText(prof.getString("id", ""));
            editText_password.setText(prof.getString("pw", ""));
            checkBox_autologin.setChecked(true);
        }

        checkBox_autologin.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(checkBox_autologin.isChecked()==true){
                    editor.putString("id", editText_email.getText().toString());
                    editor.putString("pw", editText_password.getText().toString());
                    editor.putBoolean("auto_save", true);
                    editor.commit();  //저장(안해주면 변경값 저장 안됨)
                }else{
                    editor.clear(); //모두 없애줄때
                    editor.commit();
                }
            }
        });

        button_login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                loginEvent(); //다음화면으로 넘겨주는 기능 담당??
            }
        });
        textView_signup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {  //회원가입
                Intent i = new Intent(getApplicationContext(), Signup.class);
                i.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                startActivity(i);
            }
        });

        //로그인 인터페이스 리스너
        authStateListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if(user != null) {
                    //로그인
                }else{
                    //로그아웃
                }
            }
        };

    }//onCreate

    void loginEvent(){
//        if(email.isEmpty() || password.isEmpty()){
//            Toast.makeText(Login.this, "입력해주세요.", Toast.LENGTH_SHORT).show();
//        }

        firebaseAuth.signInWithEmailAndPassword(editText_email.getText().toString(), editText_password.getText().toString()).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) { //로그인이 맞는지만 확인 (다음화면으로 넘겨주는 기능 담당하지 않음)
                if(!task.isSuccessful()) {
                    //로그인 실패한 부분
                    Toast.makeText(Login.this, task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                }else{
                    //로그인 성공
                    editor.putString("uid",firebaseAuth.getUid());
                    editor.commit();
                    Toast.makeText(Login.this, "로그인 되었습니다.", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                }
            }
        });
    }


    @Override
    protected void onStart() {
        super.onStart();
        firebaseAuth.addAuthStateListener(authStateListener);
    }

    @Override
    protected void onStop() {
        super.onStop();
        firebaseAuth.removeAuthStateListener(authStateListener);
    }
}
