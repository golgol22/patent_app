package com.example.hjy.patent;

import android.annotation.SuppressLint;
import android.app.ActivityOptions;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.design.internal.BottomNavigationItemView;
import android.support.design.internal.BottomNavigationMenuView;
import android.support.design.widget.BottomNavigationView;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TabHost;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.example.hjy.patent.model.UserModel;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.formatter.PercentFormatter;
import com.github.mikephil.charting.utils.ColorTemplate;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HTTP;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    //linear1 (동향 그래프)
    private PieChart pieChart;
    private  float[] yData = {5, 10, 15, 30, 40};
    private  String[] xData = {"유기화학", "도금", "무기화학", "전기분해", "기타"};
  //  private String URL = "http://kpat.kipris.or.kr/kpat/searchLogina.do?next=MainSearch#page3"; //파싱할 홈페이지의 URL주소
    private String htmlContentInStringFormat = ""; //가져온 데이터를 담는 변수
    private TextView textview_htmlDocument; //가져온 데이터를 보여줄 뷰

   // LinearLayout linearLayout;
    //linear2 (서류 작성)
    private Button example1, statment1;
    private Button example2, statment2;
    ProgressDialog progressDialog;

    //linear3 (office 목록)
    RecyclerView recyclerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        progressDialog = new ProgressDialog(this);
        BottomNavigationView navigation = (BottomNavigationView) findViewById(R.id.navigation);
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);
        //static으로 선언해야하지만 이너클래스 안에서 생성한 클래스이므로 static을 사용할 수 없으므로
        //클래스 객체를 생성하여 메소드를 사용하여야 함
        BottomNavigationViewHelper n = new BottomNavigationViewHelper();
        n.removeShiftMode(navigation);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        actionBar.setHomeButtonEnabled(true);
        actionBar.setDisplayHomeAsUpEnabled(true);  //뒤로가기
        actionBar.setDisplayShowTitleEnabled(false); //원래 타이틀 지우기

        TabHost tabHost = (TabHost) findViewById(R.id.tabHost);
        tabHost.setup();

        TabHost.TabSpec tab1 = tabHost.newTabSpec("TAB1").setIndicator("홈");
        tab1.setContent(R.id.linear1);
        tabHost.addTab(tab1);

        TabHost.TabSpec tab2 = tabHost.newTabSpec("TAB2").setIndicator("서류작성");
        tab2.setContent(R.id.linear2);
        tabHost.addTab(tab2);

        TabHost.TabSpec tab3 = tabHost.newTabSpec("TAB3").setIndicator("상담신청");
        tab3.setContent(R.id.linear3); //PeopleFragment
        tabHost.addTab(tab3);

        TabHost.TabSpec tab4 = tabHost.newTabSpec("TAB4").setIndicator("정보제공");
        tab4.setContent(R.id.linear4);
        tabHost.addTab(tab4);

        //탭호스트 글자색 변경
        for (int i = 0; i < tabHost.getTabWidget().getChildCount(); i++) {
            LinearLayout relLayout = (LinearLayout) tabHost.getTabWidget().getChildAt(i);
            TextView tv = (TextView) relLayout.getChildAt(1);
            tv.setTextColor(Color.WHITE);
        }
        //tabHost.setCurrentTab(0);
        passPushTokenToServer(); //푸시 메시지를 위한 토큰 넣기

       // textview_htmlDocument = (TextView) findViewById(R.id.mainactivity_textview_data);

       // linearLayout = (LinearLayout) findViewById(R.id.linear1);
        recyclerView = (RecyclerView) findViewById(R.id.main_3_recycle);
        recyclerView.setLayoutManager(new LinearLayoutManager(getLayoutInflater().getContext()));
        recyclerView.setAdapter(new PeopleRecyclerViewAdapter());

        example1 = (Button) findViewById(R.id.mainactivity_example1);
        example2 = (Button) findViewById(R.id.mainactivity_example2);
        statment1 = (Button) findViewById(R.id.mainactivity_statment1);
        statment2 = (Button) findViewById(R.id.mainactivity_statment2);

