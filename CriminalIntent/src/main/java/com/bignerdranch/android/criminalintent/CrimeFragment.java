package com.bignerdranch.android.criminalintent;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
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
    public static final String EXTRA_DELETE_CRIME = "delete_crime";
    private static final int REQUEST_DATE = 0;
    private static final int REQUEST_TIME = 1;
    private static final int REQUEST_CONTACT = 2;

    private Crime mCrime;
    private EditText mTitleField;
    private Button mDateButton;
    private Button mTimeButton;
    private CheckBox mSolvedCheckBox;
    /**
     * 发送消息
     */
    private Button mReportButton;
    /**
     * 选择联系人
     */
    private Button mSuspectButton;

    /**
     * 托管activity需要fragment实例时，转而调用newsInstance()方法，而非直接调用其构造方法。
     * 而且，为满足fragment创建argument的要求，activity可传入任何需要的参数给newInstance()方法。
     * @param crimeId
     * @return
     */
    public static CrimeFragment newInstance(UUID crimeId) {
        Bundle args = new Bundle();
        args.putSerializable(ARG_CRIME_ID, crimeId);

        /*
         * 深入学习: 为何要用fragment argument?
         * fragment argument的使用还是有点复杂。为什么不直接在CrimeFragment里创建一个实例变量呢？
         *
         * 创建实例变量的方式并不可靠。因为，在操作系统重建fragment时，设备配置发生改变时，用户暂时离开当前应用时，甚至操作系统按需回收内存时，
         * 任何实例变量都不复存在了。尤其是内存不够，操作系统强制杀掉应用的情况，可以说是无人能挡。
         *
         * 因此，可以说，fragment argument就是为应对上述场景而生。
         *
         * 我们还有另一个方法应对上述场景，那就是使用实例状态保存机制。具体来说，就是将crime ID赋值给实例变量，然后在onSaveInstanceState(Bundle)方法保存下来。
         * 要用时，从onCreate(Bundle)方法中的Bundle中取回
         *
         * 然而，这种解决方案维护成本高。举例来说，若干年后，你要修改fragment代码添加其他argument，很可能会忘记在onSaveInstanceState(Bundle)方法里保存新增argument。
         *
         * Android开发者更喜欢fragment argument这个解决方案，因为这种方式很清楚直白。若干年后，再回头修改老代码时，只需一眼就能知道，crime ID是以argument保存和传递使用的。
         * 即使要新增argument，也会记得使用argument bundle保存它。
         */
        CrimeFragment fragment = new CrimeFragment();
        fragment.setArguments(args);// 附加argument给fragment
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        // 从argument中获取crime ID
        UUID crimeId = (UUID) getArguments().getSerializable(ARG_CRIME_ID);
        mCrime = CrimeLab.get(getActivity()).getCrime(crimeId);
    }

    @Override
    public void onPause() {
        super.onPause();
        CrimeLab.get(getActivity()).updateCrime(mCrime);
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

        mReportButton = (Button) v.findViewById(R.id.crime_report);
        mReportButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(Intent.ACTION_SEND);
                i.setType("text/plain");
                i.putExtra(Intent.EXTRA_TEXT, getCrimeReport());
                i.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.crime_report_subject));
                // 使用选择器（每次都显示activity选择器）
                i = Intent.createChooser(i, getString(R.string.send_report));
                startActivity(i);
            }
        });

        // 启动联系人应用
        final Intent pickContact = new Intent(Intent.ACTION_PICK, ContactsContract.Contacts.CONTENT_URI);
        mSuspectButton = (Button) v.findViewById(R.id.crime_suspect);
        mSuspectButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivityForResult(pickContact, REQUEST_CONTACT);
            }
        });

        if (!TextUtils.isEmpty(mCrime.getSuspect())) {
            mSuspectButton.setText(mCrime.getSuspect());
        }

        // 检查可响应任务的activity (如果不做检查，一旦操作系统找不到匹配的activity，应用就会崩溃)
        // flag标志MATCH_DEFAULT_ONLY限定只搜索带CATEGORY_DEFAULT标志的activity
        PackageManager packageManager = getActivity().getPackageManager();
        if (packageManager.resolveActivity(pickContact, PackageManager.MATCH_DEFAULT_ONLY) == null) {
            mSuspectButton.setEnabled(false);
        }

        return v;
    }

    public void returnResult() {
        getActivity().setResult(Activity.RESULT_OK, null);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.fragment_crime, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_item_delete_crime:
                CrimeLab.get(getActivity()).deleteCrime(mCrime);
                setResult();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
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
        } else if (requestCode == REQUEST_CONTACT && data != null) {// 获取联系人姓名（内容提供者）
            Uri contactUri = data.getData();
            // Specify which fields you want your query to return
            // values for.
            String[] queryFields = new String[]{
                    ContactsContract.Contacts.DISPLAY_NAME
            };

            // Perform your query - the contactUri is like a "where"
            // clause here
            Cursor c = getActivity().getContentResolver()
                    .query(contactUri, queryFields, null, null, null);

            try {
                // Double-check that you actually got results
                if (c.getCount() == 0) {
                    return;
                }

                // Pull out the first column of the first row of data -
                // that is your suspect's name;
                c.moveToFirst();
                String suspect = c.getString(0);
                mCrime.setSuspect(suspect);
                mSuspectButton.setText(suspect);
            } finally {
                c.close();
            }
        }
    }

    private void updateDate() {
        mDateButton.setText(mCrime.getFormattedDate());
    }

    private void updateTime() {
        mTimeButton.setText(mCrime.getFormattedTime());
    }

    private void setResult() {
        Intent intent = new Intent();
        intent.putExtra(EXTRA_DELETE_CRIME, true);
        getActivity().setResult(Activity.RESULT_OK, intent);
        getActivity().finish();
    }

    /**
     * 创建四段字符串信息，并返回拼接完整的消息
     * @return
     */
    private String getCrimeReport() {
        String solvedString = null;
        if (mCrime.isSolved()) {
            solvedString = getString(R.string.crime_report_solved);
        } else {
            solvedString = getString(R.string.crime_report_unsolved);
        }
        
        String dataString = mCrime.getFormattedDate();
        
        String suspect = mCrime.getSuspect();
        if (TextUtils.isEmpty(suspect)) {
            suspect = getString(R.string.crime_report_no_suspect);
        } else {
            suspect = getString(R.string.crime_report_suspect, suspect);
        }
        
        String report = getString(R.string.crime_report, mCrime.getTitle(), dataString, solvedString, suspect);
        return report;
    }
}
