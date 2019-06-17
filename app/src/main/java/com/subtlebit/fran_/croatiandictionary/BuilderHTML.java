package com.subtlebit.fran_.croatiandictionary;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;

public class BuilderHTML {
    MainActivity MA;
    String default_html = "";

    BuilderHTML(MainActivity mainActivity){
        MA = mainActivity;
    }
    public void ReadHTML(){
        try{
            InputStream IS = MA.getResources().getAssets().open("default_page.html");
            byte[] buffer = new byte[IS.available()];
            IS.read(buffer);
            ByteArrayOutputStream OS = new ByteArrayOutputStream();
            OS.write(buffer);
            OS.close();
            IS.close();
            default_html = OS.toString();
            default_html = default_html.replace("replace_main_color","#" + Integer.toHexString(MA.getResources().getColor(R.color.colorPrimary) & 0x00ffffff));
            default_html = default_html.replace("replace_secondary_color","#" + Integer.toHexString(MA.getResources().getColor(R.color.colorPrimaryDark) & 0x00ffffff));
        } catch (Exception ex){
        }
    }

    int tb_counter = 0;
    String getTableContent(String[] Cols, String[] HeaderRow, String tableid, String width, String alignment,Boolean translateSecondRow){
        String bod = "";
        if(MA.IsFullModeActivated || tb_counter < 4){
            bod += "<table" + tableid + "><col width =\"" + width + "\"><col>\n";
            for(int k=1; k<Cols.length; k++){
                bod += "<tr>\n";
                bod += "<td align=\"" + alignment + "\">" + TranslateKeyWordsToOriginalLanguage(HeaderRow[k]) + "</td>\n";
                bod += "<td align=\"" + alignment + "\">" + ((translateSecondRow && k==1) ? TranslateKeyWordsToOriginalLanguage(Cols[k]) : Cols[k]) + "</td>\n";
                bod += "</tr>\n";
            }
            bod += "</table>\n";
        } else {
            bod += "<div" + tableid.replace("id=\"tb","id=\"btn-tb") + "class=\"buy-full-version-button\" onclick=\"buyClick()\">GET FULL VERSION</div>";
        }
        tb_counter++;
        return bod;
    }

    String getClassicTable(Table tb, String width, String alignment){
        String bod = "";

        String[] Rows = tb.Content.split(",,");
        String[] HeaderRow = Rows[0].split(",");

        for(int j=1; j<Rows.length; j++){
            String[] Cols = Rows[j].split(",");
            bod += "<div class=\"roundy-bx\">\n";
            bod += "<div class=\"roundy-bx-title\">";
            bod += TranslateKeyWordsToOriginalLanguage(Cols[0]);
            bod += "</div>\n";
            bod += getTableContent(Cols,HeaderRow,"",width, alignment, false);
            bod += "</div>\n";
        }

        return bod;
    }

    String getTabbedTable(Table tb, int tableNumber, String width, String alignment){
        String bod = "";

        String[] Rows = tb.Content.split(",,");
        String[] HeaderRow = Rows[0].split(",");

        bod += "<div class=\"roundy-bx\">\n";
        bod += "<div class=\"roundy-bx-title choose-bx-title\">\n";
        bod += "<table cellspacing=\"0\" class=\"choose-bx-title\">\n";
        bod += "<tr>\n";

        int N = Rows.length - 1;
        for(int i=1;i<=N;i++){
            String SelBtnExtra = i==1 ? "choose-btn-selected" : "choose-btn";
            bod += "<td align=\"center\" id=\"bn-" + Integer.toString(tableNumber) + "-r-" + Integer.toString(i);
            bod += "\" class=\"" + SelBtnExtra + "\" onclick=\"tabBtnClick(" + Integer.toString(tableNumber);
            bod += "," + Integer.toString(i) + "," + Integer.toString(N) + ")\">";
            bod += TranslateKeyWordsToOriginalLanguage(Rows[i].split(",")[0]);
            bod += "</td>\n";
        }

        bod += "</tr>\n";
        bod += "</table>\n";
        bod += "</div>\n";

        for(int i=1; i<Rows.length; i++){
            String[] Cols = Rows[i].split(",");
            String IDExtra = " id=\"tb-" + Integer.toString(tableNumber) + "-r-" + Integer.toString(i) + "\"";
            String StyleExtra = i==1 ? "" : " style=\"display: none\"";
            bod += getTableContent(Cols,HeaderRow, IDExtra + StyleExtra, width, alignment, false);
        }

        bod += "</div>\n";
        return bod;
    }

