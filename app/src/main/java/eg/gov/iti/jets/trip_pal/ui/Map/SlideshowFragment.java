package eg.gov.iti.jets.trip_pal.ui.Map;

import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.Dot;
import com.google.android.gms.maps.model.Gap;
import com.google.android.gms.maps.model.JointType;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.PatternItem;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.maps.model.RoundCap;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

import eg.gov.iti.jets.trip_pal.R;
import eg.gov.iti.jets.trip_pal.database.AppDatabase;
import eg.gov.iti.jets.trip_pal.database.TripEntity;
import eg.gov.iti.jets.trip_pal.trip.TripAdapter;
import eg.gov.iti.jets.trip_pal.ui.history.adapter.HistoryRecyclerViewAdapter;

public class SlideshowFragment extends Fragment {

    TripEntity tripEntity;
    private SlideshowViewModel slideshowViewModel;
    private List<TripEntity> tripHistoryList = new ArrayList<TripEntity>();
    PolylineOptions polylineOptions;
    SupportMapFragment mapFragment;
    Marker markerPerth;
    private LatLng location;
    private static final int COLOR_BLACK_ARGB = Color.RED;
    private static final int POLYLINE_STROKE_WIDTH_PX = 12;
    private static final int PATTERN_GAP_LENGTH_PX = 20;
    private static final PatternItem DOT = new Dot();
    private static final PatternItem GAP = new Gap(PATTERN_GAP_LENGTH_PX);
    private static final List<PatternItem> PATTERN_POLYLINE_DOTTED = Arrays.asList(GAP, DOT);
    private AppDatabase database;
    private GoogleMap mMap;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        database = AppDatabase.getInstance(getContext());
        polylineOptions = new PolylineOptions();

    }

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        slideshowViewModel = new ViewModelProvider(this).get(SlideshowViewModel.class);
        View root = inflater.inflate(R.layout.fragment_slideshow, container, false);
        //final TextView textView = root.findViewById(R.id.text_gallery);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        slideshowViewModel.getText().observe(getViewLifecycleOwner(), new Observer<String>() {
            @Override
            public void onChanged(@Nullable String s) {
                //textView.setText(s);
            }
        });
        return root;
    }


    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        database.tripDao().getDone().observe(getViewLifecycleOwner(), new Observer<List<TripEntity>>() {
            @Override
            public void onChanged(List<TripEntity> tripEntities) {
                SlideshowFragment.this.tripHistoryList = tripEntities;
                setMap();
            }
        });
        mapFragment = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.google_map);
        mapFragment.getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(GoogleMap googleMap) {
                try {
                    mMap = googleMap;
                    googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(
                            new LatLng(30.033333, 31.233334), 4
                    ));
                } catch (Exception e) {
                    Log.i("Found Exception", "onMapReady: ");
                }
            }
        });
    }

    private void setMap(){
        mMap.clear();
        for (int i = 0; i < tripHistoryList.size(); i++) {
            Random rnd = new Random();
            Log.d("MAP", "setMap: Random number is: " + rnd);
            int color = Color.argb(100, rnd.nextInt(255), rnd.nextInt(255), rnd.nextInt(255));
            PolylineOptions options = new PolylineOptions().width(10).color(color).geodesic(false);
            LatLng startPoint = new LatLng(tripHistoryList.get(i).getStartPointLat(),tripHistoryList.get(i).getStartPointLong());
            LatLng endPoint = new LatLng(tripHistoryList.get(i).getEndPointLat(),tripHistoryList.get(i).getEndPointLong());
            options.add(startPoint, endPoint);
            mMap.addPolyline(options);
        }
    }

    private void stylePolyline(Polyline polyline) {
        String type = "";
        // Get the data object stored with the polyline.
        if (polyline.getTag() != null) {
            type = polyline.getTag().toString();
        }

        polyline.setEndCap(new RoundCap());
        polyline.setWidth(POLYLINE_STROKE_WIDTH_PX);
        polyline.setColor(COLOR_BLACK_ARGB);
        polyline.setJointType(JointType.ROUND);
    }
}