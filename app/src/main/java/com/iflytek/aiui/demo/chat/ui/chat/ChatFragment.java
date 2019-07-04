package com.iflytek.aiui.demo.chat.ui.chat;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.arch.lifecycle.Observer;
import android.arch.lifecycle.Transformations;
import android.arch.lifecycle.ViewModelProvider;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Context;
import android.databinding.BindingMethod;
import android.databinding.BindingMethods;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.os.IBinder;
import android.os.PersistableBundle;
import android.support.annotation.CallSuper;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.SwipeDismissBehavior;
import android.support.v4.app.Fragment;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.view.inputmethod.InputMethodManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.iflytek.aiui.demo.chat.ChatActivity;
import com.iflytek.aiui.demo.chat.R;
import com.iflytek.aiui.demo.chat.common.Constant;
import com.iflytek.aiui.demo.chat.databinding.ChatFragmentBinding;
import com.iflytek.aiui.demo.chat.model.ChatMessage;
import com.iflytek.aiui.demo.chat.repository.chat.RawMessage;
import com.iflytek.aiui.demo.chat.repository.translation.DestLanguage;
import com.iflytek.aiui.demo.chat.repository.translation.SrcLanguage;
import com.iflytek.aiui.demo.chat.ui.chat.adapter.MessageListAdapter;
import com.iflytek.aiui.demo.chat.ui.common.PermissionChecker;
import com.iflytek.aiui.demo.chat.ui.common.ScrollSpeedLinearLayoutManger;
import com.iflytek.aiui.demo.chat.ui.common.widget.PopupWindowFactory;
import com.tbruyelle.rxpermissions2.RxPermissions;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;

import javax.inject.Inject;

import dagger.android.support.AndroidSupportInjection;

/**
 * 聊天主界面Fragment
 */

// 设置支持矢量图
@BindingMethods({
        @BindingMethod(type = android.widget.ImageView.class,
                attribute = "srcCompat",
                method = "setImageDrawable")})
public class ChatFragment extends Fragment implements PermissionChecker {
    public static final Pattern emptyPattern = Pattern.compile("^\\s+$", Pattern.DOTALL);

    @Inject
    ViewModelProvider.Factory mViewModelFactory;

    private VoiceViewModel mVoiceViewModel;
    protected ChatViewModel mMessageModel;
    private PlayerViewModel mPlayerViewModel;
    private TranslationViewModel mTransCfgViewModel;
    //当前所有交互消息列表
    protected List<ChatMessage> mInteractMessages;

    private MessageListAdapter mMsgAdapter;
    protected ChatFragmentBinding mChatBinding;
    //按住录音动画控制类
    private PopupWindowFactory mVoicePop;
    private ImageView VolumeView;
    //当前状态，取值参考Constant中STATE定义
    private int mState;

    //唤醒波浪动画
    private boolean mWaveAnim = false;

    //记录Toast方便清空
    private List<Toast> mTipsToast = new ArrayList<>();

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        AndroidSupportInjection.inject(this);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        mChatBinding = DataBindingUtil.inflate(inflater, R.layout.chat_fragment,
                container, false);

