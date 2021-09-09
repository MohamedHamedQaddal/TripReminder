package eg.gov.iti.jets.trip_pal.trip;

import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.provider.Settings;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TimePicker;
import android.widget.Toast;

import com.google.android.gms.common.api.Status;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.AutocompleteSessionToken;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.net.PlacesClient;
import com.google.android.libraries.places.widget.Autocomplete;
import com.google.android.libraries.places.widget.model.AutocompleteActivityMode;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

import eg.gov.iti.jets.trip_pal.Broadcast.AlarmBroadcast;
import eg.gov.iti.jets.trip_pal.MainActivity;
import eg.gov.iti.jets.trip_pal.R;
import eg.gov.iti.jets.trip_pal.database.AppDatabase;
import eg.gov.iti.jets.trip_pal.database.TripEntity;

import static android.app.Activity.RESULT_OK;


public class TripFillerFragment extends Fragment{

    Calendar mCalendar = Calendar.getInstance();
    Calendar exactCalendar = Calendar.getInstance();
    TripEntity tripEntity = new TripEntity();

     private PlacesClient placesClient;
     EditText tripNameEditText;
     EditText fromEditText;
     EditText toEditText;
     EditText tripDatePicker;
     EditText tripTimePicker;
     EditText notesAdder;
     RadioGroup tripTypeRadioGroup;
     RadioButton oneWayTrip;
     RadioButton roundTrip;
     Button saveTripDataButton;
     Button cancelSavingTripDataButton;


    FirebaseDatabase database;
    FirebaseAuth auth;
    DatabaseReference myRef;

    AlertDialog alert;
    boolean flag = false;


    public TripFillerFragment() {}

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        createNotificationChannel();

