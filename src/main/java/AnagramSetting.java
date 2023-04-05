import java.awt.Color;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.ThreadChannel;

public class AnagramSetting {
    private static final int VALID = 1;
    private static final int INVALID = -1;
    private static final int USED = 0;
    int countTimer;
    int score;
    int messageCounter;
    int correctCounter;
    ThreadChannel threadChannel;
    MessageChannel gameChannel;
    String lastmessage;
    String gameMode;
    String anagramLetters;
    long hostId;
    EmbedBuilder eb;
    EmbedBuilder endingEmbed;
    boolean active;
    boolean gameOver;
    boolean gameAborted;
    int[] anagramFreqArray = new int[26];
    int[] messageFreqArray = new int[26];
    HashMap<String, String> wordToFinderHashMap = new HashMap();
    HashSet<String> usedWords = new HashSet();
    HashSet<Long> participants = new HashSet();
    int longestStreak = 0;

    public AnagramSetting(int startingTime, MessageChannel gameChannel, Long hostId, String anagramLetters, String gameMode) {
        this.countTimer = startingTime;
        this.gameChannel = gameChannel;
        this.hostId = hostId;
        this.anagramLetters = anagramLetters;
        this.gameMode = gameMode;

        for(int i = 0; i < anagramLetters.length(); ++i) {
            int var10002 = this.anagramFreqArray[anagramLetters.charAt(i) - 97]++;
        }

        this.score = 0;
        this.eb = new EmbedBuilder();
        this.endingEmbed = new EmbedBuilder();
        this.active = false;
        this.gameOver = false;
        this.gameAborted = false;
        this.messageCounter = 0;
        this.lastmessage = "";
        this.correctCounter = 0;
    }

    public void addParticipant(long participantId) {
        if (participantId != this.hostId) {
            this.participants.add(participantId);
        }

    }

    public void removeParticipant(long participantId) {
        this.participants.remove(participantId);
    }

    public void updateLongestStreak() {
        this.longestStreak = Math.max(this.longestStreak, this.getCorrectCounter());
    }

    public void setCountTimer(int count) {
        this.countTimer = count;
    }

    public void updateScore(int delta) {
        this.score += this.getScore(delta);
    }

    public void setActive(Boolean active) {
        this.active = active;
    }

    public void setGameOver(Boolean gameOver) {
        this.gameOver = gameOver;
    }

    public void setLastMessage(String message) {
        this.lastmessage = "(+" + this.getScore(message.length()) + ")";
    }

    public void setGameAborted(Boolean gameAborted) {
        this.gameAborted = gameAborted;
    }

    public MessageEmbed ebBuild() {
        return this.eb.setColor(this.getColor()).setTitle("**Game is Running!** Find as many words as possible!").setDescription("**Letters are in the title of this thread.** \n**Letters: " + this.anagramLetters + "**\nYou can use setting prefix + quit to quit at anytime\nTimer: " + this.countTimer + "\tScore: " + this.score + " " + this.lastmessage + "\n").build();
    }

    public String getGameMode() {
        return this.gameMode;
    }

    public ThreadChannel getThreadChannel() {
        return this.threadChannel;
    }

    public MessageChannel getGameChannel() {
        return this.gameChannel;
    }

    public void setThreadChannel(ThreadChannel threadChannel) {
        this.threadChannel = threadChannel;
    }

    public int getMessageCounter() {
        return this.messageCounter;
    }

    public void decrementCountTimer() {
        --this.countTimer;
    }

    public void incMessageCounter() {
        ++this.messageCounter;
    }

    public void incCorrectCounter() {
        ++this.correctCounter;
    }

    public void resetMessageCounter() {
        this.messageCounter = 0;
    }

    public void resetCorrectCounter() {
        this.correctCounter = 0;
    }

    public void addWord(String word, String participantName) {
        this.wordToFinderHashMap.put(word, participantName);
    }

    public int getCountTimer() {
        return this.countTimer;
    }

    public int getFinalScore() {
        return this.score;
    }

    public long getHostId() {
        return this.hostId;
    }

    public boolean isActive() {
        return this.active;
    }

    public boolean isGameAborted() {
        return this.gameMode.equals("solo") && this.gameAborted;
    }

