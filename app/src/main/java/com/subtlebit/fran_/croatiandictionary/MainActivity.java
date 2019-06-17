package com.subtlebit.fran_.croatiandictionary;

import android.app.AlarmManager;
import android.app.Dialog;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.content.res.XmlResourceParser;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutCompat;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.Html;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.webkit.MimeTypeMap;
import android.webkit.WebView;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import com.android.billingclient.api.BillingClient;
import com.android.billingclient.api.BillingClientStateListener;
import com.android.billingclient.api.SkuDetails;
import com.android.billingclient.api.SkuDetailsResponseListener;

import org.w3c.dom.Text;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static android.webkit.ConsoleMessage.MessageLevel.LOG;

public class MainActivity extends AppCompatActivity {

    List<Word> WordList = new ArrayList<>();
    List<Word> SearchList = new ArrayList<>();
    List<Integer> HistorySearch = new ArrayList<>();
    List<Integer> Favorites = new ArrayList<>();

    public Word CurrentWord = null;

    public ListView lst;
    public WebView wbv;
    public LinearLayout lin;
    public LinearLayoutCompat MainPage;
    public LinearLayoutCompat LoadPage;
    public TextView LoadingTextView;
    public WebAppInterface webAppInterface;

    public boolean DarkModeEnabled = false;
    public boolean OriginalLanguageEnabled = false;
    public boolean ComingFromWelcomePage = true;
    public boolean IsFullModeActivated = false;
    public boolean NotificationsEnabled = false;

    public BuilderHTML BHTML;
    public Payment PaymentManager;
    String price_text = "";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        PaymentManager = new Payment(this, this);

        //SET UP CONTROLS
        Toolbar mTopToolbar = (Toolbar) findViewById(R.id.my_toolbar);
        setSupportActionBar(mTopToolbar);
        EditText searchTextBox = (EditText) findViewById(R.id.searchTextBox);
        lst = (ListView) findViewById(R.id.lstView);
        wbv = (WebView)  findViewById(R.id.wbView);
        lin = (LinearLayout) findViewById(R.id.welcomePageLayout);
        MainPage = (LinearLayoutCompat) findViewById(R.id.linearLayoutCompat);
        LoadPage = (LinearLayoutCompat) findViewById(R.id.LoadingScreen);
        LoadingTextView = (TextView) findViewById(R.id.LoadingWordsTextView);

        //LOAD HTML
        BHTML = new BuilderHTML(this);
        BHTML.ReadHTML();

        //INITIALIZE VIEW
        //ClearHistorySearch();
        LoadHistoryAndFavorites();
        LoadSettings();
        SearchTextBoxTextChanged("");
        if(IsFullModeActivated) ActivateFullMode();

        //SET UP WEBVIEW
        wbv.getSettings().setJavaScriptEnabled(true);
        wbv.getSettings().setDomStorageEnabled(true);
        webAppInterface = new WebAppInterface(this, this);
        wbv.addJavascriptInterface(webAppInterface, "Android");

