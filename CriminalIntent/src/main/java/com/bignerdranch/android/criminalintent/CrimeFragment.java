package com.bignerdranch.android.criminalintent;

import android.app.Activity;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import java.util.Date;
import java.util.UUID;

/**
 * @author lidajun
 * @email solidajun@gmail.com
 * @date 16/6/3 21:58.
 * @desc: Crime详情页
 *
 * 12.5 挑战练习：按设备类型展现
 * 参考：My Solution to This Chapter (Buggy)
 * https://forums.bignerdranch.com/t/my-solution-to-this-chapter-buggy/7935
 *        初步分析需三大步骤。第一步，替换掉onCreateDialog方法，改用onCreateView方法来创建DatePickerFragment的视图
 *        。以这种方式创建DialogFragment的话，对话框界面上看不到title区域，同样没有放置按钮的空间。
 *        这需要我们自行在dialog_date.xml布局中创建OK按钮。
 *        有了DatePickerFragment视图，接下来就能以对话框或以在activity中内嵌的方式展现
 *        。第二步，我们创建SingleFragmentActivity子类。它的任务就是托管DatePickerFragment。
 *        选择这种方式展现DatePickerFragment
 *        ，就要使用startActivityForResult机制回传日期给CrimeFragment
 *        。在DatePickerFragment中，如果目标fragment不存在，就调用托管activity的setResult(int,
 *        intent)方法回传日期给CrimeFragment。
 *        最后，修改CriminalIntent应用：如果是手机设备，就以全屏activity的方式展现DatePickerFragment
 *        ；如果是平板设备，就以对话框的方式展现DatePickerFragment。想知道如何按设备屏幕大小优化应用，请提前学习第17章的相关内容。
 */
public class CrimeFragment extends Fragment {
    private static final String TAG = "CrimeFragment";
    private static final String ARG_CRIME_ID = "crime_id";
    private static final String DIALOG_DATE = "DialogDate";
    private static final String DIALOG_TIME = "DialogTime";
    private static final int REQUEST_DATE = 0;
    private static final int REQUEST_TIME = 1;

    private Crime mCrime;
    private EditText mTitleField;
    private Button mDateButton;
    private Button mTimeButton;
    private CheckBox mSolvedCheckBox;

    public static CrimeFragment newInstance(UUID crimeId) {
        Bundle args = new Bundle();
        args.putSerializable(ARG_CRIME_ID, crimeId);

        CrimeFragment fragment = new CrimeFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        UUID crimeId = (UUID) getArguments().getSerializable(ARG_CRIME_ID);
        mCrime = CrimeLab.get(getActivity()).getCrime(crimeId);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_crime, container, false);
        mTitleField = (EditText) v.findViewById(R.id.crime_title);
        mTitleField.setText(mCrime.getTitle());
        mTitleField.addTextChangedListener(new TextWatcher() {

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                // This space intentionally left blank
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                mCrime.setTitle(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {
                // This one too
            }
        });

        mDateButton = (Button) v.findViewById(R.id.crime_date);
        updateDate();
        mDateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                /*
                 * 横屏，模拟平板设备，以对话框的方式展现DatePickerFragment
                 */
                if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
                    /*
                     * why not getSupportFragmentManager()?
                     * cstewart（作者）答：
                     *
                     * I know the API is confusing, but you're still using support
                     * fragments here. Support fragments don't have a
                     * "getSupportFragmentManager()" method. The
                     * "getFragmentManager()" method from within a support fragment
                     * returns the support library version of the fragment manager.
                     *
                     * Here are the docs for that method:
                     * https://developer.android.com/reference/android/support/v4/app/Fragment.html#getFragmentManager()
                     */
                    FragmentManager manager = getFragmentManager();
                    DatePickerFragment dateDialog = DatePickerFragment.newInstance(mCrime.getDate());
                    dateDialog.setTargetFragment(CrimeFragment.this, REQUEST_DATE);
                    dateDialog.show(manager, DIALOG_DATE);
                } else if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
                    /*
                     *  竖屏，模拟手机设备，以全屏activity的方式展现DatePickerFragment
                     */
                    Intent intent = DatePickerActivity.newInstance(getActivity(), mCrime.getDate());
                    startActivityForResult(intent, REQUEST_DATE);
                }
            }
        });

        mTimeButton = (Button) v.findViewById(R.id.crime_time);
        updateTime();
        mTimeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
                    /*
                     * 横屏，模拟平板设备，以对话框的方式展现DatePickerFragment
                     */
                    FragmentManager manager = getFragmentManager();
                    TimePickerFragment timeDialog = TimePickerFragment.newInstance(mCrime.getDate());
                    timeDialog.setTargetFragment(CrimeFragment.this, REQUEST_DATE);
                    timeDialog.show(manager, DIALOG_TIME);
                } else if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
                    /*
                     * 竖屏，模拟手机设备，以全屏activity的方式展现DatePickerFragment
                     */
                    Intent intent = TimePickerActivity.newInstance(getActivity(), mCrime.getDate());
                    startActivityForResult(intent, REQUEST_DATE);
                }
            }
        });

        mSolvedCheckBox = (CheckBox) v.findViewById(R.id.crime_solved);
        mSolvedCheckBox.setChecked(mCrime.isSolved());
        mSolvedCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                // Set the crime's solved property
                mCrime.setSolved(isChecked);
            }
        });
        return v;
    }

    public void returnResult() {
        getActivity().setResult(Activity.RESULT_OK, null);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode != Activity.RESULT_OK) {
            return;
        }

        if (requestCode == REQUEST_DATE) {
            Date date = (Date) data.getSerializableExtra(DatePickerFragment.EXTRA_DATE);
            mCrime.setDate(date);
            updateDate();
            updateTime();
        }
    }

    private void updateDate() {
        mDateButton.setText(mCrime.getFormattedDate());
    }

    private void updateTime() {
        mTimeButton.setText(mCrime.getFormattedTime());
    }
}
