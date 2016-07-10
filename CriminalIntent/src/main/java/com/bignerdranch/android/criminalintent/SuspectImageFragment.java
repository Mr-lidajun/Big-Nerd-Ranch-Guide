package com.bignerdranch.android.criminalintent;

import android.support.v7.app.AlertDialog;
import java.io.File;

import android.app.Dialog;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;

/**
 * @author lidajun
 * @email solidajun@gmail.com
 * @date 16/7/10 17:08.
 * @desc: 查看缩放版本的陋习现场图片
 */
public class SuspectImageFragment extends DialogFragment {
    private static final String TAG = "SuspectImageFragment";
    private static final String ARG_SUSPECT_IMAGE = "suspect_image";

    public static SuspectImageFragment newInstance(File photoFile) {
        Bundle args = new Bundle();
        args.putSerializable(ARG_SUSPECT_IMAGE, photoFile);

        SuspectImageFragment fragment = new SuspectImageFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        File photoFile = (File) getArguments().getSerializable(ARG_SUSPECT_IMAGE);
        Bitmap bitmap = PictureUtils.decodeSampledBitmapFromFile(photoFile.getPath(), 520, 520);

        View v = LayoutInflater.from(getActivity()).inflate(R.layout.dialog_suspect_image, null);
        ImageView photoView = (ImageView) v.findViewById(R.id.suspect_image);
        photoView.setImageBitmap(bitmap);
        return new AlertDialog.Builder(getActivity()).setView(photoView).create();
    }
}
