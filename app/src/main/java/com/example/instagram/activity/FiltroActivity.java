package com.example.instagram.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Adapter;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.instagram.R;
import com.example.instagram.adapter.AdapterMiniaturas;
import com.example.instagram.helper.ConfiguracaoFirebase;
import com.example.instagram.helper.RecyclerItemClickListener;
import com.example.instagram.helper.UsuarioFirebase;
import com.example.instagram.model.Postagem;
import com.example.instagram.model.Usuario;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.zomato.photofilters.FilterPack;
import com.zomato.photofilters.imageprocessors.Filter;
import com.zomato.photofilters.utils.ThumbnailItem;
import com.zomato.photofilters.utils.ThumbnailsManager;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.List;


public class FiltroActivity extends AppCompatActivity {

    static
    {
        System.loadLibrary("NativeImageProcessor");
    }

    private ImageView imageFotoEscolhida;
    private Bitmap imagem;
    private Bitmap imagemFiltro;
    private List<ThumbnailItem> listaFiltros;
    private String idUsuarioLogado;

    private RecyclerView recyclerFiltros;
    private AdapterMiniaturas adapterMiniaturas;
    private TextInputEditText textDescricaoFiltro;
    private ProgressBar progressBarCarregando;
    private TextView textViewCarregando;

    private DatabaseReference usuarioRef;
    private DatabaseReference usuarioLogadoRef;
    private DatabaseReference firebaseRef;
    private Usuario usuarioLogado;
    private DataSnapshot seguidoresSnapshot;

