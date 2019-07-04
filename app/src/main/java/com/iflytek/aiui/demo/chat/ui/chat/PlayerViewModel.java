package com.iflytek.aiui.demo.chat.ui.chat;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.ViewModel;
import android.content.Context;
import android.media.AudioManager;

import com.iflytek.aiui.demo.chat.repository.player.AIUIPlayerWrapper;
import com.iflytek.aiui.demo.chat.repository.player.PlayerState;
import com.iflytek.aiui.demo.chat.repository.TTSManager;
import com.iflytek.aiui.demo.chat.ui.common.SingleLiveEvent;
import com.iflytek.aiui.player.common.player.MetaItem;

import org.json.JSONArray;


import javax.inject.Inject;

/**
 * 播放器ViewModel
 * 获得播放器状态
 * 控制播放器播放，停止
 */

public class PlayerViewModel extends ViewModel {
    private AudioManager mAudioManager;
    private AIUIPlayerWrapper mPlayer;
    private TTSManager mTTSManager;
    private SingleLiveEvent<String> mPlayerTips = new SingleLiveEvent<>();

    @Inject
    public PlayerViewModel(Context context, AIUIPlayerWrapper player, TTSManager ttsManager) {
        mPlayer = player;
        mTTSManager = ttsManager;
        mAudioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);


    }

    public LiveData<PlayerState> getPlayState() {
        return mPlayer.getLiveState();
    }

    public LiveData<String> getPlayerTips() {
        return mPlayerTips;
    }

    public LiveData<String> getLiveError() {
        return mPlayer.getError();
    }

    public boolean playList(JSONArray list, String service) {
        return mPlayer.playList(list, service);
    }

    public boolean anyAvailablePlay(JSONArray data, String service) {
        return mPlayer.anyAvailablePlay(data, service);
    }

    public MetaItem play() {
        return mPlayer.play();
    }

    //自动暂停 唤醒后自动暂停或按住说话自动暂停
    public MetaItem autoPause() {
        return mPlayer.autoPause();
    }

    public MetaItem manualPause() {
        return mPlayer.manualPause();
    }

    public void resumeIfNeed() {
        mPlayer.resumeIfNeed();
    }

    public MetaItem prev() {
        MetaItem prev = mPlayer.prev();
        if(prev == null) {
           mPlayerTips.setValue("当前已经是播放列表第一项");
        }

        return prev;
    }

    public MetaItem next() {
        MetaItem next = mPlayer.next();
        if(next == null) {
            mPlayerTips.setValue("当前已经是播放列表最后一项");
        }

        return next;
    }

    public MetaItem current() {
        return mPlayer.currentMedia();
    }


    public boolean stop() {
        mPlayer.stop();
        return true;
    }

    public boolean isActive() {
       return mPlayer.isActive();
    }


    public void volumeMinus() {
        mAudioManager.adjustStreamVolume(AudioManager.STREAM_MUSIC, AudioManager.ADJUST_LOWER,
                AudioManager.FLAG_SHOW_UI);
    }

    public void volumePlus() {
        mAudioManager.adjustStreamVolume(AudioManager.STREAM_MUSIC, AudioManager.ADJUST_RAISE,
                AudioManager.FLAG_SHOW_UI);
    }

    public void volumePercent(float percent) {
        mAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC,
                (int) (mAudioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC) * percent), AudioManager.FLAG_SHOW_UI);
    }

    public void startTTS(String text, Runnable callback, boolean resume) {
        mTTSManager.startTTS(text, callback, resume);
    }

    public void pauseTTS() {
        mTTSManager.pauseTTS();
    }
}
