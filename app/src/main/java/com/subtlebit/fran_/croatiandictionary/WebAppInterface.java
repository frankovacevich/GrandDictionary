package com.subtlebit.fran_.croatiandictionary;

import android.content.Context;
import android.content.SharedPreferences;
import android.speech.tts.TextToSpeech;
import android.webkit.JavascriptInterface;
import android.widget.Toast;

import java.util.Locale;

public class WebAppInterface {
    Context context;
    TextToSpeech TTS;
    MainActivity MA;
    String language;
    WebAppInterface(Context c, MainActivity m){
        context = c;
        MA = m;
        language = context.getResources().getString(R.string.language);
        TTS = new TextToSpeech(c, new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if (status== TextToSpeech.SUCCESS){
                    TTS.setLanguage(new Locale(language));
                } else {
                    Toast.makeText(context, "Text to speech failed", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    @JavascriptInterface
    public void playButtonClick(String word){
        if(language.equals("ru-RU")){
            word = word.substring(0,word.indexOf("<"));
        }

        TTS.setPitch(0.7f);
        TTS.setSpeechRate(0.1f);
        TTS.speak(word,TextToSpeech.QUEUE_FLUSH,null);
    }

    @JavascriptInterface
    public void buyButtonClick(){
        MA.ClickOnPayButton(null);
    }

    @JavascriptInterface
    public void starButtonClick(){
        if(MA.CurrentWord.isFavorite) {
            MA.CurrentWord.isFavorite = false;
            MA.Favorites.remove(Integer.valueOf(MA.CurrentWord.ID));
        } else {
            MA.CurrentWord.isFavorite = true;
            MA.Favorites.remove(Integer.valueOf(MA.CurrentWord.ID));
            MA.Favorites.add(0,MA.CurrentWord.ID);
        }
        SharedPreferences SP = MA.getApplicationContext().getSharedPreferences(context.getResources().getString(R.string.config),Context.MODE_PRIVATE);
        String str = SP.getString("FavoritesList","");
        SharedPreferences.Editor editor = SP.edit();
        editor.putString("FavoritesList",MA.Favorites.toString().replace(" ","").replace("[","").replace("]",""));
        editor.apply();
    }
}