    private AlertDialog dialog;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_filtro);

        listaFiltros = new ArrayList<>();
        firebaseRef = ConfiguracaoFirebase.getReferenciaFirebase();
        idUsuarioLogado = UsuarioFirebase.getIdentificadorUsuario();
        usuarioRef =ConfiguracaoFirebase.getReferenciaFirebase().child("usuarios");

        imageFotoEscolhida = findViewById(R.id.imageFotoEscolhida);
        recyclerFiltros = findViewById(R.id.recyclerFiltros);
        textDescricaoFiltro = findViewById(R.id.textDescricaoFiltro);
        progressBarCarregando = findViewById(R.id.progressBarCarregando);
        textViewCarregando = findViewById(R.id.textViewCarregando);


        progressBarCarregando.setVisibility(View.GONE);
        textViewCarregando.setVisibility(View.GONE);


        //recuperar Dados do Usuario Logado:
        recuperarDadosPostagem();

        //Configurando ToolBar:
        Toolbar toolbar = findViewById(R.id.toolbarPrincipal);
        toolbar.setTitle("Filtros");
        setSupportActionBar( toolbar );
        //botao voltar:
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_close_black_24dp);

        //Recuperar a imagem Escolhida pelo Usuario:
        Bundle bundle = getIntent().getExtras();
        if( bundle != null ){
            byte[] dadosImagem = bundle.getByteArray("fotoEscolhida");
            imagem = BitmapFactory.decodeByteArray(dadosImagem,0,dadosImagem.length);
            imageFotoEscolhida.setImageBitmap( imagem );
            imagemFiltro = imagem.copy(imagem.getConfig(),true);

            //Configurando o RecyclerView:
            adapterMiniaturas = new AdapterMiniaturas(listaFiltros,getApplicationContext());
            RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(this,LinearLayoutManager.HORIZONTAL,false);
            recyclerFiltros.setLayoutManager(layoutManager);
            recyclerFiltros.setAdapter(adapterMiniaturas);

            //Evendo de click no RecyclerView:
            recyclerFiltros.addOnItemTouchListener(
                    new RecyclerItemClickListener(
                            getApplicationContext(),
                            recyclerFiltros,
                            new RecyclerItemClickListener.OnItemClickListener() {
                                @Override
                                public void onItemClick(View view, int position) {
                                    ThumbnailItem item = listaFiltros.get(position);
                                    imagemFiltro = imagem.copy(imagem.getConfig(),true);
                                    Filter filtro = item.filter;
                                    imageFotoEscolhida.setImageBitmap( filtro.processFilter(imagemFiltro) );
                                }

                                @Override
                                public void onLongItemClick(View view, int position) {

                                }

                                @Override
                                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                                }
                            }
                    ));

            recuperarFiltros();

        }

    }

    private void abrirDialogCarregamento(String titulo){
        AlertDialog.Builder alert = new AlertDialog.Builder(this);
        alert.setTitle(titulo);
        alert.setCancelable(false); // nao consegue cancelart a tela

        alert.setView(R.layout.carregamento);

        dialog = alert.create();
        dialog.show();

    }


    private void recuperarDadosPostagem(){
        abrirDialogCarregamento("Carregando dados, Aguarde!");
        usuarioLogadoRef = usuarioRef.child( idUsuarioLogado );
        usuarioLogadoRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                //RECUPERANDO DADOS DO USUARIO LOGADO:
                usuarioLogado = dataSnapshot.getValue( Usuario.class );

                //RECUPERANDO OS SEGUIDORES:
                DatabaseReference seguidoresRef = firebaseRef
                        .child("seguidores")
                        .child(idUsuarioLogado);
                seguidoresRef.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        seguidoresSnapshot = dataSnapshot;
                        dialog.cancel();
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });


            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }


    private void recuperarFiltros(){
        ThumbnailsManager.clearThumbs();
        listaFiltros.clear();
        //Configurar filtro normal:
        ThumbnailItem item = new ThumbnailItem();
        item.image = imagem;
        item.filterName = "Normal";
        ThumbnailsManager.addThumb( item );

        //LISTA TODOS OS FILTROS:
        List<Filter> filtros = FilterPack.getFilterPack(getApplicationContext());
        for( Filter filtro : filtros){
            ThumbnailItem itemFiltro = new ThumbnailItem();
            itemFiltro.image = imagem;
            itemFiltro.filter = filtro;
            itemFiltro.filterName = filtro.getName();
            ThumbnailsManager.addThumb( itemFiltro );
        }
        listaFiltros.addAll(ThumbnailsManager.processThumbs(getApplicationContext()));
        adapterMiniaturas.notifyDataSetChanged();
    }

    private void publicarPostagem(){
        abrirDialogCarregamento("Salvando postagem");
        textViewCarregando.setVisibility(View.VISIBLE);
        progressBarCarregando.setVisibility(View.VISIBLE);
        final Postagem postagem= new Postagem();
        postagem.setIdUsuario( idUsuarioLogado );
        postagem.setDescricao( textDescricaoFiltro.getText().toString() );

        //Recuperando os dados da imagem para salvar no FireBase
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        imagemFiltro.compress(Bitmap.CompressFormat.JPEG,70,baos);
        byte[] dadosImagem = baos.toByteArray();

        //salvando a imagem no firebase Storage:
        StorageReference storageRef = ConfiguracaoFirebase.getFirebaseStorage();
        StorageReference imagemRef = storageRef
                .child("imagens")
                .child("postagens")
                .child(postagem.getId() + "jpeg");
        progressBarCarregando.setProgress(30);

        UploadTask uploadTask = imagemRef.putBytes( dadosImagem );
        //MENSAGENS DE FALHA E SUCESSO AO FAZER UPLOAD:
        uploadTask.addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                textViewCarregando.setVisibility(View.GONE);
                progressBarCarregando.setVisibility(View.GONE);
                Toast.makeText(FiltroActivity.this,"Erro ao salvar a imagem",Toast.LENGTH_SHORT).show();
            }
        }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                progressBarCarregando.setProgress(60);
                //Recuperar local da foto:
                //=-=-=-=-=-=-=-=-=-=-=-=-=--=-=-=-=-=-=-==-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=
                if (taskSnapshot.getMetadata() != null) {
                    if (taskSnapshot.getMetadata().getReference() != null) {
                        Task<Uri> result = taskSnapshot.getStorage().getDownloadUrl();
                        result.addOnSuccessListener(new OnSuccessListener<Uri>() {
                            @Override
                            public void onSuccess(Uri uri) {
                                progressBarCarregando.setProgress(70);
                                postagem.setCaminhoFoto(uri.toString());

                                //Atualizar qntde de postagens:
                                int qtdPostagem = usuarioLogado.getPostagens() + 1;
                                usuarioLogado.setPostagens(qtdPostagem);
                                usuarioLogado.altualizarQtdPostgam();


                                //salvarPostagem:
                                if(postagem.salvar(  seguidoresSnapshot )){

                                    progressBarCarregando.setProgress(100);
                                    textViewCarregando.setVisibility(View.GONE);
                                    Toast.makeText(FiltroActivity.this,"Sucesso ao fazer Upload da imagem",Toast.LENGTH_SHORT).show();
                                    dialog.cancel();
                                    finish();
                                }
                            }
                        });
                    }
                }
                //=-=-=-=-=-=-=-=-=-=-=-=-=--=-=-=-=-=-=-==-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=

            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_filtro, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch ( item.getItemId() ){
            case R.id.ic_salvar_postagem:
                publicarPostagem();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return false;
    }
}