    public boolean isParticipant(long participantId) {
        return participantId != this.hostId && this.participants.contains(participantId);
    }

    public int getLongestStreak() {
        return this.longestStreak;
    }

    public int getScore(Integer len) {
        int streakBonus = 0;
        if (this.correctCounter >= 10) {
            streakBonus = this.correctCounter * 50;
        } else if (this.correctCounter >= 5) {
            streakBonus = 150;
        } else if (this.correctCounter >= 3) {
            streakBonus = 50;
        }

        return this.roundToHundreds(Main.SCORE_A * (double)len * (double)len + Main.SCORE_B * (double)len + Main.SCORE_C) + streakBonus;
    }

    public HashSet<Long> getParticipants() {
        return this.participants;
    }

    public boolean isGameOver() {
        return this.gameOver;
    }

    public String getAnagramLetters() {
        return this.anagramLetters;
    }

    public int getCorrectCounter() {
        return this.correctCounter;
    }

    public int isValid(String message) {
        int i;
        for(i = 0; i < 26; ++i) {
            this.messageFreqArray[i] = 0;
        }

        if (message.length() <= 2) {
            return -1;
        } else if (this.usedWords.contains(message)) {
            return 0;
        } else {
            for(i = 0; i < message.length(); ++i) {
                int var10002 = this.messageFreqArray[message.charAt(i) - 97]++;
                if (this.messageFreqArray[message.charAt(i) - 97] > this.anagramFreqArray[message.charAt(i) - 97]) {
                    return -1;
                }
            }

            this.usedWords.add(message);
            return 1;
        }
    }

    private int roundToHundreds(double num) {
        return 100 * Math.round((float)((int)(num / 100.0)));
    }

    public HashSet<String> getUsedWords() {
        return this.usedWords;
    }

    public HashMap<String, String> getWordToFinderHashMap() {
        return this.wordToFinderHashMap;
    }

    public MessageEmbed endingEmbedBuild() {
        String embedDescription;
        int i;
        Iterator it;
        if (this.gameMode.equals("solo")) {
            this.endingEmbed.setTitle("**Game is over!**");
            this.endingEmbed.setColor(this.getColor());
            this.resetCorrectCounter();
            embedDescription = "<@" + this.hostId + "> Your final score was: **" + this.getFinalScore() + "**\nYour rank is: " + this.getRank() + "\n\nLetters for this anagram: ";

            for(i = 0; i < this.anagramLetters.length(); ++i) {
                embedDescription = embedDescription + this.anagramLetters.charAt(i) + " ";
            }

            embedDescription = embedDescription + "\nYour longest streak was " + this.getLongestStreak() + " words!\n\n";
            it = this.getUsedWords().iterator();
            String word;
            if (!it.hasNext()) {
                embedDescription = embedDescription + "No words found!\n";
            } else {
                for(embedDescription = embedDescription + "You found " + this.getUsedWords().size() + " words: \n"; it.hasNext(); embedDescription = embedDescription + word + "\t" + this.getScore(word.length()) + "\n") {
                    word = (String)it.next();
                }
            }

            this.endingEmbed.setDescription(embedDescription).setFooter("Congrats!");
        } else if (this.gameMode.equals("coop")) {
            this.endingEmbed.setTitle("**Coop game is over!**");
            this.resetCorrectCounter();
            embedDescription = "Your score was: **" + this.getFinalScore() + "**\nLetters for this anagram:";

            for(i = 0; i < this.anagramLetters.length(); ++i) {
                embedDescription = embedDescription + this.anagramLetters.charAt(i) + " ";
            }

            embedDescription = embedDescription + "\nParticipants in this anagram: <@" + this.hostId + ">";

            long participantId;
            for(it = this.participants.iterator(); it.hasNext(); embedDescription = embedDescription + "<@" + participantId + ">") {
                participantId = (Long)it.next();
            }

            embedDescription = embedDescription + "\n\n";
            Iterator<String> it1 = this.getUsedWords().iterator();
            String word;
            if (!it1.hasNext()) {
                embedDescription = embedDescription + "No words found!\n";
            } else {
                for(embedDescription = embedDescription + "You found " + this.getUsedWords().size() + " words: \n"; it1.hasNext(); embedDescription = embedDescription + word + " (" + (String)this.wordToFinderHashMap.get(word) + ")\t" + this.getScore(word.length()) + "\n") {
                    word = (String)it1.next();
                }
            }

            this.endingEmbed.setColor(this.getColor());
            this.endingEmbed.setDescription(embedDescription);
        }

        return this.endingEmbed.build();
    }

