package com.example.newgoodbooks;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.example.newgoodbooks.R;

public class Login extends AppCompatActivity {
    Button btn_login, btn_loginGoogle;
    TextView txt_noCuenta, txt_passwordlost;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        btn_login=findViewById(R.id.btn_iniciosesion);
        btn_loginGoogle=findViewById(R.id.btn_iniciocongoogle);

        txt_passwordlost=findViewById(R.id.txt_passwordlost);
        txt_noCuenta=findViewById(R.id.txt_sincuenta);

        btn_login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(Login.this, Principal.class));
                finish();
            }
        });
        btn_loginGoogle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(Login.this, Principal.class));
                finish();
            }
        });
        txt_noCuenta.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(Login.this, Registro.class));
                finish();

            }
        });
        txt_passwordlost.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });
    }
}