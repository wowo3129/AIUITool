package com.iflytek.aiui.demo.chat.ui.common;

/**
 * 权限请求接口
 */

public interface PermissionChecker {
    /**
     * 权限请求
     * @param permission 权限名
     * @param success 权限请求通过回调
     * @param failed 权限拒绝通过回调
     */
    void checkPermission(String permission, Runnable success, Runnable failed);
}
