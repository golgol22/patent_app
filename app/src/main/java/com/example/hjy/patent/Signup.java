package com.example.hjy.patent;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.Toast;

import com.example.hjy.patent.model.UserModel;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.regex.Pattern;


public class Signup extends AppCompatActivity {


    private static final int PICK_FROM_ALBUM = 10;
    private EditText editText_email, editText_name, editText_password, editText_password_check;
    private ImageView profile;
    private RadioButton rb1;
    private Button button_signup;
    private Uri imageUri;

    private FirebaseAuth firebaseAuth;  // 파이어베이스 인증 객체 생성
    private StorageReference storageReference;
    private static final Pattern PASSWORD_PATTERN = Pattern.compile("^[a-zA-Z0-9!@.#$%^&*?_~]{4,16}$");  // 비밀번호 정규식

//    private String name, email, password, password_check;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.signup);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        actionBar.setHomeButtonEnabled(true);
        actionBar.setDisplayHomeAsUpEnabled(true);  //뒤로가기
        actionBar.setDisplayShowTitleEnabled(false);

        profile = (ImageView) findViewById(R.id.signup_imageview_profile);
        editText_name = (EditText) findViewById(R.id.signup_edittext_name);
        editText_email = (EditText) findViewById(R.id.signup_edittext_email);
        editText_password = (EditText) findViewById(R.id.signup_edittext_password);
        editText_password_check = (EditText) findViewById(R.id.signup_edittext_password_check);


        rb1 = (RadioButton) findViewById(R.id.signup_radiobutton1);
        button_signup =(Button)findViewById(R.id.signup_button_signup);

        firebaseAuth = FirebaseAuth.getInstance();
        storageReference = FirebaseStorage.getInstance().getReference();

//        name = editText_name.getText().toString().trim();
//        email = editText_email.getText().toString().trim();
//        password = editText_password.getText().toString().trim();
//        password_check = editText_password_check.getText().toString().trim();

        profile.setOnClickListener(new View.OnClickListener() {  //갤러리에서 이미지 가져오기
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Intent.ACTION_PICK);
                intent.setType(MediaStore.Images.Media.CONTENT_TYPE);
                startActivityForResult(intent, PICK_FROM_ALBUM);
            }
        });

        button_signup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {  //회원가입
//                if(name.isEmpty() || email.isEmpty() || password.isEmpty() || password_check.isEmpty() || imageUri ==null) {
//                    Toast.makeText(getApplicationContext(), "입력해주세요.", Toast.LENGTH_SHORT).show();
//                }else {
                    firebaseAuth
                            .createUserWithEmailAndPassword(editText_email.getText().toString(), editText_password.getText().toString())
                            .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                                @Override
                                public void onComplete(@NonNull final Task<AuthResult> task) {

                                    if (!task.isSuccessful()) {
                                        //회원가입 실패한 부분
                                        Toast.makeText(Signup.this, task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                                    }else{
                                        final String uid = task.getResult().getUser().getUid();
                                        UserProfileChangeRequest userProfileChangeRequest = new UserProfileChangeRequest.Builder().setDisplayName(editText_name.getText().toString()).build();
                                        task.getResult().getUser().updateProfile(userProfileChangeRequest); //회원가입을 할 때 이름이 여기에 담김

//                                        Uri file = Uri.fromFile(new File("path/to/userImages/"+uid+"/"+imageUri));
//                                        StorageReference riverRef = storageReference.child(uid+"/"+imageUri);
//                                        riverRef.putFile(file).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
//                                            @Override
//                                            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
//                                                Uri downloadUrl = taskSnapshot.getDownloadUrl();
//                                            }
//                                        })
//                                        .addOnFailureListener(new OnFailureListener() {
//                                            @Override
//                                            public void onFailure(@NonNull Exception e) {
//
//                                            }
//                                        });
                                        storageReference.child("userImages").child(uid).putFile(imageUri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                                           //이미지 파일이름을 uid로 줌
                                            @Override
                                            public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {

                                                String imageUrl = storageReference.getDownloadUrl().toString(); //이미지를 스토리지에 넣고, 그 url주소를 DB에 저장
                                                UserModel userModel = new UserModel();
                                                userModel.uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
                                                userModel.userEmail = editText_email.getText().toString();
                                                userModel.userName = editText_name.getText().toString();
                                                userModel.profileImageUrl = imageUrl;
                                                if (rb1.isChecked()){
                                                    userModel.distinguish = false;//일반인
                                                }else{
                                                    userModel.distinguish = true;//법률 사무소
                                                }

                                                //userModel에 회원가입 항목을 넣고 userModel을 DB에 저장
                                                FirebaseDatabase.getInstance().getReference().child("users").child(uid).setValue(userModel).addOnCompleteListener(new OnCompleteListener<Void>() {
                                                    @Override
                                                    public void onComplete(@NonNull Task<Void> task) {
                                                        if (!task.isSuccessful()) {
                                                            //회원가입 실패한 부분
                                                            Toast.makeText(Signup.this, task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                                                        }else {
                                                            Signup.this.finish();
                                                            Toast.makeText(Signup.this, "회원가입 성공", Toast.LENGTH_SHORT).show();
                                                            Intent i5 = new Intent(getApplicationContext(), Login.class);
                                                            i5.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                                                            startActivity(i5);
                                                        }
                                                    }
                                                });
                                        }
                                    });
                                }
                                }
                            });
