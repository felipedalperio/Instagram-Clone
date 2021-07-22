package com.example.instagram.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.example.instagram.R;
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
import java.util.HashMap;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class PerfilAmigoActivity extends AppCompatActivity {
    private Usuario usuarioSelecionado;
    private Usuario usuarioLogado;
    private Button buttonAcaoPerfil;
    private CircleImageView imagePerfil;
    private DatabaseReference usuarioRef;
    private DatabaseReference seguidoresRef;
    private DatabaseReference firebaseRef;
    private DatabaseReference usuarioLogadoRef;
    private DatabaseReference usuarioAmigoRef;
    private ValueEventListener valueEventListenerPerfilAmigo;
    private TextView textPublicacoes,textSeguidores, textSeguindo;
    private String idUsuarioLogado;
    private DatabaseReference postagensUsuarioRef;
    private GridView gridViewPerfil;
    private AdapterGrid adapterGrid;
    private List<Postagem> postagens;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_perfil_amigo);
        firebaseRef = ConfiguracaoFirebase.getReferenciaFirebase();
        usuarioRef = firebaseRef.child("usuarios");
        seguidoresRef = firebaseRef.child("seguidores");
        idUsuarioLogado = UsuarioFirebase.getIdentificadorUsuario();


        //Inicializar os componentes:
        inicializarComponentes();
        buttonAcaoPerfil.setText("Carregando...");


        //Configurando ToolBar:
        Toolbar toolbar = findViewById(R.id.toolbarPrincipal);
        toolbar.setTitle("Perfil");
        setSupportActionBar( toolbar );
        //botao voltar:
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_close_black_24dp);

        //RECUERANDO O USUARIO SELECIONADO:
        Bundle bundle = getIntent().getExtras();
        if( bundle != null ){
            usuarioSelecionado = (Usuario) bundle.getSerializable("usuarioSelecionado");
            //Configurar referencias postagens usuário:
            postagensUsuarioRef =ConfiguracaoFirebase.getReferenciaFirebase()
                    .child("postagens")
                    .child( usuarioSelecionado.getId() );

            //NOME NA TOOLBAR:
            getSupportActionBar().setTitle( usuarioSelecionado.getNome() );

            //recuperando a foto:
            String caminhoFoto = usuarioSelecionado.getCaminhoFoto();
            if(caminhoFoto != null){
                Uri url = Uri.parse( caminhoFoto );
                Glide.with(PerfilAmigoActivity.this)
                        .load( url )
                        .into(imagePerfil);
            }
        }

        //Carregar as fotos das postagens de um usuario:
        inicializarImageLoader();
        carregarFotoPostagem();

        //Abrir foto Clicada:
        gridViewPerfil.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Postagem postagem = postagens.get( position );

                Intent i = new Intent(getApplicationContext(), VisualizarPostagemActivity.class);
                i.putExtra("postagem", postagem);
                i.putExtra("usuario",usuarioSelecionado);

                startActivity(i);
            }
        });
    }

    public void inicializarImageLoader(){
        ImageLoaderConfiguration config = new ImageLoaderConfiguration
                .Builder(this)
                .memoryCache(new LruMemoryCache(2 * 1024 * 1024))
                .memoryCacheSize(2 * 1024 * 1024)
                .diskCacheSize(50 * 1024 * 1024)
                .diskCacheFileCount(100)
                .diskCacheFileNameGenerator(new HashCodeFileNameGenerator())
                .build();
        ImageLoader.getInstance().init(config);

    }


    public void carregarFotoPostagem(){
        postagens = new ArrayList<>();

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
                    postagens.add( postagem );
                    urlFotos.add(postagem.getCaminhoFoto());
                }


                //Configurar o Adapter:
                adapterGrid = new AdapterGrid(getApplicationContext(),R.layout.grid_postagem, urlFotos  );
                gridViewPerfil.setAdapter( adapterGrid );
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void recuperarDadosUsuarioLogado(){
            usuarioLogadoRef = usuarioRef.child( idUsuarioLogado );
            usuarioLogadoRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    //RECUPERANDO DADOS DO USUARIO LOGADO:
                    usuarioLogado = dataSnapshot.getValue( Usuario.class );
                    //VERIFICA SE O USUARIO ESTA SEGUINDO AMIGO SELECIONADO:
                    verificaSegueUsuarioAmigo();

                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });
    }

    private void verificaSegueUsuarioAmigo(){
        DatabaseReference seguidorRef = seguidoresRef
                .child(usuarioSelecionado.getId())
                .child( idUsuarioLogado );

        seguidorRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if( dataSnapshot.exists() ){
                    //já esta seguindo
                    habilitarBotaoSeguir(true);
                }else{
                    //Ainda não está seguindo
                    habilitarBotaoSeguir(false);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }
    private void habilitarBotaoSeguir(boolean segueUsuario){
        if(segueUsuario){
            buttonAcaoPerfil.setBackgroundResource(R.drawable.botao_seguindo);
            buttonAcaoPerfil.setText("Seguindo");
        }else{
            buttonAcaoPerfil.setText("Seguir");
            //Adicioandno o evento para seguir o usuário
            buttonAcaoPerfil.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    //salvar seguidor:
                    salvarSeguidor(usuarioLogado,usuarioSelecionado);
                }
            });
        }
    }
