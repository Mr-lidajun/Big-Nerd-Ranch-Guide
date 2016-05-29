package com.bignerdranch.android.geoquiz;

/**
 * @author lidajun
 * @email solidajun@gmail.com
 * @date 16/5/29 10:47.
 * @desc: 题目类
 */
public class Question {
    private static final String TAG = "Question";

    private int mTextResId;
    private boolean mAnswerTrue;

    public Question(int textResId, boolean answerTrue) {
        mTextResId = textResId;
        mAnswerTrue = answerTrue;
    }

    public int getTextResId() {
        return mTextResId;
    }

    public void setTextResId(int textResId) {
        mTextResId = textResId;
    }

    public boolean isAnswerTrue() {
        return mAnswerTrue;
    }

    public void setAnswerTrue(boolean answerTrue) {
        mAnswerTrue = answerTrue;
    }
}
