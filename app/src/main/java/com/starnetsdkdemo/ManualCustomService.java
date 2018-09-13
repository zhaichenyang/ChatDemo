package com.starnetsdkdemo;

import android.content.Context;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.starnet.OnMessageListener;
import com.starnet.StarNetMobile;
import com.starnet.entity.CallData;
import com.starnet.entity.MessageEntity;
import com.starnet.entity.MsgData;
import com.starnet.entity.UserInfo;
import com.starnet.entity.UserSettingInfo;
import com.starnet.entity.mqttMessageEntity.Data;
import com.starnet.entity.mqttMessageEntity.MqttMessageSend;
import com.starnet.entity.mqttMessageEntity.OnLine;
import com.starnet.enums.MsgTypeEnums;
import com.starnet.utils.DateUtils;
import com.starnet.utils.LoggerUtils;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.lang.reflect.Type;
import java.net.URLDecoder;
import java.util.Date;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

/**
 * Created by zhaichenyang on 2018/9/12.
 */

public class ManualCustomService {
    private Context mContext;
    //mqtt服务器
    private String mqttServerUrl;
    //系统接入码
    private String systemAccessCode = "1003";
    //文本消息id
    private String msgId = "16041_46";
    //设备id
    private String deviceId = android.os.Build.SERIAL;
    //个人信息
    private String userId = "";
    private String userName = "";
    //会话信息
    String callSno = "";
    String accNo = "";
    String agentId = "";
    //初始化标识
    private boolean initFlag;
    //正在排队标识
    private boolean isQueuing;
    //满意度评价入参
    private String result;
    //回调接口
    private OnChatUIChangedListener listener;

    //构造方法
    public ManualCustomService(Context context){
        this.mContext=context;
    }

    //初始化人工客服配置
    public void initManualCustomService(String host) {
        StarNetMobile.getInstance().setHost(host);
    }

    //设置用户id和用户名
    public void setUser(String userId, String userName) {
        this.userId = userId;
        this.userName = userName;
    }

    //设置系统接入码
    public void setSystemAccessCode(String systemAccessCode) {
        this.systemAccessCode = systemAccessCode;

    }

    //设置消息id
    public void setMsgId(String msgId) {
        this.msgId = msgId;
    }

    //设置回调
    public void setOnChatUIChangedListener(OnChatUIChangedListener listener){
        this.listener=listener;
    }

    //开启人工客服服务
    public void startManualCustomService() {
        getServiceInfo();
        createSession();
    }

    //关闭人工客服服务
    public void closeManualCustomService() {
        dropCall();
        sendOfflineMessage();
        closeMqtt();
    }


