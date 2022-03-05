package com.example.hjy.patent;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.hjy.patent.model.StatementModel;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.text.SimpleDateFormat;
import java.util.Date;

public class Statement_add extends AppCompatActivity {

    private static final int PICK_FILE = 20;
    private EditText title;
    private EditText content;
    private TextView filename;
    private Button button_file;
    private Button button_cancel;
    private Button button_store;
    private FirebaseStorage firebaseStorage;
    private StorageReference storageReference;
    private Uri fileUri;
    ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.statement_add);

        firebaseStorage = FirebaseStorage.getInstance();
        storageReference = firebaseStorage.getReference();
        progressDialog = new ProgressDialog(this);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        actionBar.setHomeButtonEnabled(true);
        actionBar.setDisplayHomeAsUpEnabled(true);  //뒤로가기
        actionBar.setDisplayShowTitleEnabled(false);

        title = (EditText) findViewById(R.id.statement_add_edittext_title);
        content = (EditText) findViewById(R.id.statement_add_edittext_content);
        filename = (TextView) findViewById(R.id.statement_add_textview_filename);
        button_file = (Button) findViewById(R.id.statement_add_button_file);
        button_cancel = (Button) findViewById(R.id.statement_add_button_cancel);
        button_store = (Button) findViewById(R.id.statement_add_button_store);

        button_file.setOnClickListener(new View.OnClickListener() { //파일 선택
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                fileUri = Uri.parse(Environment.getExternalStorageDirectory().getAbsolutePath()+"/Download/");
                intent.setDataAndType(fileUri, "application/*");
                startActivityForResult(Intent.createChooser(intent, "Open"), PICK_FILE);  //filename.setText(fileUri.toString());
            }
        });

        button_cancel.setOnClickListener(new View.OnClickListener() { //취소
            @Override
            public void onClick(View v) {
                title.setText("");
                content.setText("");
                filename.setText("첨부파일 없음");
                Intent intent = new Intent(getApplicationContext(), Statement_cart.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                startActivity(intent);
            }
        });

        button_store.setOnClickListener(new View.OnClickListener() { //저장
            @Override
            public void onClick(View v) {

                final String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
                final StatementModel statementModel = new StatementModel();


                long now = System.currentTimeMillis(); //현재시간을 구함
                Date date= new Date(now); //현재시간을(now) 변수에 저장
                SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy.MM.dd HH:mm:ss");
                final String time = simpleDateFormat.format(date); //현재 시간을 형식에 맞게 만들고 변수에 저장

                button_store.setEnabled(false);
                storageReference.child("statements").child(uid).child(time).putFile(fileUri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {

                        String statement_fileUrl = storageReference.getDownloadUrl().toString(); //파일을 스토리지에 넣고, 그 url주소를 DB에 저장
                        statementModel.statement_title = title.getText().toString();
                        statementModel.statement_content = content.getText().toString();
                        statementModel.fileUrl = statement_fileUrl;
                        statementModel.statement_time = time;

                        FirebaseDatabase.getInstance().getReference().child("statementrooms").child(uid).push().setValue(statementModel).addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if (!task.isSuccessful()) {
                                    Toast.makeText(Statement_add.this, task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                                }else {
                                    Statement_add.this.finish();
                                    Toast.makeText(Statement_add.this, "저장 완료", Toast.LENGTH_SHORT).show();
                                    Intent i = new Intent(getApplicationContext(), Statement_cart.class);
                                    i.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                                    startActivity(i);
                                    button_store.setEnabled(true);
                                }
                            }
                        });
                    }
                });


            }
        });


    }//onCreate


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
        if(requestCode==PICK_FILE &&resultCode==RESULT_OK) {
            filename.setText(data.getData().toString()); //선택한 파일의 경로로 변경
            fileUri=data.getData(); //이미지 경로 원본
        }
    }

}