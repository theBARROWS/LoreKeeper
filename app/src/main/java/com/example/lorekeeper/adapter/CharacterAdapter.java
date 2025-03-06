package com.example.lorekeeper.adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.lorekeeper.CampaignCharacterActivity;
import com.example.lorekeeper.CampaignCharacterSearchActivity;
import com.example.lorekeeper.CharacterActivity;
import com.example.lorekeeper.R;
import com.example.lorekeeper.models.Character;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

public class CharacterAdapter extends RecyclerView.Adapter<CharacterAdapter.CharacterViewHolder> {

    private List<Character> characterList;
    private List<Character> filteredList;
    private Context context;
    private FirebaseFirestore db;
    private boolean isSelectionMode;
    private String campaignId;
    private String campaignCreatorId;

    public CharacterAdapter(List<Character> characterList, Context context, boolean isSelectionMode, String campaignId) {
        this.characterList = characterList;
        this.filteredList = new ArrayList<>(characterList);
        this.context = context;
        this.db = FirebaseFirestore.getInstance();
        this.isSelectionMode = isSelectionMode;
        this.campaignId = campaignId;
    }


    @NonNull
    @Override
    public CharacterViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.card_character, parent, false);
        return new CharacterViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CharacterViewHolder holder, int position) {
        Character character = filteredList.get(position);
        holder.characterName.setText(character.getCharacterName());

        getRaceName(character.getRaceID(), raceName -> {
            getClassName(character.getClassID(), className -> {
                getBackgroundName(character.getBackgroundID(), backgroundName -> {
                    holder.characterRaceClass.setText(raceName + " | " + className + " | " + backgroundName);
                });
            });
        });

        holder.characterLevel.setText("Уровень: " + character.getLevel());

        String imageUrl = character.getImageURL();
        if (imageUrl != null && !imageUrl.isEmpty()) {
            Glide.with(context)
                    .load(imageUrl)
                    .placeholder(R.drawable.placeholder)
                    .into(holder.characterImage);
        }
        //почему ты нуллпойнтерэкзепршн, везде работает но не тут??????????

        holder.itemView.setOnClickListener(v -> {
            if (isSelectionMode) {
                addCharacterToCampaign(character.getId());
            } else {
                Intent intent = new Intent(v.getContext(), CharacterActivity.class);
                intent.putExtra("character_id", character.getId());
                v.getContext().startActivity(intent);
            }
        });
        if (character.getCreatorId() != null && character.getCreatorId().equals(FirebaseAuth.getInstance().getCurrentUser().getUid())) {
            holder.deleteBtn.setVisibility(View.VISIBLE);
        } else {
            holder.deleteBtn.setVisibility(View.GONE);
        }

        if (campaignCreatorId != null && campaignCreatorId!=FirebaseAuth.getInstance().getCurrentUser().getUid()) {
            holder.excludeBtn.setVisibility(View.VISIBLE);
            holder.deleteBtn.setVisibility(View.GONE);
            holder.excludeBtn.setOnClickListener(v -> {
                removeCharacterFromCampaign(character.getId());
            });
        } else {
            holder.excludeBtn.setVisibility(View.GONE);
        }

        if (!isSelectionMode) {
            holder.deleteBtn.setOnClickListener(v -> {
                db.collection("characters")
                        .document(character.getId())
                        .delete()
                        .addOnSuccessListener(aVoid -> {
                            filteredList.remove(position);
                            notifyItemRemoved(position);
                            Toast.makeText(context, "Персонаж удален", Toast.LENGTH_SHORT).show();
                        })
                        .addOnFailureListener(e -> {
                            Toast.makeText(context, "Ошибка удаления", Toast.LENGTH_SHORT).show();
                        });
            });
        } else {
            holder.deleteBtn.setVisibility(View.GONE);
        }
    }
    public void setCreatorId(String creatorId) {
        this.campaignCreatorId = creatorId;
        notifyDataSetChanged();
    }

    private void removeCharacterFromCampaign(String characterId) {
        if (campaignId == null || characterId == null) return;

        db.collection("campaigns").document(campaignId)
                .update("characterIds", FieldValue.arrayRemove(characterId))
                .addOnSuccessListener(aVoid -> {Toast.makeText(context, "Персонаж исключен", Toast.LENGTH_SHORT).show();
                    if (context instanceof CampaignCharacterSearchActivity) {
                        ((CampaignCharacterSearchActivity) context).onBackPressed();
                    }})
                .addOnFailureListener(e -> Toast.makeText(context, "Ошибка исключения", Toast.LENGTH_SHORT).show());
    }

    private void addCharacterToCampaign(String characterId) {
        if (campaignId == null) return;

        db.collection("campaigns").document(campaignId)
                .update("characterIds", FieldValue.arrayUnion(characterId))
                .addOnSuccessListener(aVoid -> {Toast.makeText(context, "Персонаж добавлен", Toast.LENGTH_SHORT).show();
                    if (context instanceof CampaignCharacterSearchActivity) {
                        ((CampaignCharacterSearchActivity) context).onBackPressed();
                    }})
                .addOnFailureListener(e -> Toast.makeText(context, "Ошибка добавления", Toast.LENGTH_SHORT).show());
    }

    @Override
    public int getItemCount() {
        return filteredList.size();
    }

    public void filter(String query) {
        filteredList.clear();
        if (query.isEmpty()) {
            filteredList.addAll(characterList);
        } else {
            String lowerCaseQuery = query.toLowerCase();
            for (Character character : characterList) {
                if (character.getCharacterName().toLowerCase().contains(lowerCaseQuery)) {
                    filteredList.add(character);
                }
            }
        }
        notifyDataSetChanged();
    }

    public void updateData(List<Character> newCharacterList) {
        characterList.clear();
        characterList.addAll(newCharacterList);
        filter("");
    }

    private void getRaceName(String raceID, final OnDataReceivedListener listener) {
        db.collection("race").document(raceID)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    String raceName = documentSnapshot.exists() ? documentSnapshot.getString("raceName") : "Unknown Race";
                    listener.onDataReceived(raceName);
                })
                .addOnFailureListener(e -> listener.onDataReceived("Error"));
    }

    private void getClassName(String classID, final OnDataReceivedListener listener) {
        db.collection("class").document(classID)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    String className = documentSnapshot.exists() ? documentSnapshot.getString("className") : "Unknown Class";
                    listener.onDataReceived(className);
                })
                .addOnFailureListener(e -> listener.onDataReceived("Error"));
    }

    private void getBackgroundName(String backgroundID, final OnDataReceivedListener listener) {
        db.collection("background").document(backgroundID)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    String backgroundName = documentSnapshot.exists() ? documentSnapshot.getString("backgroundName") : "Unknown Background";
                    listener.onDataReceived(backgroundName);
                })
                .addOnFailureListener(e -> listener.onDataReceived("Error"));
    }

    interface OnDataReceivedListener {
        void onDataReceived(String data);
    }

    public static class CharacterViewHolder extends RecyclerView.ViewHolder {

        TextView characterName, characterRaceClass, characterLevel;
        ImageView characterImage;
        ImageButton deleteBtn, excludeBtn;

        public CharacterViewHolder(View itemView) {
            super(itemView);
            characterName = itemView.findViewById(R.id.character_name);
            characterRaceClass = itemView.findViewById(R.id.character_race_class);
            characterLevel = itemView.findViewById(R.id.character_level);
            deleteBtn = itemView.findViewById(R.id.ic_delete);
            excludeBtn = itemView.findViewById(R.id.ic_exclude);
            characterImage = itemView.findViewById(R.id.character_image);
        }
    }
}
