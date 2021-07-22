package com.example.instagram.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.view.View;
import android.widget.Adapter;
import android.widget.EditText;
import android.widget.Toast;

import com.example.instagram.R;
import com.example.instagram.adapter.AdapterComentario;
import com.example.instagram.helper.ConfiguracaoFirebase;
import com.example.instagram.helper.UsuarioFirebase;
import com.example.instagram.model.Comentario;
import com.example.instagram.model.Usuario;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class ComentariosActivity extends AppCompatActivity {
    private EditText editComentario;
    private RecyclerView recyclerComentarios;
    private String idPostagem;
    private Usuario usuario;
    private AdapterComentario adapterComentario;
    private List<Comentario> listaComentaios = new ArrayList<>();

    private DatabaseReference firebaseRef;
    private DatabaseReference comentariosRef;

    private ValueEventListener valueEventListenerComentarios;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_comentarios);

        //Inicializar componentes:
        editComentario =  findViewById(R.id.editComentario);
        recyclerComentarios = findViewById(R.id.recyclerComentarios);
        usuario = UsuarioFirebase.getDadosUsuarioLogado();

        firebaseRef = ConfiguracaoFirebase.getReferenciaFirebase();

        //Configurando ToolBar:
        Toolbar toolbar = findViewById(R.id.toolbarPrincipal);
        toolbar.setTitle("Comentários");
        setSupportActionBar( toolbar );
        //botao voltar:
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_close_black_24dp);

        //Configurar recyclerView:
        adapterComentario = new AdapterComentario(listaComentaios, getApplicationContext());
        recyclerComentarios.setHasFixedSize(true);
        recyclerComentarios.setLayoutManager(new LinearLayoutManager(this));
        recyclerComentarios.setAdapter( adapterComentario );

        //Recuperando o id da Postagem:
        Bundle bundle = getIntent().getExtras();
        if( bundle != null ){
            idPostagem = bundle.getString("idPostagem");
        }

    }
    private void recuperarComentarios(){
        comentariosRef = firebaseRef.child("comentarios")
                .child(idPostagem);
        valueEventListenerComentarios =  comentariosRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                listaComentaios.clear();
                for (DataSnapshot ds : dataSnapshot.getChildren()){
                    listaComentaios.add(ds.getValue(Comentario.class));
                }
                adapterComentario.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        recuperarComentarios();
    }

    @Override
    protected void onStop() {
        super.onStop();
        comentariosRef.removeEventListener(valueEventListenerComentarios);
    }

    public void salvarComentario(View view){
        String textoComentario = editComentario.getText().toString();
        if( !textoComentario.equals("") && textoComentario != null ){
            Comentario comentario = new Comentario();
            comentario.setIdPostagem(idPostagem);

            comentario.setIdUsuario( usuario.getId() );
            comentario.setNomeUsuario(usuario.getNome());
            comentario.setCaminhoFoto(usuario.getCaminhoFoto());

            comentario.setComentario(textoComentario);

            if(comentario.salvar()){
                Toast.makeText(this, "Comentário salvo com sucesso!", Toast.LENGTH_SHORT).show();
            }

        }else{
            Toast.makeText(this, "Escreva seu comentário!", Toast.LENGTH_SHORT).show();
        }
        //limpando o texto ao enviar:
        editComentario.setText("");
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return false;
    }
}
