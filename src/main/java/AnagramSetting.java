import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.ThreadChannel;

import java.awt.*;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;

public class AnagramSetting {
    private static final int VALID = 1;
    private static final int INVALID = -1;
    private static final int USED = 0;
    int countTimer, score, messageCounter, correctCounter;
    ThreadChannel threadChannel;
    MessageChannel gameChannel;
    String lastmessage;
    String gameMode;
    String anagramLetters;
    long memberId;
    EmbedBuilder eb, endingEmbed;
    boolean active, gameOver, gameAborted;
    int[] anagramFreqArray = new int[26];
    int[] messageFreqArray = new int[26];
    HashMap<String, String> wordToFinderHashMap = new HashMap<>();
    HashSet<String> usedWords = new HashSet<>();
    HashSet<Long> participants = new HashSet<>();
    int longestStreak = 0;

    public AnagramSetting(int startingTime, MessageChannel gameChannel, Long memberId, String anagramLetters, String gameMode) {
        countTimer = startingTime;
        this.gameChannel = gameChannel;
        this.memberId = memberId;
        this.anagramLetters = anagramLetters;
        this.gameMode = gameMode;

        for(int i = 0; i < anagramLetters.length(); ++i) {
            ++anagramFreqArray[anagramLetters.charAt(i) - 'a'];
        }

        score = 0;
        eb = new EmbedBuilder();
        endingEmbed = new EmbedBuilder();
        active = false;
        gameOver = false;
        gameAborted = false;
        messageCounter = 0;

        lastmessage = "";
        correctCounter = 0;
    }

    public void addParticipant(long participantId) { participants.add(participantId); }
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
    public String getGameMode() { return gameMode; }
    public ThreadChannel getThreadChannel() { return threadChannel; }
    public MessageChannel getGameChannel() { return gameChannel; }
    public void setThreadChannel(ThreadChannel threadChannel) { this.threadChannel = threadChannel; }
    public int getMessageCounter() { return messageCounter; }
    public void decrementCountTimer() { countTimer--; }
    public void incMessageCounter() { ++messageCounter; }
    public void incCorrectCounter() { ++correctCounter; }
    public void resetMessageCounter() { messageCounter = 0; }
    public void resetCorrectCounter() { correctCounter = 0; }
    public void addWord(String word, String participantName) {
        wordToFinderHashMap.put(word, participantName);
    }

    public int getCountTimer() { return countTimer; }
    public int getFinalScore() { return score; }
    public long getMemberId() { return memberId; }
    public boolean isActive() { return active; }
    public boolean isGameAborted() { return gameMode.equals("solo") && gameAborted; }
    public boolean isParticipant(long participantId) { return participants.contains(participantId); }
    public int getLongestStreak() { return longestStreak; }
    public int getScore(Integer len) {
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

    public boolean isGameOver() { return gameOver; }
    public String getAnagramLetters() { return anagramLetters; }
    public int getCorrectCounter() { return correctCounter; }
    public int isValid(String message) {
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

    private int roundToHundreds(double num) {
        return 100 * Math.round((int)(num / 100));
    }
    public HashSet<String> getUsedWords() { return usedWords; }
    public HashMap<String, String> getWordToFinderHashMap() { return wordToFinderHashMap; }

    public MessageEmbed endingEmbedBuild() {
        if(gameMode.equals("solo")) {
            endingEmbed.setTitle("**Game is over!**");
            endingEmbed.setColor(getColor());
            resetCorrectCounter();
            String embedDescription = "<@" + memberId + "> Your final score was: **" + getFinalScore() + "**\nYour rank is: " + getRank() + "\n\nLetters for this anagram: ";
            for (int i = 0; i < anagramLetters.length(); ++i) {
                embedDescription += (anagramLetters.charAt(i) + " ");
            }
            embedDescription += "\nYour longest streak was " + getLongestStreak() + " words!\n\n";
            Iterator<String> it = getUsedWords().iterator();
            if (!it.hasNext()) {
                embedDescription += "No words found!\n";
            }
            else {
                embedDescription += "You found " + getUsedWords().size() + " words: \n";
                while (it.hasNext()) {
                    String word = it.next();
                    embedDescription += (word + "\t" + getScore(word.length()) + "\n");
                }
            }

            endingEmbed.setDescription(embedDescription).setFooter("Congrats!");
        }
        else if(gameMode.equals("coop")) {
            endingEmbed.setTitle("**Coop game is over!**");
            resetCorrectCounter();

            String embedDescription = "Participants in this anagram: ";
            Iterator<Long> it = participants.iterator();
            while(it.hasNext()) {
                long participantId = it.next();

                embedDescription += "<@" + participantId + ">";
            }
            embedDescription += "\n\n";

            Iterator<String> it1 = getUsedWords().iterator();
            if (!it1.hasNext()) {
                embedDescription += "No words found!\n";
            }
            else {
                embedDescription += "You found " + getUsedWords().size() + " words: \n";
                while (it1.hasNext()) {
                    String word = it1.next();
                    embedDescription += (word + " (" + wordToFinderHashMap.get(word) + ")\t" + getScore(word.length()) + "\n");
                }
            }
        }

        return endingEmbed.build();
    }

    public MessageEmbed endingEmbedBuild(AnagramSetting opponentAnagramSetting) {
        if(gameMode.equals("comp")) {
            endingEmbed.setTitle("**Competition is over!**");
            String embedDescription = "Letters for this anagram: **";
            resetCorrectCounter();

            for (int i = 0; i < anagramLetters.length(); ++i) {
                embedDescription += (anagramLetters.charAt(i) + " ");
            }
            embedDescription += "**\n\n<@" + memberId + "> Your final score was: **" + getFinalScore() + "**\n";

            Iterator<String> it = getUsedWords().iterator();
            if (!it.hasNext()) {
                embedDescription += "No words found!\n";
            }
            else {
                embedDescription += "You found " + getUsedWords().size() + " words: \n";
                while (it.hasNext()) {
                    String word = it.next();
                    embedDescription += (word + "\t" + getScore(word.length()) + "\n");
                }
            }

            embedDescription += "\n\n<@" + opponentAnagramSetting.getMemberId() + "> Your final score was: **" + opponentAnagramSetting.getFinalScore() + "**\n";

            Iterator<String> it2 = opponentAnagramSetting.getUsedWords().iterator();
            if (!it2.hasNext()) {
                embedDescription += "No words found!\n";
            }
            else {
                embedDescription += "You found " + opponentAnagramSetting.getUsedWords().size() + " words: \n";
                while (it2.hasNext()) {
                    String word = it2.next();
                    embedDescription += (word + "\t" + getScore(word.length()) + "\n");
                }
            }
            endingEmbed.setDescription(embedDescription);
        }

        return endingEmbed.build();
    }

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
        if(score >= 88888) {
            return ("***__ASIAN (88888+)__***");
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