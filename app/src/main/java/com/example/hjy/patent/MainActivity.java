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

    //linear1 (?????? ?????????)
    private PieChart pieChart;
    private  float[] yData = {5, 10, 15, 30, 40};
    private  String[] xData = {"????????????", "??????", "????????????", "????????????", "??????"};
  //  private String URL = "http://kpat.kipris.or.kr/kpat/searchLogina.do?next=MainSearch#page3"; //????????? ??????????????? URL??????
    private String htmlContentInStringFormat = ""; //????????? ???????????? ?????? ??????
    private TextView textview_htmlDocument; //????????? ???????????? ????????? ???

   // LinearLayout linearLayout;
    //linear2 (?????? ??????)
    private Button example1, statment1;
    private Button example2, statment2;
    ProgressDialog progressDialog;

    //linear3 (office ??????)
    RecyclerView recyclerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        progressDialog = new ProgressDialog(this);
        BottomNavigationView navigation = (BottomNavigationView) findViewById(R.id.navigation);
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);
        //static?????? ????????????????????? ??????????????? ????????? ????????? ?????????????????? static??? ????????? ??? ????????????
        //????????? ????????? ???????????? ???????????? ??????????????? ???
        BottomNavigationViewHelper n = new BottomNavigationViewHelper();
        n.removeShiftMode(navigation);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        actionBar.setHomeButtonEnabled(true);
        actionBar.setDisplayHomeAsUpEnabled(true);  //????????????
        actionBar.setDisplayShowTitleEnabled(false); //?????? ????????? ?????????

        TabHost tabHost = (TabHost) findViewById(R.id.tabHost);
        tabHost.setup();

        TabHost.TabSpec tab1 = tabHost.newTabSpec("TAB1").setIndicator("???");
        tab1.setContent(R.id.linear1);
        tabHost.addTab(tab1);

        TabHost.TabSpec tab2 = tabHost.newTabSpec("TAB2").setIndicator("????????????");
        tab2.setContent(R.id.linear2);
        tabHost.addTab(tab2);

        TabHost.TabSpec tab3 = tabHost.newTabSpec("TAB3").setIndicator("????????????");
        tab3.setContent(R.id.linear3); //PeopleFragment
        tabHost.addTab(tab3);

        TabHost.TabSpec tab4 = tabHost.newTabSpec("TAB4").setIndicator("????????????");
        tab4.setContent(R.id.linear4);
        tabHost.addTab(tab4);

        //???????????? ????????? ??????
        for (int i = 0; i < tabHost.getTabWidget().getChildCount(); i++) {
            LinearLayout relLayout = (LinearLayout) tabHost.getTabWidget().getChildAt(i);
            TextView tv = (TextView) relLayout.getChildAt(1);
            tv.setTextColor(Color.WHITE);
        }
        //tabHost.setCurrentTab(0);
        passPushTokenToServer(); //?????? ???????????? ?????? ?????? ??????

       // textview_htmlDocument = (TextView) findViewById(R.id.mainactivity_textview_data);

       // linearLayout = (LinearLayout) findViewById(R.id.linear1);
        recyclerView = (RecyclerView) findViewById(R.id.main_3_recycle);
        recyclerView.setLayoutManager(new LinearLayoutManager(getLayoutInflater().getContext()));
        recyclerView.setAdapter(new PeopleRecyclerViewAdapter());

        example1 = (Button) findViewById(R.id.mainactivity_example1);
        example2 = (Button) findViewById(R.id.mainactivity_example2);
        statment1 = (Button) findViewById(R.id.mainactivity_statment1);
        statment2 = (Button) findViewById(R.id.mainactivity_statment2);

