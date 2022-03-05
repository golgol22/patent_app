package com.example.hjy.patent;

import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.example.hjy.patent.model.UserModel;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.Map;

public class Interest_setting  extends AppCompatActivity {

    private CheckBox checkBox_company, checkBox_part;
    private EditText editText;
    private Button button_setting;
    private TextView textView_company, textView_part, textView_company2, textView_part2;
    private RadioGroup radioGroup;
    private Button button_store;
    private LinearLayout linearLayout1, linearLayout2;
    UserModel userModel;
    String uid;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.interest_setting);
        setTitle("관심 기업. 분야 설정");

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        actionBar.setHomeButtonEnabled(true);
        actionBar.setDisplayHomeAsUpEnabled(true);  //뒤로가기
        actionBar.setDisplayShowTitleEnabled(false);

        checkBox_company = (CheckBox) findViewById(R.id.interest_setting_checkbox_company);
        checkBox_part = (CheckBox) findViewById(R.id.interest_setting_checkbox_part);
        editText = (EditText) findViewById(R.id.interest_setting_edittext);
        button_setting =(Button) findViewById(R.id.interest_setting_button_setting);
        textView_company = (TextView) findViewById(R.id.interest_setting_textview_company);
        textView_part = (TextView) findViewById(R.id.interest_setting_textview_part);
        textView_company2 = (TextView) findViewById(R.id.interest_setting_textview_company2);
        textView_part2 = (TextView) findViewById(R.id.interest_setting_textview_part2);
        radioGroup = (RadioGroup) findViewById(R.id.interest_setting_radiogroup);
        button_store =(Button) findViewById(R.id.interest_setting_button_store);
        linearLayout1=(LinearLayout) findViewById(R.id.interest_setting_linear1);
        linearLayout2=(LinearLayout) findViewById(R.id.interest_setting_linear2);


       uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        FirebaseDatabase.getInstance().getReference().child("users").child(uid).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()) { //--> null오류 해결
                    userModel = dataSnapshot.getValue(UserModel.class);

                    if(userModel.interest_company ==null){
                        checkBox_company.setChecked(false);
                        linearLayout1.setVisibility(View.INVISIBLE);
                        textView_company.setText(userModel.interest_company);
                        textView_part.setText(userModel.interest_part);
                        textView_company2.setVisibility(View.VISIBLE);
                        textView_part2.setVisibility(View.VISIBLE);
                    }else{
                        checkBox_company.setChecked(true);
                        textView_company.setText(userModel.interest_company);
                        textView_part.setText(userModel.interest_part);
                        textView_company2.setVisibility(View.VISIBLE);
                        textView_part2.setVisibility(View.VISIBLE);
                    }

                    if(userModel.interest_part == null){
                        linearLayout2.setVisibility(View.INVISIBLE);
                        checkBox_part.setChecked(false);
                        textView_company.setText(userModel.interest_company);
                        textView_part.setText(userModel.interest_part);
                        textView_company2.setVisibility(View.VISIBLE);
                        textView_part2.setVisibility(View.VISIBLE);
                    }else{
                        checkBox_part.setChecked(true);
                        textView_company.setText(userModel.interest_company);
                        textView_part.setText(userModel.interest_part);
                        textView_company2.setVisibility(View.VISIBLE);
                        textView_part2.setVisibility(View.VISIBLE);
                    }
                }else{

                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        checkBox_company.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(checkBox_company.isChecked()){
                    linearLayout1.setVisibility(View.VISIBLE);
                }else{
                    linearLayout1.setVisibility(View.INVISIBLE);
                }
            }
        });

        checkBox_part.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(checkBox_part.isChecked()){
                    linearLayout2.setVisibility(View.VISIBLE);
                }else{
                    linearLayout2.setVisibility(View.INVISIBLE);
                }
            }
        });

        button_setting.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                textView_company.setText(editText.getText().toString());
                textView_company2.setVisibility(View.VISIBLE);
                editText.setText("");
            }
        });

        radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                switch (radioGroup.getCheckedRadioButtonId()){
                    case R.id.a : textView_part.setText("생활필수품/농업"); textView_part2.setVisibility(View.VISIBLE); break;
                    case R.id.b : textView_part.setText("처리조작/운수"); textView_part2.setVisibility(View.VISIBLE); break;
                    case R.id.c : textView_part.setText("화학/야금"); textView_part2.setVisibility(View.VISIBLE); break;
                    case R.id.d : textView_part.setText("섬유/지류"); textView_part2.setVisibility(View.VISIBLE); break;
                    case R.id.e : textView_part.setText("조정구조물"); textView_part2.setVisibility(View.VISIBLE); break;
                    case R.id.f :  textView_part.setText("기계공학"); textView_part2.setVisibility(View.VISIBLE); break;
                    case R.id.g :  textView_part.setText("물리학"); textView_part2.setVisibility(View.VISIBLE); break;
                    case R.id.h :  textView_part.setText("전기"); textView_part2.setVisibility(View.VISIBLE); break;
                }
            }
        });

        button_store.setOnClickListener(new View.OnClickListener() {//저장
            @Override
            public void onClick(View v) {
                if (checkBox_company.isChecked()) {
                    final String company = textView_company.getText().toString();
                    Map<String, Object> map = new HashMap<>();
                    map.put("interest_company", company);

                    FirebaseDatabase.getInstance().getReference().child("users").child(uid).updateChildren(map).addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
//                            Toast.makeText(Interest_setting.this, "설정이 완료되었습니다.\n메인화면에서 동향을 확인해보세요.", Toast.LENGTH_SHORT).show();
                        }
                    });
                }else{
                    final String company =null;
                    Map<String, Object> map = new HashMap<>();
                    map.put("interest_company", company);

                    FirebaseDatabase.getInstance().getReference().child("users").child(uid).updateChildren(map).addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
//                            Toast.makeText(Interest_setting.this, "설정이 완료되었습니다.\n메인화면에서 동향을 확인해보세요.", Toast.LENGTH_SHORT).show();
                        }
                    });
                }

                if(checkBox_part.isChecked()){
                    final String part = textView_part.getText().toString();
                    Map<String, Object> map = new HashMap<>();
                    map.put("interest_part", part);
                    FirebaseDatabase.getInstance().getReference().child("users").child(uid).updateChildren(map).addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            Toast.makeText(Interest_setting.this, "설정이 완료되었습니다.\n메인화면에서 동향을 확인해보세요.", Toast.LENGTH_SHORT).show();
                        }
                    });
                }else{
                    final String part = null;
                    Map<String, Object> map = new HashMap<>();
                    map.put("interest_part", part);
                    FirebaseDatabase.getInstance().getReference().child("users").child(uid).updateChildren(map).addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            Toast.makeText(Interest_setting.this, "설정이 완료되었습니다.\n메인화면에서 동향을 확인해보세요.", Toast.LENGTH_SHORT).show();
                        }
                    });
                }

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
}