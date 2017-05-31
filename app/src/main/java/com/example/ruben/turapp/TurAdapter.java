package com.example.ruben.turapp;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;


import java.util.ArrayList;

/**
 * Created by Ruben on 29.05.2017.
 */

public class TurAdapter extends BaseAdapter {

    private static int MAX_BESKRIVELSE = 150;
    private static String ELLIPSE = "...";

    private Context mContext;
    private ArrayList<Tur> mTur;
    private LayoutInflater mInflater;

    public TurAdapter(Context context, ArrayList<Tur> tur) {
        mContext = context;
        mTur = tur;
        mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public int getCount() {
        return mTur.size();
    }

    @Override
    public Object getItem(int i) {
        return mTur.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        ViewHolder vh;

        if (view == null) {
            view = mInflater.inflate(R.layout.tur_item, null);
            vh = new ViewHolder();
            vh.tvNavn = (TextView) view.findViewById(R.id.tur_item_tv_navn);
            vh.tvBeskrivelse = (TextView) view.findViewById(R.id.tur_item_tv_beskrivelse);
            vh.tvType = (TextView) view.findViewById(R.id.tur_item_tv_type);
            vh.tvLatitude = (TextView) view.findViewById(R.id.tur_item_tv_latitude);
            vh.tvLongitude = (TextView) view.findViewById(R.id.tur_item_tv_longitude);
            vh.tvMoh = (TextView) view.findViewById(R.id.tur_item_tv_moh);

            view.setTag(vh);
        }
        else {
            vh = (ViewHolder) view.getTag();
        }

        // Henter current Tur
        Tur item = mTur.get(i);
        vh.tvNavn.setText(item.getNavn());

        // Viser kun 150 første tegn av en beskrivelse
        String txtBeskrivelse = item.getBeskrivelse();
        if (item.getBeskrivelse().length() > MAX_BESKRIVELSE) {
            txtBeskrivelse = txtBeskrivelse.substring(0, MAX_BESKRIVELSE-1) + ELLIPSE;
        }
        vh.tvBeskrivelse.setText(txtBeskrivelse);

        vh.tvType.setText(item.getType());
        vh.tvLatitude.setText(item.getLatitude() + "");
        vh.tvLongitude.setText(item.getLongitude() + "");
        vh.tvMoh.setText(item.getMoh() + "");

        return view;
    }

    protected static class ViewHolder {
        TextView tvNavn;
        TextView tvBeskrivelse;
        TextView tvType;
        TextView tvLatitude;
        TextView tvLongitude;
        TextView tvMoh;

    }
}
