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
import com.example.eventjoy.models.Member;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class GroupAdapter extends ArrayAdapter<Group> {

    private List<Group> groups;
    private List<Group> originalList;

    //Constructor del adapter
    public GroupAdapter(Context context, List<Group> groups){
        super(context, 0, groups);
        this.groups = groups;
        originalList = new ArrayList<>();
        originalList.addAll(groups);
    }

    public void filter(final String txtSearch) {
        int length = txtSearch.length();
        if (length == 0) {
            groups.clear();
            groups.addAll(originalList);
        } else {
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
                List<Group> collecion = groups.stream()
                        .filter(i -> i.getTitle().toLowerCase().contains(txtSearch.toLowerCase()))
                        .collect(Collectors.toList());
                groups.clear();
                groups.addAll(collecion);
            } else {
                for (Group g : originalList) {
                    if (g.getTitle().toLowerCase().contains(txtSearch.toLowerCase())) {
                        groups.add(g);
                    }
                }
            }
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        Group group = this.groups.get(position);

        if(convertView==null){
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.item_group, parent, false);
        }

        TextView tvGroupTitle = convertView.findViewById(R.id.tvTitle);
        TextView tvDescription = convertView.findViewById(R.id.tvDescription);
        ImageView groupIcon = convertView.findViewById(R.id.groupIcon);

        tvGroupTitle.setText(group.getTitle());
        tvDescription.setText(group.getDescription());

        if (group.getIcon() != null && !group.getIcon().isEmpty()) {
            Picasso.get()
                    .load(group.getIcon())
                    .placeholder(R.drawable.default_profile_photo)
                    .into(groupIcon);
        }

        return convertView;
    }

}