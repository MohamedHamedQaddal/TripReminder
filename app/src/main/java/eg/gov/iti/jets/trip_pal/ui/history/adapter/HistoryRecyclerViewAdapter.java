package eg.gov.iti.jets.trip_pal.ui.history.adapter;

import android.content.Context;
import android.content.DialogInterface;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.cardview.widget.CardView;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import eg.gov.iti.jets.trip_pal.R;
import eg.gov.iti.jets.trip_pal.database.TripEntity;
import eg.gov.iti.jets.trip_pal.trip.TripAdapter;

public class HistoryRecyclerViewAdapter extends RecyclerView.Adapter<HistoryRecyclerViewAdapter.ViewHolder>{
    private static final String TAG = "RecyclerView";
    private Context context;
    private List<TripEntity> tripList;
    public TripAdapter.TripDeleterInterface mInterface = null;//*


    public HistoryRecyclerViewAdapter (Context context, List<TripEntity> tripList){
        super();
        this.context = context;
        this.tripList = tripList;
    }


    public class ViewHolder extends RecyclerView.ViewHolder {
        public ConstraintLayout constraintLayout;   //Constraint Layout
        public CardView cardView;                   //CardView container

        //TextViews
        public TextView nameLabel;                  //ex: Trip No. 01
        public TextView dateLabel;                  //ex: 15/03/2021
        public TextView timeLabel;                  //ex: 07:30 AM
        public TextView typeLabel;                  //ex: one way or round trip
        public TextView statusLabel;                //ex: upcoming, done or cancelled
        public TextView startLabel;                 //ex: Home
        public TextView endLabel;                   //ex: Work
        public TextView notesLabel;
        public  TextView tripType;

        //ImageViews
        public ImageView edit;
        public ImageView delete;
        public ImageView showNotes;

        public ImageView tripStatusImg;
        public ImageView tripTypeImg;
        public ImageView tripDateImg;
        public ImageView tripTimeImg;
        public ImageView tripFromImg;
        public ImageView tripToImg;

        Button startTripButton;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            constraintLayout = itemView.findViewById(R.id.constraint_layout_id);
            cardView = itemView.findViewById(R.id.cardview_id);

            edit = itemView.findViewById(R.id.action_edit_id);
            delete = itemView.findViewById(R.id.action_delete_id);
            showNotes = itemView.findViewById(R.id.action_show_notes_id);

            nameLabel = itemView.findViewById(R.id.trip_name_label_id);
            dateLabel = itemView.findViewById(R.id.trip_date_label_id);
            timeLabel = itemView.findViewById(R.id.trip_time_label_id);
            startLabel = itemView.findViewById(R.id.trip_from_label_id);
            endLabel = itemView.findViewById(R.id.trip_to_label_id);
            //typeLabel = v.findViewById(R.id.trip_type_label_id);
            //notesLabel = v.findViewById(R.id.trip_notes_label_id);
            tripType = itemView.findViewById(R.id.status);

            tripTypeImg = itemView.findViewById(R.id.trip_type_imageView_id);
            tripDateImg = itemView.findViewById(R.id.trip_date_imageView_id);
            tripTimeImg = itemView.findViewById(R.id.trip_time_imageView_id);
            tripFromImg =itemView.findViewById(R.id.trip_from_imageView_id);
            tripToImg = itemView.findViewById(R.id.trip_to_imageView_id);
            startTripButton = itemView.findViewById(R.id.start_button_id);
        }
    }


    @NonNull
    @Override
    public HistoryRecyclerViewAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(context);
        View v = inflater.inflate(R.layout.history_trip_layout, parent, false);
        ViewHolder viewHolder = new ViewHolder(v);
        viewHolder.setIsRecyclable(true);
        Log.i(TAG, "==  === onCreateViewHolder: =====");
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull HistoryRecyclerViewAdapter.ViewHolder holder, int position) {
        TripEntity tripEntity = tripList.get(position);


        holder.delete.setImageResource(R.drawable.i_delete);
        holder.showNotes.setImageResource(R.drawable.i_notes);

        //viewHolder.tripStatusImg.setImageResource(R.drawable.i_done);
        if(tripEntity.getTripType().equals("One Way Trip")){
            holder.tripTypeImg.setImageResource(R.drawable.i_one_way);
        }else{
            holder.tripTypeImg.setImageResource(R.drawable.i_round_trip);
        }
        holder.tripDateImg.setImageResource(R.drawable.i_date);
        holder.tripTimeImg.setImageResource(R.drawable.i_time);
        holder.tripFromImg.setImageResource(R.drawable.i_car);
        holder.tripToImg.setImageResource(R.drawable.i_location);

        holder.nameLabel.setText(tripEntity.getTripName());
        holder.dateLabel.setText(tripEntity.getTripDate());
        holder.timeLabel.setText(tripEntity.getTripTime());
        holder.startLabel.setText(tripEntity.getTripStart());
        holder.endLabel.setText(tripEntity.getTripEnd());
        //viewHolder.typeLabel.setText(tripEntity.getTripType());
        //viewHolder.statusLabel.setText(tripEntity.getTripStatus());
        //viewHolder.notesLabel.setText(tripEntity.getTripNotes());
        holder.tripType.setText(tripEntity.getTripStatus());




        if(mInterface != null){//*
            holder.delete.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {//onClick of delete generates a dialog
                    confirmDelete(position);
                }
            });
        }


        holder.showNotes.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new AlertDialog.Builder(context)
                        .setTitle("Trip Notes")
                        .setCancelable(false)
                        .setMessage((CharSequence) tripEntity.getTripNotes())
                        .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton){
                                dialog.cancel();
                            }})
                        .setNegativeButton(android.R.string.no, null).show();
            }
        });

    }


    @Override
    public int getItemCount(){return tripList.size(); }

    private void confirmDelete(int position) {
        new AlertDialog.Builder(context)
                .setTitle("Warning!")
                .setMessage("Do you really wish to delete this trip?")
                .setIcon(android.R.drawable.ic_dialog_alert)
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Toast.makeText(context, "Deleted", Toast.LENGTH_SHORT).show();
                        mInterface.deleteTrip(tripList.get(position));
                        //tripList.remove(position);
                        notifyDataSetChanged();
                    }
                })
                .setNegativeButton(android.R.string.no, null).show();

    }

    public void changeData(List<TripEntity> data){
        tripList = data;
        notifyDataSetChanged();
    }

    public interface TripDeleterInterface{//*
        void deleteTrip(TripEntity tripEntity);
    }

}