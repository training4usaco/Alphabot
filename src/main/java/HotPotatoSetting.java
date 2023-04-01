import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.ThreadChannel;

import java.util.HashSet;

public class HotPotatoSetting {
    long hostId;
    int countTimer;
    String prevWord;
    HashSet<Long> participants = new HashSet();
    HashSet<String> usedWords = new HashSet();
    MessageChannel gameChannel;
    ThreadChannel threadChannel;
    Boolean active;
    EmbedBuilder eb;

    public HotPotatoSetting(long hostId, String prevWord, MessageChannel gameChannel, int startingTime) {
        this.hostId = hostId;
        this.prevWord = prevWord;
        this.gameChannel = gameChannel;

        this.countTimer = startingTime;
        this.gameChannel = gameChannel;
        this.hostId = hostId;

        this.eb = new EmbedBuilder();
        this.active = false;
    }

    public MessageEmbed ebBuild() {
        return this.eb.build();
//        return this.eb.setTitle("**Game is Running!** Find as many words as possible!").setDescription("**Letters are in the title of this thread.** \n**Letters: " + this.anagramLetters + "**\nYou can use setting prefix + quit to quit at anytime\nTimer: " + this.countTimer + "\tScore: " + this.score + " " + this.lastmessage + "\n").build();
    }

    public long getHostId() {
        return hostId;
    }
    public int getCountTimer() { return countTimer; }
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
    public void setThreadChannel(ThreadChannel threadChannel) {
        this.threadChannel = threadChannel;
    }
}
