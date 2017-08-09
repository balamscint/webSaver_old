package com.wpdf.adapter;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Environment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.wpdf.model.ViewModel;
import com.wpdf.websaver.R;

import java.io.File;
import java.util.List;

public class ViewListAdapter extends ArrayAdapter<ViewModel> {

    private final Activity context;
    private List<ViewModel> list;
    private File root;

    public ViewListAdapter(Activity context, List<ViewModel> list) {
        super(context, R.layout.viewlayout, list);
        this.context = context;
        this.list = list;
    }

    @Override
    public int getCount() {
        return list.size();
    }

    @Override
    public ViewModel getItem(int position) {
        return list.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view = null;
        if (convertView == null) {
            LayoutInflater inflator = context.getLayoutInflater();
            view = inflator.inflate(R.layout.viewlayout, null);
            final ViewHolder viewHolder = new ViewHolder();
            viewHolder.name = view.findViewById(R.id.textView);
            viewHolder.path = view.findViewById(R.id.textView3);
            viewHolder.time = view.findViewById(R.id.textView2);
            viewHolder.imgView = view.findViewById(R.id.imageView);

            root = new File(Environment.getExternalStorageDirectory() + File.separator + "webSaver" + File.separator);

            if (list.size() > 0) {

                viewHolder.imgView
                        .setOnClickListener(new View.OnClickListener() {

                            @Override
                            public void onClick(View v) {

                                try {

                                    File f = new File(root, viewHolder.name.getText().toString() + ".pdf");
                                    Intent myIntent = new Intent(Intent.ACTION_VIEW);
                                    String extension = android.webkit.MimeTypeMap.getFileExtensionFromUrl(Uri.fromFile(f).toString());
                                    String mimetype = android.webkit.MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension);
                                    myIntent.setDataAndType(Uri.fromFile(f), mimetype);
                                    context.startActivity(myIntent);
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }
                        });


                viewHolder.name
                        .setOnClickListener(new View.OnClickListener() {

                            @Override
                            public void onClick(View v) {

                                try {

                                    File f = new File(root, viewHolder.name.getText().toString() + ".pdf");

                                    Intent myIntent = new Intent(Intent.ACTION_VIEW);

                                    String extension = android.webkit.MimeTypeMap.getFileExtensionFromUrl(Uri.fromFile(f).toString());
                                    String mimetype = android.webkit.MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension);
                                    myIntent.setDataAndType(Uri.fromFile(f), mimetype);
                                    context.startActivity(myIntent);
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }

                            }
                        });

                viewHolder.time
                        .setOnClickListener(new View.OnClickListener() {

                            @Override
                            public void onClick(View v) {

                                try {

                                    File f = new File(root, viewHolder.name.getText().toString() + ".pdf");
                                    Intent myIntent = new Intent(Intent.ACTION_VIEW);
                                    String extension = android.webkit.MimeTypeMap.getFileExtensionFromUrl(Uri.fromFile(f).toString());
                                    String mimetype = android.webkit.MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension);
                                    myIntent.setDataAndType(Uri.fromFile(f), mimetype);

                                    context.startActivity(myIntent);
                                } catch (Exception e) {
//                            e.printStackTrace();
                                }

                            }
                        });

                viewHolder.path
                        .setOnClickListener(new View.OnClickListener() {

                            @Override
                            public void onClick(View v) {

                                try {

                                    File f = new File(root, viewHolder.name.getText().toString() + ".pdf");

                                    Intent myIntent = new Intent(Intent.ACTION_VIEW);
                                    String extension = android.webkit.MimeTypeMap.getFileExtensionFromUrl(Uri.fromFile(f).toString());
                                    String mimetype = android.webkit.MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension);
                                    myIntent.setDataAndType(Uri.fromFile(f), mimetype);
                                    context.startActivity(myIntent);

                                } catch (Exception e) {
                                    // e.printStackTrace();
                                }

                            }
                        });


                view.setTag(viewHolder);


            } else {
                view = convertView;

            }
            ViewHolder holder = (ViewHolder) view.getTag();
            holder.name.setText(list.get(position).getName());
            holder.path.setText(list.get(position).getPath());
            holder.time.setText(list.get(position).getTime());

            holder.name.setTextColor(Color.DKGRAY);
            holder.path.setTextColor(Color.DKGRAY);
            holder.time.setTextColor(Color.BLUE);

        }

        return view;
    }

    static class ViewHolder {
        protected TextView name, path, time;
        protected ImageView imgView;
    }
} 