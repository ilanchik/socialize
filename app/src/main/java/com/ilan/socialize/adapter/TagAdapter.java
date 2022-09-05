package com.ilan.socialize.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.ilan.socialize.R;

import java.util.List;

public class TagAdapter extends RecyclerView.Adapter<TagAdapter.ViewHolder> {

    private Context mContext;
    private List<String> mTags;
    private List<String> mTagsCount;

    public TagAdapter(Context mContext, List<String> mTags, List<String> mTagsCount) {
        this.mContext = mContext;
        this.mTags = mTags;
        this.mTagsCount = mTagsCount;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.tag_item, parent, false);
        return new TagAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {

        holder.hashTag.setText(String.format("#%s", mTags.get(position)));
        holder.numOfPosts.setText(String.format("%s posts", mTagsCount.get(position)));

    }

    @Override
    public int getItemCount() {
        return mTags.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        public TextView hashTag;
        public TextView numOfPosts;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            hashTag = itemView.findViewById(R.id.hashtag);
            numOfPosts = itemView.findViewById(R.id.num_of_posts);
        }
    }

    public void filter(List<String> filterTags, List<String> filterTagCount) {
        this.mTags = filterTags;
        this.mTagsCount = filterTagCount;
        notifyDataSetChanged();
    }

}