        if (database == null){
            database = FirebaseDatabase.getInstance();
            myRef = FirebaseDatabase.getInstance().getReference("Users");
            myRef = myRef.child(FirebaseAuth.getInstance().getCurrentUser().getUid());
        }

    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_trip_filler,container,false);
        tripNameEditText = view.findViewById(R.id.trip_name_edit_text_id);
        fromEditText = view.findViewById(R.id.from_edit_text_id);
        toEditText = view.findViewById(R.id.to_edit_text_id);
        notesAdder = view.findViewById(R.id.add_notes_edit_text_id);
        tripDatePicker = view.findViewById(R.id.trip_date_date_picker_id);
        tripTimePicker = view.findViewById(R.id.trip_time_time_picker_id);
        tripTypeRadioGroup = view.findViewById(R.id.trip_type_radio_group_button_id);
        oneWayTrip = view.findViewById(R.id.one_way_trip_radio_button_id);
        roundTrip = view.findViewById(R.id.round_trip_radio_button_id);
        saveTripDataButton = view.findViewById(R.id.save_trip_data_button_id);
        cancelSavingTripDataButton = view.findViewById(R.id.cancel_saving_trip_data_button_id);

        int keyID = getStoredTripID();
        Log.i("KEYID", "onCreateView: " + keyID);
        if(keyID != -1){ setBlankFields(keyID);}


        if (!checkPermission()) {
            reqPermission();
        }
        flag = true;

        auth = FirebaseAuth.getInstance();


        DatePickerDialog.OnDateSetListener date = new DatePickerDialog.OnDateSetListener() {

            @Override
            public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                // TODO Auto-generated method stub
                exactCalendar.set(Calendar.YEAR, year);
                exactCalendar.set(Calendar.MONTH, monthOfYear);
                exactCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                if(exactCalendar.after(mCalendar) || exactCalendar.equals(mCalendar)) {
                    updateLabel();
                }
                else
                    Toast.makeText(getContext(), "Please enter valid date", Toast.LENGTH_SHORT).show();
            }
        };

        tripDatePicker.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                DatePickerDialog datePickerDialog = new DatePickerDialog(getContext(), date, mCalendar
                        .get(Calendar.YEAR), mCalendar.get(Calendar.MONTH),
                        mCalendar.get(Calendar.DAY_OF_MONTH));
                datePickerDialog.getDatePicker().setMinDate(System.currentTimeMillis() - 1000);
                datePickerDialog.show();
            }
        });

        tripTimePicker.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                Calendar mCurrentTime = Calendar.getInstance();
                int hour = mCurrentTime.get(Calendar.HOUR_OF_DAY);
                int minute = mCurrentTime.get(Calendar.MINUTE);
                TimePickerDialog mTimePicker;
                mTimePicker = new TimePickerDialog(getContext(), new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(TimePicker timePicker, int selectedHour, int selectedMinute) {
                        tripTimePicker.setText( selectedHour + ":" + selectedMinute);
                        exactCalendar.set(Calendar.HOUR_OF_DAY, selectedHour);
                        exactCalendar.set(Calendar.MINUTE, selectedMinute);
                    }
                }, hour, minute, true);//Yes 24 hour time
                mTimePicker.setTitle("Select Time");
                mTimePicker.show();
            }
        });

        saveTripDataButton.setOnClickListener(new View.OnClickListener() {
            @Override
        public void onClick(View v) {
            if(keyID == -1){
                saveTrip();
            }else{
                updateTrip(keyID);
            }
            Log.i("Saved", "onClick: ");
            }
        });

        cancelSavingTripDataButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getActivity().finish();
            }
        });

        return view;
    }






    public void saveTrip() {

        int isSelected = tripTypeRadioGroup.getCheckedRadioButtonId();

        if (tripNameEditText.getText().toString().isEmpty()) {
            tripNameEditText.setError("Required");
            tripNameEditText.requestFocus();
            return;
        }

        if (fromEditText.getText().toString().isEmpty()) {
            fromEditText.setError("Required");
            fromEditText.requestFocus();
            return;
        }

        if (toEditText.getText().toString().isEmpty()) {
            toEditText.setError("Required");
            toEditText.requestFocus();
            return;
        }

        if (tripDatePicker.getText().toString().isEmpty()) {
            tripDatePicker.setError("Required");
            tripDatePicker.requestFocus();
            return;
        }

        if (tripTimePicker.getText().toString().isEmpty()) {
            tripTimePicker.setError("Required");
            tripTimePicker.requestFocus();
            return;
        }

        if (isSelected == -1) {
            Toast.makeText(getContext(), "Set your trip type", Toast.LENGTH_SHORT).show();
            return;
        }

        if(tripEntity.getStartPointLat() == tripEntity.getEndPointLat() &&
                tripEntity.getStartPointLong() == tripEntity.getEndPointLong() &&
                tripEntity.getTripType().equals("One Way Trip")){
            fromEditText.setError("Invalid Input");
            fromEditText.requestFocus();
            toEditText.setError("Invalid Input");
            toEditText.requestFocus();
            Toast.makeText(getContext(), "Enter different start and end points", Toast.LENGTH_SHORT).show();
        }

        setAlarm();


        class SaveTrip extends AsyncTask<Void, Void, Void> {

            @Override
            protected Void doInBackground(Void... voids) {

                //creating a trip
                //TripEntity tripEntity = new TripEntity();
                tripEntity.setTripName(tripNameEditText.getText().toString());
                tripEntity.setTripDate(tripDatePicker.getText().toString());
                tripEntity.setTripTime(tripTimePicker.getText().toString());
                tripEntity.setTripType(getTripType());
                tripEntity.setTripStatus("Upcoming");
                tripEntity.setTripStart(fromEditText.getText().toString());
                tripEntity.setTripEnd(toEditText.getText().toString());
                ArrayList<String> notesList = new ArrayList<String>();
                notesList.add(notesAdder.getText().toString());
                tripEntity.setTripNotes(notesList);
                AppDatabase.getInstance(getContext()).tripDao().insert(tripEntity);
                return null;
            }
            @Override
            protected void onPostExecute(Void aVoid) {
                super.onPostExecute(aVoid);
                getActivity().finish();
                startActivity(new Intent(getContext(), MainActivity.class));
                Toast.makeText(getContext(), "Saved", Toast.LENGTH_LONG).show();
            }
        }
        SaveTrip saveTrip = new SaveTrip();
        saveTrip.execute();
    }



    public void deleteTrip(TripEntity tripEntity) {

        class DeleteTrip extends AsyncTask<Void, Void, Void> {

            @Override
            protected Void doInBackground(Void... voids) {
                AppDatabase.getInstance(getContext()).tripDao().delete(tripEntity);
                return null;
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                super.onPostExecute(aVoid);
                Toast.makeText(getContext(), "Deleted", Toast.LENGTH_SHORT).show();
                getActivity().finish();
                startActivity(new Intent(getContext(), MainActivity.class));
            }
        }

        DeleteTrip deleteTrip = new DeleteTrip();
        deleteTrip.execute();
    }



    public void updateTrip(int ID) {
        tripEntity = AppDatabase.getInstance(getContext()).tripDao().findTripByID(ID);
        Log.i("CHECK", "updateTrip: " + tripEntity.getTripName());
        //setting the blank fields to the data of the to_be_updated trip
        ArrayList<String> fetchedTripNotes = tripEntity.getTripNotes();


        class UpdateTrip extends AsyncTask<Void, Void, Void> {

            @Override
            protected Void doInBackground(Void... voids) {
                ArrayList<String> notesList = new ArrayList<>();
                tripEntity.setTripName(tripNameEditText.getText().toString());
                tripEntity.setTripDate(tripDatePicker.getText().toString());
                tripEntity.setTripTime(tripTimePicker.getText().toString());
                tripEntity.setTripStart(fromEditText.getText().toString());
                tripEntity.setTripEnd(toEditText.getText().toString());
                tripEntity.setTripType(getTripType());
                tripEntity.setTripStatus("Upcoming");
                notesList.add(notesAdder.getText().toString());
                tripEntity.setTripNotes(notesList);
                AppDatabase.getInstance(getContext()).tripDao().update(tripEntity);
                return null;
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                super.onPostExecute(aVoid);
                getActivity().finish();
            }
        }
        UpdateTrip updateTrip = new UpdateTrip();
        updateTrip.execute();
    }




    public void setBlankFields(int ID){
        TripEntity tripEntity = AppDatabase.getInstance(getContext()).tripDao().findTripByID(ID);
        tripNameEditText.setText(tripEntity.getTripName());
        tripDatePicker.setText(tripEntity.getTripDate());
        tripTimePicker.setText(tripEntity.getTripTime());
        fromEditText.setText(tripEntity.getTripStart());
        toEditText.setText(tripEntity.getTripEnd());
        if (tripEntity.getTripType() == "One Way"){
            oneWayTrip.setChecked(true);
        }else{
            roundTrip.setChecked(true);
        }
        notesAdder.setText(tripEntity.getTripNotes().get(0));
        //ends here
    }





    private String getNotes() {
        return notesAdder.getText().toString();
    }

    private void updateLabel() {
        String mFormat = "MM/dd/yyyy"; //In which you need put here
        SimpleDateFormat sdf = new SimpleDateFormat(mFormat, Locale.US);
        tripDatePicker.setText(sdf.format(exactCalendar.getTime()));
    }

    public String getTripType() {
        String tripType = null;
        if(oneWayTrip.isChecked()){
            tripType = "One Way Trip";
        }else if(roundTrip.isChecked()){
            tripType = "Round Trip";
        }
        return tripType;
    }


    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        fromEditText.setFocusable(false);
        toEditText.setFocusable(false);

        Places.initialize(getActivity().getApplicationContext(), "AIzaSyAJFoav8N_4Urccyq0HSynjzS835Y9HPCk");
        placesClient = Places.createClient(getActivity());
        final AutocompleteSessionToken token = AutocompleteSessionToken.newInstance();

        fromEditText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                List<Place.Field> fieldList = Arrays.asList(Place.Field.ADDRESS, Place.Field.LAT_LNG, Place.Field.NAME);
                Intent intent = new Autocomplete.IntentBuilder(AutocompleteActivityMode.OVERLAY, fieldList).build(getContext());
                startActivityForResult(intent, 100);
            }
        });

        toEditText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                List<Place.Field> fieldList = Arrays.asList(Place.Field.ADDRESS, Place.Field.LAT_LNG, Place.Field.NAME);
                Intent intent = new Autocomplete.IntentBuilder(AutocompleteActivityMode.OVERLAY, fieldList).build(getContext());
                startActivityForResult(intent, 200);
            }
        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(resultCode == RESULT_OK){
            Place place = Autocomplete.getPlaceFromIntent(data);
            switch (requestCode){
                case 100:   fromEditText.setText(place.getAddress());
                            tripEntity.setStartPointLat(place.getLatLng().latitude);
                            tripEntity.setStartPointLong(place.getLatLng().longitude);
                            break;

                case 200:   toEditText.setText(place.getAddress());
                            tripEntity.setEndPointLat(place.getLatLng().latitude);
                            tripEntity.setEndPointLong(place.getLatLng().longitude);
                            break;

                default: Log.i("Done", "onActivityResult: ");
            }
        }else{
            Status status = Autocomplete.getStatusFromIntent(data);
        }
    }


    public int getStoredTripID(){
        SharedPreferences sharedPref = getActivity().getPreferences(Context.MODE_PRIVATE);
        return sharedPref.getInt("STORED_TRIP_ID", -1);
    }

    private void setAlarm() {
        AlarmManager alarmManager = (AlarmManager) getContext().getSystemService(Context.ALARM_SERVICE);
        if(exactCalendar.after(mCalendar)) {
            Intent intent = new Intent(getContext(), AlarmBroadcast.class);
            intent.putExtra("date", exactCalendar.getTime().toString());
            intent.putExtra("location", toEditText.getText().toString());
            intent.putExtra("name", tripNameEditText.getText().toString());
            intent.putExtra("from", fromEditText.getText().toString());
            PendingIntent pendingIntent = PendingIntent.getBroadcast(getContext().getApplicationContext(), 0, intent, 0);
            alarmManager.set(AlarmManager.RTC_WAKEUP, exactCalendar.getTimeInMillis() - (1000), pendingIntent);

            Toast.makeText(getContext(), "alarm set at " + mCalendar.getTime(), Toast.LENGTH_SHORT).show();
        }
    }

    private void createNotificationChannel(){
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            NotificationChannel channel = new NotificationChannel("alarmNotification","alarmNotificationChannel", NotificationManager.IMPORTANCE_HIGH);
            channel.setDescription("channel for alarm notification");
            NotificationManager notificationManager = getContext().getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }


    private boolean checkPermission() {
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.M) {
            if (!Settings.canDrawOverlays(getContext())) {
                reqPermission();
                return false;
            }
            else {
                return true;
            }
        }else{
            return true;
        }
    }

    private void reqPermission(){
        final android.app.AlertDialog.Builder alertBuilder = new android.app.AlertDialog.Builder(getContext());
        alertBuilder.setCancelable(true);
        alertBuilder.setMessage("Enable 'Draw over other apps' to set alarm");
        alertBuilder.setPositiveButton("OPEN SETTINGS", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                        Uri.parse("package:" + getContext().getPackageName()));
                startActivityForResult(intent,RESULT_OK);
            }
        });
        alert = alertBuilder.create();
        alert.show();
    }
}