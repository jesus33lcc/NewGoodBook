package com.example.newgoodbooks;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.newgoodbooks.Datos.RepositorioUsuario;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.GoogleAuthProvider;

public class Login extends AppCompatActivity {
    private static final String TAG = "Login";

    private Button btn_login, btn_loginGoogle;
    private TextView txt_noCuenta, txt_passwordlost;
    private EditText editTextEmail, editTextPassword;
    private FirebaseAuth mAuth;
    private GoogleSignInClient clienteGoogle;
    private ActivityResultLauncher<Intent> lanzadorGoogle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        // asignacion de las variables locales
        mAuth = FirebaseAuth.getInstance();
        btn_login = findViewById(R.id.btn_iniciosesion);
        btn_loginGoogle = findViewById(R.id.btn_iniciocongoogle);
        txt_passwordlost = findViewById(R.id.txt_passwordlost);
        txt_noCuenta = findViewById(R.id.txt_sincuenta);
        editTextEmail = findViewById(R.id.editxt_email);
        editTextPassword = findViewById(R.id.edittxt_password);

        prepararGoogle();

        //metodo click, recoge el email y la contraseña introducida.
        //si la cuenta esta registrada lo lleva a la vista Principal
        btn_login.setOnClickListener(v -> {
            String email = String.valueOf(editTextEmail.getText()).trim();
            String password = String.valueOf(editTextPassword.getText());

            if (TextUtils.isEmpty(email)) {
                Toast.makeText(Login.this, "Introduce un email", Toast.LENGTH_SHORT).show();
                return;
            }
            if (TextUtils.isEmpty(password)) {
                Toast.makeText(Login.this, "Introduce una contraseña", Toast.LENGTH_SHORT).show();
                return;
            }
            mAuth.signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener(tarea -> {
                        if (tarea.isSuccessful()) {
                            entrar("Inicio de Sesión Exitoso");
                        } else {
                            Toast.makeText(Login.this, "Inicio de Sesión Fallido", Toast.LENGTH_SHORT).show();
                        }
                    });
        });

        //metodo click, si no tiene una cuenta lo lleva al Registro
        txt_noCuenta.setOnClickListener(v -> {
            startActivity(new Intent(Login.this, Registro.class));
            finish();
        });
    }

    //Google Sign-In. El boton llevaba en el layout desde 2024 sin hacer nada porque
    //nunca se registro la huella SHA-1 del keystore en Firebase.
    private void prepararGoogle() {
        //default_web_client_id lo genera el plugin de google-services a partir del
        //cliente OAuth de tipo web de google-services.json
        GoogleSignInOptions opciones = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        clienteGoogle = GoogleSignIn.getClient(this, opciones);

        lanzadorGoogle = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                resultado -> {
                    Task<GoogleSignInAccount> tarea =
                            GoogleSignIn.getSignedInAccountFromIntent(resultado.getData());
                    try {
                        GoogleSignInAccount cuenta = tarea.getResult(ApiException.class);
                        if (cuenta != null && cuenta.getIdToken() != null) {
                            entrarConCredencialDeGoogle(cuenta.getIdToken());
                        }
                    } catch (ApiException e) {
                        //el codigo 12501 es que el usuario cerro el selector de cuenta
                        if (e.getStatusCode() != 12501) {
                            Log.w(TAG, "Fallo el inicio con Google", e);
                            Toast.makeText(this, "No se ha podido entrar con Google", Toast.LENGTH_SHORT).show();
                        }
                    }
                });

        btn_loginGoogle.setOnClickListener(v -> {
            //se cierra la sesion de Google previa para que siempre deje elegir cuenta
            clienteGoogle.signOut().addOnCompleteListener(
                    ignorado -> lanzadorGoogle.launch(clienteGoogle.getSignInIntent()));
        });
    }

    private void entrarConCredencialDeGoogle(@NonNull String idToken) {
        AuthCredential credencial = GoogleAuthProvider.getCredential(idToken, null);
        mAuth.signInWithCredential(credencial)
                .addOnCompleteListener(this, tarea -> {
                    if (tarea.isSuccessful()) {
                        entrar("Sesión iniciada con Google");
                    } else {
                        Log.w(TAG, "signInWithCredential fallo", tarea.getException());
                        Toast.makeText(this, "No se ha podido entrar con Google", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void entrar(String mensaje) {
        //engancha las escuchas de Firestore al usuario recien logueado
        RepositorioUsuario.get().conectar();
        Toast.makeText(this, mensaje, Toast.LENGTH_SHORT).show();
        startActivity(new Intent(this, Principal.class));
        finish();
    }
}