        return mChatBinding.getRoot();
    }

    @SuppressLint("CheckResult")
    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        setInputState(Constant.STATE_VOICE);

        new RxPermissions(this)
                .request(Manifest.permission.READ_PHONE_STATE,
                        Manifest.permission.RECORD_AUDIO,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE,
                        Manifest.permission.ACCESS_FINE_LOCATION)
                .subscribe(granted -> {
                    mVoiceViewModel = ViewModelProviders.of(ChatFragment.this, mViewModelFactory).get(VoiceViewModel.class);
                    mMessageModel = ViewModelProviders.of(getActivity(), mViewModelFactory).get(ChatViewModel.class);
                    mPlayerViewModel = ViewModelProviders.of(ChatFragment.this, mViewModelFactory).get(PlayerViewModel.class);
                    mTransCfgViewModel = ViewModelProviders.of(ChatFragment.this, mViewModelFactory).get(TranslationViewModel.class);
                    if (!granted) {
                        mMessageModel.fakeAIUIResult(0, "permission", "请重启应用允许请求的权限");
                    }
                    //所有权限通过，初始化界面
                    onPermissionChecked();
                });
    }

    @CallSuper
    protected void onPermissionChecked() {
        initChatView();
        initPlayControl();
        initTextAction();
        initVoiceAction();
        initTransView();
    }

    private void initChatView() {
        //初始化交互消息展示列表
        ScrollSpeedLinearLayoutManger layout = new ScrollSpeedLinearLayoutManger(getActivity());
        layout.setSpeedSlow();
        layout.setStackFromEnd(true);
        mChatBinding.chatList.setLayoutManager(layout);

        mMsgAdapter = new MessageListAdapter(this);
        mChatBinding.chatList.setAdapter(mMsgAdapter);

        mChatBinding.chatList.setClipChildren(true);
        mChatBinding.chatList.setVerticalScrollBarEnabled(true);
        mChatBinding.chatList.getItemAnimator().setChangeDuration(0);

        mMsgAdapter.registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {
            @Override
            public void onItemRangeInserted(int positionStart, int itemCount) {
                super.onItemRangeInserted(positionStart, itemCount);
                mChatBinding.chatList.smoothScrollToPosition(positionStart);
            }
        });

        //获取交互消息，更新展示
        Transformations.map(mMessageModel.getInteractMessages(), input -> {
            List<ChatMessage> interactMessages = new ArrayList<>();
            for (RawMessage message : input) {
                interactMessages.add(new ChatMessage(message, ChatFragment.this, mMessageModel, mPlayerViewModel));
            }
            return interactMessages;
        }).observe(this, messages -> {
            mInteractMessages = messages;
            mMsgAdapter.replace(messages);
            mChatBinding.executePendingBindings();
        });
    }

    private void initTextAction() {
        //文本语义按钮监听
        mChatBinding.emotionSend.setOnClickListener(view -> doSend());
        mChatBinding.emotionSend.setOnKeyListener((view, keyCode, keyEvent) -> {
            if (keyEvent.getAction() == KeyEvent.ACTION_DOWN && keyCode == KeyEvent.KEYCODE_ENTER) {
                doSend();
                return true;
            }
            return false;
        });
    }


    @SuppressLint("ClickableViewAccessibility")
    private void initVoiceAction() {
        mVoiceViewModel.volume().observe(this, new Observer<Integer>() {
            @Override
            public void onChanged(@Nullable Integer integer) {

            }
        });
        //设置中唤醒模式开启关闭
        mVoiceViewModel.isWakeUpEnable().observe(this, enable -> {
            if (enable) {
                //设置中启用唤醒，进入待唤醒模式
                onWaitingWakeUp();
            } else {
                //唤醒关闭，进入按住说话的交互模式
                setInputState(Constant.STATE_VOICE);
            }
        });

        mVoiceViewModel.wakeUp().observe(this, wakeUpOrSleep -> {
            if (wakeUpOrSleep) {
                onWakeUp();
            } else {
                onWaitingWakeUp();
            }
        });


        //音量变化
        mVoiceViewModel.volume().observe(this, volume -> {
            //更新居中的音量信息
            if (VolumeView != null && VolumeView.getDrawable().setLevel(volume)) {
                VolumeView.getDrawable().invalidateSelf();
            }

            //唤醒状态下更新底部的音量波浪动画
            if (mState == Constant.STATE_WAKEUP) {
                mChatBinding.visualizer.setVolume(volume);
            }
        });

        mVoiceViewModel.isActiveInteract().observe(this, active -> {
            if (!active) {
                showTips("您好像并没有开始说话");
            }
        });

        //根据左下角图标切换输入状态
        mChatBinding.emotionVoice.setOnClickListener(view -> {
            setInputState(mState == Constant.STATE_VOICE ?
                    Constant.STATE_TEXT : Constant.STATE_VOICE);
            dismissKeyboard(view.getWindowToken());
        });

        //初始化居中显示的按住说话动画
        View view = View.inflate(getActivity(), R.layout.layout_microphone, null);
        VolumeView = view.findViewById(R.id.iv_recording_icon);
        mVoicePop = new PopupWindowFactory(getActivity(), view);

        //按住说话按钮
        mChatBinding.voiceText.setOnTouchListener((v, event) -> {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    mChatBinding.voiceText.setPressed(true);
                    if (mChatBinding.voiceText.isPressed()) {
                        mVoicePop.showAtLocation(v, Gravity.CENTER, 0, 0);
                        setInputState(Constant.STATE_VOICE_INPUTTING);
                        mVoiceViewModel.startSpeak();
                    }
                    break;
                case MotionEvent.ACTION_CANCEL:
                case MotionEvent.ACTION_UP:
                    mVoicePop.dismiss();
                    mChatBinding.voiceText.setPressed(false);
                    setInputState(Constant.STATE_VOICE);
                    mVoiceViewModel.endSpeak();
                    break;
                default:
                    break;
            }
            return true;
        });

        mVoicePop.getPopupWindow().setOnDismissListener(() -> {
            mChatBinding.voiceText.setPressed(false);
            mVoiceViewModel.endSpeak();
        });
    }


    private void setInputState(int state) {
        mState = state;
        mChatBinding.setState(state);
        mChatBinding.executePendingBindings();
    }

    private void initTransView() {
        mTransCfgViewModel.isTranslationEnable().observe(this,
                enable -> mChatBinding.transContainer.setVisibility(enable ? View.VISIBLE : View.GONE));

        mTransCfgViewModel.getTranslationMode().observe(this, transParams -> {
            mChatBinding.srcLanguegeSpinner.setSelectedIndex(Arrays.asList(SrcLanguage.values()).indexOf(transParams.getSrcLanguage()));
            mChatBinding.dstLanguegeSpinner.setSelectedIndex(Arrays.asList(DestLanguage.values()).indexOf(transParams.getDestLanguage()));
        });

        mChatBinding.srcLanguegeSpinner.setItems(SrcLanguage.values());
        mChatBinding.srcLanguegeSpinner.setOnItemSelectedListener((view, position, id, item) -> mTransCfgViewModel.setSrcLanguage((SrcLanguage) item));

        mChatBinding.dstLanguegeSpinner.setItems(DestLanguage.values());
        mChatBinding.dstLanguegeSpinner.setOnItemSelectedListener((view, position, id, item) -> mTransCfgViewModel.setDestLanguage((DestLanguage) item));
    }

    /**
     * 播放界面初始化
     */
    private void initPlayControl() {
        //播放器控制
        mChatBinding.playControlBar.controlSongName.setSelected(true);
        mChatBinding.setPlayer(mPlayerViewModel);

        mPlayerViewModel.getPlayerTips().observe(this, s -> showTips(s));
        mPlayerViewModel.getLiveError().observe(this, s -> showTips(s));

        //监听播放器状态，更新控制界面
        mPlayerViewModel.getPlayState().observe(this, playState -> {
            mChatBinding.setPlayState(playState);
            final LinearLayout playControl = mChatBinding.playControlBar.playControl;
            final CoordinatorLayout controlContainer = mChatBinding.playControlBar.controlContainer;

            if (mPlayerViewModel.isActive() && playControl.getVisibility() == View.GONE) {
                playControl.setVisibility(View.VISIBLE);
                controlContainer.setVisibility(View.VISIBLE);

                //滑动停止当前播放并隐藏播放控制条
                SwipeDismissBehavior<View> swipe = new SwipeDismissBehavior();
                swipe.setSwipeDirection(SwipeDismissBehavior.SWIPE_DIRECTION_START_TO_END);
                swipe.setListener(new SwipeDismissBehavior.OnDismissListener() {
                    @Override
                    public void onDismiss(View view) {
                        controlContainer.setVisibility(View.GONE);
                        playControl.setVisibility(View.GONE);
                        mPlayerViewModel.stop();
                    }

                    @Override
                    public void onDragStateChanged(int state) {
                    }
                });

                //将隐藏的播放控制条恢复
                CoordinatorLayout.LayoutParams coordinatorParams =
                        (CoordinatorLayout.LayoutParams) playControl.getLayoutParams();
                coordinatorParams.setBehavior(swipe);

                AlphaAnimation appearAnimation = new AlphaAnimation(0, 1);
                appearAnimation.setDuration(500);
                playControl.startAnimation(appearAnimation);

                CoordinatorLayout.LayoutParams tParams = (CoordinatorLayout.LayoutParams) playControl.getLayoutParams();
                tParams.setMargins(0, 0, 0, 0);
                playControl.requestLayout();
                playControl.setAlpha(1.0f);
            }
        });
    }

    private void doSend() {
        //语音模式按发送按钮进入文本语义模式
        if (mState == Constant.STATE_VOICE) {
            setInputState(Constant.STATE_TEXT);
            return;
        }

        //文本语义
        String msg = mChatBinding.editText.getText().toString();
        if (!TextUtils.isEmpty(msg) && !emptyPattern.matcher(msg).matches()) {
            clearTips();
            mVoiceViewModel.sendText(msg);
            mChatBinding.editText.setText("");
        } else {
            showTips("发送内容不能为空");
        }
    }

    private void showTips(String toast) {
        clearTips();

        Toast tips = Toast.makeText(getContext(), toast, Toast.LENGTH_SHORT);
        tips.show();
        mTipsToast.add(tips);
    }


    private void clearTips() {
        for (Toast item : mTipsToast) {
            item.cancel();
        }

        mTipsToast.clear();
    }

    private void onWakeUp() {
        setInputState(Constant.STATE_WAKEUP);
        if (!mWaveAnim) {
            //底部音量动画
            mChatBinding.visualizer.startAnim();
            mWaveAnim = true;
        }
    }

    private void onWaitingWakeUp() {
        //进入待唤醒状态
        setInputState(Constant.STATE_WAITING_WAKEUP);
        mChatBinding.visualizer.stopAnim();
        mWaveAnim = false;
    }


    private void dismissKeyboard(IBinder windowToken) {
        Activity activity = getActivity();
        if (activity != null) {
            InputMethodManager imm = (InputMethodManager) activity.getSystemService(
                    Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(windowToken, 0);
        }
    }

    /**
     * 切换至语义结果详情页
     *
     * @param content
     */
    public void switchToDetail(String content) {
        ((ChatActivity) getActivity()).switchToDetail(content);
    }

    @Override
    public void onResume() {
        super.onResume();
        if (mVoiceViewModel != null) {
            mVoiceViewModel.onChatResume();
        }
        mChatBinding.visualizer.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
        if (mVoiceViewModel != null) {
            mVoiceViewModel.onChatPause();
        }
        mChatBinding.visualizer.onPause();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mChatBinding.visualizer.release();
    }

    @SuppressLint("CheckResult")
    @Override
    public void checkPermission(String permission, final Runnable success, final Runnable failed) {
        new RxPermissions(this)
                .request(permission)
                .subscribe(granted -> {
                    if (granted) {
                        if (success != null) {
                            success.run();
                        }
                    } else {
                        if (failed != null) {
                            failed.run();
                        }
                    }
                });
    }
}
