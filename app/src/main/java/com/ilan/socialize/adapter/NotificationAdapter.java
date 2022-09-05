package com.ilan.socialize.adapter;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.ilan.socialize.R;
import com.ilan.socialize.fragments.PostDetailFragment;
import com.ilan.socialize.fragments.ProfileFragment;
import com.ilan.socialize.model.Notification;
import com.ilan.socialize.model.Post;
import com.ilan.socialize.model.User;
import com.squareup.picasso.Picasso;

import java.util.List;

public class NotificationAdapter extends RecyclerView.Adapter<NotificationAdapter.ViewHolder> {

    private Context mContext;
    private List<Notification> notifications;

    public NotificationAdapter(Context mContext, List<Notification> notifications) {
        this.mContext = mContext;
        this.notifications = notifications;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.notification_item, parent, false);
        return new NotificationAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {

        final Notification notification = notifications.get(position);

        getUser(holder.profileImage, holder.username, notification.getUserId());
        holder.comment.setText(notification.getText());

        if (notification.isPost()) {
            holder.postedImage.setVisibility(View.VISIBLE);
            getPostedImage(holder.postedImage, notification.getPostId());
        } else {
            holder.postedImage.setVisibility(View.GONE);
        }

        holder.itemView.setOnClickListener(view -> {
            if (notification.isPost()) {
                mContext.getSharedPreferences("PREFS", Context.MODE_PRIVATE).edit()
                        .putString("postId", notification.getPostId()).apply();

                // Call post detail fragment
                ((FragmentActivity)mContext).getSupportFragmentManager().beginTransaction()
                        .replace(R.id.fragment_container, new PostDetailFragment()).commit();
            } else {
                mContext.getSharedPreferences("PROFILE", Context.MODE_PRIVATE).edit()
                        .putString("profileId", notification.getUserId()).apply();

                // Call profile fragment
                ((FragmentActivity)mContext).getSupportFragmentManager().beginTransaction()
                        .replace(R.id.fragment_container, new ProfileFragment()).commit();
            }
        });

    }

    @Override
    public int getItemCount() {
        return notifications.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        public TextView username, comment;
        public ImageView profileImage, postedImage;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            username = itemView.findViewById(R.id.username);
            comment = itemView.findViewById(R.id.comment);
            profileImage = itemView.findViewById(R.id.profile_image);
            postedImage = itemView.findViewById(R.id.posted_image);
        }
    }

    private void getUser(ImageView imageView, TextView textView, String userId) {

        FirebaseDatabase.getInstance().getReference().child("Users").child(userId)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        User user = snapshot.getValue(User.class);

                        if (user.getImageurl().equals("default")) {
                            imageView.setImageResource(R.mipmap.ic_launcher);
                        } else {
                            Picasso.get().load(user.getImageurl()).into(imageView);
                        }

                        textView.setText(user.getUsername());
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

    private void getPostedImage(ImageView imageView, String postId) {

        FirebaseDatabase.getInstance().getReference().child("posts").child(postId)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        Post post = snapshot.getValue(Post.class);
                        Picasso.get().load(post.getImageUrl()).placeholder(R.mipmap.ic_launcher).into(imageView);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });

    }

}
