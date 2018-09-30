package com.ayit.stompwebsocketdemo;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Space;
import android.widget.TextView;
import android.widget.Toast;

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

public class CheatActivity extends AppCompatActivity {

    public static final String TAG = "CheatActivity";
    private TextView serverMessage;
    private EditText etUsername;
    private EditText etPassword;
    private EditText etUrl;
    private EditText etReg;
    private EditText etSend;
    private Button start;
    private Button stop;
    private Button send;
    private EditText editText;
    private StompClient mStompClient;
    private Button cheat;
///user/queue/pointShouts这是点对点的订阅地址
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cheat);
        bindView();
        String urlStr = SPManager.get("et_url_");
        if (!TextUtils.isEmpty(urlStr)){
            etUrl.setText(urlStr);
        }
        String regStr = SPManager.get("et_reg_");
        if (!TextUtils.isEmpty(regStr)){
            etReg.setText(regStr);
        }
        String sendStr = SPManager.get("et_send_");
        if (!TextUtils.isEmpty(sendStr)){
            etSend.setText(sendStr);
        }
        String usernameStr = SPManager.get("et_username_");
        if (!TextUtils.isEmpty(usernameStr)){
            etUsername.setText(usernameStr);
        }
        String passwordStr = SPManager.get("et_password_");
        if (!TextUtils.isEmpty(passwordStr)){
            etPassword.setText(passwordStr);
        }


        start.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                createStompClient();
                registerStompTopic();
            }
        });

        send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String sendStr = etSend.getText().toString();
                if (!TextUtils.isEmpty(sendStr)){
                    SPManager.put("et_send_",sendStr);
                }
                // 向/app/cheat发送Json数据
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
    }

    private void bindView() {

        serverMessage = (TextView) findViewById(R.id.tv_msg);
        start = (Button) findViewById(R.id.btn_start);
        stop = (Button) findViewById(R.id.btn_stop);
        send = (Button) findViewById(R.id.btn_send);
        editText = (EditText) findViewById(R.id.et_text);
        etUsername = (EditText) findViewById(R.id.et_username);
        etPassword = (EditText) findViewById(R.id.et_password);
        etUrl = (EditText) findViewById(R.id.et_url);
        etReg = findViewById(R.id.et_reg);
        etSend = findViewById(R.id.et_send);
        cheat = (Button) findViewById(R.id.btn_cheat);
    }

    private void createStompClient() {
        final String url = etUrl.getText().toString();

        String username = etUsername.getText().toString();
        String password = etUsername.getText().toString();

        if (!TextUtils.isEmpty(username)){
            SPManager.put("et_username_",username);
        }
        if (!TextUtils.isEmpty(password)){
            SPManager.put("et_password_",password);
        }


        Map<String,String> headers = new HashMap<>();

        headers.put("username",username);
        headers.put("password",password);

        mStompClient = Stomp.over(WebSocket.class, url,headers);
        List<StompHeader> headerList = new ArrayList<>();
        headerList.add(new StompHeader("username",username));
        headerList.add(new StompHeader("password",password));
        mStompClient.connect(headerList);
        Toast.makeText(CheatActivity.this,"开始连接 "+url,Toast.LENGTH_SHORT).show();
        mStompClient.lifecycle().subscribe(new Action1<LifecycleEvent>() {
            @Override
            public void call(LifecycleEvent lifecycleEvent) {
                switch (lifecycleEvent.getType()) {
                    case OPENED:
                        Log.d(TAG, "Stomp connection opened");
                        toast("连接已开启");
                        SPManager.put("et_url_",url);
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

    // 接收/user/xiaoli/message路径发布的消息
    private void registerStompTopic() {

        final String regStr = etReg.getText().toString();
        if (TextUtils.isEmpty(regStr)){
            SPManager.put("et_reg_",regStr);
        }
        Map<String,String> headers = new HashMap<>();
        headers.put("username","test");
        headers.put("password","test");
        mStompClient.topic(regStr).subscribe(new Action1<StompMessage>() {
            @Override
            public void call(StompMessage stompMessage) {
                Log.e(TAG, "call: " +stompMessage.getPayload() );
                showMessage(stompMessage);
            }
        });
    }

    private void showMessage(final StompMessage stompMessage) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                serverMessage.setText(System.currentTimeMillis() +" body is --->"+stompMessage.getPayload());
            }
        });
    }


    private void toast(final String message) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(CheatActivity.this,message,Toast.LENGTH_SHORT).show();
            }
        });
    }
}

