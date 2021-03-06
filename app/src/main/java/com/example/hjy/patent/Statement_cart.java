package com.example.hjy.patent;

import android.app.ActivityOptions;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
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
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.hjy.patent.model.StatementModel;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class Statement_cart extends AppCompatActivity {

    RecyclerView recyclerView;
//    Button button_search;
    ProgressDialog progressDialog;
    String statementRoomid="";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.statement_cart);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        actionBar.setHomeButtonEnabled(true);
        actionBar.setDisplayHomeAsUpEnabled(true);  //????????????
        actionBar.setDisplayShowTitleEnabled(false); //?????? ????????? ?????????

        progressDialog = new ProgressDialog(this);
        recyclerView = (RecyclerView) findViewById(R.id.statement_cart_recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(getLayoutInflater().getContext()));
        recyclerView.setAdapter(new StatementRecyclerViewAdapter());

//        button_search = (Button) findViewById(R.id.statement_cart_button_search);
//        button_search.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                Intent intent = new Intent(getApplicationContext(), Search_ing.class);
//                startActivity(intent);
//            }
//        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.cartmenu, menu);
        return true;
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.

        int id = item.getItemId();
        if(id == R.id.menu_add) {
            //??????
            Intent intent = new Intent(getApplicationContext(), Statement_add.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
            startActivity(intent);
        }else if(id ==R.id.menu_delete){
            //??????
        }else if(id == android.R.id.home){
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        //super.onBackPressed();
        //??????????????? ???????????? ??????(???)?????? ?????? ????????? ??????
        Intent intent = new Intent(getApplicationContext(), MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
    }



    class StatementRecyclerViewAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder>{

        private List<StatementModel> statementModels;
        private String uid;

        public StatementRecyclerViewAdapter() {  //????????? ??????
            //????????? ????????? ??????????????? ????????? ????????? ?????????

            statementModels = new ArrayList<>();
            uid= FirebaseAuth.getInstance().getCurrentUser().getUid();//???(???????????? ???????????? ?????????)

            FirebaseDatabase.getInstance().getReference().child("statementrooms").child(uid).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    for (DataSnapshot item : dataSnapshot.getChildren()) {
                        if(dataSnapshot.exists()) {
                            statementRoomid = item.getKey();
                        }else{
                            Toast.makeText(Statement_cart.this, "???????????? ??????????????????.", Toast.LENGTH_SHORT).show();
                        }
                    }
                }
                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });


            //??????????????? ????????? ????????????
            FirebaseDatabase.getInstance().getReference().child("statementrooms").child(uid).child(statementRoomid).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    statementModels.clear();
                    for (DataSnapshot item : dataSnapshot.getChildren()) {
                        if(dataSnapshot.exists()) {
                            statementModels.add(item.getValue(StatementModel.class));
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
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_statement,parent,false);
            return new StatementViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull final RecyclerView.ViewHolder holder, final int position) {  // ????????? ???????????? ????????? ????????? ??????


            FirebaseDatabase.getInstance().getReference().child("statementrooms").child(uid).child(statementRoomid).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {

                    ((StatementViewHolder)holder).textView_title.setText(statementModels.get(position).statement_title);
                    ((StatementViewHolder)holder).textView_content.setText(statementModels.get(position).statement_content);
                    ((StatementViewHolder)holder).textView_time.setText(statementModels.get(position).statement_time);

                    ((StatementViewHolder)holder).imageView.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {

                            //?????? ?????? ????????????
                            FirebaseStorage firebaseStorage = FirebaseStorage.getInstance();
                            StorageReference storageRef = firebaseStorage.getReference();
                            StorageReference islandRef = storageRef.child("statements/"+uid+"/"+statementModels.get(position).statement_time);

                            if (islandRef != null) {
                                progressDialog.setTitle("Downloading...");//"Downloading..."
                                progressDialog.setMessage(null);
                                progressDialog.show();

                                try {
                                    final File rootPath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS); // Download ????????? ??????
                                    if(!rootPath.exists()) {
                                        rootPath.mkdirs();
                                    }
                                    final File localFile = new File(rootPath, statementModels.get(position).statement_title+".pdf"); //????????? ???????????? ??????=> ????????? ??????

                                    islandRef.getFile(localFile).addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
                                        @Override
                                        public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {
                                            // Local temp file has been created
                                            // use localFile
                                            Toast.makeText(Statement_cart.this, "???????????? ??????", Toast.LENGTH_SHORT).show();
                                            progressDialog.dismiss();
                                        }
                                    }).addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception exception) {
                                            // Handle any errors
                                            progressDialog.dismiss();
                                            Toast.makeText(getApplicationContext(), exception.getMessage(), Toast.LENGTH_LONG).show();
                                        }
                                    }).addOnProgressListener(new OnProgressListener<FileDownloadTask.TaskSnapshot>() {
                                        @Override
                                        public void onProgress(FileDownloadTask.TaskSnapshot taskSnapshot) {
                                            // taskSnapshot.getBytesTransferred()
                                            // taskSnapshot.getTotalByteCount();
                                            double progress = (100.0 * taskSnapshot.getBytesTransferred()) / taskSnapshot.getTotalByteCount();
                                            progressDialog.setMessage("Downloaded " + ((int) progress) + "%...");
                                        }
                                    });
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }else {
                                Toast.makeText(getApplicationContext(), "Upload file before downloading", Toast.LENGTH_LONG).show();
                            }

                        }
                    });
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });


            ((StatementViewHolder)holder).itemView.setOnClickListener(new View.OnClickListener() {  //????????? ??????
                @Override
                public void onClick(View v) { //?????? ??? ?????? ?????? //????????? item??? ???????????? ??????????????? ??????
                    Intent intent = new Intent(v.getContext(), Statement_add.class);
                    ActivityOptions activityOptions = null; //?????? ??????????????? ??????
                    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.JELLY_BEAN) {
                        activityOptions = ActivityOptions.makeCustomAnimation(v.getContext(), R.anim.fromright,R.anim.toleft);
                        startActivity(intent, activityOptions.toBundle());
                    }
                }
            });


        }

        @Override
        public int getItemCount() {
            return statementModels.size();
        }

        private class StatementViewHolder extends RecyclerView.ViewHolder { //inner class

            public TextView textView_time;
            public CheckBox checkBox;
            public TextView textView_title;
            public TextView textView_content;
            public ImageView imageView;

            public StatementViewHolder(View view) {
                super(view);
                textView_time =(TextView) view.findViewById(R.id.statementitem_textview_time);
                checkBox = (CheckBox) view.findViewById(R.id.statementitem_checkbox);
                textView_title = (TextView)view.findViewById(R.id.statementitem_textview_title);
                textView_content = (TextView)view.findViewById(R.id.statementitem_textview_content);
                imageView = (ImageView) view.findViewById(R.id.statementitem_imageview);
            }
        }
    }

}
