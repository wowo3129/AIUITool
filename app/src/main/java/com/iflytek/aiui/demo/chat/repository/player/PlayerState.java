package com.iflytek.aiui.demo.chat.repository.player;

/**
 * 播放状态
 */
public class PlayerState {
    public boolean active;
    public boolean playing;
    public boolean error;
    //当前播放内容的标题信息
    public String info;

    public PlayerState(boolean active, boolean playing, boolean error, String info) {
        this.active = active;
        this.playing = playing;
        this.info = info;
        this.error = error;
    }
}
