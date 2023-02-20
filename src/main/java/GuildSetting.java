import org.bson.Document;

public class GuildSetting {
    String settingsPrefix, countingPrefix;
    boolean trackCounting;
    long alphabetCount;
    long prevCounterId;
    long channel;
    public GuildSetting() {
        settingsPrefix = "!";
        countingPrefix = ",";
        alphabetCount = 0;
        prevCounterId = 0;
        trackCounting = true;
    }

    public long getChannel() {
        return this.channel;
    }
    public String getSettingsPrefix() { return this.settingsPrefix; }
    public String getCountingPrefix() { return this.countingPrefix; }

    public long getAlphabetCount() { return this.alphabetCount; }
    public long getPrevCounterId() { return this.prevCounterId; }
    public boolean isTrackCounting() { return this.trackCounting; }

    public void setChannel(Long channel) {
        if(channel == null) {
            channel = -1L;
        }
        this.channel = channel;
    }
    public void setTrackCounting(Boolean track) {
        if(track == null) {
            track = true;
        }
        this.trackCounting = track;
    }
    public void setPrevCounterId(Long userId) {
        if(userId == null) {
            userId = -1L;
        }
        prevCounterId = userId;
    }
    public void setSettingsPrefix(String settingsPrefix) {
        if(settingsPrefix == null) {
            settingsPrefix = "a!";
        }
        this.settingsPrefix = settingsPrefix;
    }
    public void setCountingPrefix(String prefix) {
        if(prefix == null) {
            prefix = ",";
        }
        this.countingPrefix = prefix;
    }
    public void setAlphabetCount(Long alphabetCount) {
        if(alphabetCount == null) {
            alphabetCount = 0L;
        }
        this.alphabetCount = alphabetCount;
    }
    public void incAlphabetCount() { ++this.alphabetCount; }
    public void resetAlphabetCount() { this.alphabetCount = 0; }
    public Document toDocument(long id) {
        Document document = new Document();
        document.append("_id", id);
        document.append("alphabetCount", alphabetCount);
        document.append("channel", channel);
        document.append("countingPrefix", countingPrefix);
        document.append("prevCounterId", prevCounterId);
        document.append("settingsPrefix", settingsPrefix);
        document.append("trackCounting", trackCounting);
        return document;
    }
    public static GuildSetting fromDocument(Document document){
        GuildSetting setting = new GuildSetting();
        setting.setAlphabetCount(document.getLong("alphabetCount"));
        setting.setChannel(document.getLong("channel"));
        setting.setCountingPrefix(document.getString("countingPrefix"));
        setting.setPrevCounterId(document.getLong("prevCounterId"));
        setting.setSettingsPrefix(document.getString("settingsPefix"));
        setting.setTrackCounting(document.getBoolean("trackCounting"));
        return setting;
    }
}