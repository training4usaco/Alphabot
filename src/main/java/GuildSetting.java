//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

import org.bson.Document;

public class GuildSetting {
    String settingsPrefix = "!";
    String countingPrefix = ",";
    boolean trackCounting = true;
    boolean prefixRequired = true;
    long alphabetCount = 0L;
    long prevCounterId = 0L;
    long channel;

    public GuildSetting() {
    }

    public long getChannel() {
        return this.channel;
    }

    public String getSettingsPrefix() {
        return this.settingsPrefix;
    }

    public String getCountingPrefix() {
        return this.countingPrefix;
    }

    public boolean isPrefixRequired() {
        return this.prefixRequired;
    }

    public long getAlphabetCount() {
        return this.alphabetCount;
    }

    public long getPrevCounterId() {
        return this.prevCounterId;
    }

    public boolean isTrackCounting() {
        return this.trackCounting;
    }

    public void setChannel(Long channel) {
        if (channel == null) {
            channel = -1L;
        }

        this.channel = channel;
    }

    public void setTrackCounting(Boolean track) {
        if (track == null) {
            track = true;
        }

        this.trackCounting = track;
    }

    public void setPrefixRequired(Boolean prefixRequired) {
        if (prefixRequired == null) {
            prefixRequired = true;
        }

        this.prefixRequired = prefixRequired;
    }

    public void setPrevCounterId(Long userId) {
        if (userId == null) {
            userId = -1L;
        }

        this.prevCounterId = userId;
    }

    public void setSettingsPrefix(String settingsPrefix) {
        if (settingsPrefix == null) {
            settingsPrefix = "a!";
        }

        this.settingsPrefix = settingsPrefix;
    }

    public void setCountingPrefix(String prefix) {
        if (prefix == null) {
            prefix = ",";
        }

        this.countingPrefix = prefix;
    }

    public void setAlphabetCount(Long alphabetCount) {
        if (alphabetCount == null) {
            alphabetCount = 0L;
        }

        this.alphabetCount = alphabetCount;
    }

    public void incAlphabetCount() {
        ++this.alphabetCount;
    }

    public void resetAlphabetCount() {
        this.alphabetCount = 0L;
    }

    public Document toDocument(long id) {
        Document document = new Document();
        document.append("_id", id);
        document.append("alphabetCount", this.alphabetCount);
        document.append("channel", this.channel);
        document.append("countingPrefix", this.countingPrefix);
        document.append("prefixRequired", this.prefixRequired);
        document.append("prevCounterId", this.prevCounterId);
        document.append("settingsPrefix", this.settingsPrefix);
        document.append("trackCounting", this.trackCounting);
        return document;
    }

    public static GuildSetting fromDocument(Document document) {
        GuildSetting setting = new GuildSetting();
        setting.setAlphabetCount(document.getLong("alphabetCount"));
        setting.setChannel(document.getLong("channel"));
        setting.setCountingPrefix(document.getString("countingPrefix"));
        setting.setPrefixRequired(document.getBoolean("prefixRequired"));
        setting.setPrevCounterId(document.getLong("prevCounterId"));
        setting.setSettingsPrefix(document.getString("settingsPefix"));
        setting.setTrackCounting(document.getBoolean("trackCounting"));
        return setting;
    }
}
