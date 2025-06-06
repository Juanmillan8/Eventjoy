package com.example.eventjoy.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.eventjoy.R;
import com.example.eventjoy.listeners.OnMemberClickListener;
import com.example.eventjoy.models.Member;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class MemberAdapter extends RecyclerView.Adapter<MemberAdapter.MemberViewHolder> {

    private final Context context;
    private List<Member> members;
    private final List<Member> originalList;
    private OnMemberClickListener listener;

    public MemberAdapter(Context context, List<Member> members) {
        this.context = context;
        this.members = new ArrayList<>(members);
        this.originalList = new ArrayList<>(members);
    }

    public void setOnMemberClickListener(OnMemberClickListener listener) {
        this.listener = listener;
    }

    public void filter(String txtSearch) {
        if (txtSearch == null || txtSearch.trim().isEmpty()) {
            members = new ArrayList<>(originalList);
        } else {
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
                members = originalList.stream()
                        .filter(m -> m.getUsername().toLowerCase().contains(txtSearch.toLowerCase()))
                        .collect(Collectors.toList());
            } else {
                List<Member> filteredList = new ArrayList<>();
                for (Member m : originalList) {
                    if (m.getUsername().toLowerCase().contains(txtSearch.toLowerCase())) {
                        filteredList.add(m);
                    }
                }
                members = filteredList;
            }
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public MemberViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_member, parent, false);
        return new MemberViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MemberViewHolder holder, int position) {
        Member member = members.get(position);
        holder.tvUsername.setText(member.getUsername());

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onMemberClick(member);
            }
        });
    }

    @Override
    public int getItemCount() {
        return members.size();
    }

    public static class MemberViewHolder extends RecyclerView.ViewHolder {
        TextView tvUsername;

        public MemberViewHolder(@NonNull View itemView) {
            super(itemView);
            tvUsername = itemView.findViewById(R.id.tvUsername);
        }
    }
}
