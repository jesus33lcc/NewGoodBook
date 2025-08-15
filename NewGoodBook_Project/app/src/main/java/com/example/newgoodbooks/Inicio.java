package com.example.newgoodbooks;

import com.example.newgoodbooks.databinding.ActivityInicioBinding;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class Inicio extends AppCompatActivity {
    private ActivityInicioBinding binding;
    Button btn_Login, btn_Register;
    TextView nombre, eslogan, desc;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityInicioBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        // asignacion de las variables locales
        btn_Login=binding.btnIniciosesion;
        btn_Register=binding.btnRegistrarse;
        nombre=binding.txtviewNombre;
        eslogan=binding.txtviewDescripcionInicio;
        desc=binding.textviewDesc;
        //metodo click, lo lleva al Login
        btn_Login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(Inicio.this, Login.class));
                finish();
            }
        });
        //metodo click, lo lleva al Register
        btn_Register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(Inicio.this, Registro.class));
                finish();
            }
        });
    }
}