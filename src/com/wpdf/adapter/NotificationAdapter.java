package com.wpdf.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.wpdf.dbConfig.Dbcon;
import com.wpdf.dbConfig.Dbhelper;
import com.wpdf.model.NotificationModel;
import com.wpdf.websaver.R;

import java.util.List;

public class NotificationAdapter extends ArrayAdapter<NotificationModel> {

    private Context context;
    private List<NotificationModel> notificationModels;

    public NotificationAdapter(Context context, List<NotificationModel> notificationModels) {
        super(context, R.layout.content_notification, notificationModels);
        this.context = context;
        this.notificationModels = notificationModels;
    }

    @Override
    public int getCount() {
        return notificationModels.size();
    }

    @Override
    public NotificationModel getItem(int position) {
        return notificationModels.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {

        ViewHolder viewHolder;

        if (convertView == null) {

            LayoutInflater inflator = LayoutInflater.from(context);

            convertView = inflator.inflate(R.layout.content_notification, null);

            viewHolder = new ViewHolder();
            viewHolder.textViewMess = convertView.findViewById(R.id.textViewMess);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();

        }
        if (notificationModels.size() > 0) {

            //ViewHolder holder = (ViewHolder) convertView.getTag();
            try {
                viewHolder.textViewMess.setText(notificationModels.get(position).getStrMessage());

                viewHolder.textViewMess.setTag(notificationModels.get(position).getId());
            } catch (Exception e) {
                e.printStackTrace();
            }

            viewHolder.textViewMess.setOnLongClickListener(new View.OnLongClickListener() {
                /**
                 * Called when a view has been clicked and held.
                 *
                 * @param v The view that was clicked and held.
                 * @return true if the callback consumed the long click, false otherwise.
                 */
                @Override
                public boolean onLongClick(View v) {
                    int id = (int) v.getTag();

                    Dbcon db = new Dbcon(context);
                    db.delete(Dbhelper.NOTIFICATIONS, "notification_id=" + String.valueOf(id), null);
                    notificationModels.remove(position);
                    notifyDataSetChanged();
                    return true;
                }
            });
        }
        return convertView;
    }

    private class ViewHolder {
        TextView textViewMess;
    }
}