//        textview_htmlDocument.setMovementMethod(new ScrollingMovementMethod()); //스크롤 가능한 텍스트뷰로 만들기


        //웹 크롤링
        JsoupAsyncTask jsoupAsyncTask = new JsoupAsyncTask();
        jsoupAsyncTask.execute();  //main자체에서 네트워크 통신을 하면 오류남  Caused by: android.os.NetworkOnMainThreadException

        pieChart = (PieChart) findViewById(R.id.mainactivity_piechart);

      // linearLayout.addView(pieChart);
        pieChart.setLayoutParams(

                 new LinearLayout.LayoutParams(

                         LinearLayout.LayoutParams.MATCH_PARENT,

                         LinearLayout.LayoutParams.MATCH_PARENT

                 )

         );



        try {
            // configure pie chart
            pieChart.setUsePercentValues(true);
            pieChart.getDescription().setEnabled(false);
            pieChart.setExtraOffsets(5, 10, 5, 5);

            // enable hole and configure
            pieChart.setDrawHoleEnabled(true);
            pieChart.setHoleRadius(7);
            pieChart.setTransparentCircleRadius(10);
            pieChart.setHoleRadius(25f);
            pieChart.setTransparentCircleAlpha(10);

            // enable rotation of the chart by touch
            pieChart.setRotationAngle(0);
            pieChart.setRotationEnabled(true);

        //    pieChart.setCenterText("MOA MOA");
            pieChart.setCenterTextSize(10);
            pieChart.setDrawEntryLabels(true);

            pieChart.setEntryLabelColor(Color.WHITE);
            pieChart.setEntryLabelTextSize(12f);

            // add data
            addData();

            // customize legends
            Legend l = pieChart.getLegend();
            l.setVerticalAlignment(Legend.LegendVerticalAlignment.TOP);
            l.setHorizontalAlignment(Legend.LegendHorizontalAlignment.RIGHT);
            l.setOrientation(Legend.LegendOrientation.VERTICAL);
            l.setDrawInside(false);
            l.setXEntrySpace(7f);
            l.setYEntrySpace(0f);
            l.setYOffset(0f);
            l.setPosition(Legend.LegendPosition.RIGHT_OF_CHART);

        }catch (Exception e){
            e.printStackTrace();
        }



        example1.setOnClickListener(new View.OnClickListener() { //예시 출원서 다운로드
            @Override
            public void onClick(View v) {
                FirebaseStorage firebaseStorage = FirebaseStorage.getInstance();
                StorageReference storageRef = firebaseStorage.getReference();
                StorageReference islandRef = storageRef.child("download/1.hwp");

                if (islandRef != null) {
                    progressDialog.setTitle("Downloading...");
                    progressDialog.setMessage(null);
                    progressDialog.show();

                    try {
                        final File rootPath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS); // Download 폴더에 저장
                        if(!rootPath.exists()) {
                            rootPath.mkdirs();
                        }
                        final File localFile = new File(rootPath, "출원서_예시.hwp"); //파일이 저장되는 이름

                        islandRef.getFile(localFile).addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
                            @RequiresApi(api = Build.VERSION_CODES.O)
                            @Override
                            public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {
                                // Local temp file has been created
                                // use localFile
                                Toast.makeText(MainActivity.this, "다운로드 완료", Toast.LENGTH_SHORT).show();
                                progressDialog.dismiss();

                                String NOTIFICATION_CHANNEL_ID = "channel_id";
                                String NOTIFICATION_CHANNEL_NAME = "Channel Name";
                                NotificationManager manager = (NotificationManager)
                                        getSystemService(Context.NOTIFICATION_SERVICE);
                                if(manager != null && manager.getNotificationChannel(NOTIFICATION_CHANNEL_ID) == null) {
                                    NotificationChannel channel = new NotificationChannel(
                                            NOTIFICATION_CHANNEL_ID,
                                            NOTIFICATION_CHANNEL_NAME,
                                            NotificationManager.IMPORTANCE_DEFAULT);
                                    //channel.setLockScreenVisibility(Notification.VISIBILITY_PUBLIC);
                                    channel.setDescription("Notification Channel Description :)");
                                    manager.createNotificationChannel(channel);
                                }
                                    Notification.Builder builder =
                                            new Notification.Builder(getApplicationContext(), NOTIFICATION_CHANNEL_ID);


//
//                                    Notification notification = new NotificationCompat.Builder(getApplicationContext())
//                                        .setContentTitle("다운로드 완료")
//                                        .setContentText("출원서 예시.hwp 다운")
//                                        .setSmallIcon(R.mipmap.ic_launcher)
//                                        .setContentIntent(pendingIntent)
//                                        .build();
//
//                                notificationManager.notify(7777, notification); //알림 실행


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

        statment1.setOnClickListener(new View.OnClickListener() { //출원서 양식 다운로드
            @Override
            public void onClick(View v) {

                FirebaseStorage firebaseStorage = FirebaseStorage.getInstance();
                StorageReference storageRef = firebaseStorage.getReference();
                StorageReference islandRef = storageRef.child("download/1.hwp");

                if (islandRef != null) {
                    progressDialog.setTitle("Downloading...");
                    progressDialog.setMessage(null);
                    progressDialog.show();

                    try {
                        final File rootPath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS); // Download 폴더에 저장
                        if(!rootPath.exists()) {
                            rootPath.mkdirs();
                        }
                        final File localFile = new File(rootPath, "출원서_양식.hwp"); //파일이 저장되는 이름

                        islandRef.getFile(localFile).addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
                            @Override
                            public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {
                                // Local temp file has been created
                                // use localFile
                                Toast.makeText(MainActivity.this, "다운로드 완료", Toast.LENGTH_SHORT).show();
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

        example2.setOnClickListener(new View.OnClickListener() { //예시 명세서 다운로드
            @Override
            public void onClick(View v) {

                FirebaseStorage firebaseStorage = FirebaseStorage.getInstance();
                StorageReference storageRef = firebaseStorage.getReference();
                StorageReference islandRef = storageRef.child("download/1.hwp");

                if (islandRef != null) {
                    progressDialog.setTitle("Downloading...");
                    progressDialog.setMessage(null);
                    progressDialog.show();

                    try {
                        final File rootPath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS); // Download 폴더에 저장
                        if(!rootPath.exists()) {
                            rootPath.mkdirs();
                        }
                        final File localFile = new File(rootPath, "명세서_예시.hwp"); //파일이 저장되는 이름

                        islandRef.getFile(localFile).addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
                            @Override
                            public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {
                                // Local temp file has been created
                                // use localFile
                                Toast.makeText(MainActivity.this, "다운로드 완료", Toast.LENGTH_SHORT).show();
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

        statment2.setOnClickListener(new View.OnClickListener() { // 명세서 양식 다운로드
            @Override
            public void onClick(View v) {

                FirebaseStorage firebaseStorage = FirebaseStorage.getInstance();
                StorageReference storageRef = firebaseStorage.getReference();
                StorageReference islandRef = storageRef.child("download/1.hwp");

                if (islandRef != null) {
                    progressDialog.setTitle("Downloading...");
                    progressDialog.setMessage(null);
                    progressDialog.show();

                    try {
                        final File rootPath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS); // Download 폴더에 저장
                        if(!rootPath.exists()) {
                            rootPath.mkdirs();
                        }
                        final File localFile = new File(rootPath, "명세서_양식.hwp"); //파일이 저장되는 이름

                        islandRef.getFile(localFile).addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
                            @Override
                            public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {
                                // Local temp file has been created
                                // use localFile
                                Toast.makeText(MainActivity.this, "다운로드 완료", Toast.LENGTH_SHORT).show();
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

    }//onCreate


    private void addData(){
        ArrayList<PieEntry> yVals1= new ArrayList<>();

        for(int i=0;i<yData.length;i++)
            yVals1.add(new PieEntry(yData[i],xData[i]));


        PieDataSet dataSet = new PieDataSet(yVals1,"");
        dataSet.setSliceSpace(3f);
        dataSet.setSelectionShift(5f);

        ArrayList<Integer> colors = new ArrayList<Integer>();

        for(int c : ColorTemplate.VORDIPLOM_COLORS)
            colors.add(c);
        for(int c : ColorTemplate.JOYFUL_COLORS)
            colors.add(c);
        for(int c : ColorTemplate.COLORFUL_COLORS)
            colors.add(c);
        for(int c : ColorTemplate.LIBERTY_COLORS)
            colors.add(c);
        for (int c : ColorTemplate.PASTEL_COLORS)
            colors.add(c);

        colors.add(ColorTemplate.getHoloBlue());
        dataSet.setColors(colors);

        PieData data = new PieData(dataSet);
        data.setValueFormatter(new PercentFormatter());
        data.setValueTextColor(Color.GRAY);
        data.setValueTextSize(11f);

        pieChart.setData(data);
        pieChart.highlightValue(null);
        pieChart.invalidate();
    }


//    //웹 크롤링
    private class JsoupAsyncTask extends AsyncTask<Void, Void, Void> {

        HttpEntity resEntity;
        byte[] data;
        InputStream is = null;

    @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected Void doInBackground(Void... params) {

            try {
                String url = "http://kpat.kipris.or.kr/kpat/resulta.do";
                DefaultHttpClient httpclient = new DefaultHttpClient();
                HttpPost httpPost = new HttpPost(url);

                // set the parameters
                List <NameValuePair> nvps = new ArrayList<NameValuePair>();

                nvps.add(new BasicNameValuePair("beforeExpression", ""));
                nvps.add(new BasicNameValuePair("BOOKMARK", ""));
                nvps.add(new BasicNameValuePair("collectionValues",""));
                nvps.add(new BasicNameValuePair("config", "G1111111111111111111111S111111111000000000"));
                nvps.add(new BasicNameValuePair("configChange", "Y"));
                nvps.add(new BasicNameValuePair("currentPage", "1"));
                nvps.add(new BasicNameValuePair("expression","IPC=[A]"));
                nvps.add(new BasicNameValuePair("history", "true"));
                nvps.add(new BasicNameValuePair("FROM", ""));
                nvps.add(new BasicNameValuePair("historyQuery", "IPC=[A]"));
                nvps.add(new BasicNameValuePair("lang", ""));
                nvps.add(new BasicNameValuePair("leftGubnChk", ""));
                nvps.add(new BasicNameValuePair("leftHangjungChk", ""));
                nvps.add(new BasicNameValuePair("logFlag", "Y"));
                nvps.add(new BasicNameValuePair("measureString", ""));
                nvps.add(new BasicNameValuePair("merchandiseString", ""));
                nvps.add(new BasicNameValuePair("natlCD", ""));
                nvps.add(new BasicNameValuePair("next", "MainList"));
                nvps.add(new BasicNameValuePair("numPageLinks", "10"));
                nvps.add(new BasicNameValuePair("numPerPage", "30"));
                nvps.add(new BasicNameValuePair("NWBOOKMARK", ""));
                nvps.add(new BasicNameValuePair("passwd", ""));
                nvps.add(new BasicNameValuePair("patternString", ""));
                nvps.add(new BasicNameValuePair("piField", ""));
                nvps.add(new BasicNameValuePair("piSearchYN", ""));
                nvps.add(new BasicNameValuePair("piValue", ""));
                nvps.add(new BasicNameValuePair("queryText", "IPC=[A]"));
                nvps.add(new BasicNameValuePair("REBOOKMARK", ""));
                nvps.add(new BasicNameValuePair("remoconDocsFound", ""));
                nvps.add(new BasicNameValuePair("remoconExpression", ""));
                nvps.add(new BasicNameValuePair("remoconSelectedArticles", ""));
                nvps.add(new BasicNameValuePair("searchInResult", ""));
                nvps.add(new BasicNameValuePair("searchInResultCk", "undefined"));
                nvps.add(new BasicNameValuePair("searchInTrans", "N"));
                nvps.add(new BasicNameValuePair("searchInTransCk", "undefined"));
                nvps.add(new BasicNameValuePair("searchKeyword", ""));
                nvps.add(new BasicNameValuePair("searchSaveCnt", "0"));
                nvps.add(new BasicNameValuePair("SEL_PAT", "KPAT"));
                nvps.add(new BasicNameValuePair("selectedLang", ""));
                nvps.add(new BasicNameValuePair("sortField", "RANK"));
                nvps.add(new BasicNameValuePair("sortField1", ""));
                nvps.add(new BasicNameValuePair("sortField2", ""));
                nvps.add(new BasicNameValuePair("sortState", "DESC"));
                nvps.add(new BasicNameValuePair("sortState1", ""));
                nvps.add(new BasicNameValuePair("sortState2", ""));
                nvps.add(new BasicNameValuePair("strstat", "TOP|KW"));
                nvps.add(new BasicNameValuePair("userId", ""));
                nvps.add(new BasicNameValuePair("userInput", ""));

                // set the encoding
                UrlEncodedFormEntity ent = new UrlEncodedFormEntity( nvps , HTTP.UTF_8);
                httpPost.setEntity(ent);

                // send the http request and get the http response
                HttpResponse response = httpclient.execute(httpPost);
                resEntity = response.getEntity();

                if( response.getStatusLine().getStatusCode() == 200 ) {  // HttpStatus.SC_OK
                    // 데이터 읽기
                    //데이터(BODY) 읽기
                    //* 스트링 읽어오기

                    is =  resEntity.getContent();
                    int len = (int) resEntity.getContentLength();

                    if( len > 0 ) {
                        data = new byte[len];
                        int offset = 0;
                        int bytesRead =  0;

                        do {
                            bytesRead = is.read( data, offset, len );
                            offset += bytesRead;
                            len -= bytesRead;
                        } while( bytesRead > 0 );
                        is.close();
                        is = new ByteArrayInputStream( data );
                        //is.read(data);
                    }

                    is.close();
                } else {
                    // 서버오류
                }

            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            String string= new String(data);
          //  textview_htmlDocument.setText(string);  //텍스트 설정 htmlContentInStringFormat
}
   }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) { //툴바 아이콘

        int id = item.getItemId();
        if(id == R.id.menu) {
            Intent intent = new Intent(getApplicationContext(), Statement_cart.class);
            startActivity(intent);
        }else if(id == android.R.id.home){
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {
        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            switch (item.getItemId()) {
                case R.id.navigation_home:
                    Intent i1 = new Intent(getApplicationContext(), MainActivity.class);
                    i1.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                    startActivity(i1);
                    return true;
                case R.id.navigation_cal:
                    Intent i2 = new Intent(getApplicationContext(),Calendar.class);
                    i2.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                    startActivity(i2);
                    return true;
                case R.id.navigation_search:
                    Intent i3 = new Intent(getApplicationContext(), Search_history.class);
                    i3.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                    startActivity(i3);
                    return true;
                case R.id.navigation_talk:
                    Intent i4 = new Intent(getApplicationContext(), Talk_list.class);
                    i4.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                    startActivity(i4);
                    return true;
                case R.id.navigation_mypage:
                    Intent i5 = new Intent(getApplicationContext(), Mypage.class);
                    i5.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                    startActivity(i5);
                    return true;
            }
            return false;
        }
    };

    //bottom네비게이션 메뉴 간격 일정하게
    public class BottomNavigationViewHelper {
        @SuppressLint("RestrictedApi")
        public void removeShiftMode(BottomNavigationView view) {
            BottomNavigationMenuView menuView = (BottomNavigationMenuView) view.getChildAt(0);
            try {
                Field shiftingMode = menuView.getClass().getDeclaredField("mShiftingMode");
                shiftingMode.setAccessible(true);
                shiftingMode.setBoolean(menuView, false);
                shiftingMode.setAccessible(false);
                for (int i = 0; i < menuView.getChildCount(); i++) {
                    BottomNavigationItemView item = (BottomNavigationItemView) menuView.getChildAt(i);
                    item.setShiftingMode(false);
                    item.setChecked(item.getItemData().isChecked());
                }
            } catch (NoSuchFieldException e) {
                Log.e("BottomNav", "Unable to get shift mode field", e);
            } catch (IllegalAccessException e) {
                Log.e("BottomNav", "Unable to change value of shift mode", e);
            }
        }
    }


    class PeopleRecyclerViewAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder>{

        List<UserModel> userModels;
        private String key;
        private String uid;

        public PeopleRecyclerViewAdapter(){
            userModels = new ArrayList<>();
            uid = FirebaseAuth.getInstance().getCurrentUser().getUid();//나(단말기에 로그인한 사용자의 uid(식별자))
            key= FirebaseDatabase.getInstance().getReference().child(uid).getKey();

            FirebaseDatabase.getInstance().getReference().child("users").addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    userModels.clear(); //친구가 추가 될 경우 중복 방지
                    for(DataSnapshot snapshot : dataSnapshot.getChildren()){
                        if(!snapshot.getKey().equals(key)) { //내가 아닌 사람만 추출
                            if(snapshot.getValue(UserModel.class).distinguish==true) { //법률 사무소만 추출 (distinguish값이 true인것 )
                                userModels.add(snapshot.getValue(UserModel.class));
                }
            }
        }
                    notifyDataSetChanged();
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
        }

        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {  //레이아웃 설정
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_office, parent,false);
            return new CustomViewHolder(view);
        }

        @Override
        public void onBindViewHolder(final RecyclerView.ViewHolder holder, final int position) {  //레이아웃 안에 위젯 설정

            RequestOptions requestOptions = new RequestOptions();
            requestOptions.placeholder(R.drawable.ic_account_circle_black_24dp);
            requestOptions.fitCenter();
            requestOptions.apply(new RequestOptions().circleCrop());
            requestOptions.centerCrop();

            Glide.with(MainActivity.this)
                    .setDefaultRequestOptions(requestOptions)
                    .load(userModels.get(position).profileImageUrl)
                    .into(((CustomViewHolder)holder).imageView_profile);

//            Glide.with(MainActivity.this)
//                    .load(userModels.get(position).profileImageUrl) // Image URL
//                    .override(50,50) // Resize image
//                    .placeholder(R.drawable.ic_account_circle_black_24dp) // Place holder image
//                    .apply(new RequestOptions().circleCrop())
//                    .error(R.drawable.ic_chevron_right_black_24dp) // On error image
//                    .into(((CustomViewHolder)holder).imageView_profile); // ImageView to display image

                //load로 누구의 이미지를 불러올 것인지 결정,
                //apply로 이미지를 어떤 모양으로 만들 것인지 결정,
                //into로 어디에 이미지를 커스텀 해줄 것인지
                //holder.itemView.getContext()

            ((CustomViewHolder)holder).textView_name.setText(userModels.get(position).userName); //item에 이름 텍스트 설정
            ((CustomViewHolder)holder).textView_email.setText(userModels.get(position).userEmail); //item에 이메일 텍스트 설정

            //아이템을 클릭하면 채팅방으로 이동
            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(getApplicationContext(), Talk_ing.class);
                    ActivityOptions activityOptions = null;
                    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.JELLY_BEAN) {
                        activityOptions = ActivityOptions.makeCustomAnimation(v.getContext(), R.anim.fromright, R.anim.toleft);
                        intent.putExtra("destinationUid", userModels.get(position).uid); //대화창(Talk_ing)로 대화상대 전송
                        startActivity(intent, activityOptions.toBundle());
                    }
                }
            });
        }

        @Override
        public int getItemCount() {
            return userModels.size();
        } //아이템이 몇 개인지

        private class CustomViewHolder extends RecyclerView.ViewHolder {
            public  ImageView imageView_profile;
            public  TextView textView_name;
            public  TextView textView_email;

            public CustomViewHolder(View view) {
                super(view);

                imageView_profile = (ImageView) view.findViewById(R.id.officeitem_imageview);
                textView_name = (TextView) view.findViewById(R.id.officeitem_textview_name);
                textView_email = (TextView) view.findViewById(R.id.officeitem_textview_email);
            }
        }
    }


    void passPushTokenToServer(){ //푸시 메시지
        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        String token = FirebaseInstanceId.getInstance().getToken();// token생성

        Map<String, Object> map = new HashMap<>(); //token은 해쉬맵으로 밖에 넣어줄 수 없음
        map.put("pushToken", token);

        FirebaseDatabase.getInstance().getReference().child("users").child(uid).updateChildren(map);
        //데이터 업데이트, setValue는 기존의 데이터가 사라짐
    }



}
