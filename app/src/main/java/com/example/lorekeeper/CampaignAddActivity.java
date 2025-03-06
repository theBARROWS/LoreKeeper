package com.example.lorekeeper;

import android.content.Intent;
import android.graphics.Bitmap;
import android.media.Image;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
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
import com.example.lorekeeper.models.Campaign;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CampaignAddActivity extends AppCompatActivity {
    private EditText campaignNameInput, campaignDescriptionInput;
    private ImageButton saveButton, backbtn;
    private FirebaseFirestore firestore;
    private ImageView campaignImageView;
    private static final String ARG_CAMPAIGN_ID = "campaignId";
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
        setContentView(R.layout.activity_campaign_add);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        campaignNameInput = findViewById(R.id.campaign_name_input);
        campaignDescriptionInput = findViewById(R.id.campaign_description_input);
        campaignImageView = findViewById(R.id.campaign_image);

        saveButton = findViewById(R.id.save_button);

        firestore = FirebaseFirestore.getInstance();

        saveButton.setOnClickListener(v -> saveCampaign());
        requestQueue = Volley.newRequestQueue(this);
        ImageButton backBtn = findViewById(R.id.backbtn);
        backBtn.setOnClickListener(v -> onBackPressed());
        campaignImageView.setOnClickListener(v -> openGallery());
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
                        isImageUploaded = true;
                        Toast.makeText(this, "Изображение загружено", Toast.LENGTH_SHORT).show();
                        saveButton.setEnabled(true);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                },
                error -> {Toast.makeText(this, "Ошибка загрузки изображения", Toast.LENGTH_SHORT).show();
                    isImageUploaded = false; }) {
            @Override
            public Map<String, String> getHeaders() {
                Map<String, String> headers = new HashMap<>();
                headers.put("Authorization", "Client-ID " + IMGUR_CLIENT_ID);
                return headers;
            }
        };
        requestQueue.add(jsonObjectRequest);
    }
    private void saveCampaign() {
        String campaignName = campaignNameInput.getText().toString();
        String campaignDescription = campaignDescriptionInput.getText().toString();

        if (campaignName.isEmpty() || campaignDescription.isEmpty()) {
            Toast.makeText(this, "Все поля должны быть заполнены!", Toast.LENGTH_SHORT).show();
            return;
        }
        if (!isImageUploaded) {
            Toast.makeText(this, "Изображение не загружено.", Toast.LENGTH_SHORT).show();
            return;
        }
        String userId = user.getUid();

        DocumentReference newCampaignRef = firestore.collection("campaigns").document();
        String campaignId = newCampaignRef.getId();
        List<String> characterIds = new ArrayList<>();
        Campaign newCampaign = new Campaign(campaignId, campaignName, campaignDescription, uploadedImageUrl, userId, characterIds);

        newCampaignRef.set(newCampaign)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "Кампания добавлена!", Toast.LENGTH_SHORT).show();
                    finish();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Ошибка при добавлении кампании", Toast.LENGTH_SHORT).show();
                });
    }
}