package com.example.newgoodbooks;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class Inicio extends AppCompatActivity {
    Button btn_Login, btn_Register;
    TextView nombre, eslogan, desc;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_inicio);
        btn_Login=findViewById(R.id.btn_iniciosesion);
        btn_Register=findViewById(R.id.btn_registrarse);

        nombre=findViewById(R.id.txtview_nombre);
        eslogan=findViewById(R.id.txtview_descripcion_inicio);
        desc=findViewById(R.id.textview_desc);

        Typeface fuenteActual=Typeface.createFromAsset(getAssets(),"fonts/"+getResources().getString(R.string.fuente1));
        nombre.setTypeface(fuenteActual);
        eslogan.setTypeface(fuenteActual);
        desc.setTypeface(fuenteActual);
        btn_Login.setTypeface(fuenteActual);
        btn_Register.setTypeface(fuenteActual);

        btn_Login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(Inicio.this, Login.class));
                finish();
            }
        });

        btn_Register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(Inicio.this, Registro.class));
                finish();
            }
        });
    }
}