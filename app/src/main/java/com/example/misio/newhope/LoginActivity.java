package com.example.misio.newhope;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

public class LoginActivity extends AppCompatActivity {

    private Button signinButton;
    private EditText loginText, passText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        signinButton = findViewById(R.id.signinButton);
        signinButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Settings.saveSetting(Settings.USER_LOGIN_KEY, loginText.getText().toString(), LoginActivity.this);
                Settings.saveSetting(Settings.USER_PASS_KEY, passText.getText().toString(), LoginActivity.this);
            }
        });

        loginText = findViewById(R.id.loginText);
        passText = findViewById(R.id.passText);
    }
}
