package eg.gov.iti.jets.trip_pal.ui.done_trips;

import android.content.Intent;
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
import androidx.lifecycle.LiveData;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import eg.gov.iti.jets.trip_pal.R;
import eg.gov.iti.jets.trip_pal.database.AppDatabase;
import eg.gov.iti.jets.trip_pal.database.TripEntity;
import eg.gov.iti.jets.trip_pal.trip.TripAdapter;
import eg.gov.iti.jets.trip_pal.ui.history.adapter.HistoryRecyclerViewAdapter;
import eg.gov.iti.jets.trip_pal.ui.upcoming_trips.HomeViewModel;

public class GalleryFragment extends Fragment {

    private LiveData<List<TripEntity>> doneTrips;
    private GalleryViewModel galleryViewModel;
    public RecyclerView recyclerView;
    public List tripList;
    HistoryRecyclerViewAdapter historyAdapter;

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        galleryViewModel = new ViewModelProvider(this).get(GalleryViewModel.class);
        View root = inflater.inflate(R.layout.fragment_gallery, container, false);
        final TextView textView = root.findViewById(R.id.text_gallery);
        recyclerView = (RecyclerView) root.findViewById(R.id.recyclerview_done_id);
        recyclerView.setHasFixedSize(true);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        layoutManager.setOrientation(recyclerView.VERTICAL);
        recyclerView.setLayoutManager(layoutManager);
        tripList = new ArrayList<TripEntity>();
        historyAdapter = new HistoryRecyclerViewAdapter(getContext(),tripList);
        recyclerView.setAdapter(historyAdapter);
        historyAdapter.notifyDataSetChanged();


        galleryViewModel.getTrips(getContext()).observe(getViewLifecycleOwner(), new Observer<List<TripEntity>>() {
            @Override
            public void onChanged(List<TripEntity> tripEntities) {
                historyAdapter.changeData(tripEntities);
            }
        });


        galleryViewModel.getText().observe(getViewLifecycleOwner(), new Observer<String>() {
            @Override
            public void onChanged(@Nullable String s) {
                textView.setText(s);
            }
        });
        Log.d("Awad", "onCreateView: "+this.getClass().getSimpleName());

        return root;

    }





 /*
    private List<TripEntity> getDoneTrips() {

        class GetTrips extends AsyncTask<Void, Void, LiveData<List<TripEntity>>> {
            @Override
            protected LiveData<List<TripEntity>> doInBackground(Void... voids) {
                LiveData<List<TripEntity>> doneTripList = AppDatabase.getInstance(getContext()).tripDao().getDone();
                return doneTripList;
            }

            @Override
            protected void onPostExecute(List<TripEntity> tripList) {
                super.onPostExecute((LiveData<List<TripEntity>>) tripList);
                //mAdapter = new TripAdapter(getContext(), tripList);
                //recyclerView.setAdapter(mAdapter);
                historyAdapter.changeData(tripList);
            }
        }
        GetTrips tripGetter = new GetTrips();
        tripGetter.execute();
        return null;
    }
  */




    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        historyAdapter.mInterface = new TripAdapter.TripDeleterInterface() {
            @Override
            public void deleteTrip(TripEntity tripEntity) {
                /*new AlertDialog.Builder(getContext())
                        .setTitle("Warning!")
                        .setMessage("Do you really wish to delete this trip?")
                        .setIcon(android.R.drawable.ic_dialog_alert)
                        .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton){//onClick of yes deletes a trip
                                try{
                                    //tripList.remove(position);
                                    //notifyItemRemoved(position);
                                    //notifyItemRangeChanged(position, tripList.size());
                                    Toast.makeText(getContext(), tripEntity.getTripName(), Toast.LENGTH_SHORT).show();
                                    deleteTrip(tripEntity);
                                }catch (IndexOutOfBoundsException e) {
                                    Toast.makeText(getContext(), "Your Trip List is already Empty.", Toast.LENGTH_SHORT).show();
                                }}})
                        .setNegativeButton(android.R.string.no, null).show();
            */
                AppDatabase.getInstance(getContext()).tripDao().delete(tripEntity);
                historyAdapter.notifyDataSetChanged();
            }
        };
    }

/*
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        List<TripEntity> doneTripList = AppDatabase.getInstance(getContext()).tripDao().getDone();
        doneTripList.addAll(appDatabase.tripDao().getDone());
        historyAdapter.notifyDataSetChanged();
    }

 */
}
