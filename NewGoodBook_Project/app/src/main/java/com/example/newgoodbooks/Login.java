package com.example.newgoodbooks;

import com.example.newgoodbooks.databinding.ActivityLoginBinding;

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
    private ActivityLoginBinding binding;
    private static final String TAG = "Login";

    private Button botonAcceder, botonGoogle;
    private TextView enlaceSinCuenta, enlacePasswordOlvidada;
    private EditText editTextEmail, editTextPassword;
    private FirebaseAuth mAuth;
    private GoogleSignInClient clienteGoogle;
    private ActivityResultLauncher<Intent> lanzadorGoogle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityLoginBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        // asignacion de las variables locales
        mAuth = FirebaseAuth.getInstance();
        botonAcceder = binding.btnIniciosesion;
        botonGoogle = binding.btnIniciocongoogle;
        enlacePasswordOlvidada = binding.txtPasswordlost;
        enlaceSinCuenta = binding.txtSincuenta;
        editTextEmail = binding.editxtEmail;
        editTextPassword = binding.edittxtPassword;

        prepararGoogle();

        //metodo click, recoge el email y la contraseña introducida.
        //si la cuenta esta registrada lo lleva a la vista Principal
        botonAcceder.setOnClickListener(v -> {
            String email = String.valueOf(editTextEmail.getText()).trim();
            String password = String.valueOf(editTextPassword.getText());

            if (TextUtils.isEmpty(email)) {
                Toast.makeText(Login.this, getString(R.string.pide_email), Toast.LENGTH_SHORT).show();
                return;
            }
            if (TextUtils.isEmpty(password)) {
                Toast.makeText(Login.this, getString(R.string.pide_password), Toast.LENGTH_SHORT).show();
                return;
            }
            mAuth.signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener(tarea -> {
                        if (tarea.isSuccessful()) {
                            entrar(getString(R.string.login_ok));
                        } else {
                            Toast.makeText(Login.this, getString(R.string.login_ko), Toast.LENGTH_SHORT).show();
                        }
                    });
        });

        //el enlace de contrasena olvidada estaba en el layout sin listener, igual que
        //le pasaba al boton de Google: existia pero no hacia nada
        enlacePasswordOlvidada.setOnClickListener(v -> pedirRestablecerPassword());

        //metodo click, si no tiene una cuenta lo lleva al Registro
        enlaceSinCuenta.setOnClickListener(v -> {
            startActivity(new Intent(Login.this, Registro.class));
            finish();
        });
    }

    //Envia el correo de restablecimiento de Firebase a la direccion escrita
    private void pedirRestablecerPassword() {
        String email = String.valueOf(editTextEmail.getText()).trim();
        if (TextUtils.isEmpty(email)) {
            Toast.makeText(this, R.string.pide_email_para_recuperar, Toast.LENGTH_SHORT).show();
            editTextEmail.requestFocus();
            return;
        }
        mAuth.sendPasswordResetEmail(email)
                .addOnCompleteListener(tarea -> {
                    //siempre el mismo mensaje: decir si el correo existe o no permitiria
                    //averiguar que cuentas estan registradas
                    if (!tarea.isSuccessful()) {
                        Log.w(TAG, "Fallo al enviar el correo de recuperacion", tarea.getException());
                    }
                    Toast.makeText(this, R.string.recuperar_enviado, Toast.LENGTH_LONG).show();
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
                            Toast.makeText(this, getString(R.string.login_google_ko), Toast.LENGTH_SHORT).show();
                        }
                    }
                });

        botonGoogle.setOnClickListener(v -> {
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
                        entrar(getString(R.string.login_google_ok));
                    } else {
                        Log.w(TAG, "signInWithCredential fallo", tarea.getException());
                        Toast.makeText(this, getString(R.string.login_google_ko), Toast.LENGTH_SHORT).show();
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
