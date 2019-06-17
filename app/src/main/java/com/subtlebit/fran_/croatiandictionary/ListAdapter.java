package com.subtlebit.fran_.croatiandictionary;

import android.content.Context;
import android.graphics.Color;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

public class ListAdapter extends ArrayAdapter<Word> {

    private Context context;
    private List<Word> WordList =new ArrayList<>();
    private boolean DarkModeEnabled;

    public ListAdapter(Context ctx, List<Word> wrdlst, boolean IsDarkModeEnabled){
        super(ctx,0,wrdlst);
        WordList = wrdlst;
        context = ctx;
        DarkModeEnabled = IsDarkModeEnabled;

    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent){
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View list_item = inflater.inflate(R.layout.list_item, parent, false);
        TextView title_text = (TextView) list_item.findViewById(R.id.title_text);
        TextView content_text = (TextView) list_item.findViewById(R.id.content_text);
        LinearLayout linear_layout = (LinearLayout) list_item.findViewById(R.id.LLyout);
        ImageView star_icon = (ImageView) list_item.findViewById(R.id.staricon);

        if(WordList.get(position) == null)
            return list_item;

        String title = WordList.get(position).Name;
        String content = WordList.get(position).English.get(0);
        if(WordList.get(position).English.size()>1){
            content += ", " + WordList.get(position).English.get(1);
        }

        title_text.setText(title);
        content_text.setText(content);

        if(WordList.get(position).isFavorite)
            star_icon.setVisibility(View.VISIBLE);

        if(DarkModeEnabled){
            title_text.setTextColor(Color.parseColor("#fafafa"));
            content_text.setTextColor(Color.parseColor("#fafafa"));
            linear_layout.setBackgroundColor(Color.parseColor("#3c3c3c"));
            star_icon.setImageResource(R.drawable.ic_star_cccccc);
        } else {
            title_text.setTextColor(Color.parseColor("#000000"));
            content_text.setTextColor(Color.parseColor("#000000"));
            linear_layout.setBackgroundColor(Color.parseColor("#fafafa"));
            star_icon.setImageResource(R.drawable.ic_star_606060);
        }

        return list_item;
    }
}
