package com.example.studymatch;

import android.content.Context;
import android.content.SharedPreferences;

import java.util.UUID;

public final class GroupCreatorIdentity {
    private static final String PREFERENCES_NAME = "study_match_identity";
    private static final String KEY_ID = "creator_id";

    private GroupCreatorIdentity() {
    }

    public static String getId(Context context) {
        SharedPreferences preferences = context.getApplicationContext()
                .getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE);
        String id = preferences.getString(KEY_ID, null);
        if (id == null) {
            id = UUID.randomUUID().toString();
            preferences.edit().putString(KEY_ID, id).apply();
        }
        return id;
    }
}
