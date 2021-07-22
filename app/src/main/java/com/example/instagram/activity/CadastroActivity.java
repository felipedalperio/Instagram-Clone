package com.example.instagram.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.example.instagram.R;
import com.example.instagram.helper.ConfiguracaoFirebase;
import com.example.instagram.helper.UsuarioFirebase;
import com.example.instagram.model.Usuario;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthInvalidUserException;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseAuthWeakPasswordException;

public class CadastroActivity extends AppCompatActivity {
    //componentes Iniciais:
    private EditText campoNome,campoEmail, campoSenha;
    private Button botaoCadastrar;
    private ProgressBar progressBar;
    //MODEL USUARIO:
    private Usuario usuario;
    //Autenticacao:
    private FirebaseAuth autenticacao;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cadastro);
        inicializarComponentes();

        //EVENTO DE CLICK
        progressBar.setVisibility(View.GONE);
        botaoCadastrar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                validarCadastro();
            }
        });

    }
    public void inicializarComponentes(){

        campoNome = findViewById(R.id.editCadastroNome);
        campoEmail = findViewById(R.id.editCadastroEmail);
        campoSenha = findViewById(R.id.editCadastroSenha);
        botaoCadastrar = findViewById(R.id.buttonCadastrarEntrar);
        progressBar = findViewById(R.id.progressBarCadastrar);
    }


    public void validarCadastro(){
        //RECUPERANDO OS DADOS:
        String textoNome = campoNome.getText().toString();
        String textEmail = campoEmail.getText().toString();
        String textoSenha = campoSenha.getText().toString();
        //VALIDANDO:
        if(!textoNome.isEmpty()){
            if(!textEmail.isEmpty()){
                if(!textoSenha.isEmpty()){
                    Usuario usuario = new Usuario();

                    usuario.setEmail(textEmail);
                    usuario.setNome(textoNome);
                    usuario.setSenha(textoSenha);
                    cadastrarUsuario( usuario );


                }else{
                    Toast.makeText(CadastroActivity.this,"Preencha o campo senha",Toast.LENGTH_LONG).show();
                }
            }else {
                Toast.makeText(CadastroActivity.this,"Preencha o campo Email",Toast.LENGTH_LONG).show();
            }
        }else{
            Toast.makeText(CadastroActivity.this,"Preencha o campo nome",Toast.LENGTH_LONG).show();
        }

    }

    public void cadastrarUsuario(final Usuario usuario){
        progressBar.setVisibility(View.VISIBLE);
        autenticacao = ConfiguracaoFirebase.getFirebaseAutenticacao();
        autenticacao.createUserWithEmailAndPassword(
                usuario.getEmail(),usuario.getSenha()
        ).addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if(task.isSuccessful()){
                    try{
                        progressBar.setVisibility(View.GONE);

                        String idUsuario = task.getResult().getUser().getUid();
                        usuario.setId(idUsuario);
                        usuario.salvar();

                        //Salvar dados no profile do firebase:
                        UsuarioFirebase.atualizarNomeUsuario(usuario.getNome());

                        Toast.makeText(CadastroActivity.this,"Conta foi Cadastrada com sucesso",Toast.LENGTH_LONG).show();
                        startActivity(new Intent(getApplicationContext(),MainActivity.class));
                        finish();
                    }catch (Exception e){
                        e.printStackTrace();
                    }


                }else{
                    progressBar.setVisibility(View.GONE);

                    String erroExecao = "";
                    try{
                        throw task.getException();
                    }catch (FirebaseAuthWeakPasswordException e){
                            erroExecao = "Digite uma senha mais forte";
                    }catch (FirebaseAuthInvalidCredentialsException e){
                            erroExecao = "Porfavor, Digite um E-mail valido";
                    }catch (FirebaseAuthUserCollisionException e){
                            erroExecao = "Esta conta já foi cadastrada";
                    }catch (Exception e){
                            erroExecao = "Ao cadastrar  usuário: "+e.getMessage();
                            e.printStackTrace();
                    }
                    Toast.makeText(CadastroActivity.this,erroExecao,Toast.LENGTH_LONG).show();
                }
            }
        });
    }
}
