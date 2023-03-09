package com.example.socketserviceappliacation03195020;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity{

    private EditText et_content, et_name;
    private TextView tv_se;
    private MyAPP myAPP;
    @Override
    protected void onCreate(Bundle saveInstanceState){
        super.onCreate(saveInstanceState);
        setContentView(R.layout.activity_main);
        et_content = (EditText) findViewById(R.id.et_content);
        tv_se = (TextView) findViewById(R.id.tv_se);
        et_name = (EditText) findViewById(R.id.et_name);
        myAPP = (MyAPP)getApplication();
    }
    public void click1(View view){
        myAPP.setOnConnectListener(new MyAPP.ConnectListener() {
            @Override
            public void onReceiveData(String data) {
                tv_se.append(data + "\n");
            }
        });
    }

    public void click2(View view){
        String name = et_name.getText().toString();
        String a = name + ":" + et_content.getText().toString();
        tv_se.append(a + "\n");
        myAPP.send(a);
    }
}
