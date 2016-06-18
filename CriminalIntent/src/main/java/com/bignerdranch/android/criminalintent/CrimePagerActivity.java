package com.bignerdranch.android.criminalintent;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import java.util.List;
import java.util.UUID;

public class CrimePagerActivity extends AppCompatActivity {
    private static final String EXTRA_CRIME_ID = "com.bignerdranch.android.criminalintent.crime_id";
    private static final String EXTRA_SUBTITLE_VISIBLE = "crimepager_subtitle_visible";

    private ViewPager mViewPager;
    private List<Crime> mCrimes;

    public static Intent newIntent(Context packageContext, UUID crimeId, boolean subtitleVisible) {
        Intent intent = new Intent(packageContext, CrimePagerActivity.class);
        intent.putExtra(EXTRA_CRIME_ID, crimeId);
        intent.putExtra(EXTRA_SUBTITLE_VISIBLE, subtitleVisible);
        return intent;
    }

    /**
     * 实现层级导航带来的问题：
     * 导航回退到的目标activity会被完全重建。既然父activity是全新的activity，实例变量值以及保存的实例状态显然会彻底丢失。
     *
     * 方案二：
     * 启动CrimePagerActivity时，把子标题状态作为extra信息传给它。然后，在CrimePagerActivity中覆盖getParentActivityIntent()方法，
     * 用附带了extra信息的intent重建CrimeListActivity。这需要CrimePagerActivity类知道父类工作机制的细节。
     *
     * 参考：
     * Question about the Up button and the loss of state
     * https://forums.bignerdranch.com/t/question-about-the-up-button-and-the-loss-of-state/7714
     * @return
     */
    @Nullable
    @Override
    public Intent getParentActivityIntent() {
        boolean subtitleVisible = getIntent().getBooleanExtra(EXTRA_SUBTITLE_VISIBLE, false);
        Intent intent = CrimeListActivity.newIntent(this, subtitleVisible);
        return intent;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_crime_pager);

        mViewPager = (ViewPager) findViewById(R.id.activity_crime_pager_view_pager);

        mCrimes = CrimeLab.get(this).getCrimes();
        FragmentManager fragmentManager = getSupportFragmentManager();
        /*
         * FragmentStatePagerAdapter和FragmentPagerAdapter的区别
         * 唯一的区别：卸载不再需要的fragment时，各自采用的处理方式有所不同
         * FragmentStatePagerAdapter：会销毁不需要的fragment。事务提交后，fragment会被彻底移除 remove(Fragment)
         *         类名中的"state"表明：在销毁fragment时，可在onSaveInstanceState(Bundle)方法中保存fragment的Bundle信息。
         *         用户切换回来时，保存的实例状态可用来恢复生成新的fragment
         * FragmentPagerAdapter：只是销毁了fragment的视图，fragment实例还保留在fragmentManager中，创建的fragment永远不会被销毁，比较占用内存
         */
        mViewPager.setAdapter(new FragmentStatePagerAdapter(fragmentManager) {
            @Override
            public Fragment getItem(int position) {
                Crime crime = mCrimes.get(position);
                return CrimeFragment.newInstance(crime.getId());
            }

            @Override
            public int getCount() {
                return mCrimes.size();
            }
        });

        UUID crimeId = (UUID) getIntent().getSerializableExtra(EXTRA_CRIME_ID);
        for (int i = 0; i < mCrimes.size(); i++) {
            if (mCrimes.get(i).getId().equals(crimeId)) {
                mViewPager.setCurrentItem(i);
                break;
            }
        }
    }
}