    String auxGermanVerbTable(String TableContent, int tableNumber, String width, String alignment){
        String bod = "";

        String[] Rows = TableContent.split(",,");
        String[] TitleRow = Rows[0].split(",");

        bod += "<div class=\"roundy-bx\">\n";
        bod += "<div class=\"roundy-bx-title choose-bx-title\">\n";
        bod += "<table cellspacing=\"0\" class=\"choose-bx-title\">\n";
        bod += "<tr>\n";

        int N = TitleRow.length - 1;
        for(int i=1;i<=N;i++){
            String SelBtnExtra = i==1 ? "choose-btn-selected" : "choose-btn";
            bod += "<td align=\"center\" id=\"bn-" + Integer.toString(tableNumber) + "-r-" + Integer.toString(i);
            bod += "\" class=\"" + SelBtnExtra + "\" onclick=\"tabBtnClick(" + Integer.toString(tableNumber);
            bod += "," + Integer.toString(i) + "," + Integer.toString(N) + ")\">";
            bod += TranslateKeyWordsToOriginalLanguage(TitleRow[i]);
            bod += "</td>\n";
        }

        bod += "</tr>\n";
        bod += "</table>\n";
        bod += "</div>\n";

        int i2 = 1;
        for(int i=1; i<Rows.length; i+=2){
            String[] Cols1 = ("null,"+Rows[i]).split(",");
            Cols1[1] = "<b>" + Cols1[1] + "</b>";
            String[] Cols2 = ("null,"+Rows[i+1]).split(",");
            Cols2[1] = "<b>" + Cols2[1] + "</b>";
            String IDExtra = " id=\"tb-" + Integer.toString(tableNumber) + "-r-" + Integer.toString(i2) + "\"";
            String StyleExtra = i==1 ? "" : " style=\"display: none\"";
            bod += getTableContent(Cols2,Cols1, IDExtra + StyleExtra, width, alignment,true);
            i2++;
        }

        bod += "</div>\n";
        return bod;
    }

    int GermanVerbTableNumber = 0;
    String getGermanVerbTable(Table tb){
        GermanVerbTableNumber++;
        if(tb.TableName.equals("Basic forms")){
            return getClassicTable(tb,"50%","center");
        }
        else if(tb.TableName.equals("Simple forms")){
            String ctable = TranslateKeyWordsToOriginalLanguage("null,present and past,subjunctive,,");
            String[] Rows = tb.Content.split(",,");
            ctable += Rows[1] + ",," + Rows[3] + ",," + Rows[2] + ",," + Rows[4];
            String bod1 = auxGermanVerbTable(ctable,GermanVerbTableNumber,"50%","center");

            GermanVerbTableNumber++;
            String dtable = Rows[0] + ",," + Rows[5];
            Table dTable = new Table();
            dTable.Content = dtable;
            String bod2 = getClassicTable(dTable,"50%","center");
            return bod1 + bod2;
        }
        else if(tb.TableName.equals("Subordinate-clause forms")){
            String ctable = TranslateKeyWordsToOriginalLanguage("null,present and past,subjunctive,,");
            String[] Rows = tb.Content.split(",,");
            ctable += Rows[1] + ",," + Rows[3] + ",," + Rows[2] + ",," + Rows[4];
            String bod = auxGermanVerbTable(ctable,GermanVerbTableNumber,"50%","center");
            return bod;
        }
        else if(tb.TableName.equals("Composed forms")){
            String[] Rows = tb.Content.split(",,");

            String ctable = TranslateKeyWordsToOriginalLanguage("null,pres. perf. and pluperf.,subjunctive,,");
            ctable += Rows[1] + ",," + Rows[3] + ",," + Rows[2] + ",," + Rows[4];
            String bod1 = auxGermanVerbTable(ctable,GermanVerbTableNumber,"50%","center");

            GermanVerbTableNumber++;
            String dtable = TranslateKeyWordsToOriginalLanguage("null,future,subjunctive I,subjunctive II,,");
            dtable += Rows[6] + ",," + Rows[9] + ",," + Rows[5] + ",," + Rows[8] + ",," + Rows[7] + ",," + Rows[10];
            String bod2 = auxGermanVerbTable(dtable,GermanVerbTableNumber,"50%","center");

            return bod1 + bod2;
        }

        return "";
    }

