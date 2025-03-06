package com.example.lorekeeper;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
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
import com.example.lorekeeper.models.Campaign;
import com.example.lorekeeper.models.User;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.util.HashMap;
import java.util.Map;

public class CampaignEditActivity extends AppCompatActivity {
    private EditText campaignNameInput, campaignDescriptionInput;
    private String campaignId;
    private ImageView campaignImageView;
    private ImageButton saveButton;
    private FirebaseFirestore firestore;
    private static final int PICK_IMAGE_REQUEST = 1;
    private static final String IMGUR_CLIENT_ID = "2e2586701424cf0";
    private String uploadedImageUrl;
    private RequestQueue requestQueue;
    private boolean isImageUploaded = false;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_campaign_edit);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        campaignNameInput = findViewById(R.id.campaign_name_input);
        campaignDescriptionInput = findViewById(R.id.campaign_description_input);
        campaignImageView = findViewById(R.id.campaign_image);
        saveButton = findViewById(R.id.save_button);
        ImageButton backButton = findViewById(R.id.backbtn);

        firestore = FirebaseFirestore.getInstance();

        Intent intent = getIntent();
        campaignId = intent.getStringExtra("campaignId");
        loadCampaignData();
        requestQueue = Volley.newRequestQueue(this);

        campaignImageView.setOnClickListener(v -> openGallery());
        backButton.setOnClickListener(v -> onBackPressed());

        saveButton.setOnClickListener(v -> saveCampaignChanges());
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
                    campaignImageView.setImageBitmap(bitmap);
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
                        isImageUploaded = true;
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

    private void loadCampaignData() {
        firestore.collection("campaigns").document(campaignId).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        Campaign campaign = documentSnapshot.toObject(Campaign.class);
                        if (campaign != null) {
                            campaignNameInput.setText((campaign.getCampaignName()));
                            campaignDescriptionInput.setText(campaign.getCampaignDescription());
                            Glide.with(this).load(campaign.getImageURL()).placeholder(R.drawable.placeholder).into(campaignImageView);
                        }
                    } else {
                        Log.e("Firestore", "Документ не найден");
                    }
                })
                .addOnFailureListener(e -> Log.e("Firestore", "Ошибка загрузки", e));
    }
    private void saveCampaignChanges() {
        String newName = campaignNameInput.getText().toString().trim();
        String newDescription = campaignDescriptionInput.getText().toString().trim();

        if (newName.isEmpty() || newDescription.isEmpty()) {
            Toast.makeText(this, "Заполните все поля", Toast.LENGTH_SHORT).show();
            return;
        }
        Map<String, Object> campaignUpdates = new HashMap<>();
        campaignUpdates.put("campaignName", newName);
        campaignUpdates.put("campaignDescription", newDescription);
        if (!isImageUploaded) {
        } else {
            if (uploadedImageUrl != null && !uploadedImageUrl.isEmpty()) {
                campaignUpdates.put("imageURL", uploadedImageUrl);
            }
        }

        firestore.collection("campaigns")
                .document(campaignId)
                .update(campaignUpdates)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(CampaignEditActivity.this, "Изменения сохранены", Toast.LENGTH_SHORT).show();
                    finish();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(CampaignEditActivity.this, "Ошибка при сохранении изменений", Toast.LENGTH_SHORT).show();
                });
    }
}