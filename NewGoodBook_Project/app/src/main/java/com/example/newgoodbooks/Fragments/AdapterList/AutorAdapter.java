package com.example.newgoodbooks.Fragments.AdapterList;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.newgoodbooks.R;
import com.google.android.material.card.MaterialCardView;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

//Rejilla de autores para elegir gustos. Cada autor viene con la portada de uno de sus
//libros, que es lo unico ilustrativo que tenemos: Google Books no da retratos.
public class AutorAdapter extends RecyclerView.Adapter<AutorAdapter.AutorViewHolder> {

    //Un autor y una portada suya, que es lo que se ensenia.
    public static class Autor {
        public final String nombre;
        public final String portada;

        public Autor(String nombre, String portada) {
            this.nombre = nombre;
            this.portada = portada;
        }
    }

    private final Context contexto;
    private final List<Autor> autores;
    private final Set<String> elegidos = new LinkedHashSet<>();

    public AutorAdapter(Context contexto, List<Autor> autores) {
        this.contexto = contexto;
        this.autores = autores != null ? autores : new ArrayList<Autor>();
    }

    public List<String> getElegidos() {
        return new ArrayList<>(elegidos);
    }

    @NonNull
    @Override
    public AutorViewHolder onCreateViewHolder(@NonNull ViewGroup padre, int tipo) {
        return new AutorViewHolder(
                LayoutInflater.from(contexto).inflate(R.layout.item_autor, padre, false));
    }

    @Override
    public void onBindViewHolder(@NonNull AutorViewHolder holder, int posicion) {
        final Autor autor = autores.get(posicion);
        holder.nombre.setText(autor.nombre);
        holder.itemView.setContentDescription(autor.nombre);
        if (autor.portada != null && !autor.portada.trim().isEmpty()) {
            Picasso.get().load(autor.portada).into(holder.fondo);
        } else {
            holder.fondo.setImageDrawable(null);
        }
        pintar(holder, elegidos.contains(autor.nombre));

        holder.itemView.setOnClickListener(v -> {
            if (elegidos.contains(autor.nombre)) {
                elegidos.remove(autor.nombre);
            } else {
                elegidos.add(autor.nombre);
            }
            pintar(holder, elegidos.contains(autor.nombre));
        });
    }

    private void pintar(AutorViewHolder holder, boolean elegido) {
        holder.velo.setVisibility(elegido ? View.VISIBLE : View.GONE);
        holder.tarjeta.setStrokeWidth(elegido ? 6 : 0);
        holder.tarjeta.setStrokeColor(ContextCompat.getColor(contexto, R.color.md_tertiary));
    }

    @Override
    public int getItemCount() {
        return autores.size();
    }

    public static class AutorViewHolder extends RecyclerView.ViewHolder {
        private final ImageView fondo;
        private final TextView nombre;
        private final View velo;
        private final MaterialCardView tarjeta;

        public AutorViewHolder(@NonNull View itemView) {
            super(itemView);
            fondo = itemView.findViewById(R.id.fondoAutor);
            nombre = itemView.findViewById(R.id.nombreAutor);
            velo = itemView.findViewById(R.id.veloAutor);
            tarjeta = itemView.findViewById(R.id.tarjetaAutor);
        }
    }
}
