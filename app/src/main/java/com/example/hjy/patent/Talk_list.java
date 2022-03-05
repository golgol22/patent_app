package com.example.hjy.patent;

import android.app.ActivityOptions;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.example.hjy.patent.model.ChatModel;
import com.example.hjy.patent.model.UserModel;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;
import java.util.TreeMap;

public class Talk_list extends AppCompatActivity {  //ChatFragment, 대화방 리스트

    RecyclerView recyclerView;  //디펜던시 추가해야함
    private SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy.MM.dd HH:mm");

    UserModel userModel;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.talk_list);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        actionBar.setHomeButtonEnabled(true);
        actionBar.setDisplayHomeAsUpEnabled(true);  //뒤로가기
        actionBar.setDisplayShowTitleEnabled(false);

        recyclerView = (RecyclerView) findViewById(R.id.talk_list_recyclerview);
        recyclerView.setLayoutManager(new LinearLayoutManager(getLayoutInflater().getContext()));
        recyclerView.setAdapter(new ChatRecyclerViewAdapter());

   }//onCreate

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.talkmenu, menu);
        return true;
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
//            case R.id.talk_menu_delete:
//
//                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        //super.onBackPressed();
        //뒤로가기를 눌렀을때 에인(홈)으로 가는 화면에 추가
        Intent intent = new Intent(getApplicationContext(), MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
    }

    class ChatRecyclerViewAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder>{

        private List<ChatModel> chatModels = new ArrayList<>();
        private String uid;
        public ArrayList<String> destinationUsers = new ArrayList<>();

        public ChatRecyclerViewAdapter() {  //채팅목록
            //어댑터 생성시 컨텍스트와 데이터 배열을 가져옴

            uid= FirebaseAuth.getInstance().getCurrentUser().getUid();//나(단말기에 로그인한 사용자)

            FirebaseDatabase.getInstance().getReference().child("chatrooms").orderByChild("users/"+uid).equalTo(true).addListenerForSingleValueEvent(new ValueEventListener() {
                //equalTo(true)로 인해 내가 소속된 방만 뜬다.
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    chatModels.clear();
                        for (DataSnapshot item : dataSnapshot.getChildren()) {
                            if(dataSnapshot.exists()) {
                            chatModels.add(item.getValue(ChatModel.class));
                        }
                    }
                    notifyDataSetChanged();
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
        }

        @NonNull
        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_chat,parent,false);

            return new CustomViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, final int position) {

            final CustomViewHolder customViewHolder = (CustomViewHolder)holder;
            String destinationUid = null;

            //챗방에 있는 유저를 체크
            for(String user: chatModels.get(position).users.keySet()){
                if(!user.equals(uid)){  //내가 아닌 사람의 목록을 가져오면 채팅방 이름이 됨
                    destinationUid = user; //내가 아닌 사람을 뽑아
                    destinationUsers.add(destinationUid); //상대방 배열에 추가
                }
            }

            FirebaseDatabase.getInstance().getReference().child("users").child(destinationUid).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    userModel = dataSnapshot.getValue(UserModel.class);
                    //채팅방 목록의 이미지 설정
                    RequestOptions requestOptions = new RequestOptions();
                    requestOptions.placeholder(R.drawable.ic_account_circle_black_24dp);
                    requestOptions.fitCenter();
                    requestOptions.apply(new RequestOptions().circleCrop());
                    requestOptions.centerCrop();

                    Glide.with(Talk_list.this)
                            .setDefaultRequestOptions(requestOptions)
                            .load(userModel.profileImageUrl)
                            .into(customViewHolder.imageView);

//                    Glide.with(customViewHolder.imageView.getContext())
//                            .load(userModel.profileImageUrl)
//                            .apply(new RequestOptions().circleCrop())
//                            .into(customViewHolder.imageView);
                    //UerModel에는 이미지와 이름 두개가 담겨있는데
                    //load로 누구의 이미지를 불러올 것인지 결정,
                    //apply로 이미지를 어떤모양으로 만들 것인지 결정,
                    //into로 어디에 이미지를 커스텀 해줄 것인지 결정

                    //채팅방 목록의 타이틀 설정(상대방 이름)
                    customViewHolder.textView_title.setText(userModel.userName);

                    //마지막으로 보낸 메시지 설정
                    Map<String, ChatModel.Comment> commentMap = new TreeMap<>(Collections.<String>reverseOrder()); //채팅방 목록을 내림차순으로 정렬
                    commentMap.putAll(chatModels.get(position).comments); //메지시를 모두 담는다.
                    String lastMessageKey = (String) commentMap.keySet().toArray()[0]; //내림차순으로 정렬한 모든 메시지 중 첫번째 메시지의 키값을 뽑는다.
                    customViewHolder.textView_last_message.setText(chatModels.get(position).comments.get(lastMessageKey).message);//해당 채팅방 position의 첫 번째 메시지의 키값으로 첫번째 메시지를 불어와 설정
                    //채팅방 메시지는 암호화해서 서버에 저장됨

                    long unixTime = (long) chatModels.get(position).comments.get(lastMessageKey).timestamp;
                    Date date = new Date(unixTime);
                    simpleDateFormat.setTimeZone(TimeZone.getTimeZone("Asia/seoul"));
                    customViewHolder.textView_timestamp.setText(simpleDateFormat.format(date));
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });

            customViewHolder.itemView.setOnClickListener(new View.OnClickListener() { //채팅방을 띄워주는 부분
                @Override
                public void onClick(View v) { //클릭 후 화면 이동 //채팅방 item을 클릭하면 채팅방으로 이동
                    Intent intent = new Intent(v.getContext(), Talk_ing.class);
                    ActivityOptions activityOptions = null; //화면 애니메이션 지정
                    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.JELLY_BEAN) {
                        activityOptions = ActivityOptions.makeCustomAnimation(v.getContext(), R.anim.fromright,R.anim.toleft);
                        intent.putExtra("destinationUid", destinationUsers.get(position));
                        startActivity(intent, activityOptions.toBundle());
                }
                }
            });


        }

        @Override
        public int getItemCount() {
            return chatModels.size();
        }

        private class CustomViewHolder extends RecyclerView.ViewHolder { //inner class
            public ImageView imageView;
            public TextView textView_title;
            public TextView textView_last_message;
            public TextView textView_timestamp;

            public CustomViewHolder(View view) {
                super(view);

                imageView = (ImageView) view.findViewById(R.id.chatItem_imageview);
                textView_title = (TextView)view.findViewById(R.id.chatItem_textview_title);
                textView_last_message = (TextView)view.findViewById(R.id.chatItem_textview_last_message);
                textView_timestamp = (TextView)view.findViewById(R.id.chatItem_textview_timestamp);
            }
        }
    }

}