package com.subtlebit.fran_.croatiandictionary;

import android.os.AsyncTask;
import android.util.Log;
import android.view.View;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;

public class LoadXMLAsync extends AsyncTask<String, String, String> {

    MainActivity MA;

    LoadXMLAsync(MainActivity mainActivity){
        MA = mainActivity;
    }

    String removeParenthesis(String source) {
        if(source.startsWith("(")) {
            int index_1 = source.indexOf("(");
            int index_2 = source.indexOf(") ", index_1) + 2;
            source = source.replace(source.substring(index_1,index_2-index_1),"");
        }
        return source;
    }

    String ReplaceQuotesInXML(String s){
        if(s!=null)
            return s.replace("/QUOTE/","'").replace("/DOUBLEQUOTE/","\"");
        return null;
    }

    int avrgsearchterms = 0;
    @Override
    protected String doInBackground(String ... params){
        long t1 = System.currentTimeMillis();

        try {
            InputStream IS = MA.getResources().getAssets().open("wordlist.xml");
            XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
            factory.setNamespaceAware(true);
            XmlPullParser xmltext = factory.newPullParser();
            xmltext.setInput(new InputStreamReader(IS));

            int eventType = xmltext.getEventType();
            Word newWord = new Word();

            while (eventType != xmltext.END_DOCUMENT) {
                if (eventType == xmltext.START_TAG) {
                    /*if(newWord.ID == 15483){
                        int a = 32;
                    }*/

                    if (xmltext.getName().equals("word")){
                        newWord = new Word();
                    }

                    else if (xmltext.getName().equals("ID")){
                        xmltext.next();
                        newWord.ID = Integer.parseInt(xmltext.getText());
                        if(MA.Favorites.contains(newWord.ID))
                            newWord.isFavorite = true;
                    }
                    else if (xmltext.getName().equals("name")){
                        xmltext.next();
                        newWord.Name = xmltext.getText();
                        newWord.SearchTerms.add(newWord.Name);
                        /*if(newWord.Name.equals("Bier")){
                            int a = 32;
                        }*/
                    }
                    else if (xmltext.getName().equals("spelling")){
                        xmltext.next();
                        String spelling = xmltext.getText();
                        newWord.Name += " (" + spelling + ")";
                        newWord.SearchTerms.add(spelling);
                    }
                    else if (xmltext.getName().equals("type")){
                        xmltext.next();
                        newWord.Type = xmltext.getText();
                    }
                    else if (xmltext.getName().equals("perfimpf")){
                        xmltext.next();
                        newWord.perfimpf = xmltext.getText();
                    }
                    else if (xmltext.getName().equals("webpage")){
                        xmltext.next();
                        newWord.Webpage = xmltext.getText();
                    }
                    else if (xmltext.getName().equals("originaldef")){
                        xmltext.next();
                        newWord.OriginalDefinition = ReplaceQuotesInXML(xmltext.getText());
                    }
                    else if (xmltext.getName().equals("synonyms")){
                        xmltext.next();
                        newWord.Synonyms = ReplaceQuotesInXML(xmltext.getText());
                    }
                    else if (xmltext.getName().equals("relatedwords")){
                        xmltext.next();
                        newWord.RelatedWords = ReplaceQuotesInXML(xmltext.getText());
                    }
                    else if (xmltext.getName().equals("english")){
                        xmltext.next();
                        xmltext.next();
                        while(xmltext.getName().equals("en")) {
                            xmltext.next();
                            String nEn = ReplaceQuotesInXML(xmltext.getText());
                            newWord.English.add(nEn);
                            newWord.SearchTerms.addAll(Arrays.asList(removeParenthesis(nEn).replace(", ",",").replace(";",",").replace("to ","").replace("(",",").split(",")));
                            xmltext.next();
                            xmltext.next();
                            eventType = xmltext.next();
                            if(eventType == xmltext.END_TAG) break;

                        }
                    }
                    else if (xmltext.getName().equals("examples")){
                        xmltext.next();
                        xmltext.next();
                        while(xmltext.getName().equals("ex")) {
                            xmltext.next();
                            newWord.Examples.add(ReplaceQuotesInXML(xmltext.getText()));
                            xmltext.next();
                            xmltext.next();
                            eventType = xmltext.next();
                            if(eventType == xmltext.END_TAG) break;
                        }
                    }
                    else if (xmltext.getName().equals("tables")){
                        xmltext.next();
                        xmltext.next();
                        while(xmltext.getName().equals("tb")) {
                            Table newTable = new Table();

                            xmltext.next();
                            newTable.TableName = xmltext.getText();

                            xmltext.next();
                            xmltext.next();
                            xmltext.next();
                            xmltext.next();

                            newTable.Content = xmltext.getText();
                            newWord.Tables.add(newTable);
                            newWord.SearchTerms.addAll(Arrays.asList(newTable.Content.replace(",,",",").split(",")));
                            xmltext.next();
                            xmltext.next();
                            eventType = xmltext.next();
                            if(eventType == xmltext.END_TAG)
                                break;


                        }
                    }
                }
                if (eventType == xmltext.END_TAG) {
                    Log.v("GAGA",xmltext.getName());
                    if (xmltext.getName().equals("word")){
                        MA.WordList.add(newWord);
                        avrgsearchterms += newWord.SearchTerms.size();


                        /*A.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                MA.LoadingTextView.setText("Loading words: " + Integer.toString(MA.WordList.size()));
                            }
                        });*/

                        if(newWord.English.size()==0)
                            Log.i("WORD: ", newWord.Name);
                    }
                }

                eventType = xmltext.next();
            }

            Collections.sort(MA.WordList, new Comparator<Word>() {
                @Override
                public int compare(Word o1, Word o2) {
                    return o1.Name.compareTo(o2.Name);
                }
            });

        } catch (Exception ex){
            Log.i("CACA",ex.getMessage().toString());
        }

        Log.i("SEARCHTERMS",Integer.toString(avrgsearchterms/MA.WordList.size()));
        Log.i("CACA: ", Integer.toString(MA.WordList.get(MA.WordList.size()-1).ID));
        Log.i("CACA: ", Integer.toString(MA.WordList.get(MA.WordList.size()-2).ID));
        Log.i("CACA: ", MA.WordList.get(MA.WordList.size()-1).Name);
        Log.i("CACA: ", MA.WordList.get(MA.WordList.size()-2).Name);
        Log.i("TIME: ", Long.toString(System.currentTimeMillis()-t1));

        onPostExecute("");
        return null;
    }

    @Override
    protected void onPostExecute(String s){
        MA.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                MA.LoadPage.setVisibility(View.GONE);
                MA.MainPage.setVisibility(View.VISIBLE);
                MA.SetWelcomeText();
            }
        });
        super.onPostExecute(s);
        return;
    }


}


