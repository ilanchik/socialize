package com.ilan.socialize;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.ilan.socialize.adapter.CommentAdapter;
import com.ilan.socialize.model.Comment;
import com.ilan.socialize.model.User;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class CommentActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private CommentAdapter commentAdapter;
    private List<Comment> comments;

    private EditText comment;
    private ImageView profileImage;
    private TextView post;

    private String postId;
    private String authorId;

    FirebaseUser firebaseUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_comment);

        // Set up toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Comments");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        Intent intent = getIntent();
        postId = intent.getStringExtra("postId");
        authorId = intent.getStringExtra("authorId");

        // Set up adapter and RecyclerView
        recyclerView = findViewById(R.id.recycler_view_comments);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        comments = new ArrayList<>();
        commentAdapter = new CommentAdapter(this, comments, postId);

        recyclerView.setAdapter(commentAdapter);

        post = findViewById(R.id.post);
        profileImage = findViewById(R.id.profile_image);
        comment = findViewById(R.id.add_comment);



        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();

        getUserImage();

        post.setOnClickListener(view -> {
            if (comment.getText().toString().isEmpty()) {
                Toast.makeText(CommentActivity.this, "Please enter a comment.",
                        Toast.LENGTH_SHORT).show();
            } else {
                addComment();
            }
        });

        getComment();

    }

    private void getComment() {

        FirebaseDatabase.getInstance().getReference().child("comments").child(postId)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        comments.clear();

                        for (DataSnapshot ss : snapshot.getChildren()) {
                            Comment comment = ss.getValue(Comment.class);
                            comments.add(comment);
                        }

                        commentAdapter.notifyDataSetChanged();
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

    /**
     * Add comment to firebase database, and notify user of status
     */
    private void addComment() {

        HashMap<String, Object> hashMap = new HashMap<>();

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference().child("comments").child(postId);

        String id = ref.push().getKey();

        hashMap.put("id", id);
        hashMap.put("comment", comment.getText().toString());
        hashMap.put("publisher", firebaseUser.getUid());

        comment.setText("");

        ref.child(id).setValue(hashMap).addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(CommentActivity.this, "Comment added.",
                                Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(CommentActivity.this, task.getException().getMessage(),
                                Toast.LENGTH_SHORT).show();
                    }
                });
    }

    /**
     * Displays the user's profile image
     */
    private void getUserImage() {

        FirebaseDatabase.getInstance().getReference().child("Users").child(firebaseUser.getUid())
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        User user = snapshot.getValue(User.class);

                        if (user.getImageurl().equals("default")) {
                            profileImage.setImageResource(R.mipmap.ic_launcher);
                        } else {
                            Picasso.get().load(user.getImageurl()).into(profileImage);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }
}