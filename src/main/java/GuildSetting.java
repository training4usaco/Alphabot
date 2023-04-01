import org.bson.Document;

public class GuildSetting {
    String settingsPrefix = "!";
    String countingPrefix = ",";
    boolean trackCounting = true;
    boolean prefixRequired = true;
    long alphabetCount = 0L;
    long prevCounterId = 0L;
    long prevPiCounterId = 0L;
    long piCount = -1L;
    long channel;
    long piChannel;

    public GuildSetting() {
    }

    public long getChannel() {
        return this.channel;
    }
    public long getPiChannel() {
        return this.piChannel;
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
    public long getPiCount() { return this.piCount; }

    public long getAlphabetCount() {
        return this.alphabetCount;
    }

    public long getPrevCounterId() {
        return this.prevCounterId;
    }
    public long getPrevPiCounterId() {
        return this.prevPiCounterId;
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

    public void setPiChannel(Long channel) {
        if(channel == null) {
            channel = -1L;
        }

        this.piChannel = channel;
    }

    public void setPiCount(Long piCount) {
        if (piCount == null) {
            piCount = 0L;
        }

        this.piCount = piCount;
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

    public void setPrevPiCounterId(Long userId) {
        if (userId == null) {
            userId = -1L;
        }

        this.prevPiCounterId = userId;
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
    public void incPiCount() {
        ++this.piCount;
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
        document.append("piCount", this.piCount);
        document.append("piChannel", this.piChannel);
        document.append("prevPiCounterId", this.prevPiCounterId);
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
        setting.setPiCount(document.getLong("piCount"));
        setting.setPiChannel(document.getLong("piChannel"));
        setting.setPrevPiCounterId(document.getLong("prevPiCounterId"));
        return setting;
    }
}