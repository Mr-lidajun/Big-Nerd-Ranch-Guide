package com.bignerdranch.android.beatbox;

/**
 * @author lidajun
 * @email solidajun@gmail.com
 * @date 16/7/27 22:41.
 * @desc: Sound管理类
 */
public class Sound {
    private String mAssetPath;
    private String mName;

    /**
     * mSoundId用了Integer类型而不是int。这样，在Sound的mSoundId没有值时可以设置其为null值
     */
    private Integer mSoundId;

    public Sound(String assetPath) {
        mAssetPath = assetPath;
        String[] components = assetPath.split("/");
        String filename = components[components.length - 1];
        mName = filename.replace(".wav", "");
    }

    public String getAssetPath() {
        return mAssetPath;
    }

    public String getName() {
        return mName;
    }

    public Integer getSoundId() {
        return mSoundId;
    }

    public void setSoundId(Integer soundId) {
        mSoundId = soundId;
    }
}
