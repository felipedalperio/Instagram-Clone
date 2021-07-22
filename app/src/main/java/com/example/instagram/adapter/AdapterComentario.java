package com.example.instagram.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.instagram.R;
import com.example.instagram.model.Comentario;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class AdapterComentario extends RecyclerView.Adapter<AdapterComentario.MyViewHolder> {

    private List<Comentario> listaComentaios;
    private Context context;

    public AdapterComentario(List<Comentario> listaComentaios, Context context) {
        this.listaComentaios = listaComentaios;
        this.context = context;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemLista = LayoutInflater.from(parent.getContext())
               .inflate(R.layout.adapter_comentario,parent,false);

       return new AdapterComentario.MyViewHolder(itemLista);


    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        Comentario comentario = listaComentaios.get(position);
        holder.nomeUsuario.setText(comentario.getNomeUsuario());
        holder.comentario.setText(comentario.getComentario());
        Glide.with(context).load(comentario.getCaminhoFoto()).into(holder.imagePerfil);
    }

    @Override
    public int getItemCount() {
        return listaComentaios.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {

        CircleImageView imagePerfil;
        TextView nomeUsuario, comentario;

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);

            imagePerfil = itemView.findViewById(R.id.imageFotoComentario);
            nomeUsuario = itemView.findViewById(R.id.textNomeComentario);
            comentario = itemView.findViewById(R.id.textComentario);
        }
    }
}
