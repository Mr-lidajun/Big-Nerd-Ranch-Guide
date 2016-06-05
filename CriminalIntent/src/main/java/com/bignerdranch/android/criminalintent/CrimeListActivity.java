package com.bignerdranch.android.criminalintent;

import android.support.v4.app.Fragment;

/**
 * @author lidajun
 * @email solidajun@gmail.com
 * @date 16/6/5 13:48.
 * @desc: Crime列表页
 */
public class CrimeListActivity extends SingleFragmentActivity {
    private static final String TAG = "CrimeListActivity";

    @Override
    protected Fragment createFragment() {
        return new CrimeListFragment();
    }
}
