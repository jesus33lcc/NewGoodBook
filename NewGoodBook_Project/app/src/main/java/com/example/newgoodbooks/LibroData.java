package com.example.newgoodbooks;

import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.example.newgoodbooks.Datos.RepositorioUsuario;
import com.example.newgoodbooks.Modelos.Libro;
import com.example.newgoodbooks.UI.AccionesLibro;
import com.squareup.picasso.Picasso;

public class LibroData extends AppCompatActivity {
    private View view;
    private Libro bookSelected;
    ImageView portadaIMG;
    TextView tituloTXT;
    TextView autorTXT;
    TextView numPagTXT;
    TextView fechaPubTXT;
    TextView generosTXT;
    TextView descripcionTXT;
    ToggleButton btnFav;
    ToggleButton btnCheck;
    private ImageButton btnAddList;
    public LibroData(){ }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_libro_data);

        bookSelected = (Libro) getIntent().getSerializableExtra("libro");
        initView();
        setDetailsLibro();
    }

    private void initView(){
        portadaIMG = findViewById(R.id.imageVPortada);
        tituloTXT = findViewById(R.id.textTitulo);
        autorTXT = findViewById(R.id.textAutor);
        numPagTXT = findViewById(R.id.textNumPag);
        fechaPubTXT = findViewById(R.id.textFechaPub);
        generosTXT = findViewById(R.id.textGeneros);
        descripcionTXT = findViewById(R.id.textDescripcion);
        btnFav=findViewById(R.id.tBtnFavorite);
        btnCheck=findViewById(R.id.tBtnCheck);
        btnAddList=findViewById(R.id.tBtnAddList);
    }

    private void setDetailsLibro(){
        if (bookSelected != null) {
            Picasso.get().load(bookSelected.getLinkImg()).into(portadaIMG);
            tituloTXT.setText(bookSelected.getTitulo());
            autorTXT.setText(bookSelected.getAutor().toString());
            numPagTXT.setText(String.valueOf(bookSelected.getNumPag()));
            fechaPubTXT.setText(bookSelected.getFechaPublicacion());
            generosTXT.setText(bookSelected.getGeneros().toString());
            descripcionTXT.setText(bookSelected.getDescripcion());


            //el estado de los dos toggles lo manda Firestore: si lo marcas aqui,
            //la pantalla Home y el otro dispositivo se enteran solos
            RepositorioUsuario repo = RepositorioUsuario.get();
            repo.getFavoritos().observe(this,
                    libros -> btnFav.setChecked(libros != null && libros.contains(bookSelected)));
            repo.getLeidos().observe(this,
                    libros -> btnCheck.setChecked(libros != null && libros.contains(bookSelected)));

            btnFav.setOnClickListener(v -> repo.alternarFavorito(bookSelected));
            btnCheck.setOnClickListener(v -> repo.alternarLeido(bookSelected));
            btnAddList.setOnClickListener(v ->
                    AccionesLibro.mostrarDialogoAnadirALista(LibroData.this, bookSelected));
        } else {
            Toast.makeText(this, "Error: bookSelected is NULL", Toast.LENGTH_SHORT).show();
            finish();
        }
    }
}
