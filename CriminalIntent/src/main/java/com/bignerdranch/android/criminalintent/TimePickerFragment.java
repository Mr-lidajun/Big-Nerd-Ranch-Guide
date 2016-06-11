package com.bignerdranch.android.criminalintent;

import android.app.Activity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TimePicker;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

/**
 * @author lidajun
 * @email solidajun@gmail.com
 * @date 16/6/10 15:31.
 * @desc: 时间选择对话框
 */
public class TimePickerFragment extends PickerFragment {
    private static final String TAG = "TimePickerFragment";
    private TimePicker mTimePicker;
    private Button mTimePickerButton;

    public static TimePickerFragment newInstance(Date date) {
        Bundle args = new Bundle();
        args.putSerializable(ARG_DATE, date);

        TimePickerFragment fragment = new TimePickerFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    protected View getView(LayoutInflater inflater, ViewGroup container) {
        return inflater.inflate(R.layout.fragment_time_picker, container, false);
    }

    @Override
    protected int getPickerId() {
        return R.id.time_picker;
    }

    @Override
    protected void setPickerButtonOnClickListener(final Calendar calendar, View view) {
        mTimePickerButton = (Button) view.findViewById(R.id.time_picker_button);
        mTimePickerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Date date = getDate(calendar);
                sendResult(Activity.RESULT_OK, date);
            }
        });
    }

    @Override
    protected void setDate(Calendar calendar, View view, int pickerId) {
        int hourOfDay = calendar.get(Calendar.HOUR_OF_DAY);
        int minute = calendar.get(Calendar.MINUTE);

        mTimePicker = (TimePicker) view.findViewById(pickerId);
        mTimePicker.setIs24HourView(false);
        mTimePicker.setCurrentHour(hourOfDay);
        mTimePicker.setCurrentMinute(minute);
    }

    @Override
    protected Date getDate(Calendar calendar) {
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int dayOfMonth = calendar.get(Calendar.DAY_OF_MONTH);

        int hourOfDay = mTimePicker.getCurrentHour();
        int minute = mTimePicker.getCurrentMinute();
        return new GregorianCalendar(year, month, dayOfMonth, hourOfDay, minute).getTime();
    }

}
