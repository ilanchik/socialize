package com.ilan.socialize.adapter;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.hendraanggrian.appcompat.widget.SocialTextView;
import com.ilan.socialize.CommentActivity;
import com.ilan.socialize.FollowersActivity;
import com.ilan.socialize.R;
import com.ilan.socialize.fragments.PostDetailFragment;
import com.ilan.socialize.fragments.ProfileFragment;
import com.ilan.socialize.model.Post;
import com.ilan.socialize.model.User;
import com.squareup.picasso.Picasso;

import java.util.HashMap;
import java.util.List;

public class PostAdapter extends RecyclerView.Adapter<PostAdapter.ViewHolder> {

    private Context mContext;
    private List<Post> mPosts;

    private FirebaseUser firebaseUser;

    public PostAdapter(Context mContext, List<Post> mPosts) {
        this.mContext = mContext;
        this.mPosts = mPosts;
        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.post_item, parent, false);
        return new PostAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {

        Post post = mPosts.get(position);
        Picasso.get().load(post.getImageUrl()).into(holder.postedImage);
        holder.description.setText(post.getDescription());

        FirebaseDatabase.getInstance().getReference().child("Users").child(post.getPublisher())
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        User user = snapshot.getValue(User.class);

                        if (user.getImageurl().equals("default")) {
                            holder.profileImage.setImageResource(R.mipmap.ic_launcher);
                        } else {
                            Picasso.get().load(user.getImageurl()).into(holder.profileImage);
                        }

                        holder.username.setText(user.getUsername());
                        holder.author.setText(user.getName());
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });

        isLiked(post.getPostId(), holder.like);
        numberOfLikes(post.getPostId(), holder.numberLikes);
        getComments(post.getPostId(), holder.numberComments);
        isSaved(post.getPostId(), holder.save);

        holder.like.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (holder.like.getTag().equals("like")) {
                    FirebaseDatabase.getInstance().getReference()
                            .child("likes").child(post.getPostId()).child(firebaseUser.getUid())
                            .setValue(true);

                    addNotification(post.getPostId(), post.getPublisher());
                } else {
                    FirebaseDatabase.getInstance().getReference()
                            .child("likes").child(post.getPostId()).child(firebaseUser.getUid())
                            .removeValue();
                }
            }
        });

        holder.comment.setOnClickListener(view -> {
            Intent intent = new Intent(mContext, CommentActivity.class);
            intent.putExtra("postId", post.getPostId());
            intent.putExtra("authorId", post.getPublisher());
            mContext.startActivity(intent);
        });

        holder.numberComments.setOnClickListener(view -> {
            Intent intent = new Intent(mContext, CommentActivity.class);
            intent.putExtra("postId", post.getPostId());
            intent.putExtra("authorId", post.getPublisher());
            mContext.startActivity(intent);
        });

        holder.save.setOnClickListener(view -> {
            if (holder.save.getTag().equals("save")) {
                FirebaseDatabase.getInstance().getReference().child("saves").child(firebaseUser.getUid())
                        .child(post.getPostId()).setValue(true);
            } else {
                FirebaseDatabase.getInstance().getReference().child("saves").child(firebaseUser.getUid())
                        .child(post.getPostId()).removeValue();
            }
        });

        holder.profileImage.setOnClickListener(view -> {
            mContext.getSharedPreferences("PROFILE", Context.MODE_PRIVATE).edit()
                    .putString("profileId", post.getPublisher()).apply();
            Log.i("TAG", post.getPublisher());

            ((FragmentActivity)mContext).getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, new ProfileFragment()).commit();
        });

        holder.username.setOnClickListener(view -> {
            mContext.getSharedPreferences("PROFILE", Context.MODE_PRIVATE).edit()
                    .putString("profileId", post.getPublisher()).apply();

            ((FragmentActivity)mContext).getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, new ProfileFragment()).commit();
        });

        holder.author.setOnClickListener(view -> {
            mContext.getSharedPreferences("PROFILE", Context.MODE_PRIVATE).edit()
                    .putString("profileId", post.getPublisher()).apply();

            ((FragmentActivity)mContext).getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, new ProfileFragment()).commit();
        });

        holder.postedImage.setOnClickListener(view -> {
            mContext.getSharedPreferences("PREFS", Context.MODE_PRIVATE).edit()
                    .putString("postId", post.getPostId()).apply();

            ((FragmentActivity)mContext).getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, new PostDetailFragment()).commit();

        });

        holder.numberLikes.setOnClickListener(view -> {
            Intent intent = new Intent(mContext, FollowersActivity.class);
            intent.putExtra("id", post.getPublisher());
            intent.putExtra("title", "likes");
            mContext.startActivity(intent);
        });
    }

    private void isSaved(String postId, ImageView image) {

        FirebaseDatabase.getInstance().getReference().child("saves")
                .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.child(postId).exists()) {
                            image.setImageResource(R.drawable.ic_saved);
                            image.setTag("saved");
                        } else {
                            image.setImageResource(R.drawable.ic_save);
                            image.setTag("save");
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });

    }

    @Override
    public int getItemCount() {
        return mPosts.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        public ImageView profileImage;
        public ImageView postedImage;
        public ImageView like, comment, save, more;

        public TextView username, author;
        public TextView numberLikes, numberComments;
        SocialTextView description;


        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            profileImage = itemView.findViewById(R.id.profile_image);
            postedImage = itemView.findViewById(R.id.posted_image);
            like = itemView.findViewById(R.id.img_like);
            comment = itemView.findViewById(R.id.img_comment);
            save = itemView.findViewById(R.id.img_save);
            more = itemView.findViewById(R.id.img_more);

            username = itemView.findViewById(R.id.txt_username);
            author = itemView.findViewById(R.id.txt_author);
            numberLikes = itemView.findViewById(R.id.txt_likes);
            numberComments = itemView.findViewById(R.id.txt_number_comments);
            description = itemView.findViewById(R.id.txt_description);
        }
    }

    /**
     * Display if user liked an image, or if user is able to like an image
     * @param postId postId to reference
     * @param imageView imageView object
     */
    private void isLiked(String postId, ImageView imageView) {

        FirebaseDatabase.getInstance().getReference().child("likes").child(postId)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.child(firebaseUser.getUid()).exists()) {
                            imageView.setImageResource(R.drawable.ic_liked);
                            imageView.setTag("liked");
                        } else {
                            imageView.setImageResource(R.drawable.ic_like);
                            imageView.setTag("like");
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

    /**
     * Displays the number of likes per post
     * @param postId postId to reference
     * @param textView textView object
     */
    private void numberOfLikes(String postId, TextView textView) {
        FirebaseDatabase.getInstance().getReference().child("likes").child(postId)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        textView.setText(String.format("%s likes", snapshot.getChildrenCount()));
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

    /**
     * Gets the number of comments and displays them in the textView
     * @param postId postId related to post
     * @param text textView object
     */
    private void getComments(String postId, TextView text) {
        FirebaseDatabase.getInstance().getReference().child("comments").child(postId)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        text.setText(String.format("View All %s Comments", snapshot.getChildrenCount()));
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

    private void addNotification(String postId, String publisherId) {

        HashMap<String, Object> map = new HashMap<>();
        map.put("userId", publisherId);
        map.put("text", "Liked your post");
        map.put("postId", postId);
        map.put("isPost", true);

        // push() to create new branch
        FirebaseDatabase.getInstance().getReference().child("notifications").child(firebaseUser.getUid())
                .push().setValue(map);

    }

}
