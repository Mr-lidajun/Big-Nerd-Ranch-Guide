package com.bignerdranch.android.criminalintent;

import java.util.Date;

import android.content.Context;
import android.content.Intent;
import android.support.v4.app.Fragment;

public class TimePickerActivity extends SingleFragmentActivity {
    private static final String EXTRA_DATE = "TimePickerActivity.date";

    public static Intent newInstance(Context packagerContext, Date date) {
        Intent intent = new Intent(packagerContext, TimePickerActivity.class);
        intent.putExtra(EXTRA_DATE, date);
        return intent;
    }

    @Override
    protected Fragment createFragment() {
        Date date = (Date) getIntent().getSerializableExtra(EXTRA_DATE);
        return TimePickerFragment.newInstance(date);
    }

}
