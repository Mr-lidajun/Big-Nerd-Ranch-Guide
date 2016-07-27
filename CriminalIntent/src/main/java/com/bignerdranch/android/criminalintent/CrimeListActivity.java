package com.bignerdranch.android.criminalintent;

import android.content.Context;
import android.content.Intent;
import android.support.v4.app.Fragment;
import android.view.View;

/**
 * @author lidajun
 * @email solidajun@gmail.com
 * @date 16/6/5 13:48.
 * @desc: Crime列表页
 */
public class CrimeListActivity extends SingleFragmentActivity implements CrimeListFragment.Callbacks,
        CrimeFragment.Callbacks {
    private static final String TAG = "CrimeListActivity";
    private static final String EXTRA_SUBTITLE_VISIBLE = "crimelist_subtitle_visible";
    private static final int REQUEST_CRIME = 1;

    public static Intent newIntent(Context packageContext, boolean subtitleVisible) {
        Intent intent = new Intent(packageContext, CrimeListActivity.class);
        intent.putExtra(EXTRA_SUBTITLE_VISIBLE, subtitleVisible);
        return intent;
    }

    @Override
    protected Fragment createFragment() {
        boolean subtitleVisible = getIntent().getBooleanExtra(EXTRA_SUBTITLE_VISIBLE, false);
        return CrimeListFragment.newIntent(subtitleVisible);
    }

    @Override
    protected int getLayoutResId() {
        return R.layout.activity_masterdetail;
    }

    @Override
    public void onCrimeSelected(Crime crime) {
        View view = findViewById(R.id.detail_fragment_container);
        if (view == null) {
            Intent intent = CrimePagerActivity.newIntent(this, crime.getId(), false);
            startActivityForResult(intent, REQUEST_CRIME);
        } else {
            if (view.getVisibility() == View.GONE) {
                view.setVisibility(View.VISIBLE);
            }
            CrimeFragment newDetail = CrimeFragment.newInstance(crime.getId());
            getSupportFragmentManager().beginTransaction().replace(R.id.detail_fragment_container, newDetail).commit();
        }
    }

    @Override
    public void onCrimeUpdated(Crime crime) {
        CrimeListFragment listFragment = (CrimeListFragment) getSupportFragmentManager().findFragmentById(
                R.id.fragment_container);
        listFragment.updateUI();
    }

    @Override
    public void onCrimeDeleted(Crime crime) {
        CrimeFragment newDetail = CrimeFragment.newInstance(crime.getId());
        getSupportFragmentManager().beginTransaction().replace(R.id.detail_fragment_container, newDetail).commit();

        CrimeListFragment listFragment = (CrimeListFragment) getSupportFragmentManager().findFragmentById(
                R.id.fragment_container);
        listFragment.setDeleteCrime(true);
    }
}
