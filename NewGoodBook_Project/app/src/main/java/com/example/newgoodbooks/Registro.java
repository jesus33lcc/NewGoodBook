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

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class Registro extends AppCompatActivity {
    TextView txt_cuentaCreada;
    Button btn_registrarse;
    EditText editTextEmail, editTextPassword;
    FirebaseAuth mAuth;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registro);
        mAuth=FirebaseAuth.getInstance();
        txt_cuentaCreada=findViewById(R.id.txtview_cuentacreada);
        btn_registrarse=findViewById(R.id.btn_registrarse_register);
        editTextEmail=findViewById(R.id.edittxt_emailregister);
        editTextPassword=findViewById(R.id.edittxt_passwordRegister);



        txt_cuentaCreada.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(Registro.this, Login.class));
                finish();
            }
        });

        btn_registrarse.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String email, password;
                email=String.valueOf(editTextEmail.getText());
                password=String.valueOf(editTextPassword.getText());

                if(TextUtils.isEmpty(email)){
                    Toast.makeText(Registro.this, "Introduce un email", Toast.LENGTH_SHORT).show();
                    return;
                }
                if(TextUtils.isEmpty(password)){
                    Toast.makeText(Registro.this, "Introduce una contraseña", Toast.LENGTH_SHORT).show();
                    return;
                }
                mAuth.createUserWithEmailAndPassword(email, password)
                        .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                if (task.isSuccessful()) {
                                    Toast.makeText(Registro.this, "Registro con Exito", Toast.LENGTH_SHORT).show();
                                    startActivity(new Intent(Registro.this, Login.class));
                                    finish();

                                } else {
                                    Toast.makeText(Registro.this, "Registro fallido", Toast.LENGTH_SHORT).show();

                                }
                            }
                        });
            }
        });

    }

}