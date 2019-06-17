package com.subtlebit.fran_.croatiandictionary;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Icon;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Random;

public class AlarmReceiver extends BroadcastReceiver {

    public class SendNotificationWithWordOfTheDayAsync extends AsyncTask<String, String, String> {

        Context context;

        @Override
        protected String doInBackground(String ... params) {
            long when = System.currentTimeMillis();

            //Get word of the day
            Word wordOfTheDay = GetWordOfTheDay(context);
            if(wordOfTheDay == null) return null;
            String wordOfTheDayEnglish = wordOfTheDay.English.toString().replace("[","").replace("]","");

            //Set notification
            NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
            Intent notificationIntent = new Intent(context, MainActivity.class);
            notificationIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);

            NotificationHelper notificationHelper = new NotificationHelper(context);
            notificationHelper.sendNotification(wordOfTheDay.Name, wordOfTheDayEnglish, wordOfTheDay.ID);
            return null;
        }
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        SendNotificationWithWordOfTheDayAsync SNWWOTDA = new SendNotificationWithWordOfTheDayAsync();
        SNWWOTDA.context = context;
        SNWWOTDA.execute();
    }

    public Word GetWordOfTheDay(Context context){
        try{
            List<Word> WordList = LoadXMLExpress(context);
            if(WordList == null) return null;

            Random rand = new Random();
            Date today = new Date();
            DateFormat formatter = new SimpleDateFormat("dd/MM/yyyy");
            Date todayWithZeroTime = formatter.parse(formatter.format(today));
            rand.setSeed(todayWithZeroTime.getTime());
            int randInt = rand.nextInt(WordList.size()-1);

            Word wordOfTheDay = WordList.get(randInt);
            return wordOfTheDay;
        } catch (Exception ex){
            return null;
        }
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

    public List<Word> LoadXMLExpress(Context context) {

        List<Word> WordList = new ArrayList<>();

        try {
            InputStream IS = context.getResources().getAssets().open("wordlist.xml");
            XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
            factory.setNamespaceAware(true);
            XmlPullParser xmltext = factory.newPullParser();
            xmltext.setInput(new InputStreamReader(IS));

            int eventType = xmltext.getEventType();
            Word newWord = new Word();

            while (eventType != xmltext.END_DOCUMENT) {
                if (eventType == xmltext.START_TAG) {

                    if (xmltext.getName().equals("word")) {
                        newWord = new Word();
                    } else if (xmltext.getName().equals("ID")) {
                        xmltext.next();
                        newWord.ID = Integer.parseInt(xmltext.getText());
                    } else if (xmltext.getName().equals("name")) {
                        xmltext.next();
                        newWord.Name = xmltext.getText();
                        newWord.SearchTerms.add(newWord.Name);
                        /*if(newWord.Name.equals("Bier")){
                            int a = 32;
                        }*/
                    } else if (xmltext.getName().equals("english")) {
                        xmltext.next();
                        xmltext.next();
                        while (xmltext.getName().equals("en")) {
                            xmltext.next();
                            String nEn = ReplaceQuotesInXML(xmltext.getText());
                            newWord.English.add(nEn);
                            newWord.SearchTerms.addAll(Arrays.asList(removeParenthesis(nEn).replace(";", ",").replace("(", ",").split(",")));
                            xmltext.next();
                            xmltext.next();
                            eventType = xmltext.next();
                            if (eventType == xmltext.END_TAG) break;
                        }
                    }
                }
                if (eventType == xmltext.END_TAG) {
                    if (xmltext.getName().equals("word")){
                        WordList.add(newWord);
                    }
                }
                eventType = xmltext.next();
            }

            Collections.sort(WordList, new Comparator<Word>() {
                @Override
                public int compare(Word o1, Word o2) {
                    return o1.Name.compareTo(o2.Name);
                }
            });

            return WordList;
        } catch (Exception ex){
            return null;
        }
    }
}