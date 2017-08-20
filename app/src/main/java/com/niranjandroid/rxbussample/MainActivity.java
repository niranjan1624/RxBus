package com.niranjandroid.rxbussample;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.EditText;
import android.widget.TextView;

import com.niranjandroid.rxbus.RxBus;

public class MainActivity extends AppCompatActivity {
    TextView message1, message2;
    private static final String MESSAGE_1_IDENTIFIER = "identifier1";
    private static final String MESSAGE_2_IDENTIFIER = "identifier2";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        message1 = (TextView) findViewById(R.id.message_1);
        message2 = (TextView) findViewById(R.id.message_2);
        EditText editText = (EditText) findViewById(R.id.editText);
        findViewById(R.id.publish1).setOnClickListener(view -> {
            RxBus.getDefault().publish(new TestModel(editText.getText().toString()), MESSAGE_1_IDENTIFIER);
        });

        findViewById(R.id.publish2).setOnClickListener(view -> {
            RxBus.getDefault().publish(editText.getText().toString(), MESSAGE_2_IDENTIFIER);
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        RxBus.getDefault().register(this);
        RxBus.getDefault().subscribe(object -> {
            TestModel testModel = (TestModel) object;
            message1.setText("Message 1: \n" + testModel.getMessage());
        }, MESSAGE_1_IDENTIFIER);
        RxBus.getDefault().subscribe(object -> {
            message2.setText("Message 2: \n" + String.valueOf(object));
        }, MESSAGE_2_IDENTIFIER);
    }
    @Override
    protected void onStop() {
        super.onStop();
        RxBus.getDefault().unRegister(this);
    }
}
