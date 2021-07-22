package com.example.instagram.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import com.example.instagram.R;
import com.example.instagram.fragment.FeedFragment;
import com.example.instagram.fragment.PerfilFragment;
import com.example.instagram.fragment.PesquisaFragment;
import com.example.instagram.fragment.PostagemFragment;
import com.example.instagram.helper.ConfiguracaoFirebase;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.ittianyu.bottomnavigationviewex.BottomNavigationViewEx;

public class MainActivity extends AppCompatActivity {

    private FirebaseAuth autenticacao;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //Configurando ToolBar:
        Toolbar toolbar = findViewById(R.id.toolbarPrincipal);
        toolbar.setTitle("Instagram");
        setSupportActionBar( toolbar );

        //configuração da autenticacao:
        autenticacao = ConfiguracaoFirebase.getFirebaseAutenticacao();

        //Configurando bottom navigation view:
        configuraBottomNavigationView();
    }

    private void configuraBottomNavigationView(){
        BottomNavigationViewEx bottomNavigationViewEx = findViewById(R.id.bottomNavigation);
        //FAZENDO AS CONFIGURAÇÕES INICIAIS:
        bottomNavigationViewEx.enableAnimation(true);
        bottomNavigationViewEx.enableItemShiftingMode(false);
        bottomNavigationViewEx.enableShiftingMode(true);
        bottomNavigationViewEx.setTextVisibility(true);

        //HABILITAR NAVEGACAO:
        habilitarNavegacao(bottomNavigationViewEx);

        //ABRINDO O FEED AO EXECUTAR O APP:
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.viewPager, new FeedFragment()).commit();
    }

    //evento de click do bottomNavigation:
    private void habilitarNavegacao(BottomNavigationViewEx viewEx){
        viewEx.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {

            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
                FragmentManager fragmentManager = getSupportFragmentManager();
                FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();

               switch(menuItem.getItemId()){
                   //HOME
                   case R.id.ic_home:
                       fragmentTransaction.replace(R.id.viewPager, new FeedFragment()).commit();
                       return true;
                   //PESQUISA
                   case R.id.ic_pesquisa:
                       fragmentTransaction.replace(R.id.viewPager, new PesquisaFragment()).commit();
                       return true;
                   //POSTAGEM
                   case R.id.ic_postagem:
                       fragmentTransaction.replace(R.id.viewPager, new PostagemFragment()).commit();
                       return true;
                   //PERFIL
                   case R.id.ic_perfil:
                       fragmentTransaction.replace(R.id.viewPager, new PerfilFragment()).commit();
                       return true;
               }
                return false;
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_main, menu);

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch ( item.getItemId() ){
            case R.id.menu_sair:
                deslogarUsuario();
                startActivity(new Intent(getApplicationContext(),LoginActivity.class));
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private void deslogarUsuario(){
        try{
            autenticacao.signOut();
        }catch (Exception e){
            e.printStackTrace();
        }
    }
}