        //SET UP CLICK AND TEXT CHANGE LISTENERS
        lst.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Word clickedWord = SearchList.get(position);
                ClickOnWordSearched(clickedWord);
            }
        });

        searchTextBox.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                SearchTextBoxTextChanged(s.toString());
            }
        });

        //LOAD XML
        LoadXMLAsync LXMLA = new LoadXMLAsync(this);
        LXMLA.execute();


        //setAlarmManager();

    }


    @Override
    protected void onResume(){
        super.onResume();


        //SET UP PAYMENT MANAGER
        if(!IsFullModeActivated) {

            PaymentManager.StartConnection(new BillingClientStateListener() {
                @Override
                public void onBillingSetupFinished(int responseCode) {
                    //PaymentManager.RemoveOwnership();

                    if(PaymentManager.RecheckOwnership()){
                        ActivateFullMode();
                        return;
                    }

                    PaymentManager.QuerySingleInappSku(PaymentManager.FullVersionSKU, new SkuDetailsResponseListener() {
                        @Override
                        public void onSkuDetailsResponse(int responseCode, List<SkuDetails> skuDetailsList) {
                            if (responseCode == BillingClient.BillingResponse.OK && skuDetailsList != null) {
                                if(skuDetailsList.size()>0) {
                                    //TextView PriceTextView = ((TextView) findViewById(R.id.priceTagTextView));
                                    //PriceTextView.setText("(For only " + skuDetailsList.get(0).getPrice() + ")");
                                    //PriceTextView.setVisibility(View.VISIBLE);
                                    //Button button = ((Button) findViewById(R.id.GetFullVersionButton));
                                    price_text = "GET FULL VERSION\n(FOR ONLY " + skuDetailsList.get(0).getPrice() + ")";
                                }
                            }
                        }
                    });
                }

                @Override
                public void onBillingServiceDisconnected() {

                }
            });
        }


    }

    @Override
    protected void onDestroy() {
        //Close the Text to Speech Library
        if(webAppInterface.TTS != null) {
            webAppInterface.TTS.stop();
            webAppInterface.TTS.shutdown();
        }
        super.onDestroy();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (Integer.parseInt(Build.VERSION.SDK) > 5
                && keyCode == KeyEvent.KEYCODE_BACK
                && event.getRepeatCount() == 0) {

            if(lst.getVisibility() == View.VISIBLE){
                CancelButtonClick(null);
                return true;
            }
            else if(wbv.getVisibility() == View.VISIBLE){
                wbv.setVisibility(View.GONE);
                lst.setVisibility(View.GONE);
                lin.setVisibility(View.VISIBLE);
                return true;
            }

        }
        return super.onKeyDown(keyCode, event);
    }

    public void SetWelcomeText(){
        TextView welcomeTextView = (TextView) findViewById(R.id.WelcomeTextView);

        try{
            Random rand = new Random();
            Date today = new Date();
            DateFormat formatter = new SimpleDateFormat("dd/MM/yyyy");
            Date todayWithZeroTime = formatter.parse(formatter.format(today));
            rand.setSeed(todayWithZeroTime.getTime());
            int randInt = rand.nextInt(WordList.size()-1);
            Word wordOfTheDay = WordList.get(randInt);

            String wordOfTheDayEnglish = wordOfTheDay.English.toString().replace("[","").replace("]","");
            //String welcomeText = "Welcome to " + getString(R.string.app_name) + " Dictionary<br><br><br><br>";
            String welcomeText = "";
            welcomeText += "<h4>Word of the day</h4><br>";
            welcomeText += "<h2 style=\"font-family:sans-serif-thin\">" + wordOfTheDay.Name + "</h2>";
            welcomeText += "<br><i>" + wordOfTheDayEnglish + "</i>";

            welcomeTextView.setText(Html.fromHtml(welcomeText));
        } catch (Exception ex){
            welcomeTextView.setText("");
        }
    }

    public void ShareClick(View v){
    try {
        findViewById(R.id.shareButton).setVisibility(View.INVISIBLE);
        findViewById(R.id.grandImageShare).setVisibility(View.VISIBLE);
        findViewById(R.id.flagIconShare).setVisibility(View.VISIBLE);

        Bitmap bmp = Bitmap.createBitmap(lin.getWidth(), lin.getWidth(), Bitmap.Config.ARGB_8888);
        Canvas cnv = new Canvas(bmp);
        Drawable drawable = lin.getBackground();
        if (drawable != null)
            drawable.draw(cnv);
        else
            cnv.drawColor(Color.RED);
        lin.draw(cnv);

        findViewById(R.id.shareButton).setVisibility(View.VISIBLE);
        findViewById(R.id.grandImageShare).setVisibility(View.INVISIBLE);
        findViewById(R.id.flagIconShare).setVisibility(View.INVISIBLE);

        Uri bitmapUri = saveImageExternal(bmp);

        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("image/png");
        intent.putExtra(Intent.EXTRA_STREAM, bitmapUri);
        startActivity(Intent.createChooser(intent, "Share"));
    } catch (Exception ex){
        Toast.makeText(this, "Ups! Something went wrong", Toast.LENGTH_SHORT).show();
    }

    }

    private Uri saveImageExternal(Bitmap image) {

        Uri uri = null;
        try {
            File file = new File(getExternalFilesDir(Environment.DIRECTORY_PICTURES), "to-share.png");
            FileOutputStream stream = new FileOutputStream(file);
            image.compress(Bitmap.CompressFormat.PNG, 90, stream);
            stream.close();

            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N) {
                uri = Uri.fromFile(file);
            } else {
                uri = FileProvider.getUriForFile(this, "com.subtlebit.fran_.croatiandictionary.fileprovider", file);
            }

        } catch (IOException e) {
            Log.d("HOLA", "IOException while trying to write file for sharing: " + e.getMessage());
        }
        return uri;

    }

    //Alarm Manager to deliver push notifications
    public void setAlarmManager(boolean on){
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, 11);
        calendar.set(Calendar.MINUTE, 00);
        calendar.set(Calendar.SECOND, 0);
        Intent intent1 = new Intent(MainActivity.this, AlarmReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(MainActivity.this, 0,intent1, PendingIntent.FLAG_UPDATE_CURRENT);
        AlarmManager am = (AlarmManager) MainActivity.this.getSystemService(MainActivity.this.ALARM_SERVICE);

        if(on)
            am.setRepeating(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), AlarmManager.INTERVAL_DAY, pendingIntent);
        else
            am.cancel(pendingIntent);
    }

    public void LoadSettings(){
        SharedPreferences SP = getApplicationContext().getSharedPreferences(getResources().getString(R.string.config),Context.MODE_PRIVATE);
        DarkModeEnabled = SP.getBoolean("DarkModeEnab",false);
        if (DarkModeEnabled) {
            SetDarkMode();
            //((CheckBox) findViewById(R.id.EnableDarkBodeCheckBox)).setChecked(true);
        }

        OriginalLanguageEnabled = SP.getBoolean("OriginalLanguageEnab",false);
        //((CheckBox) findViewById(R.id.EnableOriginalLanguage)).setChecked(OriginalLanguageEnabled);

        IsFullModeActivated = SP.getBoolean("IsFullModeActivated", false);
        if(IsFullModeActivated){
            ActivateFullMode();
        }

        NotificationsEnabled = SP.getBoolean("NotificationsEnab", false);
    }

    public void ActivateFullMode() {
        SharedPreferences SP = getApplicationContext().getSharedPreferences(getResources().getString(R.string.config), Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = SP.edit();
        editor.putBoolean("IsFullModeActivated", true);
        editor.apply();

        //((TextView) findViewById(R.id.welomeTextView)).setVisibility(View.GONE);
        //((TextView) findViewById(R.id.GetFullVersionButton)).setVisibility(View.GONE);
        //((TextView) findViewById(R.id.priceTagTextView)).setVisibility(View.GONE);

        //FlagButtonClick(null);
        IsFullModeActivated = true;
    }

    public void ClickOnRateAppButton(View v){

        String appname = getResources().getString(R.string.applink);
        Uri uri = Uri.parse("market://details?id=" + appname);
        Intent goToMarket = new Intent(Intent.ACTION_VIEW, uri);

        goToMarket.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY |
                Intent.FLAG_ACTIVITY_NEW_DOCUMENT |
                Intent.FLAG_ACTIVITY_MULTIPLE_TASK);

        try {
            startActivity(goToMarket);
        } catch (ActivityNotFoundException e) {
            startActivity(new Intent(Intent.ACTION_VIEW,Uri.parse("http://play.google.com/store/apps/details?id=" + appname)));
        }

    }

    public void LoadHistoryAndFavorites(){
        SharedPreferences SP = getApplicationContext().getSharedPreferences(getResources().getString(R.string.config),Context.MODE_PRIVATE);
        String str = SP.getString("HistoryList","");
        String[] strLst = str.split(",");
        for(String s : strLst)
            if(!s.equals(""))
                HistorySearch.add(0,Integer.parseInt(s));

        str = SP.getString("FavoritesList","");
        String[] strLst2 = str.split(",");
        for(String s: strLst2)
            if(!s.equals(""))
                Favorites.add(0,Integer.parseInt(s));
    }

    public void AddWordToHistorySearch(Word word){
        HistorySearch.remove(Integer.valueOf(word.ID));
        HistorySearch.add(0,word.ID);
        SharedPreferences SP = getApplicationContext().getSharedPreferences(getResources().getString(R.string.config),Context.MODE_PRIVATE);
        String str = SP.getString("HistoryList","");
        SharedPreferences.Editor editor = SP.edit();
        editor.putString("HistoryList",HistorySearch.toString().replace(" ","").replace("[","").replace("]",""));
        editor.apply();

        if(HistorySearch.size()>50){
            HistorySearch.remove(HistorySearch.size()-1);
        }
    }

    public void ClickOnPayButton(View v){
        PaymentManager.startPurchaseFlow(PaymentManager.FullVersionSKU, BillingClient.SkuType.INAPP);
    }

    public void ClearHistorySearch(){
        SharedPreferences SP = getApplicationContext().getSharedPreferences(getResources().getString(R.string.config),Context.MODE_PRIVATE);
        String str = SP.getString("HistoryList","");
        SharedPreferences.Editor editor = SP.edit();
        editor.putString("HistoryList","");
        editor.apply();
    }

    public void ClickOnWordSearched(Word word){
        BHTML.SetWebViewContent(word);
        ComingFromWelcomePage = false;
        ((LinearLayout) findViewById(R.id.normalToolbar)).setVisibility(View.VISIBLE);
        ((LinearLayout) findViewById(R.id.searchToolbar)).setVisibility(View.GONE);
        wbv.setVisibility(View.VISIBLE);
        lst.setVisibility(View.GONE);
        AddWordToHistorySearch(word);
        hideSoftKeyboard(findViewById(R.id.searchTextBox));
        ((EditText) findViewById(R.id.searchTextBox)).setText("");
        CurrentWord = word;
    }

    public void SearchButtonClick(View v){
        ((LinearLayout) findViewById(R.id.normalToolbar)).setVisibility(View.GONE);
        ((LinearLayout) findViewById(R.id.searchToolbar)).setVisibility(View.VISIBLE);
        wbv.setVisibility(View.GONE);
        lst.setVisibility(View.VISIBLE);
        lin.setVisibility(View.GONE);
        showSoftKeyboard(findViewById(R.id.searchTextBox));
        ((EditText) findViewById(R.id.searchTextBox)).setText("");
    }

    public void SendFeedbackButtonClick(View v){
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
        LayoutInflater inflater = this.getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.feedback_dialog,null);
        dialogBuilder.setView(dialogView);

        final AlertDialog alertDialog = dialogBuilder.create();

        /*alertDialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialog) {
                ((CheckBox) findViewById(R.id.EnableOriginalLanguage)).setChecked(OriginalLanguageEnabled);
            }
        });*/

        alertDialog.setButton(Dialog.BUTTON_POSITIVE, "SEND", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String subj = getResources().getString(R.string.app_name) + " Feedback: ";
                if(((RadioButton) alertDialog.findViewById(R.id.RB_MissingWord)).isChecked()){
                    subj += "Missing Word";
                } else if(((RadioButton) alertDialog.findViewById(R.id.RB_NewFeature)).isChecked()){
                    subj += "New Feature";
                } else if(((RadioButton) alertDialog.findViewById(R.id.RB_WrongEntry)).isChecked()){
                    subj += "Wrong Entry";
                } else if(((RadioButton) alertDialog.findViewById(R.id.RB_Other)).isChecked()){
                    subj += "Other";
                }

                String msg = ((EditText) alertDialog.findViewById(R.id.FeedbackMessage)).getText().toString();
                if (msg.length()>0) {
                    SendEmail ES = new SendEmail(subj, msg , MainActivity.this);
                    ES.execute();
                }
            }
        });

        alertDialog.show();
    }

    public void CancelButtonClick(View v){
        ((LinearLayout) findViewById(R.id.normalToolbar)).setVisibility(View.VISIBLE);
        ((LinearLayout) findViewById(R.id.searchToolbar)).setVisibility(View.GONE);
        hideSoftKeyboard(findViewById(R.id.searchTextBox));
        ((EditText) findViewById(R.id.searchTextBox)).setText("");

        if(ComingFromWelcomePage){
            wbv.setVisibility(View.GONE);
            lst.setVisibility(View.GONE);
            lin.setVisibility(View.VISIBLE);
        } else {
            wbv.setVisibility(View.VISIBLE);
            lst.setVisibility(View.GONE);
            lin.setVisibility(View.GONE);
        }
    }

    public void FlagButtonClick(View v){
        //wbv.setVisibility(View.GONE);
        //lst.setVisibility(View.GONE);
        //lin.setVisibility(View.VISIBLE);
        //ComingFromWelcomePage = true;
        //TESTALLWORDS();
        OpenSettingsDialog();
    }

    public void SetNotifications(View v){
        if(!NotificationsEnabled){
            //if (((CheckBox) findViewById(R.id.EnableOriginalLanguage)).isChecked()) {
            NotificationsEnabled = true;
        } else {
            NotificationsEnabled = false;
        }

        SharedPreferences SP = getApplicationContext().getSharedPreferences(getResources().getString(R.string.config),Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = SP.edit();
        editor.putBoolean("NotificationsEnab",NotificationsEnabled);
        editor.apply();
        setAlarmManager(NotificationsEnabled);
    }

    void OpenSettingsDialog(){
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
        LayoutInflater inflater = this.getLayoutInflater();
        final View dialogView = inflater.inflate(R.layout.settings_dialog,null);
        dialogBuilder.setView(dialogView);

        final AlertDialog alertDialog = dialogBuilder.create();

        alertDialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialog) {
                if(DarkModeEnabled) {
                    ((CheckBox) dialogView.findViewById(R.id.EnableDarkBodeCheckBox)).setChecked(true);
                    //((CheckBox) dialogView.findViewById(R.id.EnableDarkBodeCheckBox)).setTextColor(Color.parseColor("#fafafa"));
                    //((CheckBox) dialogView.findViewById(R.id.EnableOriginalLanguage)).setTextColor(Color.parseColor("#fafafa"));
                    //((LinearLayout) dialogView.findViewById(R.id.settings_frame)).setBackgroundColor(Color.parseColor("#3c3c3c"));
                } else {
                    ((CheckBox) dialogView.findViewById(R.id.EnableDarkBodeCheckBox)).setChecked(false);
                    //((CheckBox) dialogView.findViewById(R.id.EnableDarkBodeCheckBox)).setTextColor(Color.parseColor("#000000"));
                    //((CheckBox) dialogView.findViewById(R.id.EnableOriginalLanguage)).setTextColor(Color.parseColor("#000000"));
                    //((LinearLayout) dialogView.findViewById(R.id.settings_frame)).setBackgroundColor(Color.parseColor("#fafafa"));
                }

                if(OriginalLanguageEnabled){
                    ((CheckBox) dialogView.findViewById(R.id.EnableOriginalLanguage)).setChecked(true);
                } else {
                    ((CheckBox) dialogView.findViewById(R.id.EnableOriginalLanguage)).setChecked(false);
                }

                if(NotificationsEnabled){
                    ((CheckBox) dialogView.findViewById(R.id.EnableNotificationsCheckBox)).setChecked(true);
                } else {
                    ((CheckBox) dialogView.findViewById(R.id.EnableNotificationsCheckBox)).setChecked(false);
                }

                if(IsFullModeActivated){
                    ((TextView) dialogView.findViewById(R.id.welomeTextView)).setVisibility(View.GONE);
                    ((TextView) dialogView.findViewById(R.id.GetFullVersionButton)).setVisibility(View.GONE);
                } else {
                    if(price_text!="") ((Button) dialogView.findViewById(R.id.GetFullVersionButton)).setText(price_text);
                }

            }
        });

        alertDialog.setButton(Dialog.BUTTON_NEGATIVE, "Close", new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                if(CurrentWord!=null) BHTML.SetWebViewContent(CurrentWord);
            }
        });

        alertDialog.show();
    }

    public void SetLightMode(){
        BHTML.default_html = BHTML.default_html.replace("color: #fafafa;","color: #000000;");
        BHTML.default_html = BHTML.default_html.replace("#606060;","#cccccc;");
        BHTML.default_html = BHTML.default_html.replace("sound_icon_606060","sound_icon_cccccc");
        BHTML.default_html = BHTML.default_html.replace("star_icon_606060","star_icon_cccccc");
        BHTML.default_html = BHTML.default_html.replace("unstar_icon_606060","unstar_icon_cccccc");
        BHTML.default_html = BHTML.default_html.replace("background-color: #3c3c3c;","background-color: #fafafa;");

        //((CheckBox) findViewById(R.id.EnableDarkBodeCheckBox)).setTextColor(Color.parseColor("#000000"));
        //((CheckBox) findViewById(R.id.EnableOriginalLanguage)).setTextColor(Color.parseColor("#000000"));
        findViewById(R.id.frmLayout).setBackgroundColor(Color.parseColor("#fafafa"));

        DarkModeEnabled = false;
    }

    public void SetDarkMode(){
        BHTML.default_html = BHTML.default_html.replace("color: #000000;","color: #fafafa;");
        BHTML.default_html = BHTML.default_html.replace("#cccccc;","#606060;");
        BHTML.default_html = BHTML.default_html.replace("sound_icon_cccccc","sound_icon_606060");
        BHTML.default_html = BHTML.default_html.replace("star_icon_cccccc","star_icon_606060");
        BHTML.default_html = BHTML.default_html.replace("unstar_icon_cccccc","unstar_icon_606060");

        BHTML.default_html = BHTML.default_html.replace("background-color: #fafafa;","background-color: #3c3c3c;");

        //((CheckBox) findViewById(R.id.EnableDarkBodeCheckBox)).setTextColor(Color.parseColor("#fafafa"));
        //((CheckBox) findViewById(R.id.EnableOriginalLanguage)).setTextColor(Color.parseColor("#fafafa"));
        findViewById(R.id.frmLayout).setBackgroundColor(Color.parseColor("#3c3c3c"));

        DarkModeEnabled = true;
    }

    public void ChangeColorMode(View v){
        if(!DarkModeEnabled){
        //if (((CheckBox) findViewById(R.id.EnableDarkBodeCheckBox)).isChecked()) {
            SetDarkMode();
       } else {
            SetLightMode();
        }

        SharedPreferences SP = getApplicationContext().getSharedPreferences(getResources().getString(R.string.config),Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = SP.edit();
        editor.putBoolean("DarkModeEnab",DarkModeEnabled);
        editor.apply();
    }

    public void ChangeLanguage(View v){
        if(!OriginalLanguageEnabled){
        //if (((CheckBox) findViewById(R.id.EnableOriginalLanguage)).isChecked()) {
            OriginalLanguageEnabled = true;
        } else {
            OriginalLanguageEnabled = false;
        }

        SharedPreferences SP = getApplicationContext().getSharedPreferences(getResources().getString(R.string.config),Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = SP.edit();
        editor.putBoolean("OriginalLanguageEnab",OriginalLanguageEnabled);
        editor.apply();
    }

    private String toUpper(String s){
        s = s.replace("a","A");
        s = s.replace("b","B");
        s = s.replace("c","C");
        s = s.replace("d","D");
        s = s.replace("e","E");
        s = s.replace("f","F");
        s = s.replace("g","G");
        s = s.replace("h","H");
        s = s.replace("i","I");
        s = s.replace("j","J");
        s = s.replace("k","K");
        s = s.replace("l","L");
        s = s.replace("m","M");
        s = s.replace("n","N");
        s = s.replace("o","O");
        s = s.replace("p","P");
        s = s.replace("q","Q");
        s = s.replace("r","R");
        s = s.replace("s","S");
        s = s.replace("t","T");
        s = s.replace("u","U");
        s = s.replace("v","V");
        s = s.replace("w","W");
        s = s.replace("x","X");
        s = s.replace("y","Y");
        s = s.replace("z","Z");

        return s;
    }

    public boolean FavortiesOnly = false;
    public void SeeOnlyFavorites(View v){
        if(FavortiesOnly){
            FavortiesOnly = false;
            ((ImageView) findViewById(R.id.seeFavButton)).setImageResource(R.drawable.ic_unstar);
        } else {
            ((ImageView) findViewById(R.id.seeFavButton)).setImageResource(R.drawable.ic_star);
            FavortiesOnly = true;
        }

        SearchTextBoxTextChanged(((EditText) findViewById(R.id.searchTextBox)).getText().toString());
    }

    public void SearchTextBoxTextChanged(final String s) {
        SearchList.clear();
        if(s.equals("")) {
            if(FavortiesOnly) {
                //SHOW FAVORITES
                for (Word w : WordList){
                    if(Favorites.contains(w.ID)){
                        SearchList.add(w);
                    }
                }
            } else {
                for(int h : HistorySearch){
                    SearchList.add(null);
                }
                for(Word w : WordList){
                    if(HistorySearch.contains(w.ID)){
                        //Log.i("HEKK:" , Integer.toString(w.ID));
                        SearchList.set(HistorySearch.indexOf(w.ID),w);
                    }
                }
                SearchList.removeAll(Collections.singleton(null));
            }
        }
        else {
            long t1 = System.currentTimeMillis();
            final String s_up = toUpper(s.substring(0,1)) + s.substring(1);

            for(Word word : WordList){

                if(FavortiesOnly && !word.isFavorite)
                    continue;

                for(int i=0;i<word.SearchTerms.size();i++){
                    //Log.i("TIME",term + " + " + word.Name);
                    if(i>350){ break; }
                    String term = word.SearchTerms.get(i);
                    if(i<3) {
                        if (term.startsWith(s) || term.startsWith(s_up)) {
                            SearchList.add(word);
                            word.SearchPriority = i;
                            break;
                        }
                    }else{
                        if (term.startsWith(s)) {
                            SearchList.add(word);
                            word.SearchPriority = i;
                            break;
                        }
                    }
                }

                //if(word.Name.contains(s)){ word.SearchPriority = 3; SearchList.add(word); }
                //else if(word.Name.contains(s_up)){ word.SearchPriority = 3; SearchList.add(word); }
                //else if(sWith(word.Name,"(" + s)){ word.SearchPriority = 3; SearchList.add(word); }
                //else if(sWith(word.English.get(0),s)){ word.SearchPriority = 2;SearchList.add(word); }
                //else if(word.English.get(0).startsWith(s_up)) { word.SearchPriority = 3; SearchList.add(word); }
                //else if(word.English.get(0).contains(" " + s)){ word.SearchPriority = 1;  SearchList.add(word); }
                //else if(word.English.size()>1) { if (word.English.get(1).contains(s)) { word.SearchPriority = 1; SearchList.add(word); } }
                /*else{
                    for(Table table : word.Tables){
                        if(table.Content.contains((s))){
                            word.SearchPriority = 1;
                            SearchList.add(word);
                            break;
                        }
                    }
                }*/


            }
            Collections.sort(SearchList, new Comparator<Word>() {
                @Override
                public int compare(Word o1, Word o2) {
                    if (o1.SearchPriority > o2.SearchPriority) {
                        return 1;
                    }
                    if (o1.SearchPriority < o2.SearchPriority) {
                        return -1;
                    }
                    return 0;
                }
            });
            Log.i("TIMEc: ", Long.toString(System.currentTimeMillis()-t1));
        }

        if(DarkModeEnabled){
            lst.setBackgroundColor(Color.parseColor("#3c3c3c"));
        } else {
            lst.setBackgroundColor(Color.parseColor("#ffffff"));
        }

        ListAdapter adp = new ListAdapter(getApplicationContext(), SearchList, DarkModeEnabled);
        lst.setAdapter(adp);
    }

    public void showSoftKeyboard(View view) {
        if (view.requestFocus()) {
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.showSoftInput(view, InputMethodManager.SHOW_IMPLICIT);
        }
    }

    public void hideSoftKeyboard(View view) {
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(view.getWindowToken(), InputMethodManager.HIDE_IMPLICIT_ONLY);
    }

    public void TESTALLWORDS(){
        for(Word w: WordList){
            Log.i("TEST",w.Name);
            BHTML.SetWebViewContent(w);
        }
    }

}
