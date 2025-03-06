package com.example.lorekeeper;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
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
import com.example.lorekeeper.models.Character;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.util.HashMap;
import java.util.Map;

public class CharacterActivity extends AppCompatActivity {

    private FirebaseFirestore db;
    private EditText characterName, dexterityText, constitutionText, intelligenceText, wisdomText, strengthText, charismaText;
    private TextView raceText, classText, levelText;
    private ImageView characterImage;
    private ImageButton editButton, backButton;
    private FirebaseUser user;
    private String characterId;
    private static final int PICK_IMAGE_REQUEST = 1;
    private static final String IMGUR_CLIENT_ID = "2e2586701424cf0";
    private String uploadedImageUrl;
    private RequestQueue requestQueue;
    private boolean isImageUploaded = false;
    private boolean isEditingEnabled = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_character);

        db = FirebaseFirestore.getInstance();

        characterName = findViewById(R.id.characterNameInput);
        raceText = findViewById(R.id.raceText);
        classText = findViewById(R.id.classText);
        levelText = findViewById(R.id.character_level);

        characterImage = findViewById(R.id.character_image);

        strengthText = findViewById(R.id.strengthInput);
        dexterityText = findViewById(R.id.dexterityInput);
        constitutionText = findViewById(R.id.constitutionInput);
        intelligenceText = findViewById(R.id.intelligenceInput);
        wisdomText = findViewById(R.id.wisdomInput);
        charismaText = findViewById(R.id.charismaInput);

        characterImage = findViewById(R.id.character_image);

        backButton = findViewById(R.id.backbtn);
        editButton = findViewById(R.id.edit_button);


        characterId = getIntent().getStringExtra("character_id");
        requestQueue = Volley.newRequestQueue(this);

        loadCharacterData(characterId);

        backButton.setOnClickListener(v -> onBackPressed());
        editButton.setOnClickListener(v -> {
            enableEditing(true);
        });
    }


    private void loadCharacterData(String characterId) {
        DocumentReference characterRef = db.collection("characters")
                .document(characterId);

        characterRef.get().addOnSuccessListener(documentSnapshot -> {
            if (documentSnapshot.exists()) {
                com.example.lorekeeper.models.Character character = documentSnapshot.toObject(Character.class);

                if (character != null) {
                    characterName.setText(character.getCharacterName());
                    raceText.setText(character.getRaceID());
                    classText.setText(character.getClassID());
                    levelText.setText("Уровень: " + character.getLevel());

                    strengthText.setText(String.valueOf(character.getStrength()));
                    dexterityText.setText(String.valueOf(character.getDexterity()));
                    constitutionText.setText(String.valueOf(character.getConstitution()));
                    intelligenceText.setText(String.valueOf(character.getIntelligence()));
                    wisdomText.setText(String.valueOf(character.getWisdom()));
                    charismaText.setText(String.valueOf(character.getCharisma()));

                    loadClassRaceBackgroundNames(character.getClassID(), character.getRaceID(), character.getBackgroundID());
                    String imageUrl = character.getImageURL();
                    if (imageUrl != null && !imageUrl.isEmpty()) {
                        Glide.with(CharacterActivity.this)
                                .load(imageUrl)
                                .placeholder(R.drawable.placeholder)
                                .into(characterImage);
                    }
                    FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
                    if (currentUser != null && !currentUser.getUid().equals(character.getCreatorId())) {
                        editButton.setVisibility(View.GONE);
                    }
                }
            }
        }).addOnFailureListener(e -> {
        });
    }
    private void loadClassRaceBackgroundNames(String classId, String raceId, String backgroundId) {
        db.collection("class")
                .document(classId)
                .get()
                .addOnSuccessListener(classDoc -> {
                    if (classDoc.exists()) {
                        String className = classDoc.getString("className");
                        classText.setText(className);
                    }
                });

        db.collection("race")
                .document(raceId)
                .get()
                .addOnSuccessListener(raceDoc -> {
                    if (raceDoc.exists()) {
                        String raceName = raceDoc.getString("raceName");
                        raceText.setText(raceName);
                    }
                });

        db.collection("background")
                .document(backgroundId)
                .get()
                .addOnSuccessListener(backgroundDoc -> {
                    if (backgroundDoc.exists()) {
                        String backgroundName = backgroundDoc.getString("backgroundName");
                        TextView backgroundText = findViewById(R.id.backgroundText);
                        backgroundText.setText(backgroundName);
                    }
                });
    }
    private void enableEditing(boolean isEditing) {
        characterName.setEnabled(isEditing);
        strengthText.setEnabled(isEditing);
        dexterityText.setEnabled(isEditing);
        constitutionText.setEnabled(isEditing);
        intelligenceText.setEnabled(isEditing);
        wisdomText.setEnabled(isEditing);
        charismaText.setEnabled(isEditing);

        if (isEditing) {
            editButton.setOnClickListener(v -> {
                saveCharacterData();
            });
            characterImage.setOnClickListener(v -> openGallery());
        }
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
                    characterImage.setImageBitmap(bitmap);
                    uploadImageToImgur(bitmap);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }


    private void uploadImageToImgur(Bitmap bitmap) {
        editButton.setEnabled(false);
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
                        editButton.setEnabled(true);
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
    private void saveCharacterData() {
        String updatedName = characterName.getText().toString().trim();  // Trim spaces

        if (updatedName.isEmpty()) {
            Toast.makeText(this, "Имя персонажа не может быть пустым", Toast.LENGTH_SHORT).show();
            return;
        }
        int updatedStrength = Integer.parseInt(strengthText.getText().toString());
        int updatedDexterity = Integer.parseInt(dexterityText.getText().toString());
        int updatedConstitution = Integer.parseInt(constitutionText.getText().toString());
        int updatedIntelligence = Integer.parseInt(intelligenceText.getText().toString());
        int updatedWisdom = Integer.parseInt(wisdomText.getText().toString());
        int updatedCharisma = Integer.parseInt(charismaText.getText().toString());


        Map<String, Object> updatedStats = new HashMap<>();
        updatedStats.put("characterName", updatedName);
        updatedStats.put("strength", updatedStrength);
        updatedStats.put("dexterity", updatedDexterity);
        updatedStats.put("constitution", updatedConstitution);
        updatedStats.put("intelligence", updatedIntelligence);
        updatedStats.put("wisdom", updatedWisdom);
        updatedStats.put("charisma", updatedCharisma);
        if (!isImageUploaded) {
        } else {
            if (uploadedImageUrl != null && !uploadedImageUrl.isEmpty()) {
                updatedStats.put("imageURL", uploadedImageUrl);
            }
        }

        db.collection("characters").document(characterId)
                .update(updatedStats)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(CharacterActivity.this, "Персонаж обновлен", Toast.LENGTH_SHORT).show();
                    enableEditing(false);
                    onBackPressed();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(CharacterActivity.this, "Ошибка при обновлении персонажа", Toast.LENGTH_SHORT).show();
                });
    }

}