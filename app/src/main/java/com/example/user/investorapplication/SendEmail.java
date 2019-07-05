package com.example.user.investorapplication;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.EditText;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import stanford.androidlib.SimpleActivity;

public class SendEmail extends SimpleActivity
{
    @BindView(R.id.et_subject)
    EditText et_subject;
    @BindView(R.id.et_message)
    EditText et_message;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_send_email);
        ButterKnife.bind(this);
    }

    @OnClick(R.id.btn_send)
    public void sendMail()
    {
        String emailId = getIntent().getStringExtra("EMAIL").toString().trim();
        String subject = et_subject.getText().toString();
        String message = et_message.getText().toString();

        Intent email = new Intent(Intent.ACTION_SEND);
        email.putExtra(Intent.EXTRA_EMAIL, new String[]{emailId});
        email.putExtra(Intent.EXTRA_SUBJECT, subject);
        email.putExtra(Intent.EXTRA_TEXT, message);
        //email.setType("message/rfc822");
        email.setType("text/plain");
        startActivity(Intent.createChooser(email, "Choose an Email client :"));
    }
}
