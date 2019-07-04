package com.iflytek.aiui.demo.chat.handler.personality;


import android.support.annotation.NonNull;

import com.iflytek.aiui.demo.chat.handler.Answer;
import com.iflytek.aiui.demo.chat.repository.personality.DynamicEntityData;
import com.iflytek.aiui.demo.chat.model.SemanticResult;
import com.iflytek.aiui.demo.chat.handler.IntentHandler;
import com.iflytek.aiui.demo.chat.ui.common.PermissionChecker;
import com.iflytek.aiui.demo.chat.ui.chat.ChatViewModel;
import com.iflytek.aiui.demo.chat.ui.chat.PlayerViewModel;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * 点菜技能处理类，自定义维度动态实体示例
 */

public class OrderMenuHandler extends IntentHandler {
    public static List<String> orders = new ArrayList<>();

    public OrderMenuHandler(ChatViewModel model, PlayerViewModel player, PermissionChecker checker) {
        super(model, player, checker);
    }

    @Override
    public Answer getFormatContent(SemanticResult result) {
        String intent = result.semantic.optString("intent");
        if(intent.equals("wantOrderIntent")){
            //开始点菜，分店选择
            return new Answer(constructSelectPrompt());
        }else if(intent.equals("orderItemIntent")){
            //点菜
            return new Answer(constructOrderPrompt(result));
        } else if(intent.equals("finishOrderIntent")){
            //结束点菜
            return new Answer(constructFinishPrompt());
        }
        return new Answer(result.answer);
    }

    @NonNull
    private String constructSelectPrompt() {
        orders.clear();
        StringBuilder orderResponse = new StringBuilder();
        orderResponse.append(String.format("请选择您所在的地区分店"));
        orderResponse.append(NEWLINE);
        orderResponse.append(NEWLINE);
        orderResponse.append(NEWLINE);
        orderResponse.append(String.format("<a href=\"%s\">饭真香001分店</a>", "001"));
        orderResponse.append(NEWLINE);
        orderResponse.append(NEWLINE);
        orderResponse.append(String.format("<a href=\"%s\">饭真香002分店</a>", "002"));

        return orderResponse.toString();
    }

    @NonNull
    private String constructOrderPrompt(SemanticResult result) {
        String item = result.semantic.optJSONArray("slots").
                optJSONObject(0).optString("value");
        orders.add(item);
        StringBuilder currentOrders = new StringBuilder();
        currentOrders.append("好的，当前您已经点了如下菜品：");
        currentOrders.append(NEWLINE);
        currentOrders.append(NEWLINE);
        currentOrders.append(NEWLINE);
        for(int index=0;index<orders.size();index++){
            currentOrders.append(String.format("%d. %s", index + 1, orders.get(index)));
            currentOrders.append(NEWLINE);
        }

        return currentOrders.toString();
    }

    @NonNull
    private String constructFinishPrompt() {
        StringBuilder finishOrder = new StringBuilder();
        int priceSum = 0;
        finishOrder.append("好的，马上为您准备上菜，当前包含如下菜品：");
        finishOrder.append(NEWLINE);
        finishOrder.append(NEWLINE);
        finishOrder.append(NEWLINE);
        for(int index=0;index<orders.size();index++){
            int price = new Random().nextInt(20) + 20;
            finishOrder.append(String.format("%d. %s %d元",
                    index + 1,
                    orders.get(index),
                    price));
            priceSum += price;
            finishOrder.append(NEWLINE);
        }
        finishOrder.append(NEWLINE);
        finishOrder.append(NEWLINE);
        finishOrder.append(String.format("<strong>总价 %d元，请马上结账，我们不支持打白条或霸王餐</strong>"
                , priceSum));
        return finishOrder.toString();
    }


    @Override
    public boolean urlClicked(String url) {
        String menuSrcData = "";
        //根据选择的不同分店，加载不同的菜单数据，上传至自定义维度动态实体
        if(url.equals("001")){
            menuSrcData = mMessageViewModel.readAssetFile("data/001_branch_menu.txt");
        }else if(url.equals("002")){
            menuSrcData = mMessageViewModel.readAssetFile("data/002_branch_menu.txt");
        }

        //根据菜单数据构造需要上传的动态实体数据
        String[] menuItemsStr = menuSrcData.split("\r?\n");
        StringBuffer menuItemData = new StringBuffer();
        for(String item : menuItemsStr){
            menuItemData.append(String.format("{\"name\": \"%s\"}\n", item));
        }

        //上传自定义维度动态实体
        mMessageViewModel.syncDynamicData(new DynamicEntityData(
                "FOOBAR.menuRes", "branch", url, menuItemData.toString()
        ));
        //设置pers_params 生效自定义维度动态实体，生效详情参考接入文档中动态实体生效使用一节
        mMessageViewModel.putPersParam("branch", url);
        constructSelectResult(url, menuItemsStr);

        return true;
    }

    private void constructSelectResult(String url, String[] menuItemsStr) {
        //构造欢迎消息放入到消息队列中
        StringBuffer branchMenu = new StringBuffer();
        branchMenu.append(String.format("您好，%s分店菜单如下", url));
        branchMenu.append(NEWLINE);
        branchMenu.append(NEWLINE);
        for(int index=0;index<menuItemsStr.length;index+=2){
            branchMenu.append(String.format("<p>%-5s|%-5s</p>",
                    menuItemsStr[index],
                    index+1>=menuItemsStr.length? "":menuItemsStr[index+1], 5));
        }
        branchMenu.append(NEWLINE);
        branchMenu.append(NEWLINE);
        branchMenu.append("可以按如下说法进行点菜：");
        branchMenu.append(NEWLINE);
        branchMenu.append("我要点xxx");
        branchMenu.append(NEWLINE);
        branchMenu.append("再点个xxx");
        branchMenu.append(NEWLINE);
        branchMenu.append(NEWLINE);
        branchMenu.append(NEWLINE);
        branchMenu.append("可以按如下说法结束点菜：");
        branchMenu.append(NEWLINE);
        branchMenu.append("可以了，上菜吧");
        branchMenu.append(NEWLINE);

        // 选择分店后，展示分店菜单欢迎信息
        mMessageViewModel.fakeAIUIResult(0, "FOOBAR.MenuSkill", branchMenu.toString());
    }
}
