package eg.gov.iti.jets.trip_pal;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.Window;
import android.widget.RemoteViews;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import java.util.Locale;

import eg.gov.iti.jets.trip_pal.Broadcast.AlarmBroadcast;

public class AlertDialogActivity extends AppCompatActivity {
    AlertDialog alertDialog;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_alert_dialog);


        Intent intent  = getIntent();
        Uri notificationSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM);
        Ringtone ringtone = RingtoneManager.getRingtone(getApplicationContext(), notificationSound);

        startAlarmRingTone(ringtone);
        AlertDialog.Builder Builder = new AlertDialog.Builder(this)
                .setMessage("It's time to start "+ intent.getStringExtra("name"))
                .setTitle("Trip reminder")
                .setIcon(android.R.drawable.ic_lock_idle_alarm)
                .setNegativeButton("Later", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        stopAlarmRingTone(ringtone);
                        startDialogService();
                        alertDialog.dismiss();
                        finish();
                    }
                })
                .setPositiveButton("Start ", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        Intent intent  = getIntent();
                        Uri gmmIntentUri = Uri.parse("geo:0,0?q="+ intent.getStringExtra("location"));
                        Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
                        mapIntent.setPackage("com.google.android.apps.maps");

                        PendingIntent pendingIntent = PendingIntent.getActivity(getApplicationContext(), 0, mapIntent, PendingIntent.FLAG_ONE_SHOT);
                        stopAlarmRingTone(ringtone);
                        startActivity(mapIntent);
                        finish();
                    }
                })
                .setNeutralButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        stopAlarmRingTone(ringtone);
                        alertDialog.dismiss();
                        finish();
                    }
                });

        alertDialog = Builder.create();
        alertDialog.show();

    }

    public void startDialogService() {

        Intent intent  = getIntent();
        //String uriString = "geo:0,0?q="+ intent.getStringExtra("from");
        //String uriString = String.format(Locale.ENGLISH, "geo:%f,%f", lat, long);
        Uri gmmIntentUri = Uri.parse("geo:0,0?q="+ intent.getStringExtra("from"));
        Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
        mapIntent.setPackage("com.google.android.apps.maps");


        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, mapIntent, PendingIntent.FLAG_ONE_SHOT);

        NotificationManagerCompat notificationManagerCompat = NotificationManagerCompat.from(this);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this,"alarmNotification")
                .setSmallIcon(R.drawable.ic_launcher_background)
                .setContentTitle("Alarm!!")
                .setContentText("It's time to start "+ intent.getStringExtra("name"))
                .setAutoCancel(true)
                .addAction(R.drawable.ic_launcher_background, "let's start",
                        pendingIntent)
                .addAction(R.drawable.ic_launcher_background, "cancel", null)
                .setPriority(NotificationCompat.PRIORITY_HIGH);

        builder.setContentIntent(pendingIntent);


        notificationManagerCompat.notify(10,builder.build());
    }

    public void startAlarmRingTone(Ringtone r) {
        r.play();
    }

    public void stopAlarmRingTone(Ringtone r) {
        r.stop();
    }
}