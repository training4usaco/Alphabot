import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.ThreadChannel;

import java.util.ArrayList;
import java.util.HashSet;

public class HotPotatoSetting {
    long hostId;
    int maxTime;
    int countTimer;
    int currentParticipantIndex;
    int messageCount;
    String prevWord;
    HashSet<Long> participants = new HashSet();
    HashSet<String> usedWords = new HashSet();
    ArrayList<Long> participantOrder = new ArrayList();
    MessageChannel gameChannel;
    ThreadChannel threadChannel;
    boolean active;
    boolean gameOver;
    EmbedBuilder eb;

    public HotPotatoSetting(long hostId, String prevWord, MessageChannel gameChannel) {
        this.hostId = hostId;
        this.prevWord = prevWord;
        this.gameChannel = gameChannel;

        this.gameChannel = gameChannel;
        this.hostId = hostId;

        this.eb = new EmbedBuilder();
        this.active = false;
        this.gameOver = false;

        this.countTimer = 5;
        messageCount = 0;
        maxTime = 5;
        this.currentParticipantIndex = 0;
    }

    public ArrayList<Long> getParticipantOrder() { return this.participantOrder; }
    public MessageEmbed ebBuild() {
        String participantOrderList = "Participants order:\n";

        ArrayList<Long> participantOrder = getParticipantOrder();
        for (int i = 0; i < participantOrder.size(); ++i) {
            participantOrderList += "<@" + participantOrder.get(i) + ">\n";
        }
        return this.eb.setTitle("**Game is Running!** Try to form a word before the game ends!").setDescription("**Current Word: " + prevWord + "**\nTimer: " + this.countTimer + "\n" + participantOrderList).build();
    }

    public boolean isGameOver() { return this.gameOver; }
    public Long getCurrentParticipantId() { return participantOrder.get(this.currentParticipantIndex); }
    public long getHostId() {
        return hostId;
    }
    public int getCountTimer() { return countTimer; }
    public int getMaxTime() { return maxTime; }
    public int getMessageCount() { return messageCount; }
    public String getPrevWord() {
        return prevWord;
    }
    public MessageChannel getGameChannel() {
        return gameChannel;
    }
    public ThreadChannel getThreadChannel() {
        return threadChannel;
    }
    public HashSet<Long> getParticipants() {
        return participants;
    }
    public HashSet<String> getUsedWords() {
        return usedWords;
    }
    public void addParticipant(long participant) {
        participants.add(participant);
    }
    public void addUsedWord(String word) {
        usedWords.add(word);
    }
    public boolean isParticipant(long participant) {
        return participants.contains(participant);
    }
    public boolean isUsedWord(String word) {
        return usedWords.contains(word);
    }
    public boolean isActive() { return active; }
    public void decrementCountTimer() { --this.countTimer; }
    public void setActive(boolean active) { this.active = active; }
    public void setGameOver(boolean gameOver) { this.gameOver = gameOver; }
    public void setThreadChannel(ThreadChannel threadChannel) {
        this.threadChannel = threadChannel;
    }
    public void setPrevWord(String prevWord) { this.prevWord = prevWord; }
    public void initParticipantOrder() {
        participantOrder.add(hostId);

        for (Long participant : participants) {
            participantOrder.add(participant);
        }
    }
    public void resetCountTimer() { countTimer = maxTime; }
    public void decrementMaxTime() { maxTime = Math.max(1, maxTime - 1); }
    public void incrementMessageCount() { ++messageCount; }
    public void incrementCurrentParticipantIndex() { ++currentParticipantIndex; currentParticipantIndex %= participantOrder.size(); }
}
