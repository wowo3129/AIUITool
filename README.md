step1:初始化

  ```java
  WebClientManager.getInstance().onCreate();
  ```

step2:初始化人脸数据接口，处理实时来人通知、打招呼、管理员数据上报功能
```java
WebClientManager.getInstance().

    setFaceListener(new BaseFaceListener() {
        @Override
        public void realTimeFaceData () {
            super.realTimeFaceData();
            //实时来人通知接口
        }
        @Override
        public void receiveFaceData (CommonRecogResult userBean){
            super.receiveFaceData(userBean);
            //打招呼功能
        }
        @Override
        public void receiveFaceDataOfAdmin (CommonRecogResult userBean){
            super.receiveFaceDataOfAdmin(userBean);
            Log.d(TAG, "realTimeFaceData 管理员数据");
        }
    });
```
step3:功能描述：人脸模块连接状态
```java
if(!WebClientManager.getInstance().isFaceWebSocketClientOpen()){
       SpeechManager.onSpeaking("人脸模块连接异常,请工作人员检查。");
}
```
step4:
  ```
  功能描述：同步云端的人脸数据到人脸算法板
  调用方法：WebClientManager.getInstance().pullCloudFaceToLocal();
  方法介绍：通过调用{@link #pullCloudFaceToLocal}发送指令到人脸算法版,人脸算法版从云后台同步拉取人脸数据到算法版，无需给andorid返回数据
  ```
