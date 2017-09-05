package com.wpdf.adapter;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.wpdf.libs.GenericFileProvider;
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
            viewHolder.textViewName = convertView.findViewById(R.id.textViewName);
            viewHolder.textViewTime = convertView.findViewById(R.id.textViewTime);
            viewHolder.textViewIcon = convertView.findViewById(R.id.textViewIcon);
            viewHolder.textViewSize = convertView.findViewById(R.id.textViewSize);

            convertView.setTag(viewHolder);


        } else {
            viewHolder = (ViewHolder) convertView.getTag();

        }


        if (list.size() > 0) {

            viewHolder.textViewName.setTag(list.get(position).getStrFileName());
            viewHolder.textViewTime.setTag(list.get(position).getStrFileName());
            viewHolder.textViewIcon.setTag(list.get(position).getStrFileName());
            viewHolder.textViewSize.setTag(list.get(position).getStrFileName());

            viewHolder.textViewIcon.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    openFile((String) v.getTag());
                }
            });

            viewHolder.textViewName.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    openFile((String) v.getTag());
                }
            });

            viewHolder.textViewTime.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View v) {
                    openFile((String) v.getTag());

                }
            });

            viewHolder.textViewSize.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    openFile((String) v.getTag());

                }
            });

            //ViewHolder holder = (ViewHolder) convertView.getTag();
            viewHolder.textViewName.setText(list.get(position).getStrFileName());
            viewHolder.textViewTime.setText(list.get(position).getStrFileModifiedTime());
            viewHolder.textViewSize.setText(list.get(position).getStrFileSize());

            viewHolder.textViewName.setTextColor(Color.DKGRAY);
            viewHolder.textViewTime.setTextColor(Color.BLUE);
        }

        return convertView;
    }

    private void openFile(String fileName) {
        try {

            File file = new File(Utils.getFile(context), fileName + ".pdf");

            Intent intent = new Intent();

            if (Build.VERSION.SDK_INT > 24) {

                intent.setAction(Intent.ACTION_VIEW);
                Uri pdfURI = GenericFileProvider.getUriForFile(context, context.getApplicationContext().getPackageName() +
                        ".provider", file);
               /* Uri pdfURI = GenericFileProvider.getUriForFile(context, context.getApplicationContext()
                        .getPackageName
                                (), f);*/
                intent.putExtra(Intent.EXTRA_STREAM, pdfURI);
                //intent.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
                intent.setType("application/pdf");
                intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            } else {
                String extension = android.webkit.MimeTypeMap.getFileExtensionFromUrl(
                        Uri.fromFile(file).toString());
                String mimetype = android.webkit.MimeTypeMap.getSingleton().
                        getMimeTypeFromExtension(extension);
                intent.setDataAndType(Uri.fromFile(file), mimetype);
            }

            context.startActivity(intent);

        } catch (ActivityNotFoundException aNFE) {
            aNFE.printStackTrace();
            //todo no intents
            Utils.toast(1, 1, context.getString(R.string.no_pdf_opener), context);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private class ViewHolder {
        TextView textViewName, textViewTime, textViewSize, textViewIcon;
    }
} 