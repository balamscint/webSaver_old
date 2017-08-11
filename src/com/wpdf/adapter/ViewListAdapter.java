package com.wpdf.adapter;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.wpdf.libs.Utils;
import com.wpdf.model.PdfModel;
import com.wpdf.websaver.R;

import java.io.File;
import java.util.List;

public class ViewListAdapter extends ArrayAdapter<PdfModel> {

    private Context context;
    private List<PdfModel> list;

    public ViewListAdapter(Activity context, List<PdfModel> list) {
        super(context, R.layout.viewlayout, list);
        this.context = context;
        this.list = list;
    }

    @Override
    public int getCount() {
        return list.size();
    }

    @Override
    public PdfModel getItem(int position) {
        return list.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        ViewHolder viewHolder;

        if (convertView == null) {

            LayoutInflater inflator = LayoutInflater.from(context);

            convertView = inflator.inflate(R.layout.viewlayout, null);

            viewHolder = new ViewHolder();
            viewHolder.name = convertView.findViewById(R.id.textView);
            viewHolder.path = convertView.findViewById(R.id.textView3);
            viewHolder.time = convertView.findViewById(R.id.textView2);
            viewHolder.imgView = convertView.findViewById(R.id.imageView);

            convertView.setTag(viewHolder);


        } else {
            viewHolder = (ViewHolder) convertView.getTag();

        }


        if (list.size() > 0) {

            viewHolder.name.setTag(list.get(position));
            viewHolder.path.setTag(list.get(position));
            viewHolder.time.setTag(list.get(position));
            viewHolder.imgView.setTag(list.get(position));

            viewHolder.imgView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    openFile((String) v.getTag());
                }
            });

            viewHolder.name.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    openFile((String) v.getTag());
                }
            });

            viewHolder.time.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View v) {
                    openFile((String) v.getTag());

                }
            });

            viewHolder.path.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    openFile((String) v.getTag());

                }
            });
        }


        ViewHolder holder = (ViewHolder) convertView.getTag();
        holder.name.setText(list.get(position).getName());
        holder.path.setText(list.get(position).getPath());
        holder.time.setText(list.get(position).getTime());

        holder.name.setTextColor(Color.DKGRAY);
        holder.path.setTextColor(Color.DKGRAY);
        holder.time.setTextColor(Color.BLUE);

        return convertView;
    }

    private void openFile(String fileName) {
        try {

            File f = new File(Utils.getFile(context), fileName + ".pdf");

            Intent myIntent = new Intent(Intent.ACTION_VIEW);
            String extension = android.webkit.MimeTypeMap.getFileExtensionFromUrl(
                    Uri.fromFile(f).toString());
            String mimetype = android.webkit.MimeTypeMap.getSingleton().
                    getMimeTypeFromExtension(extension);
            myIntent.setDataAndType(Uri.fromFile(f), mimetype);
            context.startActivity(myIntent);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private class ViewHolder {
        TextView name, path, time;
        ImageView imgView;
    }
} 