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
import com.example.newgoodbooks.Modelos.Libro;
import com.example.newgoodbooks.R;
import com.squareup.picasso.Picasso;

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
        holder.autorLibro.setText(itemLibro.getAutor().toString());
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
/*
class LibroDiffCallback extends DiffUtil.ItemCallback<Libro>{
    @Override
    public boolean areItemsTheSame(Libro oldItem,Libro newItem) {
        return oldItem.getId() == newItem.getId();
    }

    @Override
    public boolean areContentsTheSame(Libro oldItem,Libro newItem) {
        return oldItem.equals(newItem);
    }
*/
}
