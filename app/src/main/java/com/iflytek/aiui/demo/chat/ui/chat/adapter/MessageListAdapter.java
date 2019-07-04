package com.iflytek.aiui.demo.chat.ui.chat.adapter;

import android.databinding.DataBindingUtil;
import android.text.Spannable;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;

import com.iflytek.aiui.demo.chat.R;
import com.iflytek.aiui.demo.chat.databinding.ChatItemBinding;
import com.iflytek.aiui.demo.chat.model.ChatMessage;
import com.iflytek.aiui.demo.chat.ui.chat.ChatFragment;
import com.zzhoujay.richtext.RichText;
import com.zzhoujay.richtext.ext.LongClickableLinkMovementMethod;

import java.util.Arrays;

/**
 * 聊天信息列表 RecycleView Adapter
 */
public class MessageListAdapter extends DataBoundListAdapter<ChatMessage, ChatItemBinding>
        implements ItemListener {
    private ChatFragment mFragment;
    public MessageListAdapter(ChatFragment fragment){
        mFragment = fragment;
    }
    @Override
    protected ChatItemBinding createBinding(ViewGroup parent, int viewType) {
        return DataBindingUtil.inflate(LayoutInflater.from(parent.getContext()),
                R.layout.chat_item, parent, false);
    }

    @Override
    protected void bind(final ChatItemBinding binding, ChatMessage item) {
            binding.setMsg(item);
            binding.setBinding(binding);
            binding.setHandler(this);
            String content = item.getDisplayText();
            //根据消息内容中是否包含超链接，判断用RichText解析显示
            //RichText解析耗时较大，不能全部使用RichText显示
            if(content != null && (content.contains("</a>") || content.contains("<br/>"))){
                RichText.from(content)
                        .urlClick(item.getHandler())
                        .into(binding.chatItemContentText);
                binding.chatItemContentText.setOnTouchListener(new View.OnTouchListener() {
                    @Override
                    public boolean onTouch(View view, MotionEvent motionEvent) {
                        return new LongClickableLinkMovementMethod()
                                .onTouchEvent(binding.chatItemContentText, (Spannable) binding.chatItemContentText.getText(), motionEvent);
                    }
                });
            } else {
                binding.chatItemContentText.setText(item.getDisplayText());
            }
    }

    @Override
    protected boolean areItemsTheSame(ChatMessage oldItem, ChatMessage newItem) {
        return oldItem.getMessage().msgID == newItem.getMessage().msgID;
    }

    @Override
    protected boolean areContentsTheSame(ChatMessage oldItem, ChatMessage newItem) {
        return oldItem.getMessageVersion() == newItem.getMessageVersion();
    }

    @Override
    public void onMessageClick(ChatMessage msg) {
        if(!msg.getMessage().isFromUser()) {
            //AIUI消息点击进入语义详情页
            mFragment.switchToDetail(new String(msg.getMessage().msgData));
        }
    }
}
