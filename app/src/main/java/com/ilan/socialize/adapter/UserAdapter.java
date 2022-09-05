package com.ilan.socialize.adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.ilan.socialize.MainActivity;
import com.ilan.socialize.R;
import com.ilan.socialize.fragments.ProfileFragment;
import com.ilan.socialize.model.User;
import com.squareup.picasso.Picasso;

import java.net.Inet4Address;
import java.util.HashMap;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class UserAdapter extends RecyclerView.Adapter<UserAdapter.ViewHolder> {

    private Context mContext;
    private List<User> mUsers;
    private boolean isFragment; // Will use this adapter for fragment and activity, slight code change for each

    private FirebaseUser firebaseUser;

    public UserAdapter(Context mContext, List<User> mUsers, boolean isFragment) {
        this.mContext = mContext;
        this.mUsers = mUsers;
        this.isFragment = isFragment;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.user_item, parent, false);
        return new UserAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {

        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();

        User user = mUsers.get(position);
        holder.btnFollow.setVisibility(View.VISIBLE);

        holder.username.setText(user.getUsername());
        holder.name.setText(user.getName());

        // Load the image
        Picasso.get().load(user.getImageurl()).placeholder(R.mipmap.ic_launcher)
                .into(holder.profileImage);

        isFollowed(user.getId(), holder.btnFollow);

        if (user.getId().equals(firebaseUser.getUid())) {
            holder.btnFollow.setVisibility(View.GONE);
        }

        // If user is not following another, add action to enable follow of user
        holder.btnFollow.setOnClickListener(view -> {
            if (holder.btnFollow.getText().toString().equals("Follow")) {

                // Create entry for current user's following list
                FirebaseDatabase.getInstance().getReference().child("follow")
                        .child(firebaseUser.getUid()).child("following")
                        .child(user.getId()).setValue(true);

                // Create entry for user's followers list
                FirebaseDatabase.getInstance().getReference().child("follow")
                        .child(user.getId()).child("followers")
                        .child(firebaseUser.getUid()).setValue(true);

                addNotification(user.getId());
            } else {

                // Unfollow if already following
                FirebaseDatabase.getInstance().getReference().child("follow")
                        .child(firebaseUser.getUid()).child("following")
                        .child(user.getId()).removeValue();

                FirebaseDatabase.getInstance().getReference().child("follow")
                        .child(user.getId()).child("followers")
                        .child(firebaseUser.getUid()).removeValue();
            }
        });

        // If user clicked from fragment, redirect to profile fragment, or from activity to main activity
        holder.itemView.setOnClickListener(view -> {
            if (isFragment) {
                mContext.getSharedPreferences("PROFILE", Context.MODE_PRIVATE).edit()
                        .putString("profileId", user.getId()).apply();

                ((FragmentActivity)mContext).getSupportFragmentManager().beginTransaction()
                        .replace(R.id.fragment_container, new ProfileFragment()).commit();
            } else {
                Intent intent = new Intent(mContext, MainActivity.class);
                intent.putExtra("publisherId", user.getId());
                mContext.startActivity(intent);
            }
        });

    }

    private void addNotification(String userId) {

        HashMap<String, Object> map = new HashMap<>();
        map.put("userId", userId);
        map.put("text", "Started following you");
        map.put("postId", "");
        map.put("isPost", false);

        // push() to create new branch
        FirebaseDatabase.getInstance().getReference().child("notifications").child(firebaseUser.getUid())
                .push().setValue(map);

    }

    private void isFollowed(String id, Button btnFollow) {

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference()
                .child("follow").child(firebaseUser.getUid()).child("following");

        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.child(id).exists()) {
                    btnFollow.setText("Following");
                } else {
                    btnFollow.setText("Follow");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }

    @Override
    public int getItemCount() {
        return mUsers.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        public CircleImageView profileImage;
        public TextView username;
        public TextView name;
        public Button btnFollow;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            profileImage = itemView.findViewById(R.id.profile_image);
            username = itemView.findViewById(R.id.username);
            name = itemView.findViewById(R.id.fullname);
            btnFollow = itemView.findViewById(R.id.btn_follow);
        }
    }

}
