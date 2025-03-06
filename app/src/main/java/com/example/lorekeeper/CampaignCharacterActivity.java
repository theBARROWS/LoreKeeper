package com.example.lorekeeper;

import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.lorekeeper.models.Character;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

public class CampaignCharacterActivity extends AppCompatActivity {

//    private FirebaseFirestore db;
//    private TextView characterName, raceText, classText, levelText;
//    private TextView strengthText, dexterityText, constitutionText, intelligenceText, wisdomText, charismaText;
//    private FirebaseUser user;
//
//    @Override
//    protected void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_campaign_character);
//
//        db = FirebaseFirestore.getInstance();
//
//        // Initialize the TextViews
//        characterName = findViewById(R.id.characterNameInput);
//        raceText = findViewById(R.id.raceText);
//        classText = findViewById(R.id.classText);
//        levelText = findViewById(R.id.character_level);
//
//        strengthText = findViewById(R.id.strengthInput);
//        dexterityText = findViewById(R.id.dexterityInput);
//        constitutionText = findViewById(R.id.constitutionInput);
//        intelligenceText = findViewById(R.id.intelligenceInput);
//        wisdomText = findViewById(R.id.wisdomInput);
//        charismaText = findViewById(R.id.charismaInput);
//
//        ImageButton backBtn = findViewById(R.id.backbtn);
//        backBtn.setOnClickListener(v -> onBackPressed());
//
//        FirebaseAuth auth = FirebaseAuth.getInstance();
//        user = auth.getCurrentUser();
//        String userId= user.getUid();
////        String userId = getIntent().getStringExtra("user_id");
////        String campaignId = getIntent().getStringExtra("campaign_id");
//        String characterId = getIntent().getStringExtra("character_id");
//
////            loadCharacterData(characterId);
//    }
//
//
//    private void loadCharacterData(String userId, String campaignId, String characterId) {
//        // Construct the path to the character document
//        DocumentReference characterRef = db.collection("users")
//                .document(userId)
//                .collection("campaigns")
//                .document(campaignId)
//                .collection("campaignCharacters")
//                .document(characterId);
//
//        characterRef.get().addOnSuccessListener(documentSnapshot -> {
//            if (documentSnapshot.exists()) {
//                // Convert the document snapshot to a Character object
//                Character character = documentSnapshot.toObject(Character.class);
//
//                if (character != null) {
//                    // Set the TextViews with the character's data
//                    characterName.setText(character.getCharacterName());
//                    raceText.setText(character.getRaceID());
//                    classText.setText(character.getClassID());
//                    levelText.setText("Уровень: " + character.getLevel());
//
//                    strengthText.setText(String.valueOf(character.getStrength()));
//                    dexterityText.setText(String.valueOf(character.getDexterity()));
//                    constitutionText.setText(String.valueOf(character.getConstitution()));
//                    intelligenceText.setText(String.valueOf(character.getIntelligence()));
//                    wisdomText.setText(String.valueOf(character.getWisdom()));
//                    charismaText.setText(String.valueOf(character.getCharisma()));
//
//                    loadClassRaceBackgroundNames(character.getClassID(), character.getRaceID(), character.getBackgroundID());
//
//                }
//            }
//        }).addOnFailureListener(e -> {
//            // Handle any errors
//        });
//    }
//    private void loadClassRaceBackgroundNames(String classId, String raceId, String backgroundId) {
//        // Fetch the class name from the 'classes' collection
//        db.collection("class")
//                .document(classId)
//                .get()
//                .addOnSuccessListener(classDoc -> {
//                    if (classDoc.exists()) {
//                        String className = classDoc.getString("className");
//                        classText.setText(className);
//                    }
//                });
//
//        // Fetch the race name from the 'races' collection
//        db.collection("race")
//                .document(raceId)
//                .get()
//                .addOnSuccessListener(raceDoc -> {
//                    if (raceDoc.exists()) {
//                        String raceName = raceDoc.getString("raceName");
//                        raceText.setText(raceName);
//                    }
//                });
//
//        // Fetch the background name from the 'backgrounds' collection
//        db.collection("background")
//                .document(backgroundId)
//                .get()
//                .addOnSuccessListener(backgroundDoc -> {
//                    if (backgroundDoc.exists()) {
//                        String backgroundName = backgroundDoc.getString("backgroundName");
//                        // Set the background name to a TextView if needed
//                        // For example, you can add a TextView for backgroundName in your layout
//                        TextView backgroundText = findViewById(R.id.backgroundText);
//                        backgroundText.setText(backgroundName);
//                    }
//                });
//    }
}