    public MessageEmbed endingEmbedBuild(AnagramSetting opponentAnagramSetting) {
        if (this.gameMode.equals("comp")) {
            this.endingEmbed.setTitle("**Competition is over!**");
            String embedDescription = "Letters for this anagram: **";
            this.resetCorrectCounter();

            for(int i = 0; i < this.anagramLetters.length(); ++i) {
                embedDescription = embedDescription + this.anagramLetters.charAt(i) + " ";
            }

            embedDescription = embedDescription + "**\n\n<@" + this.hostId + "> Your final score was: **" + this.getFinalScore() + "**\n";
            Iterator<String> it = this.getUsedWords().iterator();
            String word;
            if (!it.hasNext()) {
                embedDescription = embedDescription + "No words found!\n";
            } else {
                for(embedDescription = embedDescription + "You found " + this.getUsedWords().size() + " words: \n"; it.hasNext(); embedDescription = embedDescription + word + "\t" + this.getScore(word.length()) + "\n") {
                    word = (String)it.next();
                }
            }

            embedDescription = embedDescription + "\n\n<@" + opponentAnagramSetting.getHostId() + "> Your final score was: **" + opponentAnagramSetting.getFinalScore() + "**\n";
            Iterator<String> it2 = opponentAnagramSetting.getUsedWords().iterator();
            if (!it2.hasNext()) {
                embedDescription = embedDescription + "No words found!\n";
            } else {
                for(embedDescription = embedDescription + "You found " + opponentAnagramSetting.getUsedWords().size() + " words: \n"; it2.hasNext(); embedDescription = embedDescription + word + "\t" + this.getScore(word.length()) + "\n") {
                    word = (String)it2.next();
                }
            }

            this.endingEmbed.setDescription(embedDescription);
        }

        return this.endingEmbed.build();
    }

    public Color getColor() {
        if (this.score >= 88888) {
            return new Color(255, 0, 0);
        } else if (this.score >= 25000) {
            return new Color(255, 255, 255);
        } else if (this.score >= 20000) {
            return new Color(254, 255, 45);
        } else if (this.score >= 17000) {
            return new Color(255, 77, 178);
        } else if (this.score >= 14000) {
            return new Color(233, 80, 255);
        } else if (this.score >= 11000) {
            return new Color(188, 84, 255);
        } else if (this.score >= 9000) {
            return new Color(183, 130, 255);
        } else if (this.score >= 7000) {
            return new Color(98, 90, 255);
        } else if (this.score >= 5000) {
            return new Color(86, 134, 255);
        } else if (this.score >= 3000) {
            return new Color(0, 218, 255);
        } else {
            return this.score >= 2000 ? new Color(9, 255, 123) : new Color(204, 255, 211);
        }
    }

    public String getRank() {
        if (this.score >= 88888) {
            return "***__ASIAN (88888+)__***";
        } else if (this.score >= 25000) {
            return "***__GOD (25000+)__***";
        } else if (this.score >= 20000) {
            return "***LEGENDARY (20000-24999)***";
        } else if (this.score >= 17000) {
            return "***Mythical (17000-19999)***";
        } else if (this.score >= 14000) {
            return "***Guru (14000-16999)***";
        } else if (this.score >= 11000) {
            return "**Master (11000-13999)**";
        } else if (this.score >= 9000) {
            return "**Advanced (9000-10999)**";
        } else if (this.score >= 7000) {
            return "Expert (7000-8999)";
        } else if (this.score >= 5000) {
            return "Seasoned (5000-6999)";
        } else if (this.score >= 3000) {
            return "Accomplished (3000-4999)";
        } else {
            return this.score >= 2000 ? "Experienced (2000-2999)" : "Apprentice (<2000)";
        }
    }
}