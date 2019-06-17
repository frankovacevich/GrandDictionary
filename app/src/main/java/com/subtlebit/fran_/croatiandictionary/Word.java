package com.subtlebit.fran_.croatiandictionary;

import java.util.ArrayList;
import java.util.List;

public class Word {
    public int ID;
    public String Name;
    public String Type;
    public String perfimpf = ""; //If type is verb, then give the alternative perfective or imperfective form
    public String Webpage;
    public String OriginalDefinition;
    public String Synonyms;
    public String RelatedWords;
    public List<String> English = new ArrayList<>();
    public List<String> Examples = new ArrayList<>();
    public List<Table>  Tables = new ArrayList<>();

    public int SearchPriority = 0;
    public List<String> SearchTerms = new ArrayList<>();
    public boolean isFavorite = false;

    public String WordTostring(){
        String result = ">";
        result += Name + "\n>";

        for(String en : English)
            result += en + "\n>";

        for(Table tb : Tables)
            result += tb.Content + "\n>";

        return result;
    }

}

class Table{
    public String TableName;
    public String Content;
}