    //获取服务器信息
    private void getServiceInfo() {
        StarNetMobile.getInstance().getServerConfigInfo(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                try {
                    String jsonStr = response.body().string();
                    JSONObject jsonObject = new JSONObject(jsonStr);
                    if ("0".equals(jsonObject.getString("returnCode"))) {
                        JSONObject returnData = (JSONObject) jsonObject.get("returnData");
                        mqttServerUrl = returnData.getString("mqttServer");
                        LoggerUtils.logger("获取服务器信息", "mqttServer:" + mqttServerUrl);
                        //用户配置信息上传
                        submitUserSettingInfo();
                    } else {
                        LoggerUtils.logger("获取服务器信息", "getServerConfigInfo调用失败:" + jsonObject.getString("returnDesc"));
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    //用户配置信息上传
    private void submitUserSettingInfo() {
        UserSettingInfo userSettingInfo = new UserSettingInfo();
        userSettingInfo.setUserId(userId);
        userSettingInfo.setNickName(userName);
        StarNetMobile.getInstance().submitUserConfigInfo(userSettingInfo, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {

            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                //由于这个接口数据很烂，所以直接初始化MQTT
                initMqtt();
            }
        });
    }

    //初始化Mqtt
    private void initMqtt() {
        initFlag = true;
        try {
            StarNetMobile.getInstance().initMqtt(deviceId, mqttServerUrl, "", "", new OnMessageListener() {
                @Override
                public void handleMessage(String topic, String message) {
                    //由于服务端会在第一次返回无用的消息，所以做了一次过滤处理
                    if (initFlag) {
                        initFlag = false;
                        return;
                    }
                    try {
                        String msg = URLDecoder.decode(message, "utf-8");
                        GsonBuilder builder = new GsonBuilder();
                        builder.registerTypeAdapter(Date.class, new JsonDeserializer<Date>() {
                            @Override
                            public Date deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
                                return new Date(json.getAsJsonPrimitive().getAsLong());
                            }
                        });
                        Gson gson = builder.create();
                        MessageEntity messageEntity = gson.fromJson(msg, MessageEntity.class);
                        Integer msgType = messageEntity.getType();
                        MsgData msgData = messageEntity.getData();
                        LoggerUtils.logger("初始化Mqtt", "message类型:" + msgType + ",message内容：" + msgData.getMsgContent());
                        switch (msgType) {
                            case MsgTypeEnums.CallConnected://通话建立成功的处理
                                accNo = messageEntity.getAccessSno();//接入流水号
                                callSno = messageEntity.getCallSno();//通话流水号
                                agentId = messageEntity.getAgentId();//坐席id
                                LoggerUtils.logger("MQTT回调", "通话建立成功的处理" + agentId);
                                //TODO 动态改变UI，数据处理
                                if(isQueuing){
                                    NotificationUtils.showMessage(mContext,"国寿e店客服提醒","客服已接通，请开始您的对话");
                                    isQueuing=false;
                                }
                                listener.addChatItem();
                                break;
                            case MsgTypeEnums.AgentCallAnswered://处理坐席应答的消息
                                LoggerUtils.logger("MQTT回调", "处理坐席应答的消息");
                                break;
                            case MsgTypeEnums.CallDisconnected://通话结束的处理
                                LoggerUtils.logger("MQTT回调", "通话结束的处理");
                                closeManualCustomService();
                                //TODO 保存聊天记录
                                break;
                            case MsgTypeEnums.CallFailed://呼叫失败的处理
                                LoggerUtils.logger("MQTT回调", "呼叫失败的处理");
                                closeManualCustomService();
                                break;
                            case MsgTypeEnums.CallQueuing://呼叫排队的处理
                                LoggerUtils.logger("MQTT回调", "呼叫排队的处理");
                                //TODO 更新UI
                                isQueuing=true;
                                break;
                            case MsgTypeEnums.QueueTimeOut://排队超时的处理
                                LoggerUtils.logger("MQTT回调", "排队超时的处理");
                                //TODO 关闭会话
                                isQueuing=false;
                                closeManualCustomService();
                                break;
                            default://默认处理文字消息
                                LoggerUtils.logger("MQTT回调", "默认处理文字消息");
                                //TODO 改变UI
                                break;
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });

        } catch (Exception e) {
            e.printStackTrace();
        }
        //发送在线消息
        sendOnlineMsg();
    }

    //发送online消息
    private void sendOnlineMsg() {
        try {
            OnLine messageInfo = new OnLine();
            //必传参数：昵称，设备号，用户id
            messageInfo.setDeviceId(deviceId);
            messageInfo.setUserId(userId);
            messageInfo.setNickName(userName);
            StarNetMobile.getInstance().sendOnlineMsg(messageInfo);
            LoggerUtils.logger("发送online消息", messageInfo.getUserId());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //登录，创建会话
    private void createSession() {
        CallData callData = new CallData();
        //必传参数：渠道，客户账号，昵称
        callData.setChannel("App");
        callData.setCustNo(userId);
        callData.setNick(userName);
        String forw = "||||^";
        Gson gson = new Gson();
        String jsoncallData = forw + gson.toJson(callData);

        UserInfo info = new UserInfo();
        info.setChannel("App");
        info.setCallData(jsoncallData);
        info.setNickName(userName);
        info.setLanguage("zh");
        info.setSystemAccessCode(systemAccessCode);
        info.setUserId(userId);
        info.setVdnId("1");
        info.setUserName(userName);
        StarNetMobile.getInstance().createSession(info, new Callback() {
                    @Override
                    public void onFailure(Call call, IOException e) {

                    }

                    @Override
                    public void onResponse(Call call, Response response) throws IOException {
                        try {
                            String jsonStr = response.body().string();
                            JSONObject jsonObj = new JSONObject(jsonStr);
                            if ("0".equals(jsonObj.getString("returnCode"))) {
                                JSONObject returnData = new JSONObject(jsonObj.getString("returnData"));
                                result = returnData.getString("result");
                                LoggerUtils.logger("创建会话", "creatSession调用成功:" + result);
                            } else {
                                LoggerUtils.logger("创建会话", "creatSession调用失败:" + jsonObj.getString("returnDesc"));
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }

        );
    }

    //发送文本消息
    public void sendTextMsg(String str) {
        try {
            MqttMessageSend mqttMessageSend = new MqttMessageSend();
            Data data = new Data();
            DateUtils dateUtils = new DateUtils();
            long longtime = dateUtils.getTime();
            data.setMsgContent(str);
            mqttMessageSend.setData(data);
            mqttMessageSend.setType(1);
            mqttMessageSend.setMsgId(msgId);
            mqttMessageSend.setTime(longtime);
            mqttMessageSend.setUserId(userId);
            mqttMessageSend.setDeviceId(deviceId);
            mqttMessageSend.setCallSno(Integer.parseInt(callSno));
            mqttMessageSend.setAccessSno(Integer.parseInt(accNo));
            mqttMessageSend.setAgentId(agentId);
            mqttMessageSend.setSendForType(2);
            Gson gson_msg = new Gson();
            String sendData = gson_msg.toJson(mqttMessageSend);
            LoggerUtils.logger("sendData", sendData);
            StarNetMobile.getInstance().sendTextMsg(sendData);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    //取消排队
    public void cancelQueue() {
        StarNetMobile.getInstance().cancelQueue(userId, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {

            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                try {
                    String jsonStr = response.body().string();
                    JSONObject jsonObj = new JSONObject(jsonStr);
                    if ("0".equals(jsonObj.getString("returnCode"))) {
                        LoggerUtils.logger("接口数据", "bt_cancelQueue成功");
                        closeManualCustomService();
                    } else {
                        LoggerUtils.logger("接口数据", "bt_cancelQueue调用失败:" + jsonObj.getString("returnDesc"));
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });

    }

    //满意度调查提交
    public void submitSatisfaction(String level) {
        //用户评价等级，0 未评价1 非常满意2 满意3 一般4 对销售服务、业务处理不满意5 对客服代表服务不满意
        StarNetMobile.getInstance().submitSatisfaction(result, level, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {

            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                try {
                    String jsonStr = response.body().string();
                    JSONObject jsonObj = new JSONObject(jsonStr);
                    if ("0".equals(jsonObj.getString("returnCode"))) {
                        Log.i("接口数据", "bt_Satisfaction成功");
                    } else {
                        Log.i("接口数据", "bt_Satisfaction调用失败:" + jsonObj.getString("returnDesc"));
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    //结束通话,在dropCall的请求后台自动调用了loginOut登出
    private void dropCall() {
        StarNetMobile.getInstance().dropCall(userId, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {

            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                try {
                    String jsonStr = response.body().string();
                    JSONObject jsonObj = new JSONObject(jsonStr);
                    if ("0".equals(jsonObj.getString("returnCode"))) {
                        LoggerUtils.logger("接口数据", "bt_dropCall成功");
                    } else {
                        LoggerUtils.logger("接口数据", "bt_dropCall调用失败:" + jsonObj.getString("returnDesc"));
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    //发送离线消息
    private void sendOfflineMessage() {
        try {
            OnLine messageInfo = new OnLine();
            messageInfo.setDeviceId(deviceId);
            messageInfo.setUserId(userId);
            messageInfo.setNickName(userName);
            StarNetMobile.getInstance().sendOfflineMsg(messageInfo);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //关闭Mqtt
    private void closeMqtt() {
        StarNetMobile.getInstance().closeMqtt();
    }


}
