package com.example.instagram.activity;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.Manifest;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.instagram.R;
import com.example.instagram.helper.ConfiguracaoFirebase;
import com.example.instagram.helper.Permissao;
import com.example.instagram.helper.UsuarioFirebase;
import com.example.instagram.model.Usuario;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.theartofdev.edmodo.cropper.CropImage;

import java.io.ByteArrayOutputStream;

import de.hdodenhof.circleimageview.CircleImageView;

public class EditarPerfilActivity extends AppCompatActivity {

    private CircleImageView imageEditarPerfil;
    private TextView textAleterarFoto;
    private TextInputEditText editNomePerfil, editEmailPerfil;
    private Button buttonSalvarAlteracoes;
    private Usuario usuarioLogado;
    private StorageReference storageRef;
    private String identificadorUsuario;
    private ProgressBar progressBarEditar;

    private String[] permissoesNecessarias = new String[]{
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.CAMERA
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_editar_perfil);

        //Validar Permissoes:
        Permissao.validarPermissoes(permissoesNecessarias,this, 1);

        //Configurações inicias:
        usuarioLogado = UsuarioFirebase.getDadosUsuarioLogado();
        storageRef = ConfiguracaoFirebase.getFirebaseStorage();
        identificadorUsuario = UsuarioFirebase.getIdentificadorUsuario();
        progressBarEditar = findViewById(R.id.progressBarEditar);
        progressBarEditar.setVisibility(View.GONE);

        //Configurando ToolBar:
        Toolbar toolbar = findViewById(R.id.toolbarPrincipal);
        toolbar.setTitle("Instagram");
        setSupportActionBar( toolbar );
        //botao voltar:
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_close_black_24dp);

        //inicializar componentes:
        inicializarComponentes();
        //Recuperando os dados do usuário:
        FirebaseUser usuarioPerfil = UsuarioFirebase.getUsuarioAtual();
        editNomePerfil.setText(usuarioPerfil.getDisplayName().toUpperCase());
        editEmailPerfil.setText(usuarioPerfil.getEmail());

        //recuperando a foto:
        Uri url = usuarioPerfil.getPhotoUrl();
        if(url != null){
            Glide.with(EditarPerfilActivity.this)
                    .load(url)
                    .into(imageEditarPerfil);
        }else{
            imageEditarPerfil.setImageResource(R.drawable.avatar);
        }

        //SALVAR ALTERAÇÕES:
        buttonSalvarAlteracoes.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String nomeAtualizado = editNomePerfil.getText().toString();
                //Atualizar Nome no perfil:
                UsuarioFirebase.atualizarNomeUsuario(nomeAtualizado);
                //atualizar nome no banco de dados:
                usuarioLogado.setNome( nomeAtualizado );
                usuarioLogado.altualizar();
                Toast.makeText(EditarPerfilActivity.this,"Dados alterados com Sucesso",Toast.LENGTH_SHORT).show();

            }
        });

        //Alterar foto do usuario:
        textAleterarFoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CropImage.activity().start(EditarPerfilActivity.this);
            }
        });
    }



//IMAGEM PARA A FOTO DO PERFIL
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK) {
            Bitmap imagem = null;
            if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
                try {
                    //SELECIONAR APENAS DA GALERIA DE FOTOS:
                    CropImage.ActivityResult result = CropImage.getActivityResult(data);
                    if (resultCode == RESULT_OK) {
                        Uri resultUri = result.getUri();
                        imagem = MediaStore.Images.Media.getBitmap(getContentResolver(), resultUri);

                        //=-=-=-=-=-=-=-=-=-=-=-=-=-=SALVANDO A IMAGEM NO FIREBASE =-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=

                        //imagem escolhida:
                        if (imagem != null) {
                            progressBarEditar.setVisibility(View.VISIBLE);
                            imageEditarPerfil.setImageBitmap(imagem);
                            //recuperar dados da imagem para o firebase
                            ByteArrayOutputStream baos = new ByteArrayOutputStream();
                            imagem.compress(Bitmap.CompressFormat.JPEG,70,baos);
                            byte[] dadosImagem = baos.toByteArray();

                            //salvar imagem no firebase
                            StorageReference imagemRef = storageRef
                                    .child("imagens")
                                    .child("perfil")
                                    .child(identificadorUsuario + ".jpeg");
                            UploadTask uploadTask = imagemRef.putBytes( dadosImagem );

                            //MENSAGENS DE FALHA E SUCESSO AO FAZER UPLOAD:
                            uploadTask.addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    progressBarEditar.setVisibility(View.GONE);
                                    Toast.makeText(EditarPerfilActivity.this,"Erro ao fazer Upload da imagem",Toast.LENGTH_SHORT).show();
                                }
                            }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                                @Override
                                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                                    //Recuperar local da foto:
                                    //=-=-=-=-=-=-=-=-=-=-=-=-=--=-=-=-=-=-=-==-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=
                                    if (taskSnapshot.getMetadata() != null) {
                                        if (taskSnapshot.getMetadata().getReference() != null) {
                                            Task<Uri> result = taskSnapshot.getStorage().getDownloadUrl();
                                            result.addOnSuccessListener(new OnSuccessListener<Uri>() {
                                                @Override
                                                public void onSuccess(Uri uri) {
                                                    atualizarFotoUsuario( uri );
                                                }
                                            });
                                        }
                                    }
                                    //=-=-=-=-=-=-=-=-=-=-=-=-=--=-=-=-=-=-=-==-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=

                                    Toast.makeText(EditarPerfilActivity.this,"Sucesso ao fazer Upload da imagem",Toast.LENGTH_SHORT).show();
                                    progressBarEditar.setVisibility(View.GONE);
                                }
                            });
                        }

//=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-==-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-

                    } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                        Exception error = result.getError();
                    }



                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }

    }

    private void atualizarFotoUsuario(Uri url){
        //ATUALIZAR A FOTO NO PERFIL
        UsuarioFirebase.atualizarFotoUsuario(url);
        //ATUALIZAR A FOTO NO FIREBASE:
        usuarioLogado.setCaminhoFoto(url.toString());
        usuarioLogado.altualizar();

        //MENSAGEM:
        Toast.makeText(EditarPerfilActivity.this,"FOTO ATUALIZADA COM SUCESSO",Toast.LENGTH_SHORT).show();
    }



    public void inicializarComponentes(){
        imageEditarPerfil = findViewById(R.id.imagePerfil);
        textAleterarFoto = findViewById(R.id.textAlterarFoto);
        editNomePerfil = findViewById(R.id.editNomePerfil);
        editEmailPerfil = findViewById(R.id.editEmailPefil);
        buttonSalvarAlteracoes = findViewById(R.id.buttonSalvarAlteracoes);

        //O usuario nao conseguira selecionar
        //o Campo email:
        editEmailPerfil.setFocusable(false);
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return false;
    }


}
