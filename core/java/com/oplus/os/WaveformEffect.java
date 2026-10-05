/*
 * Copyright (C) 2022 The Nameless-AOSP Project
 * Copyright (C) 2026 The XPerience Project
 * SPDX-License-Identifier: Apache-2.0
 */

package com.oplus.os;

import android.os.Parcel;
import android.os.Parcelable;

public class WaveformEffect implements Parcelable {

    private static final String TAG = "WaveformEffect";

    public static final Parcelable.Creator<WaveformEffect> CREATOR =
            new Parcelable.Creator<WaveformEffect>() {
        @Override
        public WaveformEffect createFromParcel(Parcel in) {
            return new WaveformEffect(in);
        }

        @Override
        public WaveformEffect[] newArray(int size) {
            return new WaveformEffect[size];
        }
    };

    private int mEffectType;
    private boolean mEffectLoop;
    private boolean mStrengthSettingEnabled;
    private int mEffectStrength;
    private boolean mAsynchronous;
    private boolean mIsRingtoneCustomized;
    private String mRingtoneFilePath;
    private int mRingtoneVibrateType;
    private int mUsageHint;

    private WaveformEffect() {
        mEffectType = -1;
        mEffectLoop = false;
        mStrengthSettingEnabled = false;
        mEffectStrength = 0;
        mAsynchronous = false;
        mIsRingtoneCustomized = false;
        mRingtoneFilePath = null;
        mRingtoneVibrateType = 0;
        mUsageHint = 0;
    }

    public int getEffectType() {
        return mEffectType;
    }

    public boolean getEffectLoop() {
        return mEffectLoop;
    }

    public boolean isStrengthSettingEnabled() {
        return mStrengthSettingEnabled;
    }

    public int getEffectStrength() {
        return mEffectStrength;
    }

    public boolean isAsynchronous() {
        return mAsynchronous;
    }

    public boolean isRingtoneCustomized() {
        return mIsRingtoneCustomized;
    }

    public String getRingtoneFilePath() {
        return mRingtoneFilePath;
    }

    public int getRingtoneVibrateType() {
        return mRingtoneVibrateType;
    }

    public int getUsageHint() {
        return mUsageHint;
    }

    public static class Builder {
        private int mEffectType;
        private boolean mEffectLoop;
        private boolean mStrengthSettingEnabled;
        private int mEffectStrength;
        private boolean mAsynchronous;
        private boolean mIsRingtoneCustomized;
        private String mRingtoneFilePath;
        private int mRingtoneVibrateType;
        private int mUsageHint;

        public Builder() {
            mEffectType = -1;
            mEffectLoop = false;
            mStrengthSettingEnabled = false;
            mEffectStrength = 0;
            mAsynchronous = false;
            mIsRingtoneCustomized = false;
            mRingtoneFilePath = null;
            mRingtoneVibrateType = 0;
            mUsageHint = 0;
        }

        public Builder(WaveformEffect effect) {
            if (effect != null) {
                mEffectType = effect.mEffectType;
                mEffectLoop = effect.mEffectLoop;
                mStrengthSettingEnabled = effect.mStrengthSettingEnabled;
                mEffectStrength = effect.mEffectStrength;
                mAsynchronous = effect.mAsynchronous;
                mIsRingtoneCustomized = effect.mIsRingtoneCustomized;
                mRingtoneFilePath = effect.mRingtoneFilePath;
                mRingtoneVibrateType = effect.mRingtoneVibrateType;
                mUsageHint = effect.mUsageHint;
            } else {
                mEffectType = -1;
                mEffectLoop = false;
                mStrengthSettingEnabled = false;
                mEffectStrength = 0;
                mAsynchronous = false;
                mIsRingtoneCustomized = false;
                mRingtoneFilePath = null;
                mRingtoneVibrateType = 0;
                mUsageHint = 0;
            }
        }

        public WaveformEffect build() {
            WaveformEffect effect = new WaveformEffect();
            effect.mEffectType = mEffectType;
            effect.mEffectLoop = mEffectLoop;
            effect.mStrengthSettingEnabled = mStrengthSettingEnabled;
            effect.mEffectStrength = mEffectStrength;
            effect.mAsynchronous = mAsynchronous;
            effect.mIsRingtoneCustomized = mIsRingtoneCustomized;
            effect.mRingtoneFilePath = mRingtoneFilePath;
            effect.mRingtoneVibrateType = mRingtoneVibrateType;
            effect.mUsageHint = mUsageHint;
            return effect;
        }

        public Builder setEffectType(int type) {
            mEffectType = type;
            return this;
        }

        public Builder setEffectLoop(boolean loop) {
            mEffectLoop = loop;
            return this;
        }

        public Builder setStrengthSettingEnabled(boolean enabled) {
            mStrengthSettingEnabled = enabled;
            return this;
        }

        public Builder setEffectStrength(int strength) {
            mEffectStrength = strength;
            return this;
        }

        public Builder setAsynchronous(boolean async) {
            mAsynchronous = async;
            return this;
        }

        public Builder setIsRingtoneCustomized(boolean customized) {
            mIsRingtoneCustomized = customized;
            return this;
        }

        public Builder setRingtoneFilePath(String path) {
            mRingtoneFilePath = path;
            return this;
        }

        public Builder setRingtoneVibrateType(int type) {
            mRingtoneVibrateType = type;
            return this;
        }

        public Builder setUsageHint(int hint) {
            mUsageHint = hint;
            return this;
        }
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(mEffectType);
        dest.writeBoolean(mEffectLoop);
        dest.writeBoolean(mStrengthSettingEnabled);
        dest.writeInt(mEffectStrength);
        dest.writeBoolean(mAsynchronous);
        dest.writeBoolean(mIsRingtoneCustomized);
        dest.writeString8(mRingtoneFilePath);
        dest.writeInt(mRingtoneVibrateType);
        dest.writeInt(mUsageHint);
    }

    private WaveformEffect(Parcel in) {
        mEffectType = in.readInt();
        mEffectLoop = in.readBoolean();
        mStrengthSettingEnabled = in.readBoolean();
        mEffectStrength = in.readInt();
        mAsynchronous = in.readBoolean();
        mIsRingtoneCustomized = in.readBoolean();
        mRingtoneFilePath = in.readString8();
        mRingtoneVibrateType = in.readInt();
        mUsageHint = in.readInt();
    }

    @Override
    public String toString() {
        return "WaveformEffect{"
                + "type=" + mEffectType
                + ", loop=" + mEffectLoop
                + ", strengthSettingEnabled=" + mStrengthSettingEnabled
                + ", strength=" + mEffectStrength
                + ", async=" + mAsynchronous
                + '}';
    }
}
