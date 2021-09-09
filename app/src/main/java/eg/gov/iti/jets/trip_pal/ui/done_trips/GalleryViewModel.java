package eg.gov.iti.jets.trip_pal.ui.done_trips;

import android.content.Context;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import java.util.List;

import eg.gov.iti.jets.trip_pal.database.AppDatabase;
import eg.gov.iti.jets.trip_pal.database.TripEntity;

public class GalleryViewModel extends ViewModel {

    private MutableLiveData<String> mText;

    public GalleryViewModel() {
        mText = new MutableLiveData<>();
        mText.setValue("");
    }

    

    public LiveData<List<TripEntity>> getTrips(Context context){
        return AppDatabase.getInstance(context).tripDao().getDone();
    }

    public LiveData<String> getText() {
        return mText;
    }
}