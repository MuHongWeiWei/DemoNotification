package chat.app.hyinfo.com.demonotification;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.Vibrator;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.List;

import me.leolin.shortcutbadger.ShortcutBadger;

public class MainActivity extends AppCompatActivity {

    public static String TAG = MainActivity.class.getSimpleName() + "1211";
    private TextView dataText;
    private NotificationManager notificationManager;
    long count;
    final String CHANNEL_ID = "channle_ID";
    final String CHANNLE_TITLE = "channle_title";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ininUI();
        changeData();
        badgeCount();

    }

    
    private void badgeCount() {
        //取得訊息數量
        FirebaseDatabase.getInstance().getReference("data").child("count").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.getValue() != null) {
                    count = (long) dataSnapshot.getValue();
                    ShortcutBadger.applyCount(getApplicationContext(), (int) count);
                }
                if (count == 0) {
                    notificationManager.cancel(5);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }


    private void ininUI() {
        dataText = findViewById(R.id.data);
        dataText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "onClick: ");
                if (count > 0) {
                    count--;
                    FirebaseDatabase.getInstance().getReference("data").child("count").setValue(count);
                }
            }
        });
    }
    
    @TargetApi(Build.VERSION_CODES.O)
    private void changeData() {

        notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

        FirebaseDatabase.getInstance()
                .getReference("data").child("content")
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        //取得變換的資料
                        String dataValue = (String) dataSnapshot.getValue();
                        dataText.setText(dataValue);

                        //增加未讀數量
                        count++;
                        FirebaseDatabase.getInstance()
                                .getReference("data")
                                .child("count").setValue(count);

                        //版本大於Ｏ
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                            //創建Channel
                            @SuppressLint("WrongConstant") NotificationChannel channel = new NotificationChannel(CHANNEL_ID, CHANNLE_TITLE, NotificationManager.IMPORTANCE_MAX);
                            notificationManager.createNotificationChannel(channel);
                            //創建Notification
                            Notification notification = new Notification.Builder(MainActivity.this)
                                    .setContentTitle("測試標題")
                                    .setContentText(dataValue)
                                    .setSmallIcon(R.drawable.ring)
                                    .setLargeIcon(BitmapFactory.decodeResource(getResources(), R.drawable.ring))
                                    .setChannelId(CHANNEL_ID)
                                    .setOngoing(true)
                                    .setWhen(System.currentTimeMillis())
                                    .setShowWhen(true)
                                    .build();

                                notificationManager.notify(5, notification);
                        } else {
                            Notification notification = new Notification.Builder(MainActivity.this)
                                    .setContentTitle("測試標題")
                                    .setDefaults(Notification.DEFAULT_ALL)
                                    .setContentText(dataValue)
                                    .setSmallIcon(R.drawable.ring)
                                    .setNumber(4)
                                    .build();
                            notificationManager.notify(5, notification);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
    }

    @Override
    public void onBackPressed() {
        new AlertDialog.Builder(this)
                .setTitle("確定離開嗎")
                .setPositiveButton("確定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        finish();
                    }
                })
                .setNegativeButton("取消", null)
                .show();
    }
}
