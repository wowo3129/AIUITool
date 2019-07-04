package com.iflytek.aiui.demo.chat.ui.chat;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.ViewModel;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;

import com.iflytek.aiui.demo.chat.handler.Answer;
import com.iflytek.aiui.demo.chat.model.ChatMessageHandler;
import com.iflytek.aiui.demo.chat.repository.chat.RawMessage;
import com.iflytek.aiui.demo.chat.repository.personality.DynamicEntityData;
import com.iflytek.aiui.demo.chat.repository.personality.SpeakableSyncData;
import com.iflytek.aiui.demo.chat.repository.config.AIUIConfigCenter;
import com.iflytek.aiui.demo.chat.repository.chat.ChatRepo;
import com.iflytek.aiui.demo.chat.repository.Contacts;
import com.iflytek.aiui.demo.chat.repository.Location;
import com.iflytek.aiui.demo.chat.repository.personality.Personnality;
import com.iflytek.aiui.demo.chat.repository.Storage;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ScheduledExecutorService;

import javax.inject.Inject;


public class ChatViewModel extends ViewModel {
    private Context mContext;
    private ScheduledExecutorService mExecutor;
    private ChatRepo mChatRepo;
    private AIUIConfigCenter mConfigManager;
    private Location mLocationRepo;
    private Personnality mPersonalRepo;
    private Contacts mContactRepository;
    private Storage mStorage;

    private JSONObject mPersParams;


    @Inject
    public ChatViewModel(Context context,
                         ScheduledExecutorService executor,
                         ChatRepo chatRepo,
                         AIUIConfigCenter configManager,
                         Storage storage,
                         Contacts contactRepository,
                         Location locationRepo,
                         Personnality personalRepo) {
        mExecutor = executor;
        mContext = context;
        mChatRepo = chatRepo;
        mStorage = storage;
        mContactRepository = contactRepository;
        mConfigManager = configManager;
        mLocationRepo = locationRepo;
        mPersonalRepo = personalRepo;
        mPersParams = new JSONObject();
    }

    public LiveData<List<RawMessage>> getInteractMessages() {
        return mChatRepo.getInteractMessages();
    }

    /**
     * 模拟AIUI结果信息，用于展示如提示语或者操作结果信息
     */
    public RawMessage fakeAIUIResult(int rc, String service, String answer) {
        return mChatRepo.fakeAIUIResult(rc, service, answer, null, null);
    }

    /**
     * 生效动态实体
     */
    public void putPersParam(String key, String value) {
        try {
            mPersParams.put(key, value);
            mPersonalRepo.setPersParams(mPersParams);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    /**
     * 更新消息列表中特定的消息内容
     */
    public void updateMessage(RawMessage msg) {
        mChatRepo.updateMessage(msg);
    }

    /**
     * 上传动态实体数据
     */
    public void syncDynamicData(DynamicEntityData data) {
        mPersonalRepo.syncDynamicEntity(data);
    }

    /**
     * 查询动态实体上传状态
     */
    public void queryDynamicStatus(String sid) {
        mPersonalRepo.queryDynamicSyncStatus(sid);
    }

    /**
     * 上传所见即可说数据
     */
    public void syncSpeakableData(SpeakableSyncData data) {
        mPersonalRepo.syncSpeakableData(data);
    }

    /**
     * 读取通讯录
     * @return
     */
    public List<String> getContacts() {
        return mContactRepository.getContacts();
    }

    /**
     * 读取Assets文件内容
     * @param filename
     * @return
     */
    public String readAssetFile(String filename){
        return mStorage.readAssetFile(filename);
    }

    public Answer getLatestAnswer() {
       return ChatMessageHandler.getLatestTTSAnswer();
    }

    /**
     * 使用自动定位
     */
    public void useLocationData() {
        mLocationRepo.autoLocate();
    }

    /**
     * 使用新的APPID设置
     * @param appid
     * @param key
     * @param scene
     */
    public void useNewAppID(String appid, String key, String scene) {
        mConfigManager.config(appid, key, scene);
    }

    /**
     * 启动Activity
     * @param intent
     */
    public void startActivity(Intent intent) {
        final PackageManager packageManager = mContext.getPackageManager();
        if(intent.resolveActivity(packageManager) != null) {
            mContext.startActivity(intent);
        }
    }

    /**
     * 判断手机是否安装启用该应用
     * @param packageName 应用包名
     * @return
     */
    public boolean isAvailable(String packageName) {
        final PackageManager packageManager = mContext.getPackageManager();
        List<PackageInfo> packages = packageManager.getInstalledPackages(0);
        List<String> packageNames = new ArrayList<String>();
        if(packages != null){
            for(int i = 0; i < packages.size(); i++){
                PackageInfo packageInfo = packages.get(i);
                if(packageInfo.applicationInfo.enabled) {
                    String packName = packageInfo.packageName;
                    packageNames.add(packName);
                }
            }
        }
        return packageNames.contains(packageName);
    }

    public ScheduledExecutorService getExecutor() {
       return mExecutor;
    }

}
