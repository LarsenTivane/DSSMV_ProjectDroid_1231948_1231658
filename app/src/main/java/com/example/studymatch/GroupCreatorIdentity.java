package com.example.studymatch;

import android.content.Context;
import android.content.SharedPreferences;

import java.util.UUID;

public final class GroupCreatorIdentity {

    private static final String PREFERENCES_NAME = "study_group_preferences";
    private static final String CREATOR_ID_KEY = "creator_id";

    private GroupCreatorIdentity() {
    }

    public static String getId(Context context) {
        SharedPreferences preferences = context.getApplicationContext()
                .getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE);
        String creatorId = preferences.getString(CREATOR_ID_KEY, null);
        if (creatorId == null) {
            creatorId = UUID.randomUUID().toString();
            preferences.edit().putString(CREATOR_ID_KEY, creatorId).apply();
        }
        return creatorId;
    }
}
