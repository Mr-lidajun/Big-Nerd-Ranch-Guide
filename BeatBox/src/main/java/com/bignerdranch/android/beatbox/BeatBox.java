package com.bignerdranch.android.beatbox;

import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.content.res.AssetManager;
import android.media.AudioManager;
import android.media.SoundPool;
import android.util.Log;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * @author lidajun
 * @email solidajun@gmail.com
 * @date 16/7/27 22:31.
 * @desc: 资源管理类
 */
public class BeatBox {
    private static final String TAG = "BeatBox";

    private static final String SOUNDS_FOLDER = "sample_sounds";
    private static final int MAX_SOUNDS = 5;

    private AssetManager mAssets;

    private List<Sound> mSounds = new ArrayList<>();
    private SoundPool mSoundPool;

    public BeatBox(Context context) {
        mAssets = context.getAssets();
        // this old constructor is deprecated, but we need it for
        // compatibility
        mSoundPool = new SoundPool(MAX_SOUNDS, AudioManager.STREAM_MUSIC, 0);
        loadSounds();
    }

    /**
     * 播放音频
     * @param sound
     */
    public void play(Sound sound) {
        Integer soundId = sound.getSoundId();
        if (soundId == null) {
            return;
        }
        mSoundPool.play(sound.getSoundId(), 1.0f, 1.0f, 1, 0, 1.0f);
    }

    /**
     * 载入全部音频文件
     */
    private void loadSounds() {
        String[] soundNames = new String[0];
        try {
            soundNames = mAssets.list(SOUNDS_FOLDER);
            Log.i(TAG, "Found " + soundNames.length + " sounds");
        } catch (IOException ioe) {
            Log.e(TAG, "Could not list assets", ioe);
        }

        for (String filename : soundNames) {
            try {
                String assetPath = SOUNDS_FOLDER + "/" + filename;
                Sound sound = new Sound(assetPath);
                load(sound);
                mSounds.add(sound);
            } catch (IOException ioe) {
                Log.e(TAG, "Could not load sound " + filename, ioe);
            }
        }
    }

    /**
     * 加载音频
     * @param sound
     * @throws IOException
     * mSoundPool.load(...)方法可以把文件载入SoundPool待播，
     * 为了方便管理、重播或卸载音频文件，mSoundPool.load(...)方法会返回一个int型ID。
     *
     */
    private void load(Sound sound) throws IOException {
        AssetFileDescriptor afd = mAssets.openFd(sound.getAssetPath());
        int soundId = mSoundPool.load(afd, 1);
        sound.setSoundId(soundId);
    }

    /**
     * 释放音频
     */
    public void release() {
        mSoundPool.release();
    }

    public List<Sound> getSounds() {
        return mSounds;
    }
}
