package com.example.instagram.model;

import com.example.instagram.helper.ConfiguracaoFirebase;
import com.google.firebase.database.DatabaseReference;

import java.util.HashMap;

public class PostagemCurtida {
    public Feed feed;
    public Usuario usuario;
    public int qtdCurtidas = 0;

    public PostagemCurtida() {
    }

    public void salvar(){
        HashMap<String, Object> dadosUsuario = new HashMap<>();
        dadosUsuario.put("nomeUsuario",usuario.getNome());
        dadosUsuario.put("caminhoFoto",usuario.getCaminhoFoto());

        DatabaseReference firebaseRef = ConfiguracaoFirebase.getReferenciaFirebase();
        DatabaseReference pCurtidasRef = firebaseRef
                .child("postagens-curtidas")
                .child(feed.getId()) // id da postagem
                .child(usuario.getId()); // id do usuario
        pCurtidasRef.setValue( dadosUsuario );

        //Atualizar quantidade de curtidas:
        atualizarQtd(1);

    }
    public void atualizarQtd(int valor){
        DatabaseReference firebaseRef = ConfiguracaoFirebase.getReferenciaFirebase();
        DatabaseReference pCurtidasRef = firebaseRef
                .child("postagens-curtidas")
                .child(feed.getId()) // id da postagem
                .child("qtdCurtidas");
        setQtdCurtidas(getQtdCurtidas() + valor);
        pCurtidasRef.setValue(getQtdCurtidas());

    }

    public void remover(){
        //REFERENCIA DO FIREBASE:
        DatabaseReference firebaseRef = ConfiguracaoFirebase.getReferenciaFirebase();
        DatabaseReference pCurtidasRef = firebaseRef
                //FILHAS:
                .child("postagens-curtidas")
                .child(feed.getId()) // id da postagem
                .child(usuario.getId()); // id do usuario
        pCurtidasRef.removeValue(); // removendo o Nó

        //Atualizando a quantidade de curtida com -1 :
        atualizarQtd(-1);
    }

    public Feed getFeed() {
        return feed;
    }

    public void setFeed(Feed feed) {
        this.feed = feed;
    }

    public Usuario getUsuario() {
        return usuario;
    }

    public void setUsuario(Usuario usuario) {
        this.usuario = usuario;
    }

    public int getQtdCurtidas() {
        return qtdCurtidas;
    }

    public void setQtdCurtidas(int qtdCurtidas) {
        this.qtdCurtidas = qtdCurtidas;
    }
}
