package com.example.newgoodbooks.Fragments.AdapterList;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.newgoodbooks.Modelos.Libro;
import com.example.newgoodbooks.R;
import com.google.android.material.card.MaterialCardView;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

//Rejilla de portadas para elegir gustos. Solo hace falta aqui, asi que no se mete en
//LibroListAdapter: aquel abre la ficha al tocar y este alterna una seleccion.
public class PortadaAdapter extends RecyclerView.Adapter<PortadaAdapter.PortadaViewHolder> {
    private final Context contexto;
    private final List<Libro> libros;
    private final Set<Libro> elegidos = new LinkedHashSet<>();

    public PortadaAdapter(Context contexto, List<Libro> libros) {
        this.contexto = contexto;
        this.libros = libros != null ? libros : new ArrayList<Libro>();
    }

    public List<Libro> getElegidos() {
        return new ArrayList<>(elegidos);
    }

    @NonNull
    @Override
    public PortadaViewHolder onCreateViewHolder(@NonNull ViewGroup padre, int tipo) {
        return new PortadaViewHolder(
                LayoutInflater.from(contexto).inflate(R.layout.item_portada, padre, false));
    }

    @Override
    public void onBindViewHolder(@NonNull PortadaViewHolder holder, int posicion) {
        final Libro libro = libros.get(posicion);
        if (libro.getLinkImg() != null && !libro.getLinkImg().trim().isEmpty()) {
            Picasso.get().load(libro.getLinkImg()).into(holder.portada);
        } else {
            holder.portada.setImageDrawable(null);
        }
        holder.itemView.setContentDescription(libro.getTitulo());
        pintar(holder, elegidos.contains(libro));

        holder.itemView.setOnClickListener(v -> {
            if (elegidos.contains(libro)) {
                elegidos.remove(libro);
            } else {
                elegidos.add(libro);
            }
            pintar(holder, elegidos.contains(libro));
        });
    }

    private void pintar(PortadaViewHolder holder, boolean elegido) {
        holder.velo.setVisibility(elegido ? View.VISIBLE : View.GONE);
        holder.tarjeta.setStrokeWidth(elegido ? 6 : 0);
        holder.tarjeta.setStrokeColor(ContextCompat.getColor(contexto, R.color.md_tertiary));
    }

    @Override
    public int getItemCount() {
        return libros.size();
    }

    public static class PortadaViewHolder extends RecyclerView.ViewHolder {
        private final ImageView portada;
        private final View velo;
        private final MaterialCardView tarjeta;

        public PortadaViewHolder(@NonNull View itemView) {
            super(itemView);
            portada = itemView.findViewById(R.id.portadaElegible);
            velo = itemView.findViewById(R.id.veloElegido);
            tarjeta = itemView.findViewById(R.id.tarjetaPortada);
        }
    }
}
