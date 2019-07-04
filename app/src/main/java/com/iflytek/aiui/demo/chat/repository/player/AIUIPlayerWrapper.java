package com.iflytek.aiui.demo.chat.repository.player;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.content.Context;

import com.iflytek.aiui.demo.chat.ui.common.SingleLiveEvent;
import com.iflytek.aiui.player.common.player.MetaItem;
import com.iflytek.aiui.player.core.AIUIPlayer;
import com.iflytek.aiui.player.core.PlayState;
import com.iflytek.aiui.player.core.PlayerListener;

import org.jetbrains.annotations.NotNull;
import org.json.JSONArray;
import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * 播放器
 */

@Singleton
public class AIUIPlayerWrapper {

    private AIUIPlayer mPlayer;
    private boolean mManualPause = false;
    //当前播放状态
    private MutableLiveData<PlayerState> mState = new MutableLiveData<>();
    //错误信息提示
    private MutableLiveData<String> mError = new SingleLiveEvent<>();
    //是否正在播放
    private boolean mActive = false;

    @Inject
    public AIUIPlayerWrapper(Context context) {
        this.mPlayer = new AIUIPlayer(context);

        mPlayer.initialize();
        //根据播放进度和状态通知外部更新
        this.mPlayer.addListener(new PlayerListener() {
            @Override
            public void onError(int error, @NotNull String info) {
                mError.postValue(info);
            }

            @Override
            public void onPlayerReady() {

            }

            @Override
            public void onStateChange(PlayState state) {
                boolean playing = state == PlayState.PLAYING;
                if(state == PlayState.PLAYING || state == PlayState.LOADING) {
                    mActive = true;
                }

                MetaItem currentPlay = mPlayer.getCurrentPlay();
                mState.postValue(new PlayerState(mActive, playing, state == PlayState.ERROR, currentPlay == null? "" : currentPlay.getTitle()));
            }

            @Override
            public void onMediaChange(MetaItem item) {
                mState.postValue(new PlayerState(mActive, true, false, mPlayer.getCurrentPlay().getTitle()));
            }

            @Override
            public void onPlayerRelease() {

            }
        });
    }

    /**
     * 播放歌曲列表
     * @param data 歌曲列表
     */

    public boolean playList(JSONArray data, String service) {
        mPlayer.reset();
        return mPlayer.play(data, service, false);
    }

    /**
     * 是否有可播放的音频
     */
    public boolean anyAvailablePlay(JSONArray data, String service) {
        return mPlayer.anyAvailablePlay(data, service);
    }

    /**
     * 获取播放器当前状态
     * @return 当前状态
     */
    public LiveData<PlayerState> getLiveState() {
        return mState;
    }

    /**
     * 获取播放器当前状态
     * @return 当前状态
     */
    public LiveData<String> getError() {
        return mError;
    }

    //自动暂停 唤醒后自动暂停或按住说话自动暂停
    public MetaItem autoPause() {
        return pause();
    }

    public MetaItem manualPause() {
        mManualPause = true;
        return pause();
    }

    public void resumeIfNeed() {
        if(!mManualPause) {
            play();
        }
    }

    private MetaItem pause() {
        mPlayer.pause();
        return mPlayer.getCurrentPlay();
    }

    public MetaItem play() {
        mPlayer.resume();
        return mPlayer.getCurrentPlay();
    }

    public MetaItem next() {
        if(mPlayer.next()) {
            return mPlayer.getCurrentPlay();
        } else {
            return null;
        }
    }

    public MetaItem prev() {
        if(mPlayer.previous()) {
            return mPlayer.getCurrentPlay();
        } else {
            return null;
        }
    }

    public void stop() {
        mActive = false;
        pause();
    }

    public MetaItem currentMedia() {
        return mPlayer.getCurrentPlay();
    }

    public boolean isActive() {
        return mActive;
    }
}

