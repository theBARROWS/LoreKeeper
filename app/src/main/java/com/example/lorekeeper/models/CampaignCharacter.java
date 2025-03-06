package com.example.lorekeeper.models;

public class CampaignCharacter {
    public String id, characterName, raceID, backgroundID, classID, campaignId, creatorId, campaignCreatorId;
    public int strength,level, dexterity, constitution, intelligence, wisdom, charisma;
//    private Map<String, Integer> money;
//    private List<Item> inventory;

    public CampaignCharacter() {
    }

    public CampaignCharacter(String id, String creatorId, String campaignId, String characterName, String raceID, String backgroundID, String classID, int strength, int level, int dexterity, int constitution, int intelligence, int wisdom, int charisma) {
        this.id = id;
        this.creatorId = creatorId;
        this.campaignId=campaignId;
        this.characterName = characterName;
        this.raceID = raceID;
        this.backgroundID = backgroundID;
        this.classID = classID;
        this.strength = strength;
        this.level = level;
        this.dexterity = dexterity;
        this.constitution = constitution;
        this.intelligence = intelligence;
        this.wisdom = wisdom;
        this.charisma = charisma;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getCreatorId() {
        return creatorId;
    }

    public void setCreatorId(String creatorId) {
        this.creatorId = creatorId;
    }

    public String getCharacterName() {
        return characterName;
    }

    public void setCharacterName(String characterName) {
        this.characterName = characterName;
    }

    public String getRaceID() {
        return raceID;
    }

    public void setRaceID(String raceID) {
        this.raceID = raceID;
    }

    public String getBackgroundID() {
        return backgroundID;
    }

    public void setBackgroundID(String backgroundID) {
        this.backgroundID = backgroundID;
    }

    public String getClassID() {
        return classID;
    }

    public void setClassID(String classID) {
        this.classID = classID;
    }

    public int getStrength() {
        return strength;
    }

    public void setStrength(int strength) {
        this.strength = strength;
    }

    public int getLevel() {
        return level;
    }

    public void setLevel(int level) {
        this.level = level;
    }

    public int getDexterity() {
        return dexterity;
    }

    public void setDexterity(int dexterity) {
        this.dexterity = dexterity;
    }

    public int getConstitution() {
        return constitution;
    }

    public void setConstitution(int constitution) {
        this.constitution = constitution;
    }

    public int getIntelligence() {
        return intelligence;
    }

    public void setIntelligence(int intelligence) {
        this.intelligence = intelligence;
    }

    public int getWisdom() {
        return wisdom;
    }

    public void setWisdom(int wisdom) {
        this.wisdom = wisdom;
    }

    public int getCharisma() {
        return charisma;
    }

    public void setCharisma(int charisma) {
        this.charisma = charisma;
    }
}
