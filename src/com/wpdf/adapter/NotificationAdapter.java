package com.wpdf.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.wpdf.dbConfig.Dbcon;
import com.wpdf.dbConfig.Dbhelper;
import com.wpdf.model.NotificationModel;
import com.wpdf.websaver.R;

import java.util.List;

public class NotificationAdapter extends RecyclerView.Adapter<NotificationAdapter.MyViewHolder> {

    private Context context;
    private List<NotificationModel> notificationModels;

    public NotificationAdapter(Context context, List<NotificationModel> notificationModels) {
        this.context = context;
        this.notificationModels = notificationModels;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.content_notification, parent, false);

        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(final MyViewHolder holder, final int position) {
        NotificationModel notificationModel = notificationModels.get(position);
        holder.textViewMess.setText(notificationModel.getStrMessage());
        holder.textViewMess.setTag(notificationModel.getId());

        Log.e("NA", "From: " + notificationModel.getStrMessage());

        holder.textViewMess.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {

                int id = (int) view.getTag();


                Dbcon db = new Dbcon(context);
                db.delete(Dbhelper.NOTIFICATIONS, "notification_id=" + String.valueOf(id), null);
                notificationModels.remove(holder.getAdapterPosition());
                notifyDataSetChanged();

                return false;
            }
        });

    }

    @Override
    public int getItemCount() {
        return notificationModels.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {
        public TextView textViewMess;

        public MyViewHolder(View view) {
            super(view);
            textViewMess = view.findViewById(R.id.textViewMess);
        }
    }
}

