package com.example.lorekeeper;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Spinner;
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
import com.example.lorekeeper.models.CampaignCharacter;
import com.example.lorekeeper.models.Character;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CharacterAddActivity extends AppCompatActivity {
    private Spinner raceSpinner, classSpinner, backgroundSpinner;
    private ImageButton saveButton, backButton;
    private ImageView characterImage;
    private FirebaseFirestore firestore;
    private FirebaseAuth auth;
    private List<String> raceList, classList, backgroundList;
    private Map<String, String> raceIdMap, classIdMap, backgroundIdMap;

    private static final int PICK_IMAGE_REQUEST = 1;
    private static final String IMGUR_CLIENT_ID = "2e2586701424cf0";
    private String uploadedImageUrl;
    private RequestQueue requestQueue;
    private boolean isImageUploaded = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_character_add);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        firestore = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();

        characterImage = findViewById(R.id.character_image);
        characterImage.setOnClickListener(v -> openGallery());

        raceSpinner = findViewById(R.id.raceSpinner);
        classSpinner = findViewById(R.id.classSpinner);
        backgroundSpinner = findViewById(R.id.backgroundSpinner);

        backButton = findViewById(R.id.backbtn);
        saveButton = findViewById(R.id.saveButton);

        raceList = new ArrayList<>();
        classList = new ArrayList<>();
        backgroundList = new ArrayList<>();

        raceIdMap = new HashMap<>();
        classIdMap = new HashMap<>();
        backgroundIdMap = new HashMap<>();

        requestQueue = Volley.newRequestQueue(this);
        String campaignId = getIntent().getStringExtra("campaignId");
        saveButton.setOnClickListener(v -> saveCharacter());
        backButton.setOnClickListener(v -> onBackPressed());
        loadData();
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
    private void loadData() {
        firestore.collection("race")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    for (DocumentSnapshot document : queryDocumentSnapshots) {
                        String id = document.getId();
                        String name = document.getString("raceName");
                        raceList.add(name);
                        raceIdMap.put(name, id);
                    }
                    // Настройка адаптера для Spinner
                    ArrayAdapter<String> raceAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, raceList);
                    raceAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    raceSpinner.setAdapter(raceAdapter);
                });

        firestore.collection("class")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    for (DocumentSnapshot document : queryDocumentSnapshots) {
                        String id = document.getId();
                        String name = document.getString("className");
                        classList.add(name);
                        classIdMap.put(name, id);
                    }
                    ArrayAdapter<String> classAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, classList);
                    classAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    classSpinner.setAdapter(classAdapter);
                });

        firestore.collection("background")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    for (DocumentSnapshot document : queryDocumentSnapshots) {
                        String id = document.getId();
                        String name = document.getString("backgroundName");
                        backgroundList.add(name);
                        backgroundIdMap.put(name, id);
                    }
                    ArrayAdapter<String> backgroundAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, backgroundList);
                    backgroundAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    backgroundSpinner.setAdapter(backgroundAdapter);
                });

    }

    private void saveCharacter() {
        String selectedRace = raceSpinner.getSelectedItem().toString();
        String selectedClass = classSpinner.getSelectedItem().toString();
        String selectedBackground = backgroundSpinner.getSelectedItem().toString();

        String raceId = raceIdMap.get(selectedRace);
        String classId = classIdMap.get(selectedClass);
        String backgroundId = backgroundIdMap.get(selectedBackground);

        String characterName = ((EditText) findViewById(R.id.characterNameInput)).getText().toString();

        int strength = getValidStatValue(((EditText) findViewById(R.id.strengthInput)).getText().toString());
        int dexterity = getValidStatValue(((EditText) findViewById(R.id.dexterityInput)).getText().toString());
        int constitution = getValidStatValue(((EditText) findViewById(R.id.constitutionInput)).getText().toString());
        int intelligence = getValidStatValue(((EditText) findViewById(R.id.intelligenceInput)).getText().toString());
        int wisdom = getValidStatValue(((EditText) findViewById(R.id.wisdomInput)).getText().toString());
        int charisma = getValidStatValue(((EditText) findViewById(R.id.charismaInput)).getText().toString());

        if (characterName.isEmpty()) {
            Toast.makeText(this, "Имя персонажа не может быть пустым.", Toast.LENGTH_SHORT).show();
            return;
        }
        if (!isImageUploaded) {
            Toast.makeText(this, "Изображение не загружено.", Toast.LENGTH_SHORT).show();
            return;
        }
        com.example.lorekeeper.models.Character newCharacter = new Character(
                null,
                auth.getCurrentUser().getUid(),
                characterName,
                raceId,
                backgroundId,
                classId,
                strength,
                1,
                dexterity,
                constitution,
                intelligence,
                wisdom,
                charisma,
                uploadedImageUrl
        );
        firestore.collection("characters")
                .add(newCharacter)
                .addOnSuccessListener(documentReference -> {
                    String generatedCharacterID = documentReference.getId();
                    documentReference.update("id", generatedCharacterID)
                            .addOnSuccessListener(aVoid -> {
                                Toast.makeText(this, "Персонаж добавлен!", Toast.LENGTH_SHORT).show();
                                finish();
                            })
                            .addOnFailureListener(e -> {
                                Toast.makeText(this, "Ошибка при обновлении ID", Toast.LENGTH_SHORT).show();
                            });
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Ошибка при добавлении персонажа", Toast.LENGTH_SHORT).show();
                });
    }
    private int getValidStatValue(String statValue) {
        try {
            int value = Integer.parseInt(statValue);

            if (value < 1) {
                return 1;
            } else if (value > 20) {
                return 20;
            } else {
                return value;
            }
        } catch (NumberFormatException e) {
            return 1;
        }
    }
}