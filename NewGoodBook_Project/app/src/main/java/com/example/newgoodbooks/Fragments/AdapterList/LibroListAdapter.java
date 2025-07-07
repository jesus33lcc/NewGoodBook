package com.example.newgoodbooks.Fragments.AdapterList;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.RecyclerView;
import com.example.newgoodbooks.LibroData;
import com.example.newgoodbooks.Modelos.Libro;
import com.example.newgoodbooks.R;
import com.squareup.picasso.Picasso;
import java.util.ArrayList;
import java.util.List;

public class LibroListAdapter extends RecyclerView.Adapter<LibroListAdapter.LibroViewHolder> {
    private Context context;
    private List<Libro> listLibroDatos;
    public LibroListAdapter(Context context, List<Libro> listLibrosFill){
        this.context = context;
        this.listLibroDatos = listLibrosFill;
    }
    @NonNull
    @Override
    public LibroViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new LibroViewHolder(LayoutInflater.from(context).inflate(R.layout.item_book_list,parent,false));
    }

    @Override
    public void onBindViewHolder(@NonNull LibroViewHolder holder, int position) {
        Libro itemLibro = listLibroDatos.get(position);
        holder.tituloLibro.setText(itemLibro.getTitulo());
        holder.autorLibro.setText(primerAutor(itemLibro));
        Picasso.get().load(itemLibro.getLinkImg()).into(holder.portadaLibro);

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent viewLibroData = new Intent(v.getContext(), LibroData.class);
                viewLibroData.putExtra("libro", itemLibro);
                context.startActivity(viewLibroData);
            }
        });
    }

    private static String primerAutor(Libro libro) {
        List<String> autores = libro.getAutor();
        return (autores == null || autores.isEmpty()) ? "" : autores.get(0);
    }

    //Actualiza calculando que filas cambian de verdad, en vez de repintar la lista
    //entera: se conservan el scroll y las animaciones.
    public void actualizar(List<Libro> nuevos) {
        final List<Libro> anteriores = this.listLibroDatos;
        final List<Libro> siguientes = nuevos != null ? nuevos : new ArrayList<Libro>();

        DiffUtil.DiffResult diferencia = DiffUtil.calculateDiff(new DiffUtil.Callback() {
            @Override
            public int getOldListSize() { return anteriores.size(); }

            @Override
            public int getNewListSize() { return siguientes.size(); }

            @Override
            public boolean areItemsTheSame(int posAntigua, int posNueva) {
                //Libro compara por id
                return anteriores.get(posAntigua).equals(siguientes.get(posNueva));
            }

            @Override
            public boolean areContentsTheSame(int posAntigua, int posNueva) {
                Libro a = anteriores.get(posAntigua);
                Libro b = siguientes.get(posNueva);
                return String.valueOf(a.getTitulo()).equals(String.valueOf(b.getTitulo()))
                        && String.valueOf(a.getLinkImg()).equals(String.valueOf(b.getLinkImg()))
                        && primerAutor(a).equals(primerAutor(b));
            }
        });
        this.listLibroDatos = siguientes;
        diferencia.dispatchUpdatesTo(this);
    }

    @Override
    public int getItemCount() {
        return this.listLibroDatos.size();
    }

    public class LibroViewHolder extends RecyclerView.ViewHolder{
        private ImageView portadaLibro;
        private TextView tituloLibro;
        private TextView autorLibro;
        public LibroViewHolder(@NonNull View itemView) {
            super(itemView);
            portadaLibro = itemView.findViewById(R.id.portadaBook_img);
            tituloLibro = itemView.findViewById(R.id.tituloBook_txt);
            autorLibro = itemView.findViewById(R.id.autorBook_txt);
        }
    }
}
