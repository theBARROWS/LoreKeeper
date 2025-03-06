package com.example.lorekeeper;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.bumptech.glide.Glide;
import com.example.lorekeeper.models.User;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.util.HashMap;
import java.util.Map;

public class ProfileEditActivity extends AppCompatActivity {
    private FirebaseAuth auth;
    private FirebaseFirestore firestore;
    private ImageView profileImageView;
    private EditText profileNameEditText, edit_profile_email;

    private ImageButton saveButton, backButton;
    private String userId;
    private String currentName;
    private FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
    private static final int PICK_IMAGE_REQUEST = 1;
    private static final String IMGUR_CLIENT_ID = "2e2586701424cf0";
    private String uploadedImageUrl;
    private RequestQueue requestQueue;
    private boolean isImageUploaded = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_profile_edit);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        auth = FirebaseAuth.getInstance();
        firestore = FirebaseFirestore.getInstance();
        userId = auth.getCurrentUser().getUid();

        profileImageView = findViewById(R.id.profile_image);
        profileNameEditText = findViewById(R.id.edit_profile_name);
        edit_profile_email = findViewById(R.id.edit_profile_email);
//        profileImageView.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
//                startActivityForResult(intent, PICK_IMAGE_REQUEST);
//            }
//        });

        saveButton = findViewById(R.id.savebtn);
        backButton = findViewById(R.id.backbtn);

        loadProfileData();

        requestQueue = Volley.newRequestQueue(this);

        profileImageView.setOnClickListener(v -> openGallery());
        backButton.setOnClickListener(v -> onBackPressed());
        saveButton.setOnClickListener(v -> saveProfileChanges());
    }

    private void openGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, PICK_IMAGE_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null) {
            Uri imageUri = data.getData();
            if (imageUri != null) {
                try {
                    Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), imageUri);
                    profileImageView.setImageBitmap(bitmap);
                    uploadImageToImgur(bitmap);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }
    private void uploadImageToImgur(Bitmap bitmap) {
        saveButton.setEnabled(false);
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, byteArrayOutputStream);
        String encodedImage = android.util.Base64.encodeToString(byteArrayOutputStream.toByteArray(), android.util.Base64.DEFAULT);

        String url = "https://api.imgur.com/3/image";
        Map<String, String> params = new HashMap<>();
        params.put("image", encodedImage);

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, url, new JSONObject(params),
                response -> {
                    try {
                        uploadedImageUrl = response.getJSONObject("data").getString("link");
                        Toast.makeText(this, "Изображение загружено", Toast.LENGTH_SHORT).show();
                        saveButton.setEnabled(true);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                },
                error ->{ Toast.makeText(this, "Ошибка загрузки изображения", Toast.LENGTH_SHORT).show();                   isImageUploaded = false; }) {
            @Override
            public Map<String, String> getHeaders() {
                Map<String, String> headers = new HashMap<>();
                headers.put("Authorization", "Client-ID " + IMGUR_CLIENT_ID);
                return headers;
            }
        };
        requestQueue.add(jsonObjectRequest);
    }

    private void loadProfileData() {
        firestore.collection("users").document(userId).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        User user = documentSnapshot.toObject(User.class);
                        if (user != null) {
                            profileNameEditText.setText(user.getNickname());
                            edit_profile_email.setText(user.getEmail());
                            Glide.with(this).load(user.getImageURL()).placeholder(R.drawable.placeholder).into(profileImageView);
                        }
                    } else {
                        Log.e("Firestore", "Документ не найден");
                    }
                })
                .addOnFailureListener(e -> Log.e("Firestore", "Ошибка загрузки", e));
    }
    private void saveProfileChanges() {
        String newName = profileNameEditText.getText().toString().trim();
        String newEmail = edit_profile_email.getText().toString().trim();

        if (!newName.isEmpty() && !newEmail.isEmpty()) {
            DocumentReference userRef = firestore.collection("users").document(userId);
            Map<String, Object> updates = new HashMap<>();
            updates.put("nickname", newName);
            updates.put("email", newEmail);
            if (uploadedImageUrl != null) {
                updates.put("imageURL", uploadedImageUrl);
            }

            userRef.update(updates)
                    .addOnSuccessListener(aVoid -> user.updateEmail(newEmail)
                            .addOnCompleteListener(task -> {
                                if (task.isSuccessful()) {
                                    Toast.makeText(this, "Профиль обновлен", Toast.LENGTH_SHORT).show();
                                    finish();
                                } else {
                                    Toast.makeText(this, "Ошибка обновления почты", Toast.LENGTH_SHORT).show();
                                }
                            }))
                    .addOnFailureListener(e -> Toast.makeText(this, "Ошибка обновления профиля", Toast.LENGTH_SHORT).show());
        } else {
            Toast.makeText(this, "Имя и почта не могут быть пустыми", Toast.LENGTH_SHORT).show();
        }
    }
}