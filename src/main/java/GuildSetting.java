import org.bson.Document;

public class GuildSetting {
    String prefix, countingPrefix;
    boolean allChannels, autoJoin, trackCounting;
    long alphabetCount;
    long prevCounterId;
    long channel;
    public GuildSetting() {
        autoJoin = true;
        prefix = "!";
        countingPrefix = ",";
        allChannels = true;
        alphabetCount = 0;
        prevCounterId = 0;
        trackCounting = true;
    }
    public boolean isAllChannels() { return this.allChannels; }
    public long getChannel() {
        return this.channel;
    }
    public boolean isAutoJoin() { return this.autoJoin; }

    public String getPrefix() { return this.prefix; }
    public String getCountingPrefix() { return this.countingPrefix; }

    public long getAlphabetCount() { return this.alphabetCount; }
    public long getPrevCounterId() { return this.prevCounterId; }
    public boolean isTrackCounting() { return this.trackCounting; }

    public void setAllChannels(boolean all) {
        allChannels = all;
        if(all) {
            channel = 0;
            autoJoin = false;
        }
    }
    public void setChannel(long channel) {
        allChannels = false;
        this.channel = channel;
    }
    public void setAutoJoin(boolean auto) {
        this.autoJoin = auto;
        if(auto) allChannels = false;
    }
    public void setTrackCounting(boolean track) { this.trackCounting = track; }
    public void setPrevCounterId(long userId) { this.prevCounterId = userId; }
    public void setPrefix(String prefix) { this.prefix = prefix; }
    public void setCountingPrefix(String prefix) { this.countingPrefix = prefix; }
    public void incAlphabetCount() { ++this.alphabetCount; }
    public void resetAlphabetCount() { this.alphabetCount = 0; }
    public Document toDocument(long id) {
        Document document = new Document();
        document.append("_id", id);
        document.append("allChannels", allChannels);
        document.append("channel", channel);
        document.append("autoJoin", autoJoin);
        document.append("prefix", prefix);
        return document;
    }
    public static GuildSetting fromDocument(Document document){
        GuildSetting setting = new GuildSetting();
        setting.setAllChannels(document.getBoolean("allChannels"));
        setting.setChannel(document.getLong("channel"));
        setting.setAutoJoin(document.getBoolean("autoJoin"));
        setting.setPrefix(document.getString("prefix"));
        return setting;
    }
}