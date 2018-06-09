package com.example.misio.newhope;

import android.app.ProgressDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import java.util.HashMap;
import java.util.Map;

public class LoginActivity extends AppCompatActivity {

    private Button signinButton;
    private EditText loginText, passText;
    private ProgressDialog nDialog;

    private Notifications notifications;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        notifications = new Notifications(this);

        signinButton = findViewById(R.id.signinButton);
        signinButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final String login = loginText.getText().toString();
                final String pass = passText.getText().toString();

                RequestQueue queue = Volley.newRequestQueue(LoginActivity.this);
                final String url = "http://healthband-app.herokuapp.com/HBPulse/check-user/";
                StringRequest postRequest = new StringRequest(Request.Method.POST, url,
                        new Response.Listener<String>()
                        {
                            @Override
                            public void onResponse(String response) {
                                // response
                                Log.d("Response", response);
                                nDialog.dismiss();

                                if(response.contains("User OK")){
                                    Settings.saveSetting(Settings.USER_LOGIN_KEY, loginText.getText().toString(), LoginActivity.this);
                                    Settings.saveSetting(Settings.USER_PASS_KEY, passText.getText().toString(), LoginActivity.this);

                                    notifications.showToast("LOGGED IN");
                                } else {
                                    notifications.showToast("FAILED TO LOGIN IN");
                                }

                            }
                        },
                        new Response.ErrorListener()
                        {
                            @Override
                            public void onErrorResponse(VolleyError error) {
                                // error
                                Log.d("Error.Response", "error");
                                nDialog.dismiss();

                                notifications.showToast("CONNECTION ERROR");
                            }
                        }
                ) {
                    @Override
                    protected Map<String, String> getParams()
                    {
                        Map<String, String>  params = new HashMap<String, String>();
                        params.put("username", login);
                        params.put("password", pass);

                        return params;
                    }
                };
                queue.add(postRequest);

                nDialog = new ProgressDialog(LoginActivity.this);
                nDialog.setMessage("Loading..");
                nDialog.setTitle("Get Data");
                nDialog.setIndeterminate(false);
                nDialog.setCancelable(true);
                nDialog.show();
            }
        });

        loginText = findViewById(R.id.loginText);
        passText = findViewById(R.id.passText);
    }
}
