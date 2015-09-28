package com.byteshatf.callrecorder.fragments;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import com.byteshatf.callrecorder.AppGlobals;
import com.byteshatf.callrecorder.Helpers;
import com.byteshatf.callrecorder.R;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

public class RecordingListFragment extends android.support.v4.app.Fragment implements
        AdapterView.OnItemClickListener {

    private ArrayAdapter<String> arrayAdapter;
    private View view;
    private ListView mRecordList;
    private Helpers mHelpers;
    private ArrayList<String> arrayList;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mHelpers = new Helpers(getActivity());
        view = inflater.inflate(R.layout.list_fragment, container, false);
        mRecordList = (ListView) view.findViewById(R.id.recording_list);
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        arrayList = mHelpers.getAllFilesFromFolder();
        arrayAdapter = new RecordingAdapter(getActivity(), android.R.layout.simple_list_item_1, arrayList);
//        mRecordList.setOnItemLongClickListener(this);
        mRecordList.setOnItemClickListener(this);
        mRecordList.setAdapter(arrayAdapter);
    }

//    @Override
//    public boolean onItemLongClick(final AdapterView<?> parent, View view, final int position, long id) {
//        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
//        builder.setTitle("Delete");
//        builder.setMessage("Do you want to delete this recording?");
//        builder.setCancelable(false);
//        builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
//            @Override
//            public void onClick(DialogInterface dialog, int which) {
//                String path = AppGlobals.getDataDirectory("CallRec")+ "/" + parent.getItemAtPosition(position) ;
//                removeFiles(path);
//                String item = arrayAdapter.getItem(position);
//                arrayAdapter.remove(item);
//                arrayAdapter.notifyDataSetChanged();
//            }
//        });
//        builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
//            @Override
//            public void onClick(DialogInterface dialog, int which) {
//                dialog.dismiss();
//            }
//        });
//        builder.create();
//        builder.show();
//        return true;
//    }

    private void removeFiles(String path) {
        File file = new File(path);
        if (file.exists()) {
            String deleteCmd = "rm -r " + path;
            Runtime runtime = Runtime.getRuntime();
            try {
                runtime.exec(deleteCmd);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        String path = AppGlobals.getDataDirectory("CallRec") + "/" + parent.getItemAtPosition(position);
        try {
            Intent intent = new Intent();
            intent.setAction(Intent.ACTION_VIEW);
            File file = new File(path);
            intent.setPackage("com.google.android.music");
            intent.setDataAndType(Uri.fromFile(file), "audio/*");
            System.out.println(path);
            startActivity(intent);
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

    class RecordingAdapter extends ArrayAdapter<String> implements View.OnClickListener {

        public RecordingAdapter(Context context, int resource, ArrayList<String> recordingList) {
            super(context, resource, recordingList);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder holder;
            if (convertView == null) {
                LayoutInflater inflater = getActivity().getLayoutInflater();
                convertView = inflater.inflate(R.layout.recording_cutom_listview, parent, false);
                holder = new ViewHolder();
                holder.title = (TextView) convertView.findViewById(R.id.FilePath);
                holder.deleteButton = (Button) convertView.findViewById(R.id.delete_button);
                holder.deleteButton.setOnClickListener(this);
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }
            String title = arrayList.get(position);
            holder.title.setText(title);
            return convertView;
        }

        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.delete_button:
                    final int position = mRecordList.getPositionForView((View) v.getParent());
                    AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                    builder.setTitle("Delete");
                    builder.setIcon(android.R.drawable.ic_menu_delete);
                    builder.setMessage("Do you want to delete this recording?");
                    builder.setCancelable(false);
                    builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            String path = AppGlobals.getDataDirectory("CallRec") + "/" + mRecordList.getItemAtPosition(position);
                            removeFiles(path);
                            String item = arrayAdapter.getItem(position);
                            arrayAdapter.remove(item);
                            arrayAdapter.notifyDataSetChanged();
                        }
                    });
                    builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    });
                    builder.create();
                    builder.show();
                    break;
            }
        }
    }

    static class ViewHolder {
        public TextView title;
        public Button deleteButton;
    }
}

