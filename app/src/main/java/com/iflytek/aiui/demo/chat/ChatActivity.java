package com.iflytek.aiui.demo.chat;

import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.app.AppCompatDelegate;
import android.support.v7.widget.Toolbar;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.blankj.utilcode.util.LogUtils;
import com.blankj.utilcode.util.Utils;
import com.iflytek.aiui.demo.chat.common.Constant;
import com.iflytek.aiui.demo.chat.ui.about.AboutFragment;
import com.iflytek.aiui.demo.chat.ui.chat.ChatFragment;
import com.iflytek.aiui.demo.chat.ui.detail.DetailFragment;
import com.iflytek.aiui.demo.chat.ui.settings.SettingsFragment;
import com.iflytek.aiui.demo.chat.ui.test.HttpTestFragement;

import java.io.File;

import javax.inject.Inject;

import dagger.android.AndroidInjection;
import dagger.android.AndroidInjector;
import dagger.android.DispatchingAndroidInjector;
import dagger.android.support.HasSupportFragmentInjector;
import gdut.bsx.share2.FileUtil;
import gdut.bsx.share2.Share2;
import gdut.bsx.share2.ShareContentType;

public class ChatActivity extends AppCompatActivity implements HasSupportFragmentInjector {
    @Inject
    DispatchingAndroidInjector<Fragment> fragmentInjector;

    private SettingsFragment mSettingsFragment;
    private ChatFragment mChatFragment;
    private AboutFragment mAboutFragment;
    private HttpTestFragement mHttpTestFragment;
    private ActionBarDrawerToggle toggle;
    private DrawerLayout drawer;
    private Toolbar toolbar;

    private boolean mIsExit;
    private boolean mIsChatFragment;

    // 矢量图兼容支持
    static {
        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        AndroidInjection.inject(ChatActivity.this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setupActionBar();
        onCreateFinish();

        initlog();
    }

    /**
     * log to /sdcard/anzerTTS/
     */
    private void initlog() {
        Utils.init(getApplicationContext());
        LogUtils.Builder builder = new LogUtils.Builder().setBorderSwitch(false).setLog2FileSwitch(true)
                .setDir(new File(Environment.getExternalStorageDirectory().getPath() + "/AIUIYUANBAN"))
                .setLogSwitch(true);
    }

    @Override
    public AndroidInjector<Fragment> supportFragmentInjector() {
        return fragmentInjector;
    }

    protected void onCreateFinish() {
        mChatFragment = new ChatFragment();
        mSettingsFragment = new SettingsFragment();
        mAboutFragment = new AboutFragment();
        mHttpTestFragment = new HttpTestFragement();

        //切换到ChatFragment聊天交互界面
//        switchChats();
        switchToTest();
    }

    /**
     * 切换到设置页面
     */
    public void switchToSettings() {
        switchFragment(mSettingsFragment, true);
    }

    /**
     * 切换到聊天交互页面
     */
    public void switchChats() {
        switchFragment(mChatFragment, false);
    }

    /**
     * 切换到关于页面
     */
    public void switchToAbout() {
        switchFragment(mAboutFragment, true);
    }

    public void switchToTest() {
        switchFragment(mHttpTestFragment, true);
    }

    /**
     * 切换到语义详情页
     */
    public void switchToDetail(String content) {
        switchFragment(DetailFragment.createDetailFragment(content), true);
    }

    protected void switchFragment(Fragment fragment, boolean backStack) {
        //收回抽屉动画
        drawer.closeDrawers();
        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        if (backStack) {
            fragmentTransaction.addToBackStack(null);
        }
        //设置fragment切换的滑动动画
        if (fragment == mChatFragment) {
            fragmentTransaction.setCustomAnimations(
                    R.anim.slide_right_in, R.anim.slide_left_out,
                    R.anim.slide_left_in, R.anim.slide_right_out);
        } else {
            fragmentTransaction.setCustomAnimations(
                    R.anim.slide_left_in, R.anim.slide_right_out,
                    R.anim.slide_right_in, R.anim.slide_left_out);
        }

        fragmentTransaction.replace(R.id.container, fragment).commitAllowingStateLoss();
    }

    private void setupActionBar() {
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        drawer = findViewById(R.id.drawer_layout);
        toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        //设置ActionBar的title，icon
        getSupportFragmentManager().registerFragmentLifecycleCallbacks(new FragmentManager.FragmentLifecycleCallbacks() {
            @Override
            public void onFragmentResumed(FragmentManager fm, Fragment f) {
                super.onFragmentResumed(fm, f);
                if (f instanceof ChatFragment) {
                    mIsChatFragment = true;
                    getSupportActionBar().setDisplayHomeAsUpEnabled(false);
                    toggle.setDrawerIndicatorEnabled(true);
                    getSupportActionBar().setTitle("AIUI");
                } else if (f instanceof SettingsFragment) {
                    mIsChatFragment = false;
                    toggle.setDrawerIndicatorEnabled(false);
                    getSupportActionBar().setDisplayHomeAsUpEnabled(true);
                    getSupportActionBar().setTitle("设置");
                } else if (f instanceof DetailFragment) {
                    mIsChatFragment = false;
                    toggle.setDrawerIndicatorEnabled(false);
                    getSupportActionBar().setDisplayHomeAsUpEnabled(true);
                    getSupportActionBar().setTitle("详情");
                } else if (f instanceof AboutFragment) {
                    mIsChatFragment = false;
                    toggle.setDrawerIndicatorEnabled(false);
                    getSupportActionBar().setDisplayHomeAsUpEnabled(true);
                    getSupportActionBar().setTitle("关于");
                } else if (f instanceof HttpTestFragement) {
                    mIsChatFragment = false;
                    toggle.setDrawerIndicatorEnabled(false);
                    getSupportActionBar().setDisplayHomeAsUpEnabled(true);
                    getSupportActionBar().setTitle("测试");
                }
            }
        }, false);

        //ActionBar的返回按钮监听
        toggle.setToolbarNavigationClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getSupportFragmentManager().popBackStack();
            }
        });

        //设置侧边栏按钮跳转
        NavigationView navigation = findViewById(R.id.nav_view);
        navigation.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.nav_settings: {
                        switchToSettings();
                        break;
                    }

                    case R.id.nav_about: {
                        switchToAbout();
                        break;
                    }

                    case R.id.nav_share_log: {
                        sendLog();
                        break;
                    }

                    case R.id.nav_net_test: {
                        switchToTest();
                    }
                }
                return false;
            }
        });
    }

    private void sendLog() {
        if (isFileExist(Constant.AIUI_LOG_PATH)) {
            new Share2.Builder(this)
                    .setContentType(ShareContentType.FILE)
                    .setTitle(getString(R.string.send_aiui_log))
                    .setOnActivityResult(100)
                    .setShareFileUri(FileUtil.getFileUri(this, ShareContentType.FILE, new File(Constant.AIUI_LOG_PATH)))
                    .build()
                    .shareBySystem();
        } else {
            showToast(getString(R.string.aiui_log_not_exist));
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 100) {
            switchChats();
        }
    }

    /**
     * 检测文件是否存在
     *
     * @param path 文件全路径
     * @return 是否存在
     */
    public static boolean isFileExist(String path) {
        File file = new File(path);

        return file.exists();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK && mIsChatFragment) {
            if (mIsExit) {
                System.exit(0);
            } else {
                showToast("再按一次退出");
                mIsExit = true;
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        mIsExit = false;
                    }
                }, 2000);
            }
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    private void showToast(String str) {
        Toast.makeText(this, str, Toast.LENGTH_SHORT).show();
    }
}
