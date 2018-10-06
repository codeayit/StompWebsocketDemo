package com.ayit.stompwebsocketdemo;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Space;
import android.widget.TextView;
import android.widget.Toast;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.ayit.klog.KLog;
import com.robot.baseapi.base.BaseApplication;
import com.robot.baseapi.util.SPManager;

import org.java_websocket.WebSocket;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import rx.Subscriber;
import rx.functions.Action1;
import ua.naiksoftware.stomp.LifecycleEvent;
import ua.naiksoftware.stomp.Stomp;
import ua.naiksoftware.stomp.StompHeader;
import ua.naiksoftware.stomp.client.StompClient;
import ua.naiksoftware.stomp.client.StompMessage;

public class MainActivity extends AppCompatActivity {

    public static final String TAG = "MainActivity";

    private TextView serverMessage;
    private EditText etUrl;
    private EditText etReg;
    private EditText etSend;
    private Button start;
    private Button stop;
    private Button send;
    private EditText editText;
    private StompClient mStompClient;
    private Button cheat;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        bindView();
        KLog.init(this,true,"klog");
        String urlStr = SPManager.get("et_url");
        if (!TextUtils.isEmpty(urlStr)){
            etUrl.setText(urlStr);
        }
        String regStr = SPManager.get("et_reg");
        if (!TextUtils.isEmpty(regStr)){
            etReg.setText(regStr);
        }
        String sendStr = SPManager.get("et_send");
        if (!TextUtils.isEmpty(sendStr)){
            etSend.setText(sendStr);
        }

        JSONObject jo = new JSONObject();

        jo.put("deviceId","1");
        jo.put("sn","lnyserialno");
        jo.put("acccountId","255");
        jo.put("msgId","1");
        jo.put("data","123");
        editText.setText(jo.toJSONString());


        start.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //创建client 实例
                createStompClient();
                //订阅消息
                registerStompTopic();
            }
        });

        send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String sendStr = etSend.getText().toString();
                if (!TextUtils.isEmpty(sendStr)){
                    SPManager.put("et_send",sendStr);
                }

//                {
//                    "deviceId":1,
//                        "sn":"lnyserialno",
//                        "acccountId":255,
//                        "msgId":"1",
//                        "data":"123"
//                }
                KLog.d("发送消息："+sendStr);
                mStompClient.send(sendStr,editText.getText().toString())
                        .subscribe(new Subscriber<Void>() {
                            @Override
                            public void onCompleted() {
                                toast("发送成功");
                            }

                            @Override
                            public void onError(Throwable e) {
                                e.printStackTrace();
                                toast("发送错误");
                            }

                            @Override
                            public void onNext(Void aVoid) {

                            }
                        });
            }
        });

        stop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mStompClient.disconnect();
            }
        });

        cheat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this,CheatActivity.class));
                if(mStompClient != null) {
                    mStompClient.disconnect();
                }
                finish();
            }
        });
    }

    private void showMessage(final StompMessage stompMessage) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                serverMessage.setText("stomp command is --->"+stompMessage.getStompCommand() +" body is --->"+stompMessage.getPayload());
            }
        });
    }

    //创建client 实例
    private void createStompClient() {

        final String url = etUrl.getText().toString();



        List<StompHeader> headerList = new ArrayList<>();
        headerList.add(new StompHeader("username","lnyserialno"));
        headerList.add(new StompHeader("password","@2scindapsu"));

        Map<String,String> headers = new HashMap<>();

        headers.put("username","lnyserialno");
        headers.put("password","@2scindapsu");

        mStompClient = Stomp.over(WebSocket.class, url,headers);
        mStompClient.connect(headerList);
        toast("开始链接："+url);
        mStompClient.lifecycle().subscribe(new Action1<LifecycleEvent>() {
            @Override
            public void call(LifecycleEvent lifecycleEvent) {
                switch (lifecycleEvent.getType()) {
                    case OPENED:
                        Log.d(TAG, "Stomp connection opened");
                        toast("连接已开启");
                        SPManager.put("et_url",url);
                        break;

                    case ERROR:
                        Log.e(TAG, "Stomp Error", lifecycleEvent.getException());
                        toast("连接出错");
                        break;
                    case CLOSED:
                        Log.d(TAG, "Stomp connection closed");
                        toast("连接关闭");
                        break;
                }
            }
        });
    }

    //订阅消息
    private void registerStompTopic() {

        final String regStr = etReg.getText().toString();
        if (TextUtils.isEmpty(regStr)){
            SPManager.put("et_reg",regStr);
        }
        KLog.d("订阅地址："+regStr);
        mStompClient.topic(regStr).subscribe(new Action1<StompMessage>() {
            @Override
            public void call(StompMessage stompMessage) {
                KLog.d("call: " +stompMessage.getPayload() );
                showMessage(stompMessage);
            }
        });

    }

    private void toast(final String message) {
        KLog.d(message);
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(MainActivity.this,message,Toast.LENGTH_SHORT).show();
            }
        });
    }



    private void bindView() {
        serverMessage = (TextView) findViewById(R.id.tv_msg);
        start = (Button) findViewById(R.id.btn_start);
        stop = (Button) findViewById(R.id.btn_stop);
        send = (Button) findViewById(R.id.btn_send);
        editText = (EditText) findViewById(R.id.et_text);
        etUrl = (EditText) findViewById(R.id.et_url);
        etReg = findViewById(R.id.et_reg);
        etSend = findViewById(R.id.et_send);
        cheat = (Button) findViewById(R.id.btn_cheat);
    }
}
