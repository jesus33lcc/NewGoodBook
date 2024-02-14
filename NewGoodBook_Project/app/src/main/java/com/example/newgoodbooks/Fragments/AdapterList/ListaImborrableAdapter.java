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
import com.example.newgoodbooks.ContenidoLista;
import com.example.newgoodbooks.Modelos.Lista;
import com.example.newgoodbooks.R;
import com.squareup.picasso.Picasso;

import java.util.List;

public class ListaImborrableAdapter extends RecyclerView.Adapter<ListaImborrableAdapter.ListaViewHolder> {
    private Context context;
    private List<Lista> listListaDatos;
    public ListaImborrableAdapter(Context context, List<Lista> listListaFill){
        this.context = context;
        this.listListaDatos = listListaFill;
    }

    @NonNull
    @Override
    public ListaImborrableAdapter.ListaViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ListaImborrableAdapter.ListaViewHolder(LayoutInflater.from(context).inflate(R.layout.item_lista_list,parent,false));
    }

    @Override
    public void onBindViewHolder(@NonNull ListaImborrableAdapter.ListaViewHolder holder, int position) {
        Lista itemLista = (Lista) listListaDatos.get(position);

        String nomLista = itemLista.getNombre();
        holder.nombreLista.setText(nomLista);

        if(nomLista.equalsIgnoreCase("Libros Favoritos")){
            holder.iconoLista.setImageResource(R.drawable.ic_favorite_on);
        }

        if(nomLista.equalsIgnoreCase("Libros Leidos")){
            holder.iconoLista.setImageResource(R.drawable.ic_librarycheck);
        }

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent viewListaData = new Intent(v.getContext(), ContenidoLista.class);
                viewListaData.putExtra("item_lista", itemLista);
                context.startActivity(viewListaData);
            }
        });
    }

    @Override
    public int getItemCount() { return this.listListaDatos.size(); }

    public class ListaViewHolder extends RecyclerView.ViewHolder{
        private ImageView iconoLista;
        private TextView nombreLista;
        public ListaViewHolder(@NonNull View itemView) {
            super(itemView);
            iconoLista = itemView.findViewById(R.id.iconoTypeLista_img);
            nombreLista = itemView.findViewById(R.id.nombreLista_txt);
        }
    }
}
