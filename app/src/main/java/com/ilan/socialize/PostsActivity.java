package com.ilan.socialize;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.webkit.MimeTypeMap;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.StorageTask;
import com.hendraanggrian.appcompat.socialview.Hashtag;
import com.hendraanggrian.appcompat.widget.HashtagArrayAdapter;
import com.hendraanggrian.appcompat.widget.SocialAutoCompleteTextView;
import com.ilan.socialize.databinding.ActivityPostsBinding;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.util.HashMap;
import java.util.List;

public class PostsActivity extends AppCompatActivity {

    private ActivityPostsBinding binding;
    private Uri imageUri;
    private String imageUrl;
    private SocialAutoCompleteTextView description;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityPostsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        description = findViewById(R.id.description);

        // Go back to main activity if user clicks on close icon
        binding.close.setOnClickListener(view -> {
            startActivity(new Intent(PostsActivity.this, MainActivity.class));
            finish();
        });

        // Create post and upload image
        binding.post.setOnClickListener(view -> {
            upload();
        });

        // Invoke Crop Image Activity
        CropImage.activity().setGuidelines(CropImageView.Guidelines.ON);
        CropImage.activity().start(PostsActivity.this);
    }

    private void upload() {

        // ***Depreciated*** Will replace upon completion
        ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Uploading");
        progressDialog.show();

        if (imageUri != null) {
            StorageReference filePath = FirebaseStorage.getInstance().getReference("posts")
                    .child(System.currentTimeMillis() + "." + getFileExtension(imageUri));

            StorageTask uploadTask = filePath.putFile(imageUri);
            uploadTask.continueWithTask(new Continuation() {
                @Override
                public Object then(@NonNull Task task) throws Exception {
                    if (!task.isSuccessful()) {
                        throw task.getException();
                    }
                    return filePath.getDownloadUrl();
                }
            }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                @Override
                public void onComplete(@NonNull Task<Uri> task) {
                    Uri downloadUri = task.getResult();
                    imageUrl = downloadUri.toString();

                    DatabaseReference reference = FirebaseDatabase.getInstance().getReference("posts");
                    String postId = reference.push().getKey();  // Creates unique ID for post

                    // Store post data in firebase database
                    HashMap<String, Object> hashMap = new HashMap<>();
                    hashMap.put("postId", postId);
                    hashMap.put("imageUrl", imageUrl);
                    hashMap.put("description", description.getText().toString());
                    hashMap.put("publisher", FirebaseAuth.getInstance().getCurrentUser().getUid());

                    reference.child(postId).setValue(hashMap);

                    // Create database reference for hashtags
                    DatabaseReference mHashTagRef = FirebaseDatabase.getInstance().getReference()
                            .child("hashTags");
                    List<String> hashTags = description.getHashtags();
                    // Check if list has hashtags
                    if (!hashTags.isEmpty()) {
                        for (String tag : hashTags) {
                            hashMap.clear();
                            hashMap.put("tag", tag.toLowerCase());
                            hashMap.put("postId", postId);

                            mHashTagRef.child(tag.toLowerCase()).child(postId).setValue(hashMap);
                        }

                    }
                    progressDialog.dismiss();
                    startActivity(new Intent(PostsActivity.this, MainActivity.class));
                    finish();
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Toast.makeText(PostsActivity.this, e.getMessage(), Toast.LENGTH_LONG).show();
                }
            });
        } else {
            Toast.makeText(PostsActivity.this, "No image was selected.", Toast.LENGTH_LONG).show();
        }
    }

    private String getFileExtension(Uri uri) {
        return MimeTypeMap.getSingleton().getExtensionFromMimeType(this.getContentResolver().getType(uri));
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE && resultCode == RESULT_OK) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            imageUri = result.getUri();
            binding.imageSelected.setImageURI(imageUri);
        } else {
            Toast.makeText(PostsActivity.this, "Try again.", Toast.LENGTH_LONG).show();
            startActivity(new Intent(PostsActivity.this, MainActivity.class));
            finish();
        }
    }

    @Override
    protected void onStart() {
        super.onStart();

        ArrayAdapter<Hashtag> hashtagAdapter = new HashtagArrayAdapter<>(getApplicationContext());

        FirebaseDatabase.getInstance().getReference().child("hashTags").addValueEventListener(
                new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        for (DataSnapshot ss : snapshot.getChildren()) {
                            hashtagAdapter.add(new Hashtag(ss.getKey(), (int) ss.getChildrenCount()));
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                }
        );

        description.setHashtagAdapter(hashtagAdapter);
    }
}