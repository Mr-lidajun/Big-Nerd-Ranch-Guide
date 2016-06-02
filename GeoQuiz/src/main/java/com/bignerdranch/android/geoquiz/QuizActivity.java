package com.bignerdranch.android.geoquiz;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;
import java.util.ArrayList;

public class QuizActivity extends AppCompatActivity {
    private static final String TAG = "QuizActivity";
    private static final String KEY_INDEX = "index";
    private static final int REQUEST_CODE_CHEAT = 0;
    private static final String KEY_CLEVER_CHEAT = "clever_cheater";

    // This is the key to store the array of cheated questions on the bundle of instance state
    private static final String KEY_CHEATED_QUESTIONS = "cheated_questions";

    private Button mTrueButton;
    private Button mFalseButton;
    private ImageButton mPreviousButton;
    private ImageButton mNextButton;
    private Button mCheatButton;
    private TextView mQuestionTextView;

    private Question[] mQuestionBank = new Question[] {
            new Question(R.string.question_oceans, true),
            new Question(R.string.question_mideast, false),
            new Question(R.string.question_africa, false),
            new Question(R.string.question_americas, true),
            new Question(R.string.question_asia, true)
    };

    private int mCurrentIndex = 0;

    /**
     * 参考论坛 https://forums.bignerdranch.com/t/my-solution-to-challenge-3/8692
     * This is a list array of integers where all cheated question's index will be stored
     */
    private ArrayList<Integer> mCheatedQuestions = new ArrayList<>();

    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate(Bundle) called");
        setContentView(R.layout.activity_quiz);

        mQuestionTextView = (TextView) findViewById(R.id.question_text_view);
        mQuestionTextView.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {
                mCurrentIndex = (mCurrentIndex + 1) % mQuestionBank.length;
                updateQuestion();
            }
        });

        mPreviousButton = (ImageButton) findViewById(R.id.previous_button);
        mPreviousButton.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {
                mCurrentIndex = (mCurrentIndex - 1 < 0 ? 0 : mCurrentIndex - 1) % mQuestionBank.length;
                updateQuestion();
            }
        });

        mNextButton = (ImageButton) findViewById(R.id.next_button);
        mNextButton.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {
                mCurrentIndex = (mCurrentIndex + 1) % mQuestionBank.length;
                updateQuestion();
            }
        });

        mTrueButton = (Button) findViewById(R.id.true_button);
        mTrueButton.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {
                checkAnswer(true);
            }
        });
        mFalseButton = (Button) findViewById(R.id.false_button);
        mFalseButton.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {
                checkAnswer(false);
            }
        });

        mCheatButton = (Button) findViewById(R.id.cheat_button);
        mCheatButton.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {
                boolean answerIsTrue = mQuestionBank[mCurrentIndex].isAnswerTrue();
                Intent i = CheatActivity.newIntent(QuizActivity.this, answerIsTrue);
                startActivityForResult(i, REQUEST_CODE_CHEAT);
            }
        });

        if (savedInstanceState != null) {
            mCurrentIndex = savedInstanceState.getInt(KEY_INDEX);
            //Retrieve the cheated questions array list
            mCheatedQuestions = savedInstanceState.getIntegerArrayList(KEY_CHEATED_QUESTIONS);
            
            //this line is no longer needed
            //mIsCheater = savedInstanceState.getBoolean(KEY_CLEVER_CHEAT, false);
        }

        updateQuestion();


    }

    @Override protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode != RESULT_OK) {
            return;
        }

        if (requestCode == REQUEST_CODE_CHEAT) {
            if (data == null) {
                return;
            }

            /*
             * Here we add the current question index if the user cheated on it but not add it if it was cheated previously
             * to avoid index duplication
             */
            if (CheatActivity.wasAnswerShown(data) && !isCheater()) {
                mCheatedQuestions.add(mCurrentIndex);
            }
        }
    }

    /**
     * 更新题目
     */
    private void updateQuestion() {
        int question = mQuestionBank[mCurrentIndex].getTextResId();
        mQuestionTextView.setText(question);
    }

    private void checkAnswer(boolean userPressedTrue) {
        boolean answerIsTrue = mQuestionBank[mCurrentIndex].isAnswerTrue();

        int messageResId = 0;

        if (isCheater()) {
            messageResId = R.string.judgment_toast;
        } else {
            if (userPressedTrue == answerIsTrue) {
                messageResId = R.string.correct_toast;
            } else {
                messageResId = R.string.incorrect_toast;
            }
        }

        Toast.makeText(this, messageResId, Toast.LENGTH_SHORT).show();
    }

    /**
     * Determine if user cheated on the current question.
     * @return
     */
    private boolean isCheater() {
        return mCheatedQuestions.contains(mCurrentIndex);
    }

    @Override protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        Log.i(TAG, "onSaveInstanceState");
        outState.putInt(KEY_INDEX, mCurrentIndex);

        // We store the cheated questions array list when the lifecycle change
        outState.putIntegerArrayList(KEY_CHEATED_QUESTIONS, mCheatedQuestions);
    }

    @Override protected void onStart() {
        super.onStart();
        Log.d(TAG, "onStart() called");
    }

    @Override protected void onPause() {
        super.onPause();
        Log.d(TAG, "onPause() called");
    }

    @Override protected void onResume() {
        super.onResume();
        Log.d(TAG, "onResume() called");
    }

    @Override protected void onStop() {
        super.onStop();
        Log.d(TAG, "onStop() called");
    }

    @Override protected void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestroy() called");
    }
}
