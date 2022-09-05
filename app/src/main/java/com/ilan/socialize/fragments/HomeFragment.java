package com.ilan.socialize.fragments;

import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Shader;
import android.os.Bundle;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.ilan.socialize.adapter.PostAdapter;
import com.ilan.socialize.databinding.FragmentHomeBinding;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.ilan.socialize.R;
import com.ilan.socialize.model.Post;

import java.util.ArrayList;
import java.util.List;

public class HomeFragment extends Fragment {

    private FragmentHomeBinding binding;
    private RecyclerView recyclerViewPosts;
    private PostAdapter postAdapter;
    private List<Post> postList;
    private List<String> followList;    // Users followed by current user, show only posts which were made by the users whom they are following

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_home, container, false);
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        TextView logo = view.findViewById(R.id.socialize_logo);
        setLogoColor(logo);

        // Initialize variables
        recyclerViewPosts = view.findViewById(R.id.recycler_view_posts);
        postList = new ArrayList<>();
        postAdapter = new PostAdapter(getContext(), postList);
        followList = new ArrayList<>();

        // Create recyclerView of posts and set up view for posts
        recyclerViewPosts.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext());
        linearLayoutManager.setStackFromEnd(true);  // Latest post will be available at the top of stack
        linearLayoutManager.setReverseLayout(true);
        recyclerViewPosts.setLayoutManager(linearLayoutManager);
        recyclerViewPosts.setAdapter(postAdapter);

        // Add values to following list containing users followed by current user
        checkFollowingUsers();

    }

    /**
     * Add values to following list containing users followed by current user
     */
    private void checkFollowingUsers() {

        FirebaseDatabase.getInstance().getReference().child("follow")
                .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                .child("following").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                followList.clear();
                snapshot.getChildren().forEach(ss -> {
                    followList.add(ss.getKey());
                });
                followList.add(FirebaseAuth.getInstance().getCurrentUser().getUid());
                // Read posts from user's following list
                readPosts();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }

    /**
     * Read all posts from user's following list
     */
    private void readPosts() {

        FirebaseDatabase.getInstance().getReference().child("posts").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                postList.clear();
                snapshot.getChildren().forEach(ss -> {
                    Post post = ss.getValue(Post.class);

                    if (followList.stream().anyMatch(id -> id.equals(post.getPublisher()))) {
                        postList.add(post);
                    }
                });

                postAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    /**
     * Used to set the logo gradient color
     * @param tv TextView object to colorize
     */
    public void setLogoColor(TextView tv) {
        Shader shader = new LinearGradient(0, 0, 0, tv.getLineHeight(),
                Color.parseColor("#E91E63"), Color.parseColor("#FFC107"),
                Shader.TileMode.REPEAT);

        tv.getPaint().setShader(shader);
    }
}