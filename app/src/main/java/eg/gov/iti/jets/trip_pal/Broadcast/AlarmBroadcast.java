package eg.gov.iti.jets.trip_pal.Broadcast;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import eg.gov.iti.jets.trip_pal.AlertDialogActivity;

public class AlarmBroadcast extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        Intent i = new Intent(context, AlertDialogActivity.class);
        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        i.putExtra("name", intent.getStringExtra("name"));
        i.putExtra("location", intent.getStringExtra("location"));
        i.putExtra("from", intent.getStringExtra("from"));//***
        context.startActivity(i);
    }
}
