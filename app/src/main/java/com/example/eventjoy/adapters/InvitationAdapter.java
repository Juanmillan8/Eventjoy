package com.example.eventjoy.adapters;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.eventjoy.R;
import com.example.eventjoy.models.Group;
import com.example.eventjoy.models.Invitation;
import com.example.eventjoy.models.Member;
import com.example.eventjoy.models.Valoration;
import com.example.eventjoy.services.GroupService;
import com.example.eventjoy.services.MemberService;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.List;

public class InvitationAdapter extends ArrayAdapter<Invitation> {

    private List<Invitation> invitations;
    private GroupService groupService;
    private MemberService memberService;
    private Context context;

    //Constructor del adapter
    public InvitationAdapter(Context context, List<Invitation> invitations){
        super(context, 0, invitations);
        this.invitations = invitations;
        this.context = context;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        Invitation invitation = this.invitations.get(position);
        groupService = new GroupService(context);
        memberService = new MemberService(context);
        if(convertView==null){
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.item_invitation, parent, false);
        }

        TextView tvTitle = convertView.findViewById(R.id.tvGroupTitle);
        TextView tvDescriptionGroup = convertView.findViewById(R.id.tvDescriptionGroup);
        TextView tvInviterUsername = convertView.findViewById(R.id.tvInviterUsername);
        ImageView groupIcon = convertView.findViewById(R.id.groupIcon);

        groupService.getGroupById(invitation.getGroupId(), new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Group group = dataSnapshot.getChildren().iterator().next().getValue(Group.class);
                tvTitle.setText(group.getTitle());

                if (group.getDescription() != null && !group.getDescription().toString().isBlank()) {
                    tvDescriptionGroup.setText(group.getDescription());
                } else {
                    tvDescriptionGroup.setText("This group does not yet have a description");
                }

                if (group.getIcon() != null && !group.getIcon().isEmpty()) {
                    Picasso.get().load(group.getIcon()).placeholder(R.drawable.default_profile_photo).into(groupIcon);
                }

                memberService.getMemberById(invitation.getInviterUserId(), new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        Member member = snapshot.getChildren().iterator().next().getValue(Member.class);

                        tvInviterUsername.setText("Invited by: " + member.getUsername());
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Log.e("Error - InvitationAdapter - getMemberById", error.getMessage());
                    }
                });

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e("Error - InvitationAdapter - getGroupById", databaseError.getMessage());
            }
        });
        return convertView;
    }

}
