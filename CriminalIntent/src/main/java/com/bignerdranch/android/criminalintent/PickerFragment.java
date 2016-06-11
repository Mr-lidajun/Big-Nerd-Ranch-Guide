package com.bignerdranch.android.criminalintent;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import java.util.Calendar;
import java.util.Date;

/**
 * @author lidajun
 * @email solidajun@gmail.com
 * @date 16/6/10 22:31.
 * @desc: 日期选择基类
 */
public abstract class PickerFragment extends DialogFragment {
    private static final String TAG = "PickerFragment";
    protected static final String ARG_DATE = "com.bignerdranch.android.criminalintent.date";
    public static final String EXTRA_DATE = "date";

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // 如果是以全屏activity的方式展现DatePickerFragment，那么getDialog返回为null
        if (getDialog() != null) {
            getDialog().requestWindowFeature(Window.FEATURE_NO_TITLE);// 去除对话框默认标题，否则会遮挡Ok按钮
        }

        final Calendar calendar = getCalendar();

        View view = getView(inflater, container);
        int pickerId = getPickerId();

        setDate(calendar, view, pickerId);

        setPickerButtonOnClickListener(calendar, view);
        return view;
    }
    
    @NonNull
    protected Calendar getCalendar() {
        Date date = (Date) getArguments().getSerializable(ARG_DATE);
        Calendar calendar = Calendar.getInstance();
        if (date != null) {
            calendar.setTime(date);
        }
        return calendar;
    }

    protected abstract View getView(LayoutInflater inflater, ViewGroup container);

    protected abstract int getPickerId();

    protected abstract void setPickerButtonOnClickListener(Calendar calendar, View view);

    protected abstract void setDate(Calendar calendar, View view, int pickerId);

    protected abstract Date getDate(Calendar calendar);

    protected void sendResult(int resultCode, Date date) {
        Intent intent = new Intent();
        intent.putExtra(EXTRA_DATE, date);

        if (getTargetFragment() == null) {
            getActivity().setResult(resultCode, intent);
            getActivity().finish();
            return;
        } else {
            getTargetFragment().onActivityResult(getTargetRequestCode(), resultCode, intent);
            dismiss();
        }
    }

}
