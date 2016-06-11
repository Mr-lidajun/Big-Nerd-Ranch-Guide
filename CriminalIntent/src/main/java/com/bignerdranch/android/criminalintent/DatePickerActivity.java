package com.bignerdranch.android.criminalintent;

import java.util.Date;

import android.content.Context;
import android.content.Intent;
import android.support.v4.app.Fragment;

public class DatePickerActivity extends SingleFragmentActivity {
    private static final String EXTRA_DATE = "DatePickerActivity.date";

    public static Intent newInstance(Context packagerContext, Date date) {
        Intent intent = new Intent(packagerContext, DatePickerActivity.class);
        intent.putExtra(EXTRA_DATE, date);
        return intent;
    }

    @Override
    protected Fragment createFragment() {
        Date date = (Date) getIntent().getSerializableExtra(EXTRA_DATE);
        return DatePickerFragment.newInstance(date);
    }

}