//        textview_htmlDocument.setMovementMethod(new ScrollingMovementMethod()); //????????? ????????? ??????????????? ?????????


        //??? ?????????
        JsoupAsyncTask jsoupAsyncTask = new JsoupAsyncTask();
        jsoupAsyncTask.execute();  //main???????????? ???????????? ????????? ?????? ?????????  Caused by: android.os.NetworkOnMainThreadException

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



        example1.setOnClickListener(new View.OnClickListener() { //?????? ????????? ????????????
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
                        final File rootPath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS); // Download ????????? ??????
                        if(!rootPath.exists()) {
                            rootPath.mkdirs();
                        }
                        final File localFile = new File(rootPath, "?????????_??????.hwp"); //????????? ???????????? ??????

                        islandRef.getFile(localFile).addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
                            @RequiresApi(api = Build.VERSION_CODES.O)
                            @Override
                            public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {
                                // Local temp file has been created
                                // use localFile
                                Toast.makeText(MainActivity.this, "???????????? ??????", Toast.LENGTH_SHORT).show();
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
//                                        .setContentTitle("???????????? ??????")
//                                        .setContentText("????????? ??????.hwp ??????")
//                                        .setSmallIcon(R.mipmap.ic_launcher)
//                                        .setContentIntent(pendingIntent)
//                                        .build();
//
//                                notificationManager.notify(7777, notification); //?????? ??????


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

        statment1.setOnClickListener(new View.OnClickListener() { //????????? ?????? ????????????
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
                        final File rootPath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS); // Download ????????? ??????
                        if(!rootPath.exists()) {
                            rootPath.mkdirs();
                        }
                        final File localFile = new File(rootPath, "?????????_??????.hwp"); //????????? ???????????? ??????

                        islandRef.getFile(localFile).addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
                            @Override
                            public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {
                                // Local temp file has been created
                                // use localFile
                                Toast.makeText(MainActivity.this, "???????????? ??????", Toast.LENGTH_SHORT).show();
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

        example2.setOnClickListener(new View.OnClickListener() { //?????? ????????? ????????????
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
                        final File rootPath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS); // Download ????????? ??????
                        if(!rootPath.exists()) {
                            rootPath.mkdirs();
                        }
                        final File localFile = new File(rootPath, "?????????_??????.hwp"); //????????? ???????????? ??????

                        islandRef.getFile(localFile).addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
                            @Override
                            public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {
                                // Local temp file has been created
                                // use localFile
                                Toast.makeText(MainActivity.this, "???????????? ??????", Toast.LENGTH_SHORT).show();
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

        statment2.setOnClickListener(new View.OnClickListener() { // ????????? ?????? ????????????
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
                        final File rootPath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS); // Download ????????? ??????
                        if(!rootPath.exists()) {
                            rootPath.mkdirs();
                        }
                        final File localFile = new File(rootPath, "?????????_??????.hwp"); //????????? ???????????? ??????

                        islandRef.getFile(localFile).addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
                            @Override
                            public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {
                                // Local temp file has been created
                                // use localFile
                                Toast.makeText(MainActivity.this, "???????????? ??????", Toast.LENGTH_SHORT).show();
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


//    //??? ?????????
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
                    // ????????? ??????
                    //?????????(BODY) ??????
                    //* ????????? ????????????

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
                    // ????????????
                }

            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            String string= new String(data);
          //  textview_htmlDocument.setText(string);  //????????? ?????? htmlContentInStringFormat
}
   }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) { //?????? ?????????

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

    //bottom??????????????? ?????? ?????? ????????????
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
            uid = FirebaseAuth.getInstance().getCurrentUser().getUid();//???(???????????? ???????????? ???????????? uid(?????????))
            key= FirebaseDatabase.getInstance().getReference().child(uid).getKey();

            FirebaseDatabase.getInstance().getReference().child("users").addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    userModels.clear(); //????????? ?????? ??? ?????? ?????? ??????
                    for(DataSnapshot snapshot : dataSnapshot.getChildren()){
                        if(!snapshot.getKey().equals(key)) { //?????? ?????? ????????? ??????
                            if(snapshot.getValue(UserModel.class).distinguish==true) { //?????? ???????????? ?????? (distinguish?????? true?????? )
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
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {  //???????????? ??????
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_office, parent,false);
            return new CustomViewHolder(view);
        }

        @Override
        public void onBindViewHolder(final RecyclerView.ViewHolder holder, final int position) {  //???????????? ?????? ?????? ??????

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

                //load??? ????????? ???????????? ????????? ????????? ??????,
                //apply??? ???????????? ?????? ???????????? ?????? ????????? ??????,
                //into??? ????????? ???????????? ????????? ?????? ?????????
                //holder.itemView.getContext()

            ((CustomViewHolder)holder).textView_name.setText(userModels.get(position).userName); //item??? ?????? ????????? ??????
            ((CustomViewHolder)holder).textView_email.setText(userModels.get(position).userEmail); //item??? ????????? ????????? ??????

            //???????????? ???????????? ??????????????? ??????
            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(getApplicationContext(), Talk_ing.class);
                    ActivityOptions activityOptions = null;
                    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.JELLY_BEAN) {
                        activityOptions = ActivityOptions.makeCustomAnimation(v.getContext(), R.anim.fromright, R.anim.toleft);
                        intent.putExtra("destinationUid", userModels.get(position).uid); //?????????(Talk_ing)??? ???????????? ??????
                        startActivity(intent, activityOptions.toBundle());
                    }
                }
            });
        }

        @Override
        public int getItemCount() {
            return userModels.size();
        } //???????????? ??? ?????????

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


    void passPushTokenToServer(){ //?????? ?????????
        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        String token = FirebaseInstanceId.getInstance().getToken();// token??????

        Map<String, Object> map = new HashMap<>(); //token??? ??????????????? ?????? ????????? ??? ??????
        map.put("pushToken", token);

        FirebaseDatabase.getInstance().getReference().child("users").child(uid).updateChildren(map);
        //????????? ????????????, setValue??? ????????? ???????????? ?????????
    }



}