//                }
            }
        });

    } //onCreate

    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(requestCode==PICK_FROM_ALBUM &&resultCode==RESULT_OK) {
            profile.setImageURI(data.getData());//가운데 뷰를 바꿈
            imageUri=data.getData(); //이미지 경로 원본
        }
    }

//    public void singUp(View view) {
//        email = editTextEmail.getText().toString();
//        password = editTextPassword.getText().toString();
//
//        if(isValidEmail() && isValidPasswd()) {
//            createUser(email, password);
//        }
//    }
//
//    public void signIn(View view) {
//        email = editTextEmail.getText().toString();
//        password = editTextPassword.getText().toString();
//
//        if(isValidEmail() && isValidPasswd()) {
//            loginUser(email, password);
//        }
//    }
//
//    // 이메일 유효성 검사
//    private boolean isValidEmail() {
//        if (email.getText().toString().isEmpty()) {
//            // 이메일 공백
//            return false;
//        } else if (!Patterns.EMAIL_ADDRESS.matcher(email.getText().toString()).matches()) {
//            // 이메일 형식 불일치
//            return false;
//        } else {
//            return true;
//        }
//    }
//
//    // 비밀번호 유효성 검사
//    private boolean isValidPasswd() {
//        if (password.getText().toString().isEmpty()) {
//            // 비밀번호 공백
//            return false;
//        } else if (!PASSWORD_PATTERN.matcher(password.getText().toString()).matches()) {
//            // 비밀번호 형식 불일치
//            return false;
//        } else {
//            return true;
//        }
//    }
//
//    // 회원가입
//    private void createUser(String email, String password) {
//        firebaseAuth.createUserWithEmailAndPassword(email, password)
//                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
//                    @Override
//                    public void onComplete(@NonNull Task<AuthResult> task) {
////                        if (task.isSuccessful()) {
////                            // 회원가입 성공
////                            Toast.makeText(Signup.this, R.string.success_signup, Toast.LENGTH_SHORT).show();
////                        } else {
////                            // 회원가입 실패
////                            Toast.makeText(Signup.this, R.string.failed_signup, Toast.LENGTH_SHORT).show();
////                        }
//                    }
//                });
//    }
//
//    // 로그인
//    private void loginUser(String email, String password)
//    {
//        firebaseAuth.signInWithEmailAndPassword(email, password)
//                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
//                    @Override
//                    public void onComplete(@NonNull Task<AuthResult> task) {
//                        if (task.isSuccessful()) {
//                            // 로그인 성공
//                            Toast.makeText(Signup.this, R.string.success_login, Toast.LENGTH_SHORT).show();
//                        } else {
//                            // 로그인 실패
//                            Toast.makeText(Signup.this, R.string.failed_login, Toast.LENGTH_SHORT).show();
//                        }
//                    }
//                });
//   }

}
