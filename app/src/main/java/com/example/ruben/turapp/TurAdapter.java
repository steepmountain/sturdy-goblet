package com.example.ruben.turapp;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * Adapter-klasse for å vise Turer i en ListView
 */
public class TurAdapter extends BaseAdapter {

    private static int MAX_BESKRIVELSE = 150;
    private static String ELLIPSE = "...";
    private static int KM = 1000;
    private static int MIL = 10000;

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
            vh.tvDistanse = (TextView) view.findViewById(R.id.tur_item_tv_distanse);

            view.setTag(vh);
        }
        else {
            vh = (ViewHolder) view.getTag();
        }

        // Henter all informasjon fra den gitte turen
        Tur item = mTur.get(i);
        vh.tvNavn.setText(item.getNavn());
        vh.tvType.setText(item.getType());

        // Viser kun 150 første tegn av en beskrivelse
        String beskrivelseTekst = item.getBeskrivelse();
        if (item.getBeskrivelse().length() > MAX_BESKRIVELSE) {
            beskrivelseTekst = beskrivelseTekst.substring(0, MAX_BESKRIVELSE-1) + ELLIPSE;
        }
        vh.tvBeskrivelse.setText(beskrivelseTekst);

        vh.tvDistanse.setText(formaterDistanse(item.getDistanseTil()));

        return view;
    }

    /**
     * Formaterer distanseteksten basert på lengde og lesbarhet
     * @param distanse distansen til turmålet
     * @return en strenge med formatert tekst
     */
    private String formaterDistanse(int distanse) {

        // Hvis distansen unnen 1km, hvis i meter
        if (distanse < KM) {
            return distanse + " meter unna.";
        }

        // Hvis distance under en mil men over 1km, hvis i km
        else if (distanse < MIL) {
            // Gjør om til double med en desimaplass
            double distanseDouble = (double) distanse/KM;
            double avrundetDistanse = (double) Math.round((distanseDouble) * 10) / 10;
            return avrundetDistanse + " km unna.";
        }

        // Hvis over en mil, vis i mil
        else {
            // Gjør om til double med en desimalplass
            double distanseDouble = (double) distanse/MIL;
            double avrundetDistanse = (double) Math.round((distanseDouble) * 10) / 10;
            return avrundetDistanse  + " mil unna";
        }
    }

    protected static class ViewHolder {
        TextView tvNavn;
        TextView tvBeskrivelse;
        TextView tvType;
        TextView tvDistanse;

    }
}
