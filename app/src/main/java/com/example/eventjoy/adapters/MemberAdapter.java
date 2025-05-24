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

public class MemberAdapter extends ArrayAdapter<Member> {

    private List<Member> members;
    private List<Member> originalList;

    //Constructor del adapter
    public MemberAdapter(Context context, List<Member> members){
        super(context, 0, members);
        this.members = members;
        originalList = new ArrayList<>();
        originalList.addAll(members);
    }

    public void filter(final String txtSearch) {
        int length = txtSearch.length();
        if (length == 0) {
            members.clear();
            members.addAll(originalList);
        } else {
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
                List<Member> collecion = members.stream()
                        .filter(i -> i.getUsername().toLowerCase().contains(txtSearch.toLowerCase()))
                        .collect(Collectors.toList());
                members.clear();
                members.addAll(collecion);
            } else {
                for (Member m : originalList) {
                    if (m.getUsername().toLowerCase().contains(txtSearch.toLowerCase())) {
                        members.add(m);
                    }
                }
            }
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        Member member = this.members.get(position);

        if(convertView==null){
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.item_member, parent, false);
        }

        TextView tvUsername = convertView.findViewById(R.id.tvUsername);
        tvUsername.setText(member.getUsername());

        return convertView;
    }

}