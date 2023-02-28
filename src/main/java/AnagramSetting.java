import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.ThreadChannel;

import java.awt.*;
import java.util.HashSet;

public class AnagramSetting {
    private static final int VALID = 1;
    private static final int INVALID = -1;
    private static final int USED = 0;
    int countTimer, score, messageCounter, correctCounter;
    ThreadChannel threadChannel;
    String msgheader, lastmessage, lastmessage2, lastmessage3;
    String anagramLetters;
    Long memberId;
    EmbedBuilder eb;
    boolean active, gameOver, gameAborted;
    int[] anagramFreqArray = new int[26];
    int[] messageFreqArray = new int[26];
    HashSet<String> usedWords = new HashSet<>();
    int longestStreak = 0;

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
        gameAborted = false;
        messageCounter = 0;

        msgheader = lastmessage = lastmessage2 = lastmessage3 = "";
        correctCounter = 0;
    }

    public void updateLongestStreak() { longestStreak = Math.max(longestStreak, getCorrectCounter()); }
    public void setCountTimer(int count) { this.countTimer = count; }
    public void updateScore(int delta) { this.score += getScore(delta); }
    public void setActive(Boolean active) { this.active = active; }
    public void setGameOver(Boolean gameOver) { this.gameOver = gameOver; }
    public void setLastMessage(String message) {
        lastmessage = "(+" + getScore(message.length()) + ")";
    }
    public void setGameAborted(Boolean gameAborted) { this.gameAborted = gameAborted; }
    public MessageEmbed ebBuild() {
        return eb.setColor(getColor()).setTitle("**Game is Running!** Find as many words as possible!").setDescription("**Letters are in the title of this thread.** You can use setting prefix + quit to quit at anytime" + "\n"
                + "Timer: " + countTimer + "\tScore: " + score + " " + lastmessage + "\n").build();
    }
    public int getMessageCounter() { return messageCounter; }
    public void decrementCountTimer() { countTimer--; }
    public void incMessageCounter() { ++messageCounter; }
    public void incCorrectCounter() { ++correctCounter; }
    public void resetMessageCounter() { messageCounter = 0; }
    public void resetCorrectCounter() { correctCounter = 0; }
    public Integer getCountTimer() { return countTimer; }
    public Integer getScore() { return score; }
    public Long getMemberId() { return memberId; }
    public Boolean isActive() { return active; }
    public Boolean isGameAborted() { return gameAborted; }
    public Integer getLongestStreak() { return longestStreak; }
    public Integer getScore(Integer len) {
        int streakBonus = 0;

        if(correctCounter >= 10) {
            streakBonus = correctCounter * 50;
        }
        else if(correctCounter >= 5) {
            streakBonus = 150;
        }
        else if(correctCounter >= 3) {
            streakBonus = 50;
        }

        return roundToHundreds(Main.SCORE_A * len * len + Main.SCORE_B * len + Main.SCORE_C) + streakBonus;
    }
    public Boolean isGameOver() { return gameOver; }
    public String getAnagramLetters() { return anagramLetters; }
    public Integer getCorrectCounter() { return correctCounter; }
    public Integer isValid(String message) {
        for(int i = 0; i < 26; ++i) {
            messageFreqArray[i] = 0;
        }

        if(message.length() <= 2) {
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

    private Integer roundToHundreds(Double num) {
        return 100 * Math.round((int)(num / 100));
    }
    public HashSet<String> getUsedWords() { return usedWords; }

    public Color getColor() {
        if(score >= 88888) {
            return new Color(255,0,0);
        }
        if(score >= Main.GOD_SCORE) {
            return new Color(255, 255, 255);
        }
        if(score >= Main.LEGENDARY_SCORE) {
            return new Color(254,255,45);   // 227
        }
        if(score >= Main.MYTHICAL_SCORE) {
            return new Color(255,77,178);   // 205
        }
        if(score >= Main.GURU_SCORE) {
            return new Color(233,80,255);   // 171
        }
        if(score >= Main.MASTER_SCORE) {
            return new Color(188,84,255);  // 135
        }
        if(score >= Main.ADVANCED_SCORE) {
            return new Color(183,130,255);  // 141
        }
        if(score >= Main.EXPERT_SCORE) {
            return new Color(98,90,255);   // 63
        }
        if(score >= Main.SEASONED_SCORE) {
            return new Color(86,134,255);   // 69
        }
        if(score >= Main.ACCOMPLISHED_SCORE) {
            return new Color(0,218,255);    // 81
        }
        if(score >= Main.EXPERIENCED_SCORE) {
            return new Color(9,255,123);   // 84
        }

        return new Color(204,255,211);  // 194
    }

    public String getRank() {
        if(score >= 888888) {
            return ("***__ASIAN (8888888+)__***");
        }
        if(score >= Main.GOD_SCORE) {
            return ("***__GOD (" + Main.GOD_SCORE + "+)__***");
        }
        if(score >= Main.LEGENDARY_SCORE) {
            return ("***LEGENDARY (" + Main.LEGENDARY_SCORE + "-" + (Main.GOD_SCORE - 1) +")***");
        }
        if(score >= Main.MYTHICAL_SCORE) {
            return ("***Mythical (" + Main.MYTHICAL_SCORE + "-" + (Main.LEGENDARY_SCORE - 1) + ")***");
        }
        if(score >= Main.GURU_SCORE) {
            return("***Guru (" + Main.GURU_SCORE + "-" + (Main.MYTHICAL_SCORE - 1) + ")***");
        }
        if(score >= Main.MASTER_SCORE) {
            return ("**Master (" + Main.MASTER_SCORE + "-" + (Main.GURU_SCORE - 1) + ")**");
        }
        if(score >= Main.ADVANCED_SCORE) {
            return ("**Advanced (" + Main.ADVANCED_SCORE + "-" + (Main.MASTER_SCORE - 1) + ")**");
        }
        if(score >= Main.EXPERT_SCORE) {
            return ("Expert (" + Main.EXPERT_SCORE + "-" + (Main.ADVANCED_SCORE - 1) + ")");
        }
        if(score >= Main.SEASONED_SCORE) {
            return ("Seasoned (" + Main.SEASONED_SCORE + "-" + (Main.EXPERT_SCORE - 1) + ")");
        }
        if(score >= Main.ACCOMPLISHED_SCORE) {
            return ("Accomplished (" + Main.ACCOMPLISHED_SCORE + "-" + (Main.SEASONED_SCORE - 1) + ")");
        }
        if(score >= Main.EXPERIENCED_SCORE) {
            return ("Experienced (" + Main.EXPERIENCED_SCORE + "-" + (Main.ACCOMPLISHED_SCORE - 1) + ")");
        }
        return ("Apprentice (<" + Main.EXPERIENCED_SCORE + ")");
    }

}