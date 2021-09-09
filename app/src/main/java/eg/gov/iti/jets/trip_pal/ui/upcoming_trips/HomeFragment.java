package eg.gov.iti.jets.trip_pal.ui.upcoming_trips;

import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import eg.gov.iti.jets.trip_pal.R;
import eg.gov.iti.jets.trip_pal.database.AppDatabase;
import eg.gov.iti.jets.trip_pal.database.TripEntity;
//import eg.gov.iti.jets.trip_pal.trip.Trip;
import eg.gov.iti.jets.trip_pal.trip.TripAdapter;

public class HomeFragment extends Fragment {

    private HomeViewModel homeViewModel;
    public RecyclerView recyclerView;
    public List tripList;
    TripAdapter mAdapter;


    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        homeViewModel = new ViewModelProvider(this).get(HomeViewModel.class);
        View root = inflater.inflate(R.layout.fragment_home, container, false);
        final TextView textView = root.findViewById(R.id.text_home);
        recyclerView = (RecyclerView) root.findViewById(R.id.recyclerview_id);
        recyclerView.setHasFixedSize(true);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        layoutManager.setOrientation(recyclerView.VERTICAL);
        recyclerView.setLayoutManager(layoutManager);
        tripList = new ArrayList<TripEntity>();
        mAdapter = new TripAdapter(getContext(), tripList);
        recyclerView.setAdapter(mAdapter);
        mAdapter.notifyDataSetChanged();


        homeViewModel.getTrips(getContext()).observe(getViewLifecycleOwner(), new Observer<List<TripEntity>>() {
            @Override
            public void onChanged(List<TripEntity> tripEntities) {
                mAdapter.changeData(tripEntities);
            }
        });



        homeViewModel.getText().observe(getViewLifecycleOwner(), new Observer<String>() {
            @Override
            public void onChanged(@Nullable String s) {
                textView.setText(s);
            }
        });
        Log.d("Awad", "onCreateView: "+this.getClass().getSimpleName());
        return root;
    }


    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mAdapter.mInterface = new TripAdapter.TripDeleterInterface() {
            @Override
            public void deleteTrip(TripEntity tripEntity) {
            AppDatabase.getInstance(getContext()).tripDao().delete(tripEntity);
            mAdapter.notifyDataSetChanged();
            }
        };
    }


}