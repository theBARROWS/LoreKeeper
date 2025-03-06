package com.example.lorekeeper.fragments;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.lorekeeper.LoreKeeperAbout;
import com.example.lorekeeper.ProfileEditActivity;
import com.example.lorekeeper.R;
import com.example.lorekeeper.Start;
import com.example.lorekeeper.models.User;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link ProfileFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ProfileFragment extends Fragment {
    private TextView profileName, profileEmail;
    private ImageView profile_image;
    FirebaseFirestore db = FirebaseFirestore.getInstance();
    private SwipeRefreshLayout swipeRefreshLayout;
    private boolean isLoading = false;

    public ProfileFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_profile, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        profileName = view.findViewById(R.id.profile_name);
        profileEmail = view.findViewById(R.id.profile_email);

        profile_image = view.findViewById(R.id.profile_image);

        swipeRefreshLayout = view.findViewById(R.id.swipe_refresh_layout);
        swipeRefreshLayout.setOnRefreshListener(() -> loadUserInfo());


        loadUserInfo();

        view.findViewById(R.id.profile_settings).setOnClickListener(v -> {
            Toast.makeText(getContext(), "Открываем настройки...", Toast.LENGTH_SHORT).show();
        });

        view.findViewById(R.id.profile_about).setOnClickListener(v -> {
            startActivity(new Intent(getActivity(), LoreKeeperAbout.class));
        });
        view.findViewById(R.id.editbtn).setOnClickListener(v -> {
            startActivity(new Intent(getActivity(), ProfileEditActivity.class));
        });

        view.findViewById(R.id.profile_logout).setOnClickListener(v -> {
            FirebaseAuth.getInstance().signOut();

            if (getActivity() != null) {
                getActivity().getSharedPreferences("user_preferences", getActivity().MODE_PRIVATE)
                        .edit()
                        .putBoolean("is_logged_in", false)
                        .apply();
            }

            Toast.makeText(getContext(), "Выход из аккаунта", Toast.LENGTH_SHORT).show();

            startActivity(new Intent(getActivity(), Start.class));
            getActivity().finish();
        });
    }

    private void loadUserInfo() {
        if (isLoading) {
            return;
        }
        isLoading = true;
        swipeRefreshLayout.setRefreshing(true);

        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        if (firebaseUser != null) {
            String userId = firebaseUser.getUid();

            db.collection("users").document(userId).get()
                    .addOnSuccessListener(documentSnapshot -> {
                        if (documentSnapshot.exists()) {
                            User user = documentSnapshot.toObject(User.class);
                            if (user != null) {
                                profileName.setText(user.getNickname());
                                profileEmail.setText(user.getEmail());

                                if (isAdded()) {
                                    Glide.with(requireContext().getApplicationContext())
                                            .load(user.getImageURL())
                                            .placeholder(R.drawable.placeholder)
                                            .timeout(6500)
                                            .into(profile_image);
                                }
                            }
                        } else {
                            Log.e("Firestore", "Документ не найден");
                        }
                    })
                    .addOnFailureListener(e -> Log.e("Firestore", "Ошибка загрузки", e))
                    .addOnCompleteListener(task -> {
                        swipeRefreshLayout.setRefreshing(false);
                        isLoading = false;
                    });;

        }
    }

}