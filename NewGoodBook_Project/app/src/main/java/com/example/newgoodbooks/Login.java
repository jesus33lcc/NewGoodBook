package com.example.newgoodbooks;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.newgoodbooks.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class Login extends AppCompatActivity {
    Button btn_login, btn_loginGoogle;
    TextView txt_noCuenta, txt_passwordlost;
    EditText editTextEmail, editTextPassword;
    FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        // asignacion de las variables locales
        mAuth=FirebaseAuth.getInstance();
        btn_login=findViewById(R.id.btn_iniciosesion);
        btn_loginGoogle=findViewById(R.id.btn_iniciocongoogle);
        txt_passwordlost=findViewById(R.id.txt_passwordlost);
        txt_noCuenta=findViewById(R.id.txt_sincuenta);
        editTextEmail=findViewById(R.id.editxt_email);
        editTextPassword=findViewById(R.id.edittxt_password);

        //metodo click, recoge el email y la contraseña introducida.
        //si la cuenta esta registrada lo lleva a la vista Principal
        btn_login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email, password;
                email=String.valueOf(editTextEmail.getText());
                password=String.valueOf(editTextPassword.getText());

                if(TextUtils.isEmpty(email)){
                    Toast.makeText(Login.this, "Introduce un email", Toast.LENGTH_SHORT).show();
                    return;
                }
                if(TextUtils.isEmpty(password)){
                    Toast.makeText(Login.this, "Introduce una contraseña", Toast.LENGTH_SHORT).show();
                    return;
                }
                mAuth.signInWithEmailAndPassword(email, password)
                        .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                if (task.isSuccessful()) {
                                    Toast.makeText(Login.this,"Inicio de Sesión Exitoso", Toast.LENGTH_SHORT).show();
                                    startActivity(new Intent(Login.this, Principal.class));
                                    finish();
                                } else {Toast.makeText(Login.this, "Inicio de Sesión Fallido", Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
            }
        });
        //metodo click, si no tiene una cuenta lo lleva al Registro
        txt_noCuenta.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(Login.this, Registro.class));
                finish();
            }
        });
    }
}