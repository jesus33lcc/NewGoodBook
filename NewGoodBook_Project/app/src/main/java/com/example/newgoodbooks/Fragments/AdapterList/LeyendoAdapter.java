package com.example.newgoodbooks.Fragments.AdapterList;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.newgoodbooks.LibroData;
import com.example.newgoodbooks.Modelos.Lectura;
import com.example.newgoodbooks.Modelos.Libro;
import com.example.newgoodbooks.R;
import com.google.android.material.progressindicator.LinearProgressIndicator;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

//Los libros a medias, con su barra de progreso. Es la unica lista de la aplicacion
//que cambia cada dia, y por eso va la primera en la pantalla de listas.
public class LeyendoAdapter extends RecyclerView.Adapter<LeyendoAdapter.LeyendoViewHolder> {
    private final Context contexto;
    private List<Libro> libros;
    private Map<String, Lectura> lecturas;

    public LeyendoAdapter(Context contexto, List<Libro> libros, Map<String, Lectura> lecturas) {
        this.contexto = contexto;
        this.libros = libros != null ? libros : new ArrayList<Libro>();
        this.lecturas = lecturas;
    }

    public void actualizar(List<Libro> libros, Map<String, Lectura> lecturas) {
        this.libros = libros != null ? libros : new ArrayList<Libro>();
        this.lecturas = lecturas;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public LeyendoViewHolder onCreateViewHolder(@NonNull ViewGroup padre, int tipo) {
        return new LeyendoViewHolder(
                LayoutInflater.from(contexto).inflate(R.layout.item_leyendo, padre, false));
    }

    @Override
    public void onBindViewHolder(@NonNull LeyendoViewHolder holder, int posicion) {
        final Libro libro = libros.get(posicion);
        holder.titulo.setText(libro.getTitulo());
        if (libro.getLinkImg() != null && !libro.getLinkImg().trim().isEmpty()) {
            Picasso.get().load(libro.getLinkImg()).into(holder.portada);
        }
        Lectura lectura = lecturas != null ? lecturas.get(libro.getId()) : null;
        int porcentaje = lectura != null ? lectura.porcentaje(libro.getNumPag()) : 0;
        holder.barra.setProgress(porcentaje);
        holder.progreso.setText(contexto.getString(R.string.formato_progreso,
                lectura != null ? lectura.getPagina() : 0, libro.getNumPag(), porcentaje));

        holder.itemView.setOnClickListener(v -> {
            Intent ver = new Intent(v.getContext(), LibroData.class);
            ver.putExtra("libro", libro);
            contexto.startActivity(ver);
        });
    }

    @Override
    public int getItemCount() {
        return libros.size();
    }

    public static class LeyendoViewHolder extends RecyclerView.ViewHolder {
        private final ImageView portada;
        private final TextView titulo;
        private final TextView progreso;
        private final LinearProgressIndicator barra;

        public LeyendoViewHolder(@NonNull View itemView) {
            super(itemView);
            portada = itemView.findViewById(R.id.portadaLeyendo);
            titulo = itemView.findViewById(R.id.tituloLeyendo);
            progreso = itemView.findViewById(R.id.progresoLeyendo);
            barra = itemView.findViewById(R.id.barraLeyendo);
        }
    }
}
