package eg.gov.iti.jets.trip_pal;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.Menu;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.navigation.NavController;
import androidx.navigation.NavDestination;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import eg.gov.iti.jets.trip_pal.database.AppDatabase;
import eg.gov.iti.jets.trip_pal.database.TripEntity;
import eg.gov.iti.jets.trip_pal.trip.TripCreatorActivity;
import eg.gov.iti.jets.trip_pal.ui.Map.SlideshowFragment;
import eg.gov.iti.jets.trip_pal.ui.done_trips.GalleryFragment;
import eg.gov.iti.jets.trip_pal.ui.upcoming_trips.HomeFragment;

public class MainActivity extends AppCompatActivity {

    private AppBarConfiguration mAppBarConfiguration;
    private final int LAUNCH_SECOND_ACTIVITY = 1;
    FragmentManager fragmentManager;
    FragmentTransaction fragmentTransaction;

    private DrawerLayout drawer;
    String email;
    FirebaseUser user;
    DatabaseReference myRef;
    FirebaseDatabase database;
    public List tripList;

    TripEntity trip = new TripEntity();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        tripList = new ArrayList<TripEntity>();
        if (database == null){
            database = FirebaseDatabase.getInstance();
            myRef = FirebaseDatabase.getInstance().getReference("Users");
            myRef = myRef.child(FirebaseAuth.getInstance().getCurrentUser().getUid());
        }

        tripList = new ArrayList<TripEntity>();


        user = FirebaseAuth.getInstance().getCurrentUser();
        email = user.getEmail();

        drawer = findViewById(R.id.drawer_layout);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setBackgroundColor(getResources().getColor(R.color.quantum_orange));
        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                storeTripID(-1);
                Intent intent = new Intent(MainActivity.this, TripCreatorActivity.class);
                startActivityForResult(intent, LAUNCH_SECOND_ACTIVITY);
                //startActivity(intent);
            }
        });

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        NavigationView navigationView = findViewById(R.id.nav_view);

        View headerView = navigationView.getHeaderView(0);
        TextView navUsername = (TextView) headerView.findViewById(R.id.nav_title);
        navUsername.setText(email);


        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        mAppBarConfiguration = new AppBarConfiguration.Builder(
                R.id.nav_home, R.id.nav_gallery, R.id.nav_slideshow)
                .setDrawerLayout(drawer)
                .build();
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        NavigationUI.setupActionBarWithNavController(this, navController, mAppBarConfiguration);
        NavigationUI.setupWithNavController(navigationView, navController);

        navController.addOnDestinationChangedListener(new NavController.OnDestinationChangedListener() {
            @Override
            public void onDestinationChanged(@NonNull NavController controller, @NonNull NavDestination destination, @Nullable Bundle arguments) {
                int menuID = destination.getId();

                switch (menuID){
                    case R.id.nav_gallery:
                        fragmentManager = getSupportFragmentManager();
                        fragmentTransaction = fragmentManager.beginTransaction();
                        fragmentTransaction.replace(R.id.nav_host_fragment, new GalleryFragment());
                        fragmentTransaction.addToBackStack(null).commit();
                        fab.hide();
                        break;

                    case R.id.nav_slideshow:
                        fragmentManager = getSupportFragmentManager();
                        fragmentTransaction = fragmentManager.beginTransaction();
                        fragmentTransaction.replace(R.id.nav_host_fragment, new SlideshowFragment());
                        fragmentTransaction.addToBackStack(null).commit();
                        //fab.hide();
                        break;

                    case R.id.nav_home:
                        fragmentManager = getSupportFragmentManager();
                        fragmentTransaction = fragmentManager.beginTransaction();
                        fragmentTransaction.replace(R.id.nav_host_fragment, new HomeFragment());
                        fragmentTransaction.addToBackStack(null).commit();
                        fab.show();
                        break;
                    default:
                        fab.show();
                }
            }
        });
    }



    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onSupportNavigateUp() {
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        return NavigationUI.navigateUp(navController, mAppBarConfiguration)
                || super.onSupportNavigateUp();
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
            if (requestCode == LAUNCH_SECOND_ACTIVITY) {
                if(resultCode == Activity.RESULT_OK){
                    try{
                    String fullTripStr;
                    fullTripStr = data.getStringExtra("trip");
                    //Log.i("SOMETHING!!!!!", "onActivityResult: ");
                    //Log.i(fullTripStr + "bl7", "onActivityResult: ");
                }catch(Exception e){
                        Log.i("Found an Exception", "onActivityResult: ");
                    }
                }
                if (resultCode == Activity.RESULT_CANCELED) {
                    //Write your code if there's no result
                    //Log.i("NOTHING!!!!!", "onActivityResult: ");
                }
            }
        //Log.i(fullTrip.getTripName(), "onActivityResult: ");

    }//onActivityResult


    public void storeTripID(int ID){
        SharedPreferences sharedPref = getPreferences(Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putInt("STORED_TRIP_ID", ID).commit();
    }


    public void clickedOption(MenuItem item) {
        switch (item.getItemId()){
            case R.id.nav_sync:
                tripList =  AppDatabase.getInstance(MainActivity.this).tripDao().getAllTrips();
                Log.i("size", "clickedOption: " + tripList.size());
                saveTripToFirebase();
                drawer.closeDrawers();
                break;

            case R.id.nav_signout:
                FirebaseAuth.getInstance().signOut();
                drawer.closeDrawers();
                break;
        }
    }


    private void saveTripToFirebase() {

        for (int i = 0; i< tripList.size(); i++){
            HashMap<String, Object> hashMap = new HashMap<>();

            trip = (TripEntity) tripList.get(i);
            hashMap.put("tripName", trip.getTripName());
            hashMap.put("tripFrom", trip.getTripStart());
            hashMap.put("tripTo", trip.getTripEnd());
            hashMap.put("tripDate", trip.getTripDate());
            hashMap.put("tripTime", trip.getTripTime());
            //hashMap.put("tripNotes", trip.getTripNotes());
            hashMap.put("tripType", trip.getTripType());


            myRef.orderByChild("tripName").equalTo(trip.getTripName()).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {

                    if(!snapshot.exists()){
                        myRef.push().setValue(hashMap).addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                Log.e("TAG", "onSuccess: ");
                            }
                        })
                                .addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        Log.e("TAG", "onFailure: ", e);
                                    }
                                });

                        ValueEventListener valueEventListener = new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                for(DataSnapshot ds : snapshot.getChildren()){
                                    String key = ds.getKey();
                                    Log.i("key", "onDataChange: " + key);

                                    DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
                                    for (String note : trip.getTripNotes()) {
                                        ref.child(FirebaseAuth.getInstance().getCurrentUser().getUid()).child(key).child("notes").child("note"+(trip.getTripNotes().indexOf(note)+1)).setValue(note);
                                    }
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {

                            }
                        };
                        myRef.addListenerForSingleValueEvent(valueEventListener);

                        Log.i("TAG", "saveTripToFirebase: ");
                    }
                    else{
                        Toast.makeText(MainActivity.this, "Up to date", Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });

        }
    }


}