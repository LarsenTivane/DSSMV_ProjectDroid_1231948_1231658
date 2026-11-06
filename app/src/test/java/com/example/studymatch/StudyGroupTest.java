package com.example.studymatch;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class StudyGroupTest {

    @Test
    public void addMemberReplacesExistingMembershipForSameDevice() {
        StudyGroup group = new StudyGroup();
        group.addMember(new StudyGroupMember("device-1", "Ana", "12345"));
        group.addMember(new StudyGroupMember("device-1", "Ana Silva", "12345"));

        assertEquals(1, group.getMembers().size());
        assertEquals("Ana Silva", group.getMembers().get(0).getName());
    }

    @Test
    public void removeMemberKeepsOtherGroupMembers() {
        StudyGroup group = new StudyGroup();
        group.addMember(new StudyGroupMember("device-1", "Ana", "12345"));
        group.addMember(new StudyGroupMember("device-2", "Rui", "67890"));

        group.removeMember("device-1");

        assertFalse(group.hasMember("device-1"));
        assertTrue(group.hasMember("device-2"));
        assertEquals(1, group.getMembers().size());
    }

    @Test
    public void groupJsonStoresMemberDetailsWithoutGlobalJoinedFlag() {
        StudyGroup group = new StudyGroup();
        group.addMember(new StudyGroupMember("device-1", "Ana", "12345"));
        group.setJoined(true);

        JsonObject json = new JsonParser().parse(new Gson().toJson(group)).getAsJsonObject();
        JsonObject member = json.getAsJsonArray("members").get(0).getAsJsonObject();

        assertEquals("device-1", member.get("memberId").getAsString());
        assertEquals("Ana", member.get("name").getAsString());
        assertEquals("12345", member.get("studentNumber").getAsString());
        assertFalse(json.has("joined"));
    }
}