//  ---------------------------- SALVAR SEGUIDORES --------------------------------------------------

    private void salvarSeguidor(Usuario uLogado, Usuario uAmigo){
        /* seguidores
        *      id_usuarioLogado
        *           id_seguidores
        *                  dados seguindo*/

        HashMap<String, Object> dadosUsuarioLogado = new HashMap<>();
        dadosUsuarioLogado.put("nome",uLogado.getNome());
        dadosUsuarioLogado.put("caminhoFoto",uLogado.getCaminhoFoto());

        DatabaseReference seguidorRef = seguidoresRef
                .child( uAmigo.getId() )
                .child( uLogado.getId());

        seguidorRef.setValue(dadosUsuarioLogado);
        //ALTERAR O BOTAO:
        buttonAcaoPerfil.setBackgroundResource(R.drawable.botao_seguindo);
        buttonAcaoPerfil.setText("Seguindo");
        //REMOVENDO O EVENTO DE CLICK:
        buttonAcaoPerfil.setOnClickListener(null);
        //incrementar o seguindo do usuário Logado

//  ---------------------------- SEGUINDO --------------------------------------------------
        int seguindo = uLogado.getSeguindo() + 1;

        HashMap<String, Object>  dadosSeguindo = new HashMap<>();
        dadosSeguindo.put("seguindo",seguindo);
        DatabaseReference usuarioSeguindo = usuarioRef
                .child( uLogado.getId() );
        usuarioSeguindo.updateChildren(dadosSeguindo);

//  ---------------------------- FIM SEGUINDO --------------------------------------------------

//  ---------------------------- SEGUIDORES --------------------------------------------------
        int seguidores = uAmigo.getSeguidores() + 1;

        HashMap<String, Object>  dadosSeguidores = new HashMap<>();
        dadosSeguidores.put("seguidores",seguidores);
        DatabaseReference usuarioSeguidores = usuarioRef
                .child( uAmigo.getId() );
        usuarioSeguidores.updateChildren(dadosSeguidores);

//  ---------------------------- FIM SEGUIDORES ----------------------------------------------

    }

//  ----------------------- FIM SALVAR SEGUIDORES ------------------------------------------------

    @Override
    protected void onStart() {
        super.onStart();
        recuperarDadosPerfilAmigo();
        recuperarDadosUsuarioLogado();
    }

    @Override
    protected void onStop() {
        super.onStop();
        usuarioAmigoRef.removeEventListener( valueEventListenerPerfilAmigo );
    }



    private void recuperarDadosPerfilAmigo(){
        usuarioAmigoRef = usuarioRef.child( usuarioSelecionado.getId() );
        valueEventListenerPerfilAmigo = usuarioAmigoRef.addValueEventListener(new ValueEventListener() {
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

    private void inicializarComponentes(){
        gridViewPerfil = findViewById(R.id.gridViewPerfil);
        buttonAcaoPerfil = findViewById(R.id.buttonAcaoPerfil);
        imagePerfil = findViewById(R.id.imagePerfil);
        textPublicacoes = findViewById(R.id.textPublicacoes);
        textSeguidores = findViewById(R.id.textSeguindores);
        textSeguindo = findViewById(R.id.textSeguindo);
    }


    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return false;
    }
}
