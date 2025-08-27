package com.example.newgoodbooks.Fragments.AdapterList;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.RecyclerView;

import com.example.newgoodbooks.ContenidoLista;
import com.example.newgoodbooks.Modelos.Libro;
import com.squareup.picasso.Picasso;
import com.example.newgoodbooks.Modelos.Lista;
import com.example.newgoodbooks.R;

import java.util.ArrayList;
import java.util.List;

//Un unico adaptador para las dos rejillas de listas. Antes habia dos clases casi
//identicas (ListaListAdapter y ListaImborrableAdapter) que solo se diferenciaban en
//como elegian el icono, y encima lo hacian comparando el nombre con cadenas magicas.
//Ahora el icono se resuelve por el id de la lista.
public class ListaAdapter extends RecyclerView.Adapter<ListaAdapter.ListaViewHolder> {

    //Aviso de que se han pedido las opciones de una fila. Lo resuelve quien monta el
    //adaptador, que es quien sabe si puede borrar y como confirmarlo.
    public interface AlPedirOpciones {
        void enLista(Lista lista, android.view.View ancla);
    }

    private final Context context;
    private List<Lista> listas;
    private AlPedirOpciones alPedirOpciones;

    public ListaAdapter(Context context, List<Lista> listas) {
        this.context = context;
        this.listas = listas != null ? listas : new ArrayList<Lista>();
    }

    public void setAlPedirOpciones(AlPedirOpciones escucha) {
        this.alPedirOpciones = escucha;
    }

    @NonNull
    @Override
    public ListaViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ListaViewHolder(
                LayoutInflater.from(context).inflate(R.layout.item_lista_list, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ListaViewHolder holder, int position) {
        final Lista lista = listas.get(position);
        holder.nombreLista.setText(lista.getNombreVisible(context));
        holder.iconoLista.setImageResource(iconoDe(lista));
        pintarPila(holder, lista);
        int cuantos = lista.getLibros().size();
        holder.contador.setText(context.getResources()
                .getQuantityString(R.plurals.n_libros, cuantos, cuantos));

        //Las listas fijas no se pueden borrar: sin opciones que ofrecer, el boton
        //sobra y ademas confundiria.
        boolean hayOpciones = alPedirOpciones != null && !lista.esImborrable();
        holder.opciones.setVisibility(hayOpciones ? View.VISIBLE : View.GONE);
        holder.opciones.setOnClickListener(hayOpciones
                ? v -> alPedirOpciones.enLista(lista, v) : null);

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent verContenido = new Intent(v.getContext(), ContenidoLista.class);
                //se pasa el id, no el objeto: una copia Serializable obligaba a borrar por duplicado
                verContenido.putExtra(ContenidoLista.EXTRA_LISTA_ID, lista.getId());
                context.startActivity(verContenido);
            }
        });
    }

    //Las tres primeras portadas de la lista, apiladas. Si la lista esta vacia no hay
    //nada que ensenar y vuelve el icono.
    private void pintarPila(ListaViewHolder holder, Lista lista) {
        List<Libro> libros = lista.getLibros();
        boolean vacia = libros.isEmpty();
        holder.marcoIcono.setVisibility(vacia ? View.VISIBLE : View.GONE);

        for (int i = 0; i < holder.portadas.length; i++) {
            ImageView hueco = holder.portadas[i];
            String enlace = i < libros.size() ? libros.get(i).getLinkImg() : null;
            if (enlace == null || enlace.trim().isEmpty()) {
                //cancelar antes de esconder: si no, una peticion pendiente de una fila
                //reciclada acaba pintando la portada de otra lista
                Picasso.get().cancelRequest(hueco);
                hueco.setImageDrawable(null);
                hueco.setVisibility(View.GONE);
            } else {
                hueco.setVisibility(View.VISIBLE);
                Picasso.get().load(enlace).into(hueco);
            }
        }
    }

    private static int iconoDe(Lista lista) {
        if (Lista.ID_FAVORITOS.equals(lista.getId())) {
            return R.drawable.ic_favorite_on;
        }
        if (Lista.ID_LEIDOS.equals(lista.getId())) {
            return R.drawable.ic_librarycheck;
        }
        return R.drawable.ic_listas;
    }

    //Actualiza calculando que filas han cambiado de verdad, en vez de repintar la
    //rejilla entera: se conservan el scroll y las animaciones. Importante ahora que
    //los datos llegan solos desde Firestore y pueden refrescarse en cualquier momento.
    public void actualizar(List<Lista> nuevas) {
        final List<Lista> anteriores = this.listas;
        final List<Lista> siguientes = nuevas != null ? nuevas : new ArrayList<Lista>();

        DiffUtil.DiffResult diferencia = DiffUtil.calculateDiff(new DiffUtil.Callback() {
            @Override
            public int getOldListSize() {
                return anteriores.size();
            }

            @Override
            public int getNewListSize() {
                return siguientes.size();
            }

            @Override
            public boolean areItemsTheSame(int posAntigua, int posNueva) {
                return anteriores.get(posAntigua).equals(siguientes.get(posNueva));
            }

            @Override
            public boolean areContentsTheSame(int posAntigua, int posNueva) {
                Lista a = anteriores.get(posAntigua);
                Lista b = siguientes.get(posNueva);
                //el recuento se pinta en la fila, asi que un cambio de tamanio
                //tiene que provocar repintado
                //las portadas se pintan en la fila, asi que cambiar el primer libro
                //tiene que provocar repintado aunque el recuento sea el mismo
                return a.getNombre().equals(b.getNombre())
                        && a.getLibros().size() == b.getLibros().size()
                        && primeros(a).equals(primeros(b));
            }
        });
        this.listas = siguientes;
        diferencia.dispatchUpdatesTo(this);
    }

    //identidad de las portadas que se ven, para el DiffUtil
    private static List<String> primeros(Lista lista) {
        List<String> ids = new ArrayList<>();
        for (int i = 0; i < Math.min(3, lista.getLibros().size()); i++) {
            ids.add(String.valueOf(lista.getLibros().get(i).getLinkImg()));
        }
        return ids;
    }

    @Override
    public int getItemCount() {
        return listas.size();
    }

    public static class ListaViewHolder extends RecyclerView.ViewHolder {
        private final ImageView iconoLista;
        private final View marcoIcono;
        //en orden de pintado: el primero queda encima
        private final ImageView[] portadas;
        private final TextView nombreLista;
        private final TextView contador;
        private final ImageButton opciones;

        public ListaViewHolder(@NonNull View itemView) {
            super(itemView);
            iconoLista = itemView.findViewById(R.id.iconoTypeLista_img);
            marcoIcono = itemView.findViewById(R.id.marcoIcono);
            portadas = new ImageView[]{
                    itemView.findViewById(R.id.portadaUno),
                    itemView.findViewById(R.id.portadaDos),
                    itemView.findViewById(R.id.portadaTres)};
            nombreLista = itemView.findViewById(R.id.nombreLista_txt);
            contador = itemView.findViewById(R.id.contadorLista_txt);
            opciones = itemView.findViewById(R.id.opcionesLista_btn);
        }
    }
}
