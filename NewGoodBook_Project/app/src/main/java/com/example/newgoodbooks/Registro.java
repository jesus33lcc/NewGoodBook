package com.example.newgoodbooks;

import com.example.newgoodbooks.databinding.ActivityRegistroBinding;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import androidx.annotation.NonNull;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.example.newgoodbooks.Datos.RepositorioUsuario;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;

public class Registro extends AppCompatActivity {
    private ActivityRegistroBinding binding;
    TextView txt_cuentaCreada;
    Button btn_registrarse;
    EditText editTextNombre, editTextEmail, editTextPassword;
    FirebaseAuth mAuth;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityRegistroBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        // asignacion de las variables locales
        mAuth=FirebaseAuth.getInstance();
        txt_cuentaCreada=binding.txtviewCuentacreada;
        btn_registrarse=binding.btnRegistrarseRegister;
        editTextNombre=binding.edittxtName;
        editTextEmail=binding.edittxtEmailregister;
        editTextPassword=binding.edittxtPasswordRegister;

        //metodo click, si tiene una cuenta lo lleva al Login
        txt_cuentaCreada.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(Registro.this, Login.class));
                finish();
            }
        });
        //coge los campos de email y password e intenta crear una cuenta, luego de eso lo lleva al login
        btn_registrarse.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final String nombre;
                String email, password;
                nombre=String.valueOf(editTextNombre.getText()).trim();
                email=String.valueOf(editTextEmail.getText());
                password=String.valueOf(editTextPassword.getText());

                if(TextUtils.isEmpty(nombre)){
                    Toast.makeText(Registro.this, getString(R.string.pide_nombre), Toast.LENGTH_SHORT).show();
                    return;
                }
                if(TextUtils.isEmpty(email)){
                    Toast.makeText(Registro.this, getString(R.string.pide_email), Toast.LENGTH_SHORT).show();
                    return;
                }
                if(TextUtils.isEmpty(password)){
                    Toast.makeText(Registro.this, getString(R.string.pide_password), Toast.LENGTH_SHORT).show();
                    return;
                }
                mAuth.createUserWithEmailAndPassword(email, password)
                        .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                if (task.isSuccessful()) {
                                    guardarNombre(nombre);
                                    Toast.makeText(Registro.this, getString(R.string.registro_ok), Toast.LENGTH_SHORT).show();
                                    //createUserWithEmailAndPassword ya deja la sesion abierta:
                                    //mandar al login obligaba a escribir las credenciales otra vez.
                                    //Se va directo a elegir gustos, que es lo que necesita el
                                    //recomendador para no empezar a ciegas.
                                    RepositorioUsuario.get().conectar();
                                    startActivity(new Intent(Registro.this, Onboarding.class));
                                    finish();

                                } else {
                                    Toast.makeText(Registro.this, getString(R.string.registro_ko), Toast.LENGTH_SHORT).show();

                                }
                            }
                        });
            }
        });

    }

    //el nombre que se pedia en el formulario no lo leia nadie: se tecleaba y se tiraba.
    //createUserWithEmailAndPassword ya deja la sesion iniciada, asi que aqui ya hay
    //usuario al que ponerselo. Va al perfil de Auth y no a Firestore para que este
    //disponible sin tener que leer nada, y para que sobreviva al borrado de datos.
    private void guardarNombre(String nombre) {
        FirebaseUser usuario = mAuth.getCurrentUser();
        if (usuario == null) {
            return;
        }
        usuario.updateProfile(new UserProfileChangeRequest.Builder()
                .setDisplayName(nombre)
                .build());
    }

}