package com.bignerdranch.android.geoquiz;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.ViewAnimationUtils;
import android.widget.Button;
import android.widget.TextView;

public class CheatActivity extends AppCompatActivity {

    private static final String EXTRA_ANSWER_IS_TRUE = "com.bignerdranch.android.geoquiz.answer_is_true";
    private static final String EXTRA_ANSWER_SHOWN = "com.bignerdranch.android.geoquiz.answer_shown";
    private static final String KEY_ANSWER_REVEALED = "answer_revealed";
    private static final String KEY_PRESS = "key_press";

    private boolean mAnswerIsTrue;
    private TextView mAnswerTextView;
    private Button mShowAnswer;

    // Variable to store boolean that says whether user saw answer or not.
    private boolean mAnswerPeeked;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cheat);

        mAnswerIsTrue = getIntent().getBooleanExtra(EXTRA_ANSWER_IS_TRUE, false);

        mAnswerTextView = (TextView) findViewById(R.id.answerTextView);
        TextView apiLevel = (TextView) findViewById(R.id.tv_api_level);
        apiLevel.setText("API level " + Build.VERSION.SDK_INT);
        mShowAnswer = (Button) findViewById(R.id.showAnswerButton);
        mShowAnswer.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {
                if (mAnswerIsTrue) {
                    mAnswerTextView.setText(R.string.true_button);
                } else {
                    mAnswerTextView.setText(R.string.false_button);
                }

                // If user clicks this variable records that the user has seen the answer.
                mAnswerPeeked = setAnswerShownResult(true);

                /*
                 * API级别过滤非常有用，可以让我们知道应用要用到的类在哪个API级别可用。
                 * 
                 * 如想查看ViewAnimationUtils类的哪些方法可用于API
                 * 16级，可按API级别过滤引用。在页面左边按包索引的类列表上方，找到API级别过滤框，目前它显示为API level:
                 * 21。展开下拉表单，选择数字16。一般而言，所有API
                 * 16级以后引入的方法都会被过滤掉，自动变为灰色。ViewAnimationUtils类是在API
                 * 21级引入的，所以，我们会看到一条该类无法用于API 16级的警示信息。
                 */
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    int cx = mShowAnswer.getWidth() / 2;
                    int cy = mShowAnswer.getHeight() / 2;
                    float radius = mShowAnswer.getWidth();
                    Animator anim =
                            ViewAnimationUtils.createCircularReveal(mShowAnswer, cx, cy, radius, 0);
                    anim.addListener(new AnimatorListenerAdapter() {
                        @Override public void onAnimationEnd(Animator animation) {
                            super.onAnimationEnd(animation);
                            mShowAnswer.setVisibility(View.INVISIBLE);
                        }
                    });
                    anim.start();
                } else {
                    mShowAnswer.setVisibility(View.INVISIBLE);
                }
            }
        });

        if (savedInstanceState != null) {
            setAnswerShownResult(savedInstanceState.getBoolean(KEY_ANSWER_REVEALED, false));
            mAnswerPeeked = savedInstanceState.getBoolean(KEY_PRESS, false);
            if (mAnswerPeeked) {
                if (mAnswerIsTrue) {
                    mAnswerTextView.setText(R.string.true_button);
                } else {
                    mAnswerTextView.setText(R.string.false_button);
                }
            }
        }
    }

    public static Intent newIntent(Context packageContext, boolean answerIsTrue) {
        Intent i = new Intent(packageContext, CheatActivity.class);
        i.putExtra(EXTRA_ANSWER_IS_TRUE, answerIsTrue);
        return i;
    }

    public static boolean wasAnswerShown(Intent reslut) {
        return reslut.getBooleanExtra(EXTRA_ANSWER_SHOWN, false);
    }

    private boolean setAnswerShownResult(boolean isAnswerShown) {
        Intent data = new Intent();
        data.putExtra(EXTRA_ANSWER_SHOWN, isAnswerShown);
        setResult(RESULT_OK, data);
        return isAnswerShown;
    }

    @Override protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean(KEY_ANSWER_REVEALED, mAnswerIsTrue);
        outState.putBoolean(KEY_PRESS, mAnswerPeeked);
    }
}
