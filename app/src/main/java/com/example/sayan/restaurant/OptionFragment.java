package com.example.sayan.restaurant;

import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

/**
 * Created by Sayan on 22-Apr-17.
 */

public class OptionFragment extends DialogFragment {

    public OptionFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_picture_option, container, false);
    }

    @Nullable
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Button camera = null, gallery = null;
        View view = getActivity().getLayoutInflater().inflate(R.layout.fragment_picture_option, null);
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity()).setView(view);
        final Dialog dialog = builder.create();
        camera = (Button) view.findViewById(R.id.picture_id);
        gallery = (Button) view.findViewById(R.id.gallery_id);

        camera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
                Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                intent.putExtra(MediaStore.EXTRA_VIDEO_QUALITY, 1);
                getActivity().startActivityForResult(intent, 123);
            }
        });
        gallery.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                intent.setType("image/*");
                getActivity().startActivityForResult(Intent.createChooser(intent, "Select Picture"), 321);
            }
        });
        return dialog;
    }
}
