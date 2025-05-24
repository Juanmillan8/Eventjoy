package com.example.eventjoy.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.eventjoy.R;
import com.example.eventjoy.models.Group;
import com.example.eventjoy.models.Valoration;
import com.squareup.picasso.Picasso;

import java.util.List;

public class ValorationAdapter extends ArrayAdapter<Valoration> {

    private List<Valoration> valorations;

    //Constructor del adapter
    public ValorationAdapter(Context context, List<Valoration> valorations){
        super(context, 0, valorations);
        this.valorations = valorations;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        Valoration valoration = this.valorations.get(position);

        if(convertView==null){
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.item_valoration, parent, false);
        }

        TextView tvTitle = convertView.findViewById(R.id.tvTitle);

        tvTitle.setText(valoration.getTitle());

        return convertView;
    }

}