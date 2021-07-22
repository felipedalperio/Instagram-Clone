package com.example.instagram.helper;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

public class ConfiguracaoFirebase {
    private static DatabaseReference referenciaFirebase;
    private static FirebaseAuth referenciaAutenticacao;
    private static StorageReference storage;

    //=-=-=-=-=-=-=-=REFERENCIA_FIREBASE=-=-=-=-=-=-=-=-=-=

    public static DatabaseReference getReferenciaFirebase(){
        if(referenciaFirebase == null){
            referenciaFirebase = FirebaseDatabase.getInstance().getReference();
        }
        return referenciaFirebase;
    }
    //=-=-=-=-=-=-=-=FIM_REFERENCIA_FIREBASE=-=-=-=-=-=-=-=-=

   //=-=-=-=-=-=-=-=AUTENTICACAO=-=-=-=-=-=-=-=-=-=-=-=-=-=-=
    public static FirebaseAuth getFirebaseAutenticacao(){
        if(referenciaAutenticacao == null){
            referenciaAutenticacao = FirebaseAuth.getInstance();
        }
        return referenciaAutenticacao;
    }
   //=-=-=-=-=-=-=-=FIM_AUTENTICACAO=-=-=-=-=-=-=-=-=-=-=-=-=


   //=-=-=-=-=-=-=-==-=-=-STORAGE=-=-=-=-=-=-=-=-=-=-=-=-=-=
   public static StorageReference getFirebaseStorage(){
       if(storage == null){
           storage = FirebaseStorage.getInstance().getReference();
       }
       return storage;
   }
   //=-=-=-=-=-=-=-==-=-=FIM_STORAGE=-=-=-=-=-=-=-=-=-=-=-=-=

}
