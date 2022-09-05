package com.ilan.socialize.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.ilan.socialize.PostsActivity;
import com.ilan.socialize.R;
import com.ilan.socialize.fragments.PostDetailFragment;
import com.ilan.socialize.model.Post;
import com.squareup.picasso.Picasso;

import java.util.List;

public class MediaAdapter extends RecyclerView.Adapter<MediaAdapter.ViewHolder> {

    private Context mContext;
    private List<Post> mPosts;

    public MediaAdapter(Context mContext, List<Post> mPosts) {
        this.mContext = mContext;
        this.mPosts = mPosts;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.media_item, parent, false);
        return new MediaAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {

        Post post = mPosts.get(position);
        Picasso.get().load(post.getImageUrl()).placeholder(R.mipmap.ic_launcher).into(holder.media);

        holder.media.setOnClickListener(view -> {
            mContext.getSharedPreferences("PREFS", Context.MODE_PRIVATE).edit()
                    .putString("postId", post.getPostId()).apply();

            ((FragmentActivity)mContext).getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, new PostDetailFragment()).commit();

        });

    }

    @Override
    public int getItemCount() {
        return mPosts.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        public ImageView media;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            media = itemView.findViewById(R.id.media_post);
        }
    }

}
