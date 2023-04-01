//import org.bson.Document;
//
//public class MemberSetting {
//
//    public MemberSetting() {
//    }
//
//
//
//    public Document toDocument(long id) {
//        Document document = new Document();
//        document.append("_id", id);
//        document.append("alphabetCount", this.alphabetCount);
//        document.append("channel", this.channel);
//        document.append("countingPrefix", this.countingPrefix);
//        document.append("prefixRequired", this.prefixRequired);
//        document.append("prevCounterId", this.prevCounterId);
//        document.append("settingsPrefix", this.settingsPrefix);
//        document.append("trackCounting", this.trackCounting);
//        return document;
//    }
//
//    public static GuildSetting fromDocument(Document document) {
//        GuildSetting setting = new GuildSetting();
//        setting.setAlphabetCount(document.getLong("alphabetCount"));
//        setting.setChannel(document.getLong("channel"));
//        setting.setCountingPrefix(document.getString("countingPrefix"));
//        setting.setPrefixRequired(document.getBoolean("prefixRequired"));
//        setting.setPrevCounterId(document.getLong("prevCounterId"));
//        setting.setSettingsPrefix(document.getString("settingsPefix"));
//        setting.setTrackCounting(document.getBoolean("trackCounting"));
//        return setting;
//    }
//}