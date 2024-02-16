package com.example.newgoodbooks.Fragments.AdapterList;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.newgoodbooks.Modelos.Lista;
import com.example.newgoodbooks.R;

import java.util.List;

public class DeleteAdapter extends RecyclerView.Adapter<DeleteViewHolder> {
    Context context;
    List<Lista> listasList;
    public DeleteAdapter(Context context, List<Lista> listasList){

    }
    @NonNull
    @Override
    public DeleteViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View Item = LayoutInflater.from(context).inflate(R.layout.itemdel_layout,parent,false);
        return null;
    }

    @Override
    public void onBindViewHolder(@NonNull DeleteViewHolder holder, int position) {

    }

    @Override
    public int getItemCount() {
        return listasList.size();
    }
}
