package com.example.eventjoy.adapters;

import android.content.Context;
import android.util.Log;
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
import com.example.eventjoy.models.Valoration;
import com.example.eventjoy.services.MemberService;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.List;

public class ValorationAdapter extends ArrayAdapter<Valoration> {

    private List<Valoration> valorations;
    private MemberService memberService;
    private Context context;

    //Constructor del adapter
    public ValorationAdapter(Context context, List<Valoration> valorations){
        super(context, 0, valorations);
        this.valorations = valorations;
        this.context = context;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        Valoration valoration = this.valorations.get(position);
        memberService = new MemberService(context);

        if(convertView==null){
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.item_valoration, parent, false);
        }

        TextView tvRating = convertView.findViewById(R.id.tvRating);
        TextView tvTitleValoration = convertView.findViewById(R.id.tvTitleValoration);
        TextView tvValorationDescription = convertView.findViewById(R.id.tvValorationDescription);
        TextView tvUsername = convertView.findViewById(R.id.tvUsername);
        ImageView profileIcon = convertView.findViewById(R.id.profileIcon);

        tvRating.setText("Valoration: " + valoration.getRating());
        tvTitleValoration.setText(valoration.getTitle());
        tvValorationDescription.setText(valoration.getDescription());

        memberService.getMemberById(valoration.getRaterUserId(), new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Member member = dataSnapshot.getChildren().iterator().next().getValue(Member.class);

                if (member.getPhoto() != null && !member.getPhoto().isEmpty()) {
                    Picasso.get().load(member.getPhoto()).placeholder(R.drawable.default_profile_photo).into(profileIcon);
                }
                tvUsername.setText(member.getUsername());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e("Error - ValorationAdapter - getMemberById", databaseError.getMessage());
            }
        });
        return convertView;
    }

}