    void SetWebViewContent(Word word){
        tb_counter = 0;
        String bod = "";
        bod += "<div style=\"height: 90px;\">\n<div style=\"float: left;\">\n";
        bod += "<div class=\"main-wrd\" id=\"word_name\">" + word.Name.replace(" (","<br />(") + "</div>\n";
        bod += "<div class=\"sub-wrd\">" + TranslateKeyWordsToOriginalLanguage(word.Type) + "</div></div>\n";
        bod += "<div class=\"play-button\" onclick=\"playClick() \"><image class=\"play-button-img\"></image></div>\n";
        if(word.isFavorite){
            bod += "<div class=\"play-button\" onclick=\"starClick()\"><image id=\"starbtn\" class=\"star-button-img\"></image></div>\n";
        }else{
            bod += "<div class=\"play-button\" onclick=\"starClick()\"><image id=\"starbtn\" class=\"unstar-button-img\"></image></div>\n";
        }
        bod += "</div>";

        if(word.Name.contains("(")){ bod += "<br/>"; }

        if(word.English.size()>0) {
            bod += "<div class=\"roundy-bx\">\n<div class=\"roundy-bx-title\">" + TranslateKeyWordsToOriginalLanguage("English Translation") + "</div><div style=\"padding: 5px\">\n";
            for (int i = 0; i < word.English.size(); i++) {
                bod += Integer.toString(i+1) + ". " + word.English.get(i) + "<br>\n";
            }
            bod+="</div></div>\n";
        }

        if(word.perfimpf != "") {
            bod += "<div class=\"roundy-bx\">\n<div class=\"roundy-bx-title\">" + TranslateKeyWordsToOriginalLanguage(word.Type.contains("imperfective") ? "Perfective form" : "Imperfective form") + "</div><div style=\"padding: 5px\">\n";
            bod += word.perfimpf;
            bod+="</div></div>\n";
        }

        /*if (word.OriginalDefinition != null) {
            bod += "<div class=\"roundy-bx\">\n<div class=\"roundy-bx-title\">" + TranslateKeyWordsToOriginalLanguage("Original Definition") + "</div><div style=\"padding: 5px;\">\n";
            String[] OrDef = word.OriginalDefinition.split(",,");
            for (int i = 0; i < OrDef.length; i++) {
                bod += Integer.toString(i + 1) + ". " + OrDef[i] + "<br>\n";
            }
            bod += "</div></div>\n";
        }*/


        /*if(word.Synonyms!=null){
            bod += "<div class=\"roundy-bx\">\n<div class=\"roundy-bx-title\">" + TranslateKeyWordsToOriginalLanguage("Synonyms") + "</div><div style=\"padding: 5px;\">\n";
            String[] OrDef = word.Synonyms.split(",,");
            for (int i = 0; i < OrDef.length; i++) {
                bod += Integer.toString(i+1) + ". " + OrDef[i] + "<br>\n";
            }
            bod+="</div></div>\n";
        }*/

        if(word.RelatedWords!=null){
            bod += "<div class=\"roundy-bx\">\n<div class=\"roundy-bx-title\">" + TranslateKeyWordsToOriginalLanguage("Related Words")  + "</div><div style=\"padding: 5px;\">\n";
            bod += word.RelatedWords + "\n";
            bod+="</div></div>\n";
        }

        if(word.Examples.size()>0) {
            bod += "<div class=\"roundy-bx\">\n<div class=\"roundy-bx-title\">" + TranslateKeyWordsToOriginalLanguage("Examples") + "</div>";
            if(MA.IsFullModeActivated) {
                bod += "<div style=\"padding: 5px;\">\n";
                for (int i = 0; i < word.Examples.size(); i++) {
                    bod += Integer.toString(i+1) + ". " + word.Examples.get(i) + "<br>\n";
                }
                bod+="</div></div>\n";
            }else {
                bod += "<div class=\"buy-full-version-button\" onclick=\"buyClick()\">GET FULL VERSION</div></div>";
            }
        }

        // ================================================================
        // DRAW TABLES
        // ================================================================
        int tableNumber = 0;
        String language = MA.getResources().getString(R.string.language);
        for(Table tb : word.Tables){
            bod += "<div class=\"tabl-wrd\">" + TranslateKeyWordsToOriginalLanguage(tb.TableName) + "</div>\n";

            if(language.equals("hr-HR")){
                if(word.Type.startsWith("Adjective") || word.Type.startsWith("Noun")){
                    bod += getTabbedTable(tb, tableNumber++, "55%", "left");
                    continue;
                }
            }

            if(language.equals("ru-RU")){
                if(word.Type.startsWith("Adjective") || word.Type.startsWith("Noun")) {
                    bod += getTabbedTable(tb, tableNumber++, "35%", "left");
                    continue;
                }
                if(word.Type.startsWith("Verb")){
                    if(tb.TableName.startsWith("Present and Future") || tb.TableName.startsWith("Participles")){
                        bod += getTabbedTable(tb, tableNumber++, "30%", "left");
                        continue;
                    }
                }
            }


            if(language.equals("de-DE")){
                if(word.Type.startsWith("Adjective")) {
                    bod += getTabbedTable(tb, tableNumber++, "57%", "left");
                    continue;
                }
                if(word.Type.startsWith("Noun")) {
                    bod += getTabbedTable(tb, tableNumber++, "50%", "center");
                    continue;
                }
                if(word.Type.startsWith("Verb")){
                    bod += getGermanVerbTable(tb);
                    continue;
                }
            }


            bod += getClassicTable(tb,"50%", "center");
        }
        // ================================================================
        //
        // ================================================================

        String[] Articles = word.Webpage.split(",,");
        if(Articles.length == 3){
            bod += "<div style=\"text-align: justify;\"><a>Content extracted from the Wiktionary articles </a>";
            bod += "<a href=\"" + Articles[1] + "\">" + Articles[1] + "</a>";
            bod += "<a> and </a><a href=\"" + Articles[2] + "\">" + Articles[2] + "</a>";
            bod += "<a>, which are released under the </a>";
            bod += "<a href\"https://creativecommons.org/licenses/by-sa/3.0/\">Creative Commons Attribution-Share-Alike License 3.0</a></div>";
        } else {
            bod += "<div style=\"text-align: justify;\"><a>Content extracted from the Wiktionary article </a>";
            bod += "<a href=\"" + Articles[1] + "\">" + Articles[1] + "</a>";
            bod += "<a>, which is released under the </a>";
            bod += "<a href\"https://creativecommons.org/licenses/by-sa/3.0/\">Creative Commons Attribution-Share-Alike License 3.0</a></div>";
        }

        MA.wbv.clearCache(true);
        MA.wbv.loadDataWithBaseURL(null,default_html.replace("replace_bod",bod), "text/html", "UTF-8",null);
    }

