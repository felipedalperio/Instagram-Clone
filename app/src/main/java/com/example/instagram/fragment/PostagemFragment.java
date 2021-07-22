package com.example.instagram.fragment;


import android.Manifest;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.example.instagram.R;
import com.example.instagram.activity.EditarPerfilActivity;
import com.example.instagram.activity.FiltroActivity;
import com.example.instagram.helper.Permissao;
import com.theartofdev.edmodo.cropper.CropImage;

import java.io.ByteArrayOutputStream;

/**
 * A simple {@link Fragment} subclass.
 */
public class PostagemFragment extends Fragment {
    private Button buttonAbrirGaleria;
    private String[] permissoesNecessarias = new String[]{
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.CAMERA
    };

    public PostagemFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view =  inflater.inflate(R.layout.fragment_postagem, container, false);
        //Validar Permissoes:
        Permissao.validarPermissoes(permissoesNecessarias, getActivity(), 1);

        buttonAbrirGaleria = view.findViewById(R.id.buttonAbrirGaleria);

        //Evendo de click:
        buttonAbrirGaleria.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                CropImage.activity()
                        .setMinCropResultSize(300,300)
                        .setMaxCropResultSize(3000,4000)
                        .start(getContext(), PostagemFragment.this);
            }
        });
        return view;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == getActivity().RESULT_OK) {
            Bitmap imagem = null;
            if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
                CropImage.ActivityResult result = CropImage.getActivityResult(data);

                try {
                    if (resultCode == getActivity().RESULT_OK) {
                        Uri localImagemSelecionada = result.getUri();
                        imagem = MediaStore.Images.Media.getBitmap(getActivity().getContentResolver(), localImagemSelecionada);

//=-=-=-=-=-=-=-=-=-=-=-==-=-=-=-=-=-=VALIDANDO/ENVIANDO =-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=
                        if(imagem != null){
                            //TRANSFORMANDO EM UM BYTEARRAY:
                            ByteArrayOutputStream baos = new ByteArrayOutputStream();
                            imagem.compress(Bitmap.CompressFormat.JPEG,40,baos);
                            byte[] dadosImagem = baos.toByteArray();

                            //ENVIANDO A IMAGEM PARA A APLICAÇÃO DE FILTRO:
                            Intent i = new Intent(getActivity(), FiltroActivity.class);
                            i.putExtra("fotoEscolhida", dadosImagem);
                            startActivity(i);
                        }

                    } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                        Exception error = result.getError();
                    }


                }catch (Exception e){
                    e.printStackTrace();
                }
            }

        }
    }
}

