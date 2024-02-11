package com.example.newgoodbooks;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import com.example.newgoodbooks.Modelos.Libro;
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
    public LibroData(){ }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_home);

        bookSelected = (Libro) getIntent().getSerializableExtra("libro");
        //this.libroDetail = getIntent().getParcelableExtra("libro");
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
        } else {
            Toast.makeText(this, "Error: bookSelected is NULL", Toast.LENGTH_SHORT).show();
            finish();
        }
    }
}