    public String TranslateKeyWordsToOriginalLanguage(String s) {
        String language = MA.getResources().getString(R.string.language);
        if(!MA.OriginalLanguageEnabled) {
            return s;
        }

        if(language.equals("hr-HR")) {
            s = s.replace("nominative", "nominativ");
            s = s.replace("genitive", "genitiv");
            s = s.replace("dative", "dativ");
            s = s.replace("accusative", "akuzativ");
            s = s.replace("anim.", "živo");
            s = s.replace("in.", "neživo");
            s = s.replace("vocative", "vokativ");
            s = s.replace("locative", "lokativ");
            s = s.replace("instrumental", "instrumental");
            s = s.replace("singular", "jednine");
            s = s.replace("plural", "množine");
            s = s.replace("masculine", "muški rod");
            s = s.replace("feminine", "ženski rod");
            s = s.replace("neuter", "srednji rod");

            s = s.replace("Conjugation of", "Konjugacija");
            s = s.replace("Declension of", "Deklinacija");
            s = s.replace("Conjugation", "Konjugacija");
            s = s.replace("Declension", "Deklinacija");
            s = s.replace("positive indefinite", "pozitiv neodređeni");
            s = s.replace("positive definite", "pozitiv određeni");
            s = s.replace("comparative forms", "komparativ");
            s = s.replace("superlative forms", "superlativ");
            s = s.replace("comparative", "komparativ");
            s = s.replace("superlative", "superlativ");

            s = s.replace("Noun", "Imenica");
            s = s.replace("Verb", "Glagol");
            s = s.replace("perfective", "perfekt");
            s = s.replace("imperfective", "imperfekt");
            s = s.replace("Adjective", "Prilog");
            s = s.replace("Adverb", "Prilog");
            s = s.replace("Abbreviation", "Skraćenica");
            s = s.replace("Pronoun", "Zamjenica");
            s = s.replace("Preposition", "Prijedlog");
            s = s.replace("Numeral", "Broj");
            s = s.replace("Particle", "Riječca");
            s = s.replace("Interjection", "Ubacivanje");
            s = s.replace("Conjunction", "Veznik");

            s = s.replace("English Translation", "Engleski Prijevod");
            s = s.replace("Original Definition", "Definicija");
            s = s.replace("Synonyms", "Sinonimi");
            s = s.replace("Related Words", "Srodne Riječi");
            s = s.replace("Examples", "Primjeri");

            s = s.replace("Present", "Predstaviti");
            s = s.replace("Future", "Budućnost");
            s = s.replace("Past Perfect", "Perfekt");
            s = s.replace("Past Pluperfect", "Pluskvamperfekt");
            s = s.replace("Past participle", "Glagolski prilog");
            s = s.replace("Active past participle", "Aktivni");
            s = s.replace("Passive past participle", "Pasivni");
            s = s.replace("Aorist", "Aorist");
            s = s.replace("Imperfect", "Nesavršen");
            s = s.replace("Conditional", "Uvjetno");
            s = s.replace("Imperative", "Imperativ");

        } else if(language.equals("ru-RU")) {
            s = s.replace("nominative", "назоўный");
            s = s.replace("genitive", "родный");
            s = s.replace("dative", "давальный");
            s = s.replace("accusative", "вінавальный");
            s = s.replace("locative", "месный");
            s = s.replace("instrumental", "творный");
            s = s.replace("prepositional", "предложный");
            s = s.replace("anim.", "одуш.");
            s = s.replace("in.", "неод.");

            s = s.replace("singular", "е. число");
            s = s.replace("plural", "м. число");
            s = s.replace("masculine", "м. род");
            s = s.replace("feminine", "ж. род");
            s = s.replace("neuter", "с. род");
            s = s.replace("(m)", "(м. род)");
            s = s.replace("(f)", "(ж. род)");
            s = s.replace("(n)", "(с. род)");

            s = s.replace("Noun", "Сущ.");
            s = s.replace("Verb", "Глагол");
            s = s.replace("Perfective form", "совершенная форма");
            s = s.replace("Imperfective form", "несовершенная форма");
            s = s.replace("perfective", "сов.");
            s = s.replace("imperfective", "несов.");
            s = s.replace("Adjective", "Прил.");
            s = s.replace("Adverb", "Наречие");
            s = s.replace("Abbreviation", "Аббревиатура");
            s = s.replace("Pronoun", "Mестоимения");
            s = s.replace("Preposition", "Предлог");
            s = s.replace("Numeral", "Числительное");
            s = s.replace("Particle", "Частица");
            s = s.replace("Interjection", "Междометие");
            s = s.replace("Conjunction", "Союз");
            s = s.replace("Determiner", "Мест. прил.");
            s = s.replace("Predicative", "Предикатив");

            s = s.replace("English Translation", "Перевод");
            s = s.replace("Original Definition", "Значение");
            s = s.replace("Synonyms", "Синонимы");
            s = s.replace("Related Words", "Родственные слова");
            s = s.replace("Examples", "Пример");

            s = s.replace("Present and Future", "настоящее и будущее");
            s = s.replace("present tense", "настоящее время");
            s = s.replace("future tense", "будущее время");
            s = s.replace("imperative", "повелительное наклонение");
            s = s.replace("past tense", "прошедшее время");
            s = s.replace("active", "действительный залог");
            s = s.replace("passive", "страдательный залог");
            s = s.replace("adverbial", "деепричастие");

        } else if(language.equals("de-DE")) {
            s = s.replace("nominative", "Nominativ");
            s = s.replace("genitive", "Genitiv");
            s = s.replace("dative", "Dativ");
            s = s.replace("accusative", "Akkusativ");
            s = s.replace("weak decl.", "schwache Fl.");
            s = s.replace("strong decl.", "starke Fl.");
            s = s.replace("mixed decl.", "gemischte Fl.");
            s = s.replace("Declension", "Flexion");
            s = s.replace("Basic forms", "Grundformen");
            s = s.replace("Simple forms", "Einfache Formen");
            s = s.replace("Subordinate-clause forms", "Nebensatz Formen");
            s = s.replace("subordinate-clause", "Nebensatz");
            s = s.replace("Composed forms", "Zusammengesetzte Formen");
            s = s.replace("Declension", "Flexion");

            s = s.replace("zu infinitive", "Infinitivsätze");
            s = s.replace("infinitive", "Infinitiv");
            s = s.replace("participle participle", "Partizip Präsens");
            s = s.replace("past participle ", "Partizip Perfekt");
            s = s.replace("participle", "Partizip");
            s = s.replace("auxiliary", "Hilfsverb");

            s = s.replace("(with definite article", "(mit");

            s = s.replace("Prefesent perfect", "Perfekt");
            s = s.replace("present", "Präsens");
            s = s.replace("Present", "Präsens");
            s = s.replace("past", "Präteritum");
            s = s.replace("Past", "Präteritum");
            s = s.replace("simple", "");
            s = s.replace("Imperative", "Imperativ");
            s = s.replace("pres. perf. and pluperf.", "Perf. und Plusquam.");
            s = s.replace("and", "und");
            s = s.replace("Pluperfect", "Plusquamperfekt");
            s = s.replace("future", "Futur");
            s = s.replace("Future", "Futur");
            s = s.replace("subjunctive", "Konjunjtiv");
            s = s.replace("Subjunctive", "Konjunjtiv");
            s = s.replace("perfect", "Perfekt");

            s = s.replace("English Translation", "Übersetzungen");
            s = s.replace("Original definition", "Bedeutungen");
            s = s.replace("Synonyms", "Synonyme");
            s = s.replace("Related Words", "Wortbildung");
            s = s.replace("Examples", "Beispiel");

            s = s.replace("German personal pronouns", "Personalpronomen ");
            s = s.replace("masc. s.", "Männ. S.");
            s = s.replace("fem. s.", "Fem. S.");
            s = s.replace("neuter s.", "Kas. S.");
            s = s.replace("plural", "Plural");
            s = s.replace("singular", "Singular");
            s = s.replace("(m)", "(M.)");
            s = s.replace("(n)", "(K.)");
            s = s.replace("(f)", "(F.)");
            s = s.replace("First person", "1. Pers.");
            s = s.replace("Second person", "2. Pers.");
            s = s.replace("Third person", "3. Pers");
            s = s.replace("Polite address", "Höflich");
            s = s.replace("possessive", "Possessivepronomen");

            s = s.replace("With possessive", "mit Possessivpronomen");
            s = s.replace("determined", "bestimmt");
            s = s.replace("undetermined", "unbestimmt");
            s = s.replace("Isolated", "isoliert");

            s = s.replace("Abbreviation", "Abkürzung");
            s = s.replace("Adjective", "Adjektiv");
            s = s.replace("Adverb", "Adverb");
            s = s.replace("Interjection", "Zwischenruf");
            s = s.replace("Numeral", "Ziffer");
            s = s.replace("Conjunction", "Verbindung");
            s = s.replace("Particle", "Partikel");
            s = s.replace("Noun", "Substantiv");
            s = s.replace("Pronoun", "Pronomen");
            s = s.replace("Proper noun", "Eigenname");
            s = s.replace("Verb", "Verb");
            s = s.replace("Determiner", "Determiner");
            s = s.replace("Article", "Artikel");
            s = s.replace("Preposition", "Präposition");
            s = s.replace("Postposition", "Präposition");

        }
        return s;
    }

}
