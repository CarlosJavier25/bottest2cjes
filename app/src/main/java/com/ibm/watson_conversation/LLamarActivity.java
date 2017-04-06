package com.ibm.watson_conversation;

import android.content.Intent;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

public class LLamarActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_llamar);






        try{
            startActivity(new Intent(Intent.ACTION_CALL, Uri.parse("tel:5552705900")));
        }catch(Exception e){
            e.printStackTrace();
        }



    }
}
