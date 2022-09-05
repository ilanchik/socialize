package com.ilan.socialize.fragments;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.provider.ContactsContract;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.ilan.socialize.EditProfileActivity;
import com.ilan.socialize.FollowersActivity;
import com.ilan.socialize.OptionsActivity;
import com.ilan.socialize.R;
import com.ilan.socialize.adapter.MediaAdapter;
import com.ilan.socialize.adapter.PostAdapter;
import com.ilan.socialize.model.Post;
import com.ilan.socialize.model.User;
import com.squareup.picasso.Picasso;

import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class ProfileFragment extends Fragment {

    private RecyclerView recyclerViewSaves;
    private MediaAdapter postAdapterSaves;
    private List<Post> mySavedPosts;

    private RecyclerView recyclerView;
    private MediaAdapter mediaAdapter;
    private List<Post> myMediaList;

    private CircleImageView profileImage;
    private ImageView options;
    private TextView posts;
    private TextView followers, following;
    private TextView fullName, bio, username;

    private ImageButton myPosts;
    private ImageButton savedPosts;
    private Button editProfile;

    private FirebaseUser firebaseUser;
    private String profileId;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_profile, container, false);
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();

        String data = getContext().getSharedPreferences("PROFILE", Context.MODE_PRIVATE)
                .getString("profileId", "none");

        if (data.equals("none")) {
            profileId = firebaseUser.getUid();
        } else {
            profileId = data;
            getContext().getSharedPreferences("PROFILE", Context.MODE_PRIVATE).edit().clear().apply();
        }

        profileImage = view.findViewById(R.id.profile_image);
        options = view.findViewById(R.id.options);
        posts = view.findViewById(R.id.posts);
        followers = view.findViewById(R.id.followers);
        following = view.findViewById(R.id.following);
        fullName = view.findViewById(R.id.fullname);
        bio = view.findViewById(R.id.bio);
        username = view.findViewById(R.id.username);
        myPosts = view.findViewById(R.id.my_posts);
        savedPosts = view.findViewById(R.id.saved_posts);
        editProfile = view.findViewById(R.id.edit_profile);

        // Create the recyclerView of media items (user's posts)
        recyclerView = view.findViewById(R.id.recycler_view_profile);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new GridLayoutManager(getContext(), 3));
        myMediaList = new ArrayList<>();
        mediaAdapter = new MediaAdapter(getContext(), myMediaList);
        recyclerView.setAdapter(mediaAdapter);

        // Create recyclerView of saved posts
        recyclerViewSaves = view.findViewById(R.id.recycler_view_saved_posts);
        recyclerViewSaves.setHasFixedSize(true);
        recyclerViewSaves.setLayoutManager(new GridLayoutManager(getContext(), 3));
        mySavedPosts = new ArrayList<>();
        postAdapterSaves = new MediaAdapter(getContext(), mySavedPosts);
        recyclerViewSaves.setAdapter(postAdapterSaves);
        
        // Get user info
        userInformation();
        getFollowersCount();
        getFollowingCount();
        getPostCount();
        myMedia();
        getSavedPosts();

        // If user is on their own profile, allow for user to edit their profile
        // else if not on user's profile, display follow or unfollow based on current following status
        if (profileId.equals(firebaseUser.getUid())) {
            editProfile.setText("Edit Profile");
        } else {
            checkFollowingStatus();
        }

        editProfile.setOnClickListener(v -> {
            String btnText = editProfile.getText().toString();

            if (btnText.equals("Edit Profile")) {
                startActivity(new Intent(getContext(), EditProfileActivity.class));
            } else {
                if (btnText.equals("Follow")) {
                    FirebaseDatabase.getInstance().getReference().child("follow").child(firebaseUser.getUid())
                            .child("following").child(profileId).setValue(true);

                    FirebaseDatabase.getInstance().getReference().child("follow").child(profileId)
                            .child("followers").child(firebaseUser.getUid()).setValue(true);
                } else {
                    FirebaseDatabase.getInstance().getReference().child("follow").child(firebaseUser.getUid())
                            .child("following").child(profileId).removeValue();

                    FirebaseDatabase.getInstance().getReference().child("follow").child(profileId)
                            .child("followers").child(firebaseUser.getUid()).removeValue();
                }
            }
        });

        recyclerView.setVisibility(View.VISIBLE);
        recyclerViewSaves.setVisibility(View.GONE);

        myPosts.setOnClickListener(view1 -> {
            recyclerView.setVisibility(View.VISIBLE);
            recyclerViewSaves.setVisibility(View.GONE);
        });

        savedPosts.setOnClickListener(view1 -> {
            recyclerView.setVisibility(View.GONE);
            recyclerViewSaves.setVisibility(View.VISIBLE);
        });

        followers.setOnClickListener(view1 -> {
            Intent intent = new Intent(getContext(), FollowersActivity.class);
            intent.putExtra("id", profileId);
            intent.putExtra("title", "followers");
            startActivity(intent);
        });

        following.setOnClickListener(view1 -> {
            Intent intent = new Intent(getContext(), FollowersActivity.class);
            intent.putExtra("id", profileId);
            intent.putExtra("title", "following");
            startActivity(intent);
        });

        options.setOnClickListener(view1 -> {
            startActivity(new Intent(getContext(), OptionsActivity.class));
        });

    }

    private void getSavedPosts() {

        List<String> savedId = new ArrayList<>();

        FirebaseDatabase.getInstance().getReference().child("saves").child(firebaseUser.getUid())
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        for (DataSnapshot ss : snapshot.getChildren()) {
                            savedId.add(ss.getKey());
                        }

                        FirebaseDatabase.getInstance().getReference().child("posts").addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot2) {
                                mySavedPosts.clear();

                                for (DataSnapshot ss2 : snapshot2.getChildren()) {
                                    Post post = ss2.getValue(Post.class);

                                    for (String id : savedId) {
                                        if (post.getPostId().equals(id)) {
                                            mySavedPosts.add(post);
                                        }
                                    }
                                }

                                postAdapterSaves.notifyDataSetChanged();
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {

                            }
                        });
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });

    }

    private void myMedia() {

        FirebaseDatabase.getInstance().getReference().child("posts").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                myMediaList.clear();

                for (DataSnapshot ss : snapshot.getChildren()) {
                    Post post = ss.getValue(Post.class);

                    // If post was made by current user, add to the list
                    if (post.getPublisher().equals(profileId)) {
                        myMediaList.add(post);
                    }
                }

                Collections.reverse(myMediaList);
                mediaAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }

    private void checkFollowingStatus() {

        FirebaseDatabase.getInstance().getReference().child("follow").child(firebaseUser.getUid())
                .child("following").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.child(profileId).exists()) {
                    editProfile.setText("Following");
                } else {
                    editProfile.setText("Follow");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }

    private void getPostCount() {

        FirebaseDatabase.getInstance().getReference().child("posts").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                int count = 0;

                for (DataSnapshot ss : snapshot.getChildren()) {
                    Post post = ss.getValue(Post.class);

                    if (post.getPublisher().equals(profileId)) count++;
                }
                posts.setText(String.valueOf(count));
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }

    private void getFollowingCount() {

        FirebaseDatabase.getInstance().getReference().child("follow").child(profileId)
                .child("following").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                following.setText(String.valueOf(snapshot.getChildrenCount()));
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }

    private void getFollowersCount() {

        FirebaseDatabase.getInstance().getReference().child("follow").child(profileId)
                .child("followers").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                followers.setText(String.valueOf(snapshot.getChildrenCount()));
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }

    private void userInformation() {

        FirebaseDatabase.getInstance().getReference().child("Users").child(profileId)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        User user = snapshot.getValue(User.class);

                        Picasso.get().load(user.getImageurl()).into(profileImage);
                        username.setText(user.getUsername());
                        fullName.setText(user.getName());
                        bio.setText(user.getBio());
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });

    }

}