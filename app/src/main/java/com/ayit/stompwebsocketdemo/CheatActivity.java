package com.ayit.stompwebsocketdemo;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.robot.baseapi.base.BaseApplication;
import com.robot.baseapi.util.SPManager;

import org.java_websocket.WebSocket;

import rx.Subscriber;
import rx.functions.Action1;
import ua.naiksoftware.stomp.LifecycleEvent;
import ua.naiksoftware.stomp.Stomp;
import ua.naiksoftware.stomp.client.StompClient;
import ua.naiksoftware.stomp.client.StompMessage;

public class CheatActivity extends AppCompatActivity {

    public static final String TAG = "CheatActivity";

    private EditText url;
    private EditText cheat;
    private Button start;
    private Button send;
    private LinearLayout message;
    private StompClient mStompClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cheat);
        bindView();

        String url_ = SPManager.getString("url_cheat");

        if (!TextUtils.isEmpty(url_)){
            url.setText(url_);
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
                // 向/app/cheat发送Json数据
                mStompClient.send("/app/cheat","{\"userId\":\"lincoln\",\"message\":\""+cheat.getText()+"\"}")
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

        url = (EditText) findViewById(R.id.et_url);
        cheat = (EditText) findViewById(R.id.et_cheat);
        send = (Button) findViewById(R.id.btn_send);
        message = (LinearLayout) findViewById(R.id.ll_message);
    }

    private void createStompClient() {
        final String urlStr = url.getText().toString();
        mStompClient = Stomp.over(WebSocket.class, urlStr);
        mStompClient.connect();
        Toast.makeText(CheatActivity.this,"开始连接 "+urlStr,Toast.LENGTH_SHORT).show();
        mStompClient.lifecycle().subscribe(new Action1<LifecycleEvent>() {
            @Override
            public void call(LifecycleEvent lifecycleEvent) {
                switch (lifecycleEvent.getType()) {
                    case OPENED:
                        Log.d(TAG, "Stomp connection opened");
                        toast("连接已开启");
                        SPManager.put("url_main",urlStr);
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
        mStompClient.topic("/user/xiaoli/message").subscribe(new Action1<StompMessage>() {
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
                TextView text = new TextView(CheatActivity.this);
                text.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
                text.setText(System.currentTimeMillis() +" body is --->"+stompMessage.getPayload());
                message.addView(text);
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

