package com.example.hjy.patent;

import android.app.ActivityOptions;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.example.hjy.patent.model.ChatModel;
import com.example.hjy.patent.model.NotificationModel;
import com.example.hjy.patent.model.UserModel;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;
import com.google.gson.Gson;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;


public class Talk_ing extends AppCompatActivity { //MessageActivity

    private ImageButton button;
    private EditText editText;
    private TextView title;

    private String destinationUid;
    private String uid;
    private String chatRoomUid;
    UserModel userModel;

    private static final int PICK_FILE = 20;
    private Uri fileUri;

    private RecyclerView recyclerView;

    private SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy.MM.dd HH:mm");

    private UserModel destinationUserModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.talk_ing);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        actionBar.setHomeButtonEnabled(true);
        actionBar.setDisplayHomeAsUpEnabled(true);  //????????????
        actionBar.setDisplayShowTitleEnabled(false);

        destinationUid = getIntent().getStringExtra("destinationUid"); //????????? ????????? ?????????

        FirebaseDatabase.getInstance().getReference().child("users").child(destinationUid).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()) { //--> null?????? ??????
                    userModel = dataSnapshot.getValue(UserModel.class);
                    title.setText(userModel.userName); //??????????????? ????????? ???????????? ??????

                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });


        button = (ImageButton) findViewById(R.id.btn_send);
        editText = (EditText) findViewById(R.id.edit_input);
        recyclerView = (RecyclerView) findViewById(R.id.talk_ing_recyclerview);
        title = (TextView) findViewById(R.id.talk_ing_textview_title);

        uid = FirebaseAuth.getInstance().getCurrentUser().getUid();

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {  //????????????(???????????? ????????? ??????
                //????????? ?????? Firebase Database ????????? ??????

                ChatModel chatModel = new ChatModel();
                chatModel.users.put(uid, true); //????????? ??????
                chatModel.users.put(destinationUid, true); //?????? ??????

                if(chatRoomUid == null){
                    button.setEnabled(false); //????????? ????????? ????????? ????????? ????????? ???????????? ????????? ????????? ?????? ???????????? ?????? ?????? ????????? ????????? ???
                    FirebaseDatabase.getInstance().getReference().child("chatrooms").push().setValue(chatModel).addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            checkChatRoom(); //????????? ????????? ?????? ???????????? ????????? ????????? ??????!
                        }
                    });  //???????????? push??? ????????????
                }else {
                    ChatModel.Comment comment = new ChatModel.Comment();
                    comment.uid = uid;
                    comment.message = editText.getText().toString();
                    comment.timestamp = ServerValue.TIMESTAMP;  //???????????? ?????? ????????? ?????? ??????
                    FirebaseDatabase.getInstance().getReference().child("chatrooms").child(chatRoomUid).child("comments").push().setValue(comment).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            sendGcm();
                            editText.setText(""); //???????????? ??????(??????)??? ?????? ????????? ?????? ?????????
                        }
                    });
                }
            }
        });
        checkChatRoom();

    }//onCreate

    void sendGcm() {
        Gson gson = new Gson();

        String userNmae = FirebaseAuth.getInstance().getCurrentUser().getDisplayName();
        NotificationModel notificationModel = new NotificationModel();
        notificationModel.to = destinationUserModel.pushToken;
        notificationModel.notification.title = userNmae;
        notificationModel.notification.text = editText.getText().toString();
        notificationModel.data.title = userNmae;
        notificationModel.data.text = editText.getText().toString();

        RequestBody requestBody = RequestBody.create(MediaType.parse("application/json; charset = utf8"), gson.toJson(notificationModel));
        Request requset = new Request.Builder()
                .header("Content-Type", "application/json")
                .addHeader("Authorization", "key=AIzaSyD4Y1hnSnGgLZjt6-gmxp-hjHU-N6V87kA")
                .url("https://fcm.googleapis.com/fcm/send ")
                .post(requestBody)
                .build();
        OkHttpClient okHttpClient = new OkHttpClient();
        okHttpClient.newCall(requset).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) { //??????

            }

            @Override
            public void onResponse(Call call, Response response) throws IOException { //??????

            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.sharemenu, menu);
        return true;
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
            case R.id.share:
                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                fileUri = Uri.parse(Environment.getExternalStorageDirectory().getAbsolutePath()+"/Download/");
                intent.setDataAndType(fileUri, "application/*");
                startActivityForResult(Intent.createChooser(intent, "Open"), PICK_FILE);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        //super.onBackPressed();
        finish();
        Intent intent = new Intent(getApplicationContext(), Talk_list.class);
        ActivityOptions activityOptions = null;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.JELLY_BEAN) {
            activityOptions = ActivityOptions.makeCustomAnimation(getApplicationContext(), R.anim.fromleft, R.anim.toright);
            startActivity(intent, activityOptions.toBundle());
        }
    }

    void checkChatRoom() {  //????????? ?????? ??????
        FirebaseDatabase.getInstance().getReference().child("chatrooms").orderByChild("users/"+uid).equalTo(true).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for(DataSnapshot item:dataSnapshot.getChildren()){
                    ChatModel chatModel = item.getValue(ChatModel.class);
                        if(chatModel.users.containsKey(destinationUid)){
                            chatRoomUid = item.getKey();
                            button.setEnabled(true); //??? ???id??? ??????????????? ?????? ?????????????????? ??????
                            recyclerView.setLayoutManager(new LinearLayoutManager(Talk_ing.this));
                            recyclerView.setAdapter(new RecyclerViewAdapter());
                        }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    class RecyclerViewAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

        List<ChatModel.Comment> comments;

        public RecyclerViewAdapter() {
            comments = new ArrayList<>();

            FirebaseDatabase.getInstance().getReference().child("users").child(destinationUid).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    if(dataSnapshot.exists()) { //--> null?????? ??????
                        destinationUserModel = dataSnapshot.getValue(UserModel.class);
                        getMessageList();
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
        }

        void getMessageList(){  //????????? ????????? ????????????
            FirebaseDatabase.getInstance().getReference().child("chatrooms").child(chatRoomUid).child("comments").addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    comments.clear(); //???????????? ?????? ????????? ?????? ??????

                    for (DataSnapshot item : dataSnapshot.getChildren()){
                            comments.add(item.getValue(ChatModel.Comment.class));
                    }
                    //???????????? ??????
                    notifyDataSetChanged();

                    recyclerView.scrollToPosition(comments.size()-1); //??????????????? ?????? ???????????? ????????? ???, ?????? ??????????????? ???????????? ?????????
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });

        }

        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view  = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_message, parent, false);
            return new MessageViewHolder(view);
        }

        @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR1)
        @Override
        public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
            MessageViewHolder messageViewHolder = ((MessageViewHolder)holder);

            //?????? ?????? ?????????
            if(comments.get(position).uid.equals(uid)){
                messageViewHolder.textView_message.setText(comments.get(position).message);
                messageViewHolder.textView_message.setBackgroundResource(R.drawable.ic_1);
                messageViewHolder.imageview_profile.setVisibility(View.INVISIBLE);
                messageViewHolder.textView_name.setVisibility(View.GONE); //??? ???????????? ?????? ???????????? ?????? ??????(gone?????? ???????????? ????????? ???)
                messageViewHolder.textView_message.setTextSize(16);
                //?????????????????? ????????? ????????? ?????? ???????????? ???????????? ????????? ?????? ??????
                long unixTime = (long)comments.get(position).timestamp;
                Date date = new Date(unixTime);
                simpleDateFormat.setTimeZone(TimeZone.getTimeZone("Asia/Seoul"));
                String time = simpleDateFormat.format(date);
                messageViewHolder.textView_timestamp.setText(time);
                messageViewHolder.textView_timestamp.setGravity(Gravity.RIGHT);
                messageViewHolder.linearLayout_1.setGravity(Gravity.RIGHT);
                messageViewHolder.linearLayout_2.setGravity(Gravity.RIGHT);
                messageViewHolder.linearLayout_main.setGravity(Gravity.RIGHT); //??????????????? ??????
            }else{ //???????????? ?????? ?????????
                RequestOptions requestOptions = new RequestOptions();
                requestOptions.placeholder(R.drawable.ic_account_circle_black_24dp);
                requestOptions.fitCenter();
                requestOptions.apply(new RequestOptions().circleCrop());
                requestOptions.centerCrop();

                Glide.with(Talk_ing.this)
                        .setDefaultRequestOptions(requestOptions)
                        .load(destinationUserModel.profileImageUrl)
                        .into(messageViewHolder.imageview_profile);

//                Glide.with(holder.itemView.getContext())
//                        .load(userModel.profileImageUrl)
//                        .apply(new RequestOptions().circleCrop())
//                        .into(messageViewHolder.imageview_profile);
                messageViewHolder.textView_name.setText(destinationUserModel.userName);
                messageViewHolder.textView_name.setVisibility(View.VISIBLE);
                messageViewHolder.imageview_profile.setVisibility(View.VISIBLE);
                messageViewHolder.textView_message.setBackgroundResource(R.drawable.ic_2);
                messageViewHolder.textView_message.setText(comments.get(position).message);
                messageViewHolder.textView_message.setTextSize(16);
                messageViewHolder.textView_message.setMaxWidth(1000);
                //?????????????????? ????????? ????????? ?????? ???????????? ???????????? ????????? ?????? ??????
                long unixTime = (long)comments.get(position).timestamp;
                Date date = new Date(unixTime);
                simpleDateFormat.setTimeZone(TimeZone.getTimeZone("Asia/Seoul"));
                String time = simpleDateFormat.format(date);
                messageViewHolder.textView_timestamp.setText(time);
                messageViewHolder.textView_timestamp.setGravity(Gravity.LEFT);
                messageViewHolder.linearLayout_1.setGravity(Gravity.LEFT);
                messageViewHolder.linearLayout_2.setGravity(Gravity.LEFT);
                messageViewHolder.linearLayout_main.setGravity(Gravity.LEFT); //???????????? ??????
            }//else

        }

        @Override
        public int getItemCount() {
            return comments.size();
        }

        private class MessageViewHolder extends RecyclerView.ViewHolder {
            public TextView textView_message;
            public TextView textView_name;
            public ImageView imageview_profile;
            public LinearLayout linearLayout_main;
            public LinearLayout linearLayout_1, linearLayout_2;
            public TextView textView_timestamp;

            public MessageViewHolder(View view) {
                super(view);
                textView_message = (TextView) view.findViewById(R.id.messageItem_textView_message);
                textView_name = (TextView) view.findViewById(R.id.messageItem_textView_name);
                imageview_profile = (ImageView) view.findViewById(R.id.messageItem_imageview_profile);
                linearLayout_1 = (LinearLayout) view.findViewById(R.id.messageItem_linearlayout_1);
                linearLayout_2 = (LinearLayout) view.findViewById(R.id.messageItem_linearlayout_2);
                linearLayout_main = (LinearLayout) view.findViewById(R.id.messageItem_linearlayout_main);
                textView_timestamp = (TextView) view.findViewById(R.id.messageItem_textView_timpstamp);
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(requestCode==PICK_FILE &&resultCode==RESULT_OK) {
//            filename.setText(data.getData().toString());
            fileUri=data.getData(); //????????? ?????? ??????
        }
    }

}
