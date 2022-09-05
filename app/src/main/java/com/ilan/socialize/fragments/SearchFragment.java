package com.ilan.socialize.fragments;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.provider.ContactsContract;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.hendraanggrian.appcompat.widget.SocialAutoCompleteTextView;
import com.ilan.socialize.R;
import com.ilan.socialize.adapter.TagAdapter;
import com.ilan.socialize.adapter.UserAdapter;
import com.ilan.socialize.databinding.FragmentSearchBinding;
import com.ilan.socialize.model.User;

import java.util.ArrayList;
import java.util.List;

public class SearchFragment extends Fragment {

    private FragmentSearchBinding binding;

    private RecyclerView recyclerView;      // For User list
    private RecyclerView recyclerViewTags;  // For hashtags

    private List<User> mUsers;
    private List<String> mHashTags;
    private List<String> mHashTagCount;

    private UserAdapter userAdapter;
    private TagAdapter tagAdapter;

    private SocialAutoCompleteTextView searchBar;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_search, container, false);

        // RecyclerView to list Users
        recyclerView = view.findViewById(R.id.recycler_view_users);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        // RecyclerView to list Hashtags and tag count
        recyclerViewTags = view.findViewById(R.id.recycler_view_tags);
        recyclerViewTags.setHasFixedSize(true);
        recyclerViewTags.setLayoutManager(new LinearLayoutManager(getContext()));

        // Set up Lists for users and tags
        mUsers = new ArrayList<>();
        mHashTags = new ArrayList<>();
        mHashTagCount = new ArrayList<>();

        // Bind adapters
        userAdapter = new UserAdapter(getContext(), mUsers, true);
        tagAdapter = new TagAdapter(getContext(), mHashTags, mHashTagCount);
        recyclerView.setAdapter(userAdapter);
        recyclerViewTags.setAdapter(tagAdapter);

        searchBar = view.findViewById(R.id.search_bar);

        readUsers();
        readTags();

        // Display based off what is typed in search box, each character at a time
        searchBar.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                searchUser(charSequence.toString());
            }

            @Override
            public void afterTextChanged(Editable editable) {
                filter(editable.toString());
            }
        });

        return view;
    }

    /**
     * This method reads hashtags from Firebase database and adds to List of tags
     */
    private void readTags() {

        FirebaseDatabase.getInstance().getReference().child("hashTags").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                mHashTags.clear();
                mHashTagCount.clear();

                for (DataSnapshot s : snapshot.getChildren()) {
                    mHashTags.add(s.getKey());
                    mHashTagCount.add(s.getChildrenCount() + "");
                }

                tagAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }

    private void filter(String text) {
        List<String> mSearchTags = new ArrayList<>();
        List<String> mSearchTagCount = new ArrayList<>();

        // *** Replace with stream  ***
        for (String s : mHashTags) {
            if(s.toLowerCase().contains(text.toLowerCase())) {
                mSearchTags.add(s);
                mSearchTagCount.add(mHashTagCount.get(mHashTags.indexOf(s)));
            }
        }

        tagAdapter.filter(mSearchTags, mSearchTagCount);
    }

    /**
     * This method reads Users from Firebase database and adds to List of users
     */
    private void readUsers() {

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference()
                .child("Users");
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (searchBar.getText().toString().isEmpty()) {
                    mUsers.clear();
                    for (DataSnapshot ss : snapshot.getChildren()) {
                        User user = ss.getValue(User.class);
                        mUsers.add(user);
                    }
                    userAdapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    /**
     * used to search for user in search box
     * Utilizes Firebase database to obtain a list of users from the app
     * @param s
     */
    private void searchUser(String s) {
        Query query = FirebaseDatabase.getInstance().getReference().child("Users")
                .orderByChild("username").startAt(s).endAt(s + "\uf8ff");

        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                mUsers.clear();
                for (DataSnapshot s : snapshot.getChildren()) {
                    User user = s.getValue(User.class);
                    mUsers.add(user);
                }
                userAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}