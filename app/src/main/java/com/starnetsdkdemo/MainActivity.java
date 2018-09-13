package com.starnetsdkdemo;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.huawei.esdk.cc.common.NotifyMessage;
import com.huawei.esdk.cc.utils.StringUtils;
import com.starnet.OnMessageListener;
import com.starnet.StarNetMobile;
import com.starnet.entity.CallData;
import com.starnet.entity.CallInfoEntity;
import com.starnet.entity.MessageEntity;
import com.starnet.entity.MessageInfo;
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

public class MainActivity extends Activity implements View.OnClickListener {
    TextView tv;
    Button bt_getServiceInfo, bt_login, bt_loginOut, bt_uploadFile;
    Button bt_sendTextMsg, bt_getQueueInfo, bt_cancelQueue;
    Button bt_recordCall, bt_updateCall;
    Button bt_Satisfaction, bt_querySessionById;
    Button bt_queryChatBySno, bt_querySeatStatue;
    Button bt_submitUserSettingInfo, bt_queryUserSettingInfo;
    //Button bt_audioCall, bt_videoCall;
    Button bt_dropCall;
    Button bt_clear;

    EditText et_host;
    Button bt_confirm;
    LinearLayout l_content;

    //LinearLayout view_audio;
    //Button switch_audio, mute_audio, mute_speaker;

    //个人信息
    private String deviceId = android.os.Build.SERIAL;
    private String userId = "15807106474";
    String userName = "test";
    String callSno = "111";
    String accNo = "11";
    String agentId = "8001";


    //服务器配置
    private String icsServerUrl;
    private String iswServerUrl;
    private String sipServerUrl;
    private String xunFeiVoiceServerUrl;
    private String mqttServerUrl;

    //Mqtt
    public static final int INITMQTT = 1; //初始化mqtt
    //public static final int INITSDK = 3; //初始化sdk


    //登录
    public static final int SESSION = 2; //登录成功

    //拨打语音、视频广播
    String[] actions = new String[]{
            NotifyMessage.CALL_MSG_ON_CONNECTED, NotifyMessage.CALL_MSG_ON_QUEUING,
            NotifyMessage.CALL_MSG_ON_SUCCESS, NotifyMessage.CALL_MSG_ON_APPLY_MEETING,
            NotifyMessage.CALL_MSG_USER_JOIN, NotifyMessage.CALL_MSG_USER_STATUS,
            NotifyMessage.CALL_MSG_ON_DISCONNECTED};

    //上传
    public static final int UPLOAD = 8; //


    private int mute = 0;//1静音0恢复
    private int speaker = 0;//0是扬声器 1 是听筒
    private int speakerMute = 0;//1静音0恢复

    StringBuffer buffer = new StringBuffer();
    //String host = "http://172.16.40.123:8090";
    String host = "http://10.31.61.3:8080";


    Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case INITMQTT:
                    try {
                        StarNetMobile.getInstance().initMqtt(deviceId, mqttServerUrl, "", "", new OnMessageListener() {

                            @Override
                            public void handleMessage(String topic, String message) {
                                LoggerUtils.logger("回调数据", message);

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
                                    sendMessageToTextView(new Gson().toJson(messageEntity));
                                    LoggerUtils.logger("回调数据", "message类型:" + msgType);
                                    switch (msgType) {
                                        case MsgTypeEnums.CallConnected://通话建立成功的处理
                                            accNo = messageEntity.getAccessSno();
                                            callSno = messageEntity.getCallSno();
                                            agentId = messageEntity.getAgentId();
                                            LoggerUtils.logger("回调数据", "accNo:" + accNo);
                                            LoggerUtils.logger("回调数据", "callSno:" + callSno);
                                            LoggerUtils.logger("回调数据", "agentId:" + agentId);

                                            break;
                                        case MsgTypeEnums.AgentCallAnswered://处理坐席应答的消息
                                            break;
                                        case MsgTypeEnums.CallDisconnected://通话结束的处理
                                            break;
                                        case MsgTypeEnums.CallFailed://呼叫失败的处理
                                            break;
                                        case MsgTypeEnums.CallQueuing://呼叫排队的处理
                                            break;
                                        case MsgTypeEnums.QueueTimeOut://排队超时的处理
                                            break;
                                        default://默认处理文字消息

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
                    try {
                        OnLine messageInfo = new OnLine();
                        messageInfo.setDeviceId(deviceId);
                        messageInfo.setUserId(userId);
                        messageInfo.setHead("http://img.zcool.cn/community/010a1b554c01d1000001bf72a68b37.jpg@1280w_1l_2o_100sh.png");
                        messageInfo.setNickName(userName);
                        StarNetMobile.getInstance().sendOnlineMsg(messageInfo);
                        LoggerUtils.logger("在线消息", messageInfo.getUserId());
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    break;
                case SESSION:

                    break;
                /*case INITSDK:

                    break;*/
                case 100:
                    tv.setText(String.valueOf(msg.obj));
                    break;
                case UPLOAD:
                    String path = String.valueOf(msg.obj);
                    LoggerUtils.logger("接口数据", path);
                    try {
                        StarNetMobile.getInstance().postUpLoadFile(path, new okhttp3.Callback() {
                            @Override
                            public void onFailure(Call call, IOException e) {
                                e.printStackTrace();
                            }

                            @Override
                            public void onResponse(Call call, Response response) throws IOException {
                                try {
                                    String jsonStr = response.body().string();
                                    sendMessageToTextView(jsonStr);
                                    LoggerUtils.logger("接口数据", jsonStr);
                                    JSONObject jsonObj = new JSONObject(jsonStr);
                                    if ("0".equals(jsonObj.getString("returnCode"))) {
                                        LoggerUtils.logger("接口数据", "bt_uploadFile成功");
                                    } else {
                                        LoggerUtils.logger("接口数据", "bt_uploadFile调用失败:" + jsonObj.getString("returnDesc"));
                                    }
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                            }
                        });
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                    break;
                default:
                    break;
            }
        }
    };



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initView();

    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    private void initView() {
        tv = (TextView) findViewById(R.id.tv);
        bt_getServiceInfo = (Button) findViewById(R.id.bt_getServiceInfo);
        bt_login = (Button) findViewById(R.id.bt_login);
        bt_loginOut = (Button) findViewById(R.id.bt_loginOut);
        bt_uploadFile = (Button) findViewById(R.id.bt_uploadFile);
        bt_sendTextMsg = (Button) findViewById(R.id.bt_sendTextMsg);
        bt_getQueueInfo = (Button) findViewById(R.id.bt_getQueueInfo);
        bt_cancelQueue = (Button) findViewById(R.id.bt_cancelQueue);
        bt_recordCall = (Button) findViewById(R.id.bt_recordCall);
        bt_updateCall = (Button) findViewById(R.id.bt_updateCall);
        bt_Satisfaction = (Button) findViewById(R.id.bt_Satisfaction);
        bt_querySessionById = (Button) findViewById(R.id.bt_querySessionById);
        bt_queryChatBySno = (Button) findViewById(R.id.bt_queryChatBySno);
        bt_querySeatStatue = (Button) findViewById(R.id.bt_querySeatStatue);
        bt_submitUserSettingInfo = (Button) findViewById(R.id.bt_submitUserSettingInfo);
        bt_queryUserSettingInfo = (Button) findViewById(R.id.bt_queryUserSettingInfo);
        /*bt_audioCall = (Button) findViewById(bt_audioCall);
        bt_videoCall = (Button) findViewById(bt_videoCall);*/
        bt_dropCall = (Button) findViewById(R.id.bt_dropCall);
        bt_clear = (Button) findViewById(R.id.bt_clear);
        //view_audio = (LinearLayout) findViewById(view_audio);
        /*switch_audio = (Button) findViewById(switch_audio);
        mute_audio = (Button) findViewById(mute_audio);
        mute_speaker = (Button) findViewById(mute_speaker);*/

        et_host = (EditText) findViewById(R.id.et_host);
        bt_confirm = (Button) findViewById(R.id.bt_confirm);
        l_content = (LinearLayout) findViewById(R.id.l_content);

        bt_getServiceInfo.setOnClickListener(this);
        bt_login.setOnClickListener(this);
        bt_loginOut.setOnClickListener(this);
        bt_uploadFile.setOnClickListener(this);
        bt_sendTextMsg.setOnClickListener(this);
        bt_getQueueInfo.setOnClickListener(this);
        bt_cancelQueue.setOnClickListener(this);
        bt_recordCall.setOnClickListener(this);
        bt_updateCall.setOnClickListener(this);
        bt_Satisfaction.setOnClickListener(this);
        bt_querySessionById.setOnClickListener(this);
        bt_queryChatBySno.setOnClickListener(this);
        bt_querySeatStatue.setOnClickListener(this);
        bt_submitUserSettingInfo.setOnClickListener(this);
        bt_queryUserSettingInfo.setOnClickListener(this);
        /*bt_videoCall.setOnClickListener(this);
        bt_audioCall.setOnClickListener(this);*/
        bt_dropCall.setOnClickListener(this);
        bt_clear.setOnClickListener(this);
       /* switch_audio.setOnClickListener(this);
        mute_audio.setOnClickListener(this);
        mute_speaker.setOnClickListener(this);*/
        bt_confirm.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.bt_confirm://设置服务器地址
                if (StringUtils.isStringEmpty(et_host.getText().toString())) {
                    return;
                }
                host = et_host.getText().toString();
                StarNetMobile.getInstance().setHost(host);
                l_content.setVisibility(View.VISIBLE);
                break;
            case R.id.bt_getServiceInfo://获取服务器信息
                StarNetMobile.getInstance().getServerConfigInfo(new Callback() {
                    @Override
                    public void onFailure(Call call, IOException e) {
                        e.printStackTrace();
                    }

                    @Override
                    public void onResponse(Call call, Response response) throws IOException {
                        try {
                            String jsonStr = response.body().string();
                            sendMessageToTextView(jsonStr);
                            LoggerUtils.logger("接口数据", jsonStr);
                            JSONObject jsonObject = new JSONObject(jsonStr);
                            if ("0".equals(jsonObject.getString("returnCode"))) {
                                JSONObject returnData = (JSONObject) jsonObject.get("returnData");
                                String icsServer = returnData.getString("icsServer");
                                String iswServer = returnData.getString("iswServer");
                                String sipServer = returnData.getString("sipServer");
                                xunFeiVoiceServerUrl = returnData.getString("xunFeiVoiceServer");
                                mqttServerUrl = returnData.getString("mqttServer");
                                JSONObject icsServer_jsn = new JSONObject(icsServer);
                                String icsIp = icsServer_jsn.getString("ip");
                                String icsPort = icsServer_jsn.getString("port");
                                icsServerUrl = icsIp + ":" + icsPort;
                                JSONObject sipServer_jsn = new JSONObject(sipServer);
                                String sipIp = sipServer_jsn.getString("ip");
                                String sipPort = sipServer_jsn.getString("port");
                                sipServerUrl = sipIp + ":" + sipPort;
                                LoggerUtils.logger("接口数据", "icsServerUrl:" + icsServerUrl);
                                LoggerUtils.logger("接口数据", "sipServerUrl:" + sipServerUrl);
                                handler.sendEmptyMessage(INITMQTT);
                                //handler.sendEmptyMessage(INITSDK);
                            } else {
                                LoggerUtils.logger("接口数据", "getServerConfigInfo调用失败:" + jsonObject.getString("returnDesc"));
                            }

                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                });
                break;
            case R.id.bt_login://登录

                CallData callData = new CallData();
                callData.setAgentId("");
                callData.setChannel("App");
                callData.setCity("wuHan");
                callData.setCountry("china");
                callData.setCustLevel(1);
                callData.setCustNo(userId);
                callData.setHead("http://img.zcool.cn/community/010a1b554c01d1000001bf72a68b37.jpg@1280w_1l_2o_100sh.png");
                callData.setNick(userName);
                callData.setOgCallNo(userId);
                callData.setProvince("hubei");
                callData.setSex("1");
                String forw = "||||^";
                Gson gson = new Gson();
                String jsoncallData = forw + gson.toJson(callData);
                LoggerUtils.logger("接口", jsoncallData);

                UserInfo info = new UserInfo();
                info.setChannel("App");
                info.setCallData(jsoncallData);
                info.setNickName(userName);
                info.setLanguage("zh");
                //info.setSystemAccessCode("3000");
                info.setSystemAccessCode("1003");
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
                                    sendMessageToTextView(jsonStr);
                                    LoggerUtils.logger("接口数据", jsonStr);
                                    JSONObject jsonObj = new JSONObject(jsonStr);
                                    if ("0".equals(jsonObj.getString("returnCode"))) {
                                        /*JSONObject returnData = (JSONObject) jsonObj.get("returnData");
                                        CallData callData = new Gson().fromJson(returnData.getString("callData").split("\\^")[1], CallData.class);
                                        callSno = callData.getCallSno();
                                        accNo = String.valueOf(callData.getAccNo());*/
                                        handler.sendEmptyMessage(SESSION);
                                    } else {
                                        LoggerUtils.logger("接口数据", "creatSession调用失败:" + jsonObj.getString("returnDesc"));
                                    }
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                            }
                        }

                );
                break;
            case R.id.bt_loginOut://登出
                StarNetMobile.getInstance().loginOut(userId, new Callback() {
                    @Override
                    public void onFailure(Call call, IOException e) {

                    }

                    @Override
                    public void onResponse(Call call, Response response) throws IOException {
                        try {
                            String jsonStr = response.body().string();
                            sendMessageToTextView(jsonStr);
                            LoggerUtils.logger("接口数据", jsonStr);
                            JSONObject jsonObj = new JSONObject(jsonStr);
                            if ("0".equals(jsonObj.getString("returnCode"))) {
                                LoggerUtils.logger("接口数据", "loginOut成功");
                            } else {
                                LoggerUtils.logger("接口数据", "loginOut调用失败:" + jsonObj.getString("returnDesc"));
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                });
                break;
            case R.id.bt_uploadFile://上传文件
                Intent upload = new Intent(Intent.ACTION_GET_CONTENT);
                upload.setDataAndType(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, "image/*");
                startActivityForResult(upload, 0);
                break;
            case R.id.bt_sendTextMsg://发送消息
                try {
                    MqttMessageSend mqttMessageSend = new MqttMessageSend();
                    Data data = new Data();
                    DateUtils dateUtils = new DateUtils();
                    long longtime = dateUtils.getTime();
                    data.setMsgContent("发送一条消息");
                    mqttMessageSend.setData(data);
                    mqttMessageSend.setType(1);
                    //mqttMessageSend.setMsgId("773943bd‐96a4‐4926‐8d7f‐30b84248f1qs");
                    mqttMessageSend.setMsgId("16041_46");
                    mqttMessageSend.setTime(longtime);
                    mqttMessageSend.setUserId(userId);
                    mqttMessageSend.setDeviceId(deviceId);
                    mqttMessageSend.setCallSno(Integer.parseInt(callSno));
                    mqttMessageSend.setAccessSno(Integer.parseInt(accNo));
                    //mqttMessageSend.setForwardSno("");
                    mqttMessageSend.setAgentId(agentId);
                    mqttMessageSend.setSendForType(2);
                    Gson gson_msg = new Gson();
                    String sendData = gson_msg.toJson(mqttMessageSend);

                    LoggerUtils.logger("sendData", sendData);
                    StarNetMobile.getInstance().sendTextMsg(sendData);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                break;
            case R.id.bt_getQueueInfo://获取排队信息
                StarNetMobile.getInstance().getCallQueueInfo(userId, new Callback() {
                    @Override
                    public void onFailure(Call call, IOException e) {

                    }

                    @Override
                    public void onResponse(Call call, Response response) throws IOException {
                        try {
                            String jsonStr = response.body().string();
                            sendMessageToTextView(jsonStr);
                            LoggerUtils.logger("接口数据", jsonStr);
                            JSONObject jsonObj = new JSONObject(jsonStr);
                            if ("0".equals(jsonObj.getString("returnCode"))) {
                                LoggerUtils.logger("接口数据", "getCallQueueInfo成功");
                            } else {
                                LoggerUtils.logger("接口数据", "getCallQueueInfo调用失败:" + jsonObj.getString("returnDesc"));
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                });
                break;
            case R.id.bt_cancelQueue://取消排队
                StarNetMobile.getInstance().cancelQueue(userId, new Callback() {
                    @Override
                    public void onFailure(Call call, IOException e) {

                    }

                    @Override
                    public void onResponse(Call call, Response response) throws IOException {
                        try {
                            String jsonStr = response.body().string();
                            sendMessageToTextView(jsonStr);
                            LoggerUtils.logger("接口数据", jsonStr);
                            JSONObject jsonObj = new JSONObject(jsonStr);
                            if ("0".equals(jsonObj.getString("returnCode"))) {
                                LoggerUtils.logger("接口数据", "bt_cancelQueue成功");
                            } else {
                                LoggerUtils.logger("接口数据", "bt_cancelQueue调用失败:" + jsonObj.getString("returnDesc"));
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                });
                break;
            case R.id.bt_recordCall://新增视频、语句呼叫记录接口
                CallInfoEntity entity = new CallInfoEntity();
                entity.setCallSno(callSno);
                entity.setChatType("2");
                entity.setChatUserName("zsp-电话坐席");
                entity.setChatUser("2014");
                StarNetMobile.getInstance().addCallInfo(entity, new Callback() {
                    @Override
                    public void onFailure(Call call, IOException e) {

                    }

                    @Override
                    public void onResponse(Call call, Response response) throws IOException {
                        try {
                            String jsonStr = response.body().string();
                            sendMessageToTextView(jsonStr);
                            LoggerUtils.logger("接口数据", jsonStr);
                            JSONObject jsonObj = new JSONObject(jsonStr);
                            if ("0".equals(jsonObj.getString("returnCode"))) {
                                LoggerUtils.logger("接口数据", "bt_recordCall成功");
                            } else {
                                LoggerUtils.logger("接口数据", "bt_recordCall调用失败:" + jsonObj.getString("returnDesc"));
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                });

                break;
            case R.id.bt_updateCall://更新语音，视频通话时长
                StarNetMobile.getInstance().updateCallDuration(callSno, "15971999", new Callback() {
                    @Override
                    public void onFailure(Call call, IOException e) {

                    }

                    @Override
                    public void onResponse(Call call, Response response) throws IOException {
                        try {
                            String jsonStr = response.body().string();
                            sendMessageToTextView(jsonStr);
                            LoggerUtils.logger("接口数据", jsonStr);
                            JSONObject jsonObj = new JSONObject(jsonStr);
                            if ("0".equals(jsonObj.getString("returnCode"))) {
                                LoggerUtils.logger("接口数据", "bt_updateCall成功");
                            } else {
                                LoggerUtils.logger("接口数据", "bt_updateCall调用失败:" + jsonObj.getString("returnDesc"));
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                });

                break;
            case R.id.bt_Satisfaction://满意度调查接口

                break;
            case R.id.bt_querySessionById://获取通话流水
                StarNetMobile.getInstance().fingCallLogByAccount(userId, 1, 2, new Callback() {
                    @Override
                    public void onFailure(Call call, IOException e) {

                    }

                    @Override
                    public void onResponse(Call call, Response response) throws IOException {
                        try {
                            String jsonStr = response.body().string();
                            sendMessageToTextView(jsonStr);
                            LoggerUtils.logger("接口数据", jsonStr);
                            JSONObject jsonObj = new JSONObject(jsonStr);
                            if ("0".equals(jsonObj.getString("returnCode"))) {
                                LoggerUtils.logger("接口数据", "bt_querySessionById成功");
                            } else {
                                LoggerUtils.logger("接口数据", "bt_querySessionById调用失败:" + jsonObj.getString("returnDesc"));
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                });
                break;
            case R.id.bt_queryChatBySno://查询消息记录接口
                MessageInfo messageEntityInfo = new MessageInfo();
                //messageEntityInfo.setCallSno(callSno);
                messageEntityInfo.setAccSno(accNo);
                //messageEntityInfo.setOppoNo("8068");
                messageEntityInfo.setPageNow(1);
                messageEntityInfo.setPageSize(20);
                StarNetMobile.getInstance().queryMessageInfo(messageEntityInfo, new Callback() {
                    @Override
                    public void onFailure(Call call, IOException e) {

                    }

                    @Override
                    public void onResponse(Call call, Response response) throws IOException {
                        try {
                            String jsonStr = response.body().string();
                            sendMessageToTextView(jsonStr);
                            LoggerUtils.logger("接口数据", jsonStr);
                            JSONObject jsonObj = new JSONObject(jsonStr);
                            if ("0".equals(jsonObj.getString("returnCode"))) {
                                LoggerUtils.logger("接口数据", "bt_queryChatBySno成功");
                            } else {
                                LoggerUtils.logger("接口数据", "bt_queryChatBySno调用失败:" + jsonObj.getString("returnDesc"));
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                });

                break;
            case R.id.bt_querySeatStatue://查询坐席信息接口
                StarNetMobile.getInstance().queryAgentInfo(agentId, new Callback() {
                    @Override
                    public void onFailure(Call call, IOException e) {

                    }

                    @Override
                    public void onResponse(Call call, Response response) throws IOException {
                        try {
                            String jsonStr = response.body().string();
                            sendMessageToTextView(jsonStr);
                            LoggerUtils.logger("接口数据", jsonStr);
                            JSONObject jsonObj = new JSONObject(jsonStr);
                            if ("0".equals(jsonObj.getString("returnCode"))) {
                                LoggerUtils.logger("接口数据", "bt_querySeatStatue成功");
                            } else {
                                LoggerUtils.logger("接口数据", "bt_querySeatStatue调用失败:" + jsonObj.getString("returnDesc"));
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                });
                break;
            case R.id.bt_submitUserSettingInfo://用户配置信息上传
                UserSettingInfo userSettingInfo = new UserSettingInfo();
                userSettingInfo.setUserId(userId);
                userSettingInfo.setNickName(userName);
                StarNetMobile.getInstance().submitUserConfigInfo(userSettingInfo, new Callback() {
                    @Override
                    public void onFailure(Call call, IOException e) {

                    }

                    @Override
                    public void onResponse(Call call, Response response) throws IOException {
                        try {
                            String jsonStr = response.body().string();
                            sendMessageToTextView(jsonStr);
                            LoggerUtils.logger("接口数据", jsonStr);
                            JSONObject jsonObj = new JSONObject(jsonStr);
                            if ("0".equals(jsonObj.getString("returnCode"))) {
                                LoggerUtils.logger("接口数据", "bt_submitUserSettingInfo成功");
                            } else {
                                LoggerUtils.logger("接口数据", "bt_submitUserSettingInfo调用失败:" + jsonObj.getString("returnDesc"));
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                });
                break;
            case R.id.bt_queryUserSettingInfo://获取用户配置信息接口
                StarNetMobile.getInstance().getUserSetting(userId, new Callback() {
                    @Override
                    public void onFailure(Call call, IOException e) {

                    }

                    @Override
                    public void onResponse(Call call, Response response) throws IOException {
                        try {
                            String jsonStr = response.body().string();
                            sendMessageToTextView(jsonStr);
                            LoggerUtils.logger("接口数据", jsonStr);
                            JSONObject jsonObj = new JSONObject(jsonStr);
                            if ("0".equals(jsonObj.getString("returnCode"))) {
                                LoggerUtils.logger("接口数据", "bt_queryUserSettingInfo成功");
                            } else {
                                LoggerUtils.logger("接口数据", "bt_queryUserSettingInfo调用失败:" + jsonObj.getString("returnDesc"));
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                });
                break;
          /*  case bt_audioCall:
                int flag_audio = StarNetMobile.getInstance().makeCall("3000", "3", " ", " ");
                Toast.makeText(MainActivity.this, "" + flag_audio, Toast.LENGTH_SHORT).show();
                break;
            case bt_videoCall:
                int flag_video = StarNetMobile.getInstance().makeCall("3000", "0", " ", " ");
                Toast.makeText(MainActivity.this, "" + flag_video, Toast.LENGTH_SHORT).show();
                break;*/

            case R.id.bt_dropCall://结束通话
                StarNetMobile.getInstance().dropCall(userId, new Callback() {
                    @Override
                    public void onFailure(Call call, IOException e) {

                    }

                    @Override
                    public void onResponse(Call call, Response response) throws IOException {
                        try {
                            String jsonStr = response.body().string();
                            sendMessageToTextView(jsonStr);
                            LoggerUtils.logger("接口数据", jsonStr);
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
                try {
                    OnLine messageInfo = new OnLine();
                    messageInfo.setDeviceId(deviceId);
                    messageInfo.setUserId(userId);
                    messageInfo.setHead("http://img.zcool.cn/community/010a1b554c01d1000001bf72a68b37.jpg@1280w_1l_2o_100sh.png");
                    messageInfo.setNickName(userName);
                    StarNetMobile.getInstance().sendOfflineMsg(messageInfo);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                break;
            case R.id.bt_clear:
                buffer = new StringBuffer();
                tv.setText("");
                break;

            default:
                break;
          /*  case switch_audio:
                ++speaker;
                if (0 == speaker % 2) {
                    StarNetMobile.getInstance().changeAudioRoute(1);
                } else {
                    StarNetMobile.getInstance().changeAudioRoute(0);
                }
                Toast.makeText(MainActivity.this, (0 == speaker % 2 ? "speaker_mode" : "receiver_mode"), Toast.LENGTH_SHORT)
                        .show();
                break;
            case mute_audio:
                ++mute;
                if (1 == mute % 2) {
                    StarNetMobile.getInstance().setMicMute(true);
                    mute_audio.setText("恢复");
                } else {
                    StarNetMobile.getInstance().setMicMute(false);
                    mute_audio.setText("麦克风静音");
                }
                break;
            case mute_speaker:
                ++speakerMute;
                if (1 == speakerMute % 2) {
                    StarNetMobile.getInstance().setSpeakerMute(true);
                    mute_speaker.setText("恢复");
                } else {
                    StarNetMobile.getInstance().setSpeakerMute(false);
                    mute_speaker.setText("扬声器静音");
                }
                break;*/
        }
    }

    public void sendMessageToTextView(String str) {
        buffer.append(str + "\n" + "\n");
        Message message = new Message();
        message.what = 100;
        message.obj = buffer.toString();
        handler.sendMessage(message);
    }

    /**
     * 图片选择及拍照结果
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {
            Uri uri = null;
            switch (requestCode) {
                case 0://相册
                    if (data == null) {
                        return;
                    }
                    uri = data.getData();
                    String[] proj = {MediaStore.Images.Media.DATA};
                    Cursor cursor = managedQuery(uri, proj, null, null, null);
                    int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
                    cursor.moveToFirst();
                    String path = cursor.getString(column_index);// 图片在的路径
                    Message message = new Message();
                    message.what = UPLOAD;
                    message.obj = path;
                    handler.sendMessage(message);
                    break;
                default:
                    break;
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }
}
