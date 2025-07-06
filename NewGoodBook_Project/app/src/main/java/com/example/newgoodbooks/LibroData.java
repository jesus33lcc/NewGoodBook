package com.example.newgoodbooks;

import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.example.newgoodbooks.Datos.RepositorioUsuario;
import com.example.newgoodbooks.Modelos.Libro;
import com.example.newgoodbooks.Modelos.Lista;
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
    Button btnNext;
    ToggleButton btnFav;
    ToggleButton btnCheck;
    private ImageButton btnAddList;
    public LibroData(){ }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_home);

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
        btnNext = findViewById(R.id.btnSiguiente);
        btnFav=findViewById(R.id.tBtnFavorite);
        btnCheck=findViewById(R.id.tBtnCheck);
        btnAddList=findViewById(R.id.tBtnAddList);
        btnNext.setVisibility(View.GONE);
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
            btnAddList.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    showAlertDialogSingleChoice_addToList();
                }
            });
        } else {
            Toast.makeText(this, "Error: bookSelected is NULL", Toast.LENGTH_SHORT).show();
            finish();
        }
    }
    private void showAlertDialogSingleChoice_addToList() {
        String[] nomListas = RepositorioUsuario.get().getNombresListasPersonales();
        if (nomListas.length == 0) {
            Toast.makeText(this, "Todavia no tienes ninguna lista. Crea una en Listas.",
                    Toast.LENGTH_SHORT).show();
            return;
        }
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(LibroData.this); // Cambiado getBaseContext() a LibroData.this
        alertDialogBuilder.setTitle("Añadir a lista");
        alertDialogBuilder.setIcon(R.drawable.ic_listas);
        alertDialogBuilder.setSingleChoiceItems(nomListas, -1, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int index) {
                addLibro_To_Lista(index);
                dialog.cancel();
            }
        });
        alertDialogBuilder.setNegativeButton("Cancelar", new DialogInterface.OnClickListener(){
            @Override
            public void onClick(DialogInterface dialog, int i){
                dialog.cancel();
            }
        });
        AlertDialog addDialog = alertDialogBuilder.create();
        addDialog.show();
    }
    //metodo que añade el libro a la lista seleccionada
    private void addLibro_To_Lista(int index){
        RepositorioUsuario repo = RepositorioUsuario.get();
        Lista listaSelected = repo.getListaPersonalPorIndice(index);
        if (listaSelected == null || bookSelected == null) {
            return;
        }
        repo.anadirLibroALista(listaSelected.getId(), bookSelected);

        Toast.makeText(getBaseContext(), "Añadido a '" + listaSelected.getNombre() + "'", Toast.LENGTH_SHORT).show();
    }
}