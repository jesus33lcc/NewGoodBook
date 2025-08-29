package com.example.newgoodbooks.Fragments.AdapterList;

import android.content.Context;
import android.app.Activity;
import android.content.ContextWrapper;
import androidx.core.app.ActivityOptionsCompat;
import androidx.core.view.ViewCompat;
import com.example.newgoodbooks.Fragments.HomeIU.HomeFragment;
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

    //Aviso de pulsacion larga. En cuadricula no hay deslizamiento posible, asi que esta
    //es la via para las acciones sobre un libro.
    public interface AlMantenerPulsado {
        void enPosicion(int posicion);
    }

    private AlMantenerPulsado alMantener;

    public void setAlMantenerPulsado(AlMantenerPulsado escucha) {
        this.alMantener = escucha;
    }
    private Context context;
    private List<Libro> listLibroDatos;
    //true = cuadricula. Se infla otro layout, pero los ids son los mismos, asi que
    //onBindViewHolder no cambia.
    private boolean cuadricula;

    public void setCuadricula(boolean cuadricula) {
        this.cuadricula = cuadricula;
    }

    public LibroListAdapter(Context context, List<Libro> listLibrosFill){
        this.context = context;
        this.listLibroDatos = listLibrosFill;
    }
    @NonNull
    @Override
    public LibroViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new LibroViewHolder(LayoutInflater.from(context).inflate(
                cuadricula ? R.layout.item_book_grid : R.layout.item_book_list, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull LibroViewHolder holder, int position) {
        Libro itemLibro = listLibroDatos.get(position);
        holder.tituloLibro.setText(itemLibro.getTitulo());
        holder.autorLibro.setText(primerAutor(itemLibro));
        Picasso.get().load(itemLibro.getLinkImg()).into(holder.portadaLibro);
        //Un nombre distinto por fila. Si todas las portadas visibles se llamaran igual,
        //el sistema no sabria cual es la que viaja y al volver aterrizaria en otra.
        ViewCompat.setTransitionName(holder.portadaLibro, "portada_" + itemLibro.getId());

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent viewLibroData = new Intent(v.getContext(), LibroData.class);
                viewLibroData.putExtra("libro", itemLibro);
                abrirConLaPortada(viewLibroData, holder.portadaLibro);
            }
        });
        holder.itemView.setOnLongClickListener(alMantener == null ? null : v -> {
            //se resuelve al pulsar, no al enlazar: la lista puede haber cambiado
            int actual = holder.getBindingAdapterPosition();
            if (actual != RecyclerView.NO_POSITION) {
                alMantener.enPosicion(actual);
            }
            return true;
        });
    }

    //La portada crece hasta su sitio en la ficha en vez de que la pantalla salte.
    //Solo se puede hacer desde una Activity: si el contexto no lo es (o el aparato no
    //da para la animacion) se abre de la forma de siempre.
    private void abrirConLaPortada(Intent destino, View portada) {
        Activity actividad = actividadDe(context);
        if (actividad == null) {
            context.startActivity(destino);
            return;
        }
        actividad.startActivity(destino, ActivityOptionsCompat
                .makeSceneTransitionAnimation(actividad, portada, HomeFragment.TRANSICION_PORTADA)
                .toBundle());
    }

    private static Activity actividadDe(Context contexto) {
        while (contexto instanceof ContextWrapper) {
            if (contexto instanceof Activity) {
                return (Activity) contexto;
            }
            contexto = ((ContextWrapper) contexto).getBaseContext();
        }
        return null;
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
