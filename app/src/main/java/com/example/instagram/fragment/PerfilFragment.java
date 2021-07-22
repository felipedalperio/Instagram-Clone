package com.example.instagram.fragment;


import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.example.instagram.R;
import com.example.instagram.activity.EditarPerfilActivity;
import com.example.instagram.activity.PerfilAmigoActivity;
import com.example.instagram.adapter.AdapterGrid;
import com.example.instagram.helper.ConfiguracaoFirebase;
import com.example.instagram.helper.UsuarioFirebase;
import com.example.instagram.model.Postagem;
import com.example.instagram.model.Usuario;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import com.nostra13.universalimageloader.cache.disc.naming.HashCodeFileNameGenerator;
import com.nostra13.universalimageloader.cache.memory.impl.LruMemoryCache;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;

import java.util.ArrayList;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

/**
 * A simple {@link Fragment} subclass.
 */
public class PerfilFragment extends Fragment {

    private ProgressBar progressBar;
    private CircleImageView imagePerfil;
    public GridView gridViewPerfil;
    private TextView textPublicacoes,textSeguidores, textSeguindo;
    private Button buttonAcaoPerfil;
    private Usuario usuarioLogado;
    private DatabaseReference usuarioRef;
    private DatabaseReference usuarioLogadoRef;
    private DatabaseReference firebaseRef;
    private ValueEventListener valueEventListenerPerfil;
    private DatabaseReference postagensUsuarioRef;
    private AdapterGrid adapterGrid;

    public PerfilFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view =  inflater.inflate(R.layout.fragment_perfil, container, false);

        //Configurações Iniciais:
        usuarioLogado = UsuarioFirebase.getDadosUsuarioLogado();
        firebaseRef = ConfiguracaoFirebase.getReferenciaFirebase();
        usuarioRef = firebaseRef.child("usuarios");

        postagensUsuarioRef =ConfiguracaoFirebase.getReferenciaFirebase()
                .child("postagens")
                .child( usuarioLogado.getId() );

        //Configurações dos componentes:
        InicializarComponentes(view);


        //Abre edição de perfil(evento de click)
        buttonAcaoPerfil.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(getActivity(), EditarPerfilActivity.class);
                startActivity(i);
            }
        });

        //inicializando o ImageLoader:
        inicializarImageLoader();
        carregarFotoPostagem();

        return view;
    }

    public void inicializarImageLoader(){
        ImageLoaderConfiguration config = new ImageLoaderConfiguration
                .Builder( getActivity() )
                .memoryCache(new LruMemoryCache(2 * 1024 * 1024))
                .memoryCacheSize(2 * 1024 * 1024)
                .diskCacheSize(50 * 1024 * 1024)
                .diskCacheFileCount(100)
                .diskCacheFileNameGenerator(new HashCodeFileNameGenerator())
                .build();
        ImageLoader.getInstance().init(config);

    }

    public void carregarFotoPostagem(){
        //RECUPERAR AS FOTOS POSTADAS PELO USUARIO:
        postagensUsuarioRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                //CONFIGURAR O TAMANHO DO GRID:
                int tamanhoGrid = getResources().getDisplayMetrics().widthPixels;
                int tamanhoImagem = tamanhoGrid / 2;
                gridViewPerfil.setColumnWidth(tamanhoImagem);

                List<String> urlFotos = new ArrayList<>();

                for (DataSnapshot ds: dataSnapshot.getChildren()){
                    Postagem postagem = ds.getValue( Postagem.class );
                    urlFotos.add(postagem.getCaminhoFoto());
                }
                //Configurar o Adapter:
                adapterGrid = new AdapterGrid(getActivity(),R.layout.grid_postagem, urlFotos  );
                gridViewPerfil.setAdapter( adapterGrid );
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }
    private void InicializarComponentes(View view){
        gridViewPerfil = view.findViewById(R.id.gridViewPerfil);
        imagePerfil = view.findViewById(R.id.imagePerfil);
        progressBar = view.findViewById(R.id.progressBar);
        textPublicacoes = view.findViewById(R.id.textPublicacoes);
        textSeguidores = view.findViewById(R.id.textSeguindores);
        textSeguindo = view.findViewById(R.id.textSeguindo);
        buttonAcaoPerfil = view.findViewById(R.id.buttonAcaoPerfil);
    }
    private void recuperarDadosUsuarioLogado(){
        usuarioLogadoRef = usuarioRef.child( usuarioLogado.getId() );
        valueEventListenerPerfil = usuarioLogadoRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                Usuario usuario = dataSnapshot.getValue( Usuario.class );

                String postagens = String.valueOf( usuario.getPostagens() );
                String seguindo = String.valueOf( usuario.getSeguindo() );
                String seguidores = String.valueOf( usuario.getSeguidores() );

                textPublicacoes.setText( postagens );
                textSeguindo.setText(seguindo);
                textSeguidores.setText(seguidores);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void recuperarFotoUsuario(){

        usuarioLogado = UsuarioFirebase.getDadosUsuarioLogado();

        //RECUPERANDO FOTO DO USUARIO LOGADO:
        String caminhoFoto = usuarioLogado.getCaminhoFoto();
        if(caminhoFoto != null){
            Uri url = Uri.parse( caminhoFoto );
            Glide.with(PerfilFragment.this)
                    .load( url )
                    .into(imagePerfil);
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        recuperarDadosUsuarioLogado();
        //RECUPERAR FOTO:
        recuperarFotoUsuario();
    }

    @Override
    public void onStop() {
        super.onStop();
        usuarioLogadoRef.removeEventListener( valueEventListenerPerfil );
    }
}
