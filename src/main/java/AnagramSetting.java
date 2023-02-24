import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.ThreadChannel;

import java.awt.*;
import java.util.HashSet;

public class AnagramSetting {
    private static final int VALID = 1;
    private static final int INVALID = -1;
    private static final int USED = 0;
    int countTimer, score;
    ThreadChannel threadChannel;
    String msgheader, lastmessage, lastmessage2, lastmessage3;
    String anagramLetters;
    Long memberId;
    EmbedBuilder eb;
    boolean active, gameOver;
    int[] anagramFreqArray = new int[26];
    int[] messageFreqArray = new int[26];
    HashSet<String> usedWords = new HashSet<>();

    public AnagramSetting(int startingTime, ThreadChannel threadChannel, Long memberId, String anagramLetters) {
        countTimer = startingTime;
        this.threadChannel = threadChannel;
        this.memberId = memberId;
        this.anagramLetters = anagramLetters;

        for(int i = 0; i < anagramLetters.length(); ++i) {
            ++anagramFreqArray[anagramLetters.charAt(i) - 'a'];
        }

        score = 0;
        eb = new EmbedBuilder();
        active = false;
        gameOver = false;

        msgheader = lastmessage = lastmessage2 = lastmessage3 = "";
    }

    public void setCountTimer(int count) { this.countTimer = count; }
    public void updateScore(int delta) { this.score += (delta * 100); }
    public void setScore(int score) { this.score = score; }
    public void setEb(EmbedBuilder eb) { this.eb = eb; }
    public void setActive(Boolean active) { this.active = active; }
    public void setGameOver(Boolean gameOver) { this.gameOver = gameOver; }
    public void setLastMessage(String message) {
        lastmessage = "(+" + message.length() * 100 + ")";
    }
    public MessageEmbed ebBuild() {
        return eb.setColor(getColor()).setTitle("**Game is Running!**").setDescription("**Letters are in the title of this thread.** You can use setting prefix + quit to quit at anytime" + "\n"
                + "Timer: " + countTimer + "\tScore: " + score + " " + lastmessage + "\n").build();
    }
    public void decrementCountTimer() { countTimer--; }
    public Integer getCountTimer() { return countTimer; }
    public Integer getScore() { return score; }
    public Long getMemberId() { return memberId; }
    public Boolean isActive() { return active; }
    public Boolean isGameOver() { return gameOver; }
    public Integer isValid(String message) {
        for(int i = 0; i < 26; ++i) {
            messageFreqArray[i] = 0;
        }

        if(message.length() <= 1) {
            return INVALID;
        }
        if(usedWords.contains(message)) {
            return USED;
        }

        for(int i = 0; i < message.length(); ++i) {
            ++messageFreqArray[message.charAt(i) - 'a'];

            if(messageFreqArray[message.charAt(i) - 'a'] > anagramFreqArray[message.charAt(i) - 'a']) {
                return INVALID;
            }
        }

        usedWords.add(message);

        return VALID;
    }
    public HashSet<String> getUsedWords() { return usedWords; }

    public Color getColor() {
        if(score >= 8000) {
            return new Color(254,255,45);   // 227
        }
        if(score >= 7000) {
            return new Color(255,77,178);   // 205
        }
        if(score >= 6500) {
            return new Color(233,80,255);   // 171
        }
        if(score >= 6000) {
            return new Color(188,84,255);  // 135
        }
        if(score >= 5500) {
            return new Color(183,130,255);  // 141
        }
        if(score >= 5000) {
            return new Color(98,90,255);   // 63
        }
        if(score >= 4000) {
            return new Color(86,134,255);   // 69
        }
        if(score >= 3000) {
            return new Color(0,218,255);    // 81
        }
        if(score >= 2000) {
            return new Color(9,255,123);   // 84
        }
        return new Color(204,255,211);  // 194
    }

    public String getRank() {
        if(score >= 8000) {
            return ("***LEGENDARY (8000+)***");
        }
        if(score >= 7000) {
            return ("***Mythical (7000-7999)***");
        }
        if(score >= 6500) {
            return("***Guru (6500-6999)***");
        }
        if(score >= 6000) {
            return ("**Master (6000-6499)**");
        }
        if(score >= 5500) {
            return ("**Advanced (5500-5999)**");
        }
        if(score >= 5000) {
            return ("Expert (5000-5499)");
        }
        if(score >= 4000) {
            return ("Seasoned (4000-4999)");
        }
        if(score >= 3000) {
            return ("Accomplished (3000-3999)");
        }
        if(score >= 2000) {
            return ("Experienced (2000-2999)");
        }
        return ("Apprentice (<2000)");
    }

}