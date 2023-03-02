import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import com.mongodb.ConnectionString;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.model.ReplaceOptions;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.events.guild.GuildJoinEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import org.bson.Document;
import org.slf4j.LoggerFactory;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.*;

import static com.mongodb.client.model.Filters.eq;

public class Main extends ListenerAdapter {
    public static final int GOD_SCORE = 25000;
    public static final int LEGENDARY_SCORE = 20000;
    public static final int MYTHICAL_SCORE = 17000;
    public static final int GURU_SCORE = 14000;
    public static final int MASTER_SCORE = 11000;
    public static final int ADVANCED_SCORE = 9000;
    public static final int EXPERT_SCORE = 7000;
    public static final int SEASONED_SCORE = 5000;
    public static final int ACCOMPLISHED_SCORE = 3000;
    public static final int EXPERIENCED_SCORE = 2000;
    public static final double SCORE_A = 71.4286;
    public static final double SCORE_B = -234.286;
    public static final double SCORE_C = 372.857;
    public static JDA jda;
    private static final long ADMIN = 524989303206707214L;
    public static final String TEXT_CYAN = "\u001B[36m";
    public static final String TEXT_RESET = "\u001B[0m";
    public static final int GAME_TOTAL_TIME = 60;
    public static final int GAME_LOAD_TIME = 10;
    public static final int GAME_CLOSE_TIME = 5;
    public static final int NUM_LETTERS = 15;
    public static final int NUM_VOWELS = 5;
    public static final int MESSAGE_RESET_LIMIT = 5;
    private static final int VALID = 1;
    private static final int INVALID = -1;
    private static final int USED = 0;
    private static MongoCollection<Document> guilds;
    private final HashMap<Long, GuildSetting> guildSettingHashMap;
    private final HashMap<String, AnagramSetting> anagramHashMap = new HashMap<>();
    private final HashSet<Long> madeRequestsUsers = new HashSet<>();
    private final HashSet<Long> receivedRequestsUsers = new HashSet<>();
    private static final HashSet<String> Words = new HashSet<>();
    private final ReplaceOptions options = new ReplaceOptions().upsert(true);
    private final static String[] ALPHABET = new String[]{"a", "b", "c", "d", "e", "f", "g", "h", "i", "j", "k", "l", "m", "n", "o", "p", "q", "r", "s", "t", "u", "v", "w", "x", "y", "z"};
    private final static String[] VOWELS = new String[]{"a", "a", "e", "e", "i", "i", "o", "o", "u"};
    private final static String[] CONSONANTS = new String[]{"b", "c", "d", "d", "f", "g", "h", "h", "h", "j", "k", "l", "l", "m", "n", "n", "n", "p", "q", "r", "r", "s", "s", "t", "t", "v", "w", "x", "y", "z"};
    public static void main(String[] args) throws IOException {
        Scanner config = new Scanner(new File("Config.txt"));
        try {
            jda = JDABuilder.createDefault(config.nextLine()).build();
            System.out.println("Logged in as " + jda.getSelfUser().getName() + "#" + jda.getSelfUser().getDiscriminator());
        } catch (Exception e) {
            System.out.println("Login failed");
            e.printStackTrace();
        }
        Scanner wordScanner = new Scanner(new File("words_alpha.txt"));
        while (wordScanner.hasNextLine()) {
            String word = wordScanner.nextLine();
            Words.add(word);
        }
        jda.addEventListener(new Main(new ConnectionString(config.nextLine())));
        System.err.println(TEXT_CYAN + "Logged in as " + jda.getSelfUser().getName() + "#" + jda.getSelfUser().getDiscriminator() + TEXT_RESET);
        System.err.println(TEXT_CYAN + "jvm version: " + System.getProperty("java.version") + TEXT_RESET);
    }

    private Main(ConnectionString string) {
        LoggerFactory.getLogger("org.mongodb");
        LoggerContext loggerContext = (LoggerContext) LoggerFactory.getILoggerFactory();
        for(Logger logger : loggerContext.getLoggerList()) {
//			System.err.println(logger.getName());
            if(logger.getName().startsWith("com.mongodb") || logger.getName().startsWith("org.mongodb") || logger.getName().startsWith("net.dv8tion")) {
                logger.setLevel(Level.WARN);
            }
        }

        guildSettingHashMap = new HashMap<>();

        MongoClient client = MongoClients.create(string);
        guilds = client.getDatabase("alphabot_database").getCollection("guilds");

        System.err.println(TEXT_CYAN + guilds.countDocuments());

        loadSettings();
    }

    @Override
    public void onMessageReceived(MessageReceivedEvent event) {
        if (event.getAuthor().isBot()) return;

        long guildId = event.getGuild().getIdLong();

        String message = event.getMessage().getContentRaw();
        GuildSetting currentGuildSetting = guildSettingHashMap.get(guildId);
        MessageChannel channel = event.getChannel();

        System.err.println(TEXT_CYAN + "Message received: " + message + " in channel " + channel.getName() + " in guild " + event.getGuild().getName() + " from " + event.getMember().getNickname() + TEXT_RESET);

        if (currentGuildSetting == null) {
            channel.sendMessage("please set up guild settings").queue();
            return;
        }

        if(event.getChannelType().equals(ChannelType.GUILD_PUBLIC_THREAD)) {
            String channelKey = event.getThreadChannel().getName();
            channelKey = channelKey.substring(0, 11 + String.valueOf(event.getMember().getIdLong()).length());

            if(anagramHashMap.containsKey(channelKey)) {
                ThreadChannel thread = event.getThreadChannel();
                AnagramSetting currAnagramSetting = anagramHashMap.get(channelKey);

                if (currAnagramSetting.isGameOver()) {
                    currAnagramSetting.resetCorrectCounter();
                    return;
                }
                if (currAnagramSetting.getMemberId() != event.getAuthor().getIdLong() && !currAnagramSetting.isParticipant(event.getAuthor().getIdLong())) {
                    event.getThreadChannel().deleteMessageById(thread.getLatestMessageId()).queue();
                    return;
                }

                if(message.startsWith(currentGuildSetting.getSettingsPrefix())) {
                    message = message.substring(currentGuildSetting.getSettingsPrefix().length());
                    if(message.startsWith("quit")) {
                        currAnagramSetting.setGameAborted(true);
                        currAnagramSetting.setCountTimer(0);
                        return;
                    }
                }

                if (!currAnagramSetting.isActive()) {
                    thread.sendMessage("Please wait for the game to start!").queue();
                    return;
                }

                currAnagramSetting.incMessageCounter();
//                System.err.println("message counter: " + currAnagramSetting.getMessageCounter());
                if(currAnagramSetting.getMessageCounter() == MESSAGE_RESET_LIMIT) {
                    EmbedBuilder reminder = new EmbedBuilder();
                    reminder.setTitle("Letters reminder!");
                    String anagramLetters = currAnagramSetting.getAnagramLetters();
                    String description = "Letters: ";

                    for(int i = 0; i < anagramLetters.length(); ++i) {
                        description += (anagramLetters.charAt(i) + " ");
                    }
                    reminder.setColor(currAnagramSetting.getColor());
                    thread.sendMessageEmbeds(reminder.setDescription(description).build()).queue();
                    currAnagramSetting.resetMessageCounter();
                }
//                currAnagram.setLastMessage(message);
                if(!Words.contains(message)) {
                    currAnagramSetting.resetCorrectCounter();
                    event.getMessage().addReaction("U+274C").queue();
                    return;
                }

                int validity = currAnagramSetting.isValid(message.toLowerCase());
                if(validity == INVALID) {
                    currAnagramSetting.resetCorrectCounter();
                    event.getMessage().addReaction("U+274C").queue();
                }
                else if(validity == VALID) {
                    currAnagramSetting.incCorrectCounter();
                    currAnagramSetting.updateScore(message.length());
                    currAnagramSetting.updateLongestStreak();
                    currAnagramSetting.setLastMessage(message);
                    currAnagramSetting.addWord(message, event.getAuthor().getName());
                    int streak = currAnagramSetting.getCorrectCounter();
                    if(streak >= 10) {
                        event.getMessage().addReaction("U+1F4AB").queue();
                    }
                    else if(streak >= 5) {
                        event.getMessage().addReaction("U+1F31F").queue();
                    }
                    else if(streak >= 3) {
                        event.getMessage().addReaction("U+2B50").queue();
                    }
                    else {
                        event.getMessage().addReaction("U+2705").queue();
                    }
                }
                else if(validity == USED) {
                    event.getMessage().addReaction("U+1F7E1").queue();
                }

//                event.getChannel().deleteMessageById(thread.getLatestMessageId()).queue();
            }
        }

        if (message.startsWith(currentGuildSetting.getSettingsPrefix())) {
            message = message.substring(currentGuildSetting.getSettingsPrefix().length());
            if (message.startsWith("end")) {
                channel.sendMessage("Counting will now be disabled").queue();
                currentGuildSetting.setTrackCounting(false);
            }
            else if (message.startsWith("getChannel")) {
                String channelName = "";

                List<GuildChannel> channels = event.getGuild().getChannels();
                for (GuildChannel currchannel : channels) {
                    if (Long.toString(currchannel.getIdLong()).equals(message)) {
                        channelName = currchannel.getName();
                        break;
                    }
                }
                channel.sendMessage("Current counting channel is <#" + channelName + ">").queue();
            }
            else if (message.startsWith("getCountingPrefix")) {
                channel.sendMessage("Current counting prefix is " + currentGuildSetting.getCountingPrefix()).queue();
            }
            else if (message.startsWith("getPrefix")) {
                channel.sendMessage("Current prefix is " + currentGuildSetting.getSettingsPrefix()).queue();
            }
            else if (message.startsWith("setChannel ")) {
                message = message.substring(11);

                System.err.println(TEXT_CYAN + "Setting channel: " + message);
                boolean channelFound = false;
                String channelName = "";

                List<GuildChannel> channels = event.getGuild().getChannels();
                for (GuildChannel currchannel : channels) {
                    if (Long.toString(currchannel.getIdLong()).equals(message)) {
                        channelFound = true;
                        channelName = currchannel.getName();
                        break;
                    }
                }

                if (!channelFound) {
                    channel.sendMessage("Please set to valid channel").queue();
                } else {
                    channel.sendMessage("Set alphabet counting channel to <#" + channelName + ">").queue();
                    currentGuildSetting.setChannel(Long.parseLong(message));
                }
            }
            else if (message.startsWith("setCountingPrefix ")) {
                channel.sendMessage("Setting counting prefix to " + message.substring(18)).queue();
                currentGuildSetting.setCountingPrefix(message.substring(18));
            }
            else if (message.startsWith("setPrefix ")) {
                channel.sendMessage("Setting prefix to " + message.substring(10)).queue();
                currentGuildSetting.setSettingsPrefix(message.substring(10));
            }
            else if (message.startsWith("setPrefReq ")) {
                if(message.substring(11).equalsIgnoreCase("on")) {
                    channel.sendMessage("Counting prefix is now required to count").queue();
                    currentGuildSetting.setPrefixRequired(true);
                }
                else {
                    channel.sendMessage("Prefix is no longer required to count. **Be careful!**").queue();
                    currentGuildSetting.setPrefixRequired(false);
                }
            }
            else if (message.startsWith("start")) {
                channel.sendMessage("Counting will now be enabled").queue();
                currentGuildSetting.setTrackCounting(true);
            }
        }
        else {
            if (channel.getIdLong() == currentGuildSetting.getChannel()) {
                if(message.startsWith(currentGuildSetting.getCountingPrefix()) || !guildSettingHashMap.get(guildId).isPrefixRequired()) {
                    if (currentGuildSetting.isTrackCounting()) {
                        if (guildSettingHashMap.get(guildId).isPrefixRequired()) {
                            message = message.substring(currentGuildSetting.getCountingPrefix().length());
                        }
                        message = message.split(" ", 2)[0];

                        if (event.getMember().getIdLong() != ADMIN && event.getMember().getIdLong() == currentGuildSetting.getPrevCounterId()) {
                            event.getMessage().addReaction("U+274C").queue();
                            resetCount(channel, currentGuildSetting, event.getMember().getIdLong(), "You can't count twice in a row! Let your friends count too... if you have any.");
                            guilds.replaceOne(eq("_id", event.getGuild().getIdLong()), currentGuildSetting.toDocument(event.getGuild().getIdLong()), options);
                            return;
                        }

                        if (event.getMember().getIdLong() != ADMIN && hasNumber(message)) {
                            event.getMessage().addReaction("U+274C").queue();
                            resetCount(channel, currentGuildSetting, event.getMember().getIdLong(), "Incorrect count!");
                            guilds.replaceOne(eq("_id", event.getGuild().getIdLong()), currentGuildSetting.toDocument(event.getGuild().getIdLong()), options);
                            return;
                        }

                        if(getStringFromValue(currentGuildSetting.getAlphabetCount()).endsWith("z")) {
//                            System.err.println("incing alphacount");
                            currentGuildSetting.incAlphabetCount();
                        }

                        if (!message.equals(getStringFromValue(currentGuildSetting.getAlphabetCount() + 1L))) {
                            if(event.getMember().getIdLong() != ADMIN) {
                                event.getMessage().addReaction("U+274C").queue();
                                resetCount(channel, currentGuildSetting, event.getMember().getIdLong(), "Incorrect count!");
                                guilds.replaceOne(eq("_id", event.getGuild().getIdLong()), currentGuildSetting.toDocument(event.getGuild().getIdLong()), options);
                            }
                        }
                        else {
                            currentGuildSetting.setPrevCounterId(event.getMember().getIdLong());
                            event.getMessage().addReaction("U+2705").queue();
                            currentGuildSetting.incAlphabetCount();
                            guilds.replaceOne(eq("_id", event.getGuild().getIdLong()), currentGuildSetting.toDocument(event.getGuild().getIdLong()), options);
                        }
                    } else {
                        channel.sendMessage("Counting is currently disabled").queue();
                    }
                }
            }
        }

        guilds.replaceOne(eq("_id", event.getGuild().getIdLong()), currentGuildSetting.toDocument(event.getGuild().getIdLong()), options);
        super.onMessageReceived(event);
    }

    @Override
    public void onGuildJoin(GuildJoinEvent event) {
        EmbedBuilder eb = new EmbedBuilder();
        eb.setTitle("Welcome to Alphabot!");
        eb.appendDescription("Hello! Someone invited me to your server! \n"
                + "This bot will help you learn your abcs in no time!\n\n"
                + "idk random stuff still wip leave me alone :C. \n");
        event.getGuild().getSystemChannel().sendMessageEmbeds(eb.build()).queue();
        GuildSetting gSetting = new GuildSetting();

        guildSettingHashMap.put(event.getGuild().getIdLong(), gSetting);
        guilds.replaceOne(eq("_id", event.getGuild().getIdLong()), gSetting.toDocument(event.getGuild().getIdLong()), options);

        loadSettings();
        super.onGuildJoin(event);
    }

    @Override
    public void onSlashCommandInteraction(SlashCommandInteractionEvent event) {
        event.deferReply().queue();

        if(event.getName().equals("slashtest")) {
            Long value = event.getOption("value").getAsLong();
            event.getHook().sendMessage(getStringFromValue(value)).queue();
        }
        else if(event.getName().equals("abchelp")) {
            OptionMapping page = event.getOption("page");

            if(page == null || page.getAsInt() == 1) {
                EmbedBuilder eb = new EmbedBuilder().setTitle("ALPHABOT HELP").setDescription("**Use `/abcsettings` to see current Alphabot settings and prefixes**\n\n"
                        + "Thank you for using **Alphabot**!\n"
                        + "This bot was made to help you learn your abc's and a few word minigames!\n\n"
                        + "**General Help**\n"
                        + "For counting make sure you set a counting channel! (see page 2 for more info)\n"
                        + "Checkout page 3-4 for more info on Anagram and how to play!\n\n"
                        + "**Enjoy!**");
                eb.setFooter("Page 1").setColor(new Color(232,29,99));

                event.getHook().sendMessageEmbeds(eb.build()).queue();
            }
            else if(page.getAsInt() == 2) {
                EmbedBuilder eb = new EmbedBuilder().setTitle("COUNTING HELP").setDescription("**Note that commands without slash use the prefix**\n\n"
                        + "**How to Count**\n"
                        + "Just your classic abc's...\n"
                        + "Until you reach z, after which it will reset back to aa which is followed by the normal abc's with an extra a in front.\n"
                        + "Once you reach az, the a will be incremented so the next number will be ba, and so on.\n"
                        + "Eventually, once you get to zz it will reset back to aaa just like normal counting.\n\n"
                        + "**Counting Rules**\n"
                        + "You can't count twice in a row\n\n"
                        + "**Setup Commands**\n"
                        + "◈ `setChannel` sets the channel that counting will be tracked in\n"
                        + "◈ `setCountingPrefix` sets the prefix for counting\n"
                        + "◈ `setPrefix` sets the prefix for bot commands\n\n"
                        + "**Counting Commands**\n"
                        + "◈ `start` - enables counting\n"
                        + "◈ `end` - disables counting\n"
                        + "◈ `/abcPrefReq on/off` - sets whether or not prefix is required to count\n");
                eb.setFooter("Page 2").setColor(new Color(248,162,6));

                event.getHook().sendMessageEmbeds(eb.build()).queue();
            }
            else if(page.getAsInt() == 3) {
                EmbedBuilder eb = new EmbedBuilder().setTitle("ANAGRAM HELP").setDescription("**Scoring and Rank system is on the next page**\n\n"
                        + "**How to Play**\n"
                        + "You will have 60 seconds to try and form as many words as possible using the letters provided the title of the thread.\n"
                        + "Every 5 messages the bot will resend the letters.\n\n"
                        + "**Commands (with settings prefix)**\n"
                        + "◈ `anagram` sets the channel that counting will be tracked in\n"
                        + "◈ `quit` type in the thread of the game to quit the game\n\n"
                        + "**Reactions**\n"
                        + "✅ - found an anagram\n"
                        + "❌ - not an anagram (doesn't use right letters or isn't a word and resets your streak)\n"
                        + ":yellow_circle: - word already found (does not break your streak)\n"
                        + "⭐ - streak of 3 or more\n"
                        + ":star2: - streak of 5 or more\n"
                        + ":dizzy: - streak of 10 or more\n");
                eb.setFooter("Page 3").setColor(new Color(248,201,91));

                event.getHook().sendMessageEmbeds(eb.build()).queue();
            }
            else if(page.getAsInt() == 4) {
                EmbedBuilder eb = new EmbedBuilder().setTitle("ANAGRAM SCORING").setDescription("**Scoring system can be seen while playing the game**\n\n"
                        + "◈ **How to gain points**\n"
                        + "◈ Scoring will be based on length of word and streak.\n"
                        + "◈ Correct words increase your streak, while incorrect words reset it (note used words doesn't affect your streak).\n\n"
                        + "◈ **Points System (Rest can be seen in game)**\n"
                        + "◈ Words of length <= 2 are not counted.\n"
                        + "◈ Words of length 3 are worth " + getRawScore(3) + " points\n"
                        + "◈ Words of length 4 are worth " + getRawScore(4) + " points\n"
                        + "◈ Words of length 5 are worth " + getRawScore(5) + " points\n"
                        + "◈ Words of length 6 are worth " + getRawScore(6) + " points\n"
                        + "◈ Words of length 7 are worth " + getRawScore(7) + " points\n"
                        + "◈ Words of length 8 are worth " + getRawScore(8) + " points\n\n"
                        + "**Streak System**\n"
                        + "◈ Streak of 3 adds an extra 50 points per message\n"
                        + "◈ Streak of 5 adds an extra 150 points per message\n"
                        + "◈ Streak of 10 adds an extra streak x 50 points per message\n\n"
                        + "**Ranks**\n"
                        + "◈ ***__GOD (" + Main.GOD_SCORE + "+)__***\n"
                        + "◈ ***LEGENDARY (" + Main.LEGENDARY_SCORE + "-" + (Main.GOD_SCORE - 1) +")***\n"
                        + "◈ ***Mythical (" + Main.MYTHICAL_SCORE + "-" + (Main.LEGENDARY_SCORE - 1) + ")***\n"
                        + "◈ ***Guru (" + Main.GURU_SCORE + "-" + (Main.MYTHICAL_SCORE - 1) + ")***\n"
                        + "◈ **Master (" + Main.MASTER_SCORE + "-" + (Main.GURU_SCORE - 1) + ")**\n"
                        + "◈ **Advanced (" + Main.ADVANCED_SCORE + "-" + (Main.MASTER_SCORE - 1) + ")**\n"
                        + "◈ Expert (" + Main.EXPERT_SCORE + "-" + (Main.ADVANCED_SCORE - 1) + ")\n"
                        + "◈ Seasoned (" + Main.SEASONED_SCORE + "-" + (Main.EXPERT_SCORE - 1) + ")\n"
                        + "◈ Accomplished (" + Main.ACCOMPLISHED_SCORE + "-" + (Main.SEASONED_SCORE - 1) + ")\n"
                        + "◈ Experienced (" + Main.EXPERIENCED_SCORE + "-" + (Main.ACCOMPLISHED_SCORE - 1) + ")\n"
                        + "◈ Apprentice (<" + Main.EXPERIENCED_SCORE + ")\n");
                eb.setFooter("Page 4").setColor(new Color(47,204,113));

                event.getHook().sendMessageEmbeds(eb.build()).queue();
            }
            else {
                event.getHook().sendMessage("Please enter a valid page number").queue();
            }
        }
        else if(event.getName().equals("abcsettings")) {
            EmbedBuilder eb = new EmbedBuilder().setTitle("ALPHABOT SETTINGS");
            String currCount = getStringFromValue(guildSettingHashMap.get(event.getGuild().getIdLong()).getAlphabetCount());
            String isTracking;
            String prevCounterString;
            String prefReq;

            if(currCount.equals("")) {
                currCount = "N/A";
            }
            if(guildSettingHashMap.get(event.getGuild().getIdLong()).isTrackCounting()) {
                isTracking = "Yes";
            }
            else {
                isTracking = "No";
            }
            if(guildSettingHashMap.get(event.getGuild().getIdLong()).getPrevCounterId() == -1) {
                prevCounterString = "N/A";
            }
            else {
                prevCounterString = "<@" + guildSettingHashMap.get(event.getGuild().getIdLong()).getPrevCounterId() + ">";
            }
            if(guildSettingHashMap.get(event.getGuild().getIdLong()).isPrefixRequired()) {
                prefReq = "On";
            }
            else {
                prefReq = "Off - **Be Careful**";
            }

            eb.setDescription("**Current Count: " + currCount + "**\n\n"
//                    + "Debug, next val: " + getStringFromValue(guildSettingHashMap.get(event.getGuild().getIdLong()).getAlphabetCount() + 1) + "\n"
                    + "**Guild Settings**\n"
                    + "◈ Prefix Required: " + prefReq + "\n"
                    + "◈ Tracking counting: " + isTracking + "\n"
                    + "◈ Current counting prefix: `" + guildSettingHashMap.get(event.getGuild().getIdLong()).getCountingPrefix() + "`\n"
                    + "◈ Current setting prefix: `" + guildSettingHashMap.get(event.getGuild().getIdLong()).getSettingsPrefix() + "`\n"
                    + "◈ Counting Channel: " + "<#" + guildSettingHashMap.get(event.getGuild().getIdLong()).getChannel() + ">" + "\n"
                    + "◈ Previous Counter: " + prevCounterString + "\n");

            event.getHook().sendMessageEmbeds(eb.setColor(new Color(200,244,228)).build()).queue();
        }
        else if(event.getName().equals("abcprefreq")) {
            String setting = event.getOption("setting").getAsString();
            if(setting.equals("on")) {
                guildSettingHashMap.get(event.getGuild().getIdLong()).setPrefixRequired(true);
                event.getHook().sendMessage("Prefix is now required!").queue();
            }
            else {
                guildSettingHashMap.get(event.getGuild().getIdLong()).setPrefixRequired(false);
                event.getHook().sendMessage("Prefix is no longer required. **Be Careful!**").queue();
            }
        }
        else if(event.getName().equals("abcanagram")) {
            if(anagramHashMap.containsKey("<@" + event.getMember().getIdLong() + "> anagram")) {
                event.getHook().sendMessage("You already have a game running currently!").queue();
                return;
            }

            OptionMapping compOption = event.getOption("comp");
            OptionMapping coopOption = event.getOption("coop");

            if(compOption != null && coopOption != null) {
                event.getHook().sendMessage("Please choose one of the two options").queue();
                return;
            }

            if(compOption == null && coopOption == null) {
                String letters = "";
                String title = "<@" + event.getMember().getIdLong() + "> anagram; ";
                for (int i = 0; i < NUM_LETTERS; ++i) {
                    if (i < NUM_VOWELS) {
                        letters += VOWELS[(int) (Math.random() * VOWELS.length)];
                    } else {
                        letters += CONSONANTS[(int) (Math.random() * CONSONANTS.length)];
                    }
                }

                char tempArray[] = letters.toCharArray();
                Arrays.sort(tempArray);
                letters = new String(tempArray);

                for (int i = 0; i < NUM_LETTERS; ++i) {
                    title += (String.valueOf(letters.charAt(i)).toUpperCase() + " ");
                }

                final String threadTitle = title;
                final String anagramLetters = letters;
                event.getHook().sendMessage("Creating a new thread").queue(createThreadMessage -> {
                    ((TextChannel) event.getChannel()).createThreadChannel(threadTitle, createThreadMessage.getIdLong()).queue(threadChannel -> {
                        AnagramSetting currentAnagramSetting = new AnagramSetting(GAME_TOTAL_TIME, event.getChannel(), event.getMember().getIdLong(), anagramLetters, "solo");
                        AnagramSetting otherAnagramSetting = new AnagramSetting(0, event.getChannel(), event.getMember().getIdLong(), anagramLetters, "solo");
                        currentAnagramSetting.setThreadChannel(threadChannel);
                        createAnagramGame(currentAnagramSetting, otherAnagramSetting);
                    });
                });
            }
            else if(compOption != null) {
                if(madeRequestsUsers.contains(event.getMember().getIdLong())) {
                    event.getHook().sendMessage("Sorry, you currently have a pending request!").queue();
                    return;
                }
                else if(anagramHashMap.containsKey("<@" + event.getMember().getIdLong() + "> anagram")) {
                    event.getHook().sendMessage("Sorry, you have a game currently running!").queue();
                    return;
                }

                String uid = compOption.getAsString();

                if (uid.startsWith("<@") && uid.endsWith(">")) {
                    uid = uid.substring(2, uid.length() - 1);
                }

                for(int i = 0; i < uid.length(); ++i) {
                    if(!Character.isDigit(uid.charAt(i))) {
                        event.getHook().sendMessage("Please send a valid user ID.").queue();
                        return;
                    }
                }

                if(event.getGuild().retrieveMemberById(Long.valueOf(uid)) == null) {
                    event.getHook().sendMessage("No user found with that ID.").queue();
                    return;
                }

                if(Long.valueOf(uid) == event.getMember().getIdLong()) {
                    event.getHook().sendMessage("Sorry, you can't create a request with yourself!").queue();
                    return;
                }

                if(receivedRequestsUsers.contains(uid)) {
                    event.getHook().sendMessage("Sorry, they have a pending anagram request!").queue();
                    return;
                }
                else if(anagramHashMap.containsKey("<@" + uid + "> anagram")) {
                    event.getHook().sendMessage("Sorry, they have a game running currently.").queue();
                    return;
                }

                final Button confirmButton = Button.success("compConfirmButton " + event.getMember().getIdLong() + " " + uid, "Yes");
                final Button cancelButton = Button.danger("compCancelButton " + event.getMember().getIdLong() + " " + uid, "No");

                receivedRequestsUsers.add(Long.valueOf(uid));
                madeRequestsUsers.add(event.getMember().getIdLong());
                final Long recievedRequestUser = Long.valueOf(uid);
                EmbedBuilder challengeRequestEmbed = new EmbedBuilder();
                challengeRequestEmbed.setTitle("**Anagram Challenge Request**").setDescription("<@" + event.getMember().getIdLong() + "> wants to challenge you to a 1v1 game of anagram! Do you want to accept?");
                event.getHook().sendMessageEmbeds(challengeRequestEmbed.build()).setContent("<@" + Long.valueOf(uid) + ">")
                        .addActionRow(
                                confirmButton,
                                cancelButton)
                        .queue(message -> {
                            Timer buttonDisableTimer = new Timer();
                            buttonDisableTimer.schedule(new TimerTask() {
                                public void run() {
                                    if(!confirmButton.isDisabled()) {
                                        receivedRequestsUsers.remove(Long.valueOf(recievedRequestUser));
                                        madeRequestsUsers.remove(event.getMember().getIdLong());
                                        message.editMessageEmbeds(challengeRequestEmbed.build()).setActionRows(
                                                ActionRow.of(
                                                        confirmButton.withDisabled(true),
                                                        cancelButton.withDisabled(true))
                                        ).queue();
                                    }
                                }
                            }, 120 * 1000);
                        });

            }
            else if(coopOption != null) {
                String uid = coopOption.getAsString();

                if (uid.startsWith("<@") && uid.endsWith(">")) {
                    uid = uid.substring(2, uid.length() - 1);
                }

                for(int i = 0; i < uid.length(); ++i) {
                    if(!Character.isDigit(uid.charAt(i))) {
                        event.getHook().sendMessage("Please send a valid user ID.").queue();
                    }
                }
                event.getHook().sendMessage("Creating 2 threads for competitors").queue();
            }
        }
    }

    public void createAnagramGame(AnagramSetting currentAnagramSetting, AnagramSetting otherAnagramSetting) {
        Timer startingTimer = new java.util.Timer();
        Long participantUserId = currentAnagramSetting.getMemberId();
        ThreadChannel threadChannel = currentAnagramSetting.getThreadChannel();
        MessageChannel gameChannel = currentAnagramSetting.getGameChannel();
        String anagramLetters = currentAnagramSetting.getAnagramLetters();

        threadChannel.sendMessage("<@" + participantUserId + "> Your game thread has been created").queue();
        anagramHashMap.put("<@" + participantUserId + "> anagram", currentAnagramSetting);

        EmbedBuilder anagrameb = new EmbedBuilder();
        anagrameb.setTitle("Anagram Game");
        anagrameb.setDescription("Starting game in " + GAME_LOAD_TIME + " seconds.\nTry and form as many words as possible using the letters in the title of this thread!\n**Letters: " + anagramLetters + "**");
        anagrameb.setColor(new Color(220, 46, 68));
        threadChannel.sendMessageEmbeds(anagrameb.build()).queue((countdownEmbedMessage) -> {
            Long countdownMessageId = countdownEmbedMessage.getIdLong();
            System.err.println(countdownMessageId);
            startingTimer.scheduleAtFixedRate(
                    new java.util.TimerTask() {
                        Integer loadClock = GAME_LOAD_TIME - 1;

                        @Override
                        public void run() {
                            if (loadClock > 0) {
                                anagrameb.setDescription("Starting game in " + loadClock + " seconds.\nTry and form as many words as possible using the letters in the title of this thread!\n**Letters: " + anagramLetters + "**");
                                threadChannel.editMessageEmbedsById(countdownMessageId, anagrameb.build()).queue();
                                --loadClock;
                                if (anagramHashMap.get("<@" + participantUserId + "> anagram").isGameAborted()) {
                                    loadClock = 0;
                                }
                            }
                            else {
                                startingTimer.cancel();
                                Timer gameTimer = new java.util.Timer();
                                if (currentAnagramSetting.isGameAborted()) {
                                    EmbedBuilder eb = new EmbedBuilder();
                                    threadChannel.sendMessageEmbeds(eb.setTitle("**Game Aborted!**").build()).queue();
                                    currentAnagramSetting.setCountTimer(0);
                                }
                                else {
                                    threadChannel.editMessageEmbedsById(countdownMessageId, currentAnagramSetting.ebBuild()).queue();
                                    currentAnagramSetting.decrementCountTimer();
                                    currentAnagramSetting.setActive(true);
                                }

                                gameTimer.scheduleAtFixedRate(
                                        new java.util.TimerTask() {
                                            @Override
                                            public void run() {
                                                if (currentAnagramSetting.getCountTimer() > 0) {
                                                    if (currentAnagramSetting.getCountTimer() == GAME_TOTAL_TIME / 2) {
                                                        threadChannel.sendMessage("You have " + currentAnagramSetting.getCountTimer() + " seconds left!").queue();
                                                    }
                                                    if (currentAnagramSetting.getCountTimer() == GAME_TOTAL_TIME / 4) {
                                                        threadChannel.sendMessage("You have " + currentAnagramSetting.getCountTimer() + " seconds left!").queue();
                                                    }
                                                    if (currentAnagramSetting.getCountTimer() <= 5) {
                                                        threadChannel.sendMessage("You have " + currentAnagramSetting.getCountTimer() + " seconds left!").queue();
                                                    }
                                                    //                                                                threadChannel.deleteMessageById(countdownMessageId).queue();
                                                    //                                                                threadChannel.sendMessageEmbeds(currentAnagramSetting.ebBuild()).queue();
                                                    threadChannel.editMessageEmbedsById(countdownMessageId, currentAnagramSetting.ebBuild()).queue();
                                                    currentAnagramSetting.decrementCountTimer();
                                                }
                                                else {
                                                    gameTimer.cancel();
                                                    currentAnagramSetting.setActive(false);
                                                    currentAnagramSetting.setGameOver(true);
                                                    Timer endingTimer = new java.util.Timer();
                                                    EmbedBuilder endingEmbed = new EmbedBuilder();
                                                    EmbedBuilder closingEmbed = new EmbedBuilder();
                                                    closingEmbed.setTitle("**Game is over!**");
                                                    threadChannel.sendMessageEmbeds(closingEmbed.setDescription("Thread closing in " + GAME_CLOSE_TIME + " seconds.\nA summary of this game will be reported in <#" + gameChannel.getIdLong() + "> after this thread is closed\n").build()).queue(closingEmbedMessage -> {
                                                        Long closingThreadMessageId = closingEmbedMessage.getIdLong();
                                                        endingTimer.scheduleAtFixedRate(
                                                                new java.util.TimerTask() {
                                                                    Integer closeClock = GAME_CLOSE_TIME - 1;

                                                                    @Override
                                                                    public void run() {
                                                                        if (closeClock > 0) {
                                                                            closingEmbed.setTitle("**Game is over!**");
                                                                            closingEmbed.setDescription("Thread closing in " + closeClock + " seconds.\nA summary of this game will be reported in <#" + gameChannel.getIdLong() + "> after this thread is closed\n");

                                                                            threadChannel.editMessageEmbedsById(closingThreadMessageId, closingEmbed.build()).queue();

                                                                            --closeClock;
                                                                        }
                                                                        else {
                                                                            endingTimer.cancel();
                                                                            if (currentAnagramSetting.isGameAborted()) {
                                                                                EmbedBuilder eb = new EmbedBuilder();
                                                                                gameChannel.sendMessageEmbeds(eb.setTitle("**Game Aborted!**").build()).queue();
                                                                                endingTimer.cancel();
                                                                                gameTimer.cancel();
                                                                                startingTimer.cancel();
                                                                                anagramHashMap.remove("<@" + participantUserId + "> anagram");
                                                                                threadChannel.delete().queue();
                                                                                return;
                                                                            }

                                                                            if(currentAnagramSetting.getGameMode().equals("solo")) {
                                                                                gameChannel.sendMessageEmbeds(currentAnagramSetting.endingEmbedBuild()).queue();
                                                                            }
                                                                            else if(currentAnagramSetting.getGameMode().equals("comp")) {
                                                                                if(currentAnagramSetting.getFinalScore() > otherAnagramSetting.getFinalScore()) {
                                                                                    gameChannel.sendMessageEmbeds(currentAnagramSetting.endingEmbedBuild(otherAnagramSetting)).queue();

                                                                                    EmbedBuilder winnerEb = new EmbedBuilder();

                                                                                    winnerEb.setTitle("Competition result:\n");
                                                                                    winnerEb.setDescription("**<@" + currentAnagramSetting.getMemberId() + "> is the winner!**").setFooter("Congrats!");
                                                                                    gameChannel.sendMessageEmbeds(winnerEb.build()).queue();
                                                                                }
                                                                                else if(currentAnagramSetting.getFinalScore() == otherAnagramSetting.getFinalScore()){
                                                                                    if(currentAnagramSetting.getMemberId() < otherAnagramSetting.getMemberId()) {
                                                                                        gameChannel.sendMessageEmbeds(currentAnagramSetting.endingEmbedBuild(otherAnagramSetting)).queue();
                                                                                        EmbedBuilder drawEb = new EmbedBuilder();

                                                                                        drawEb.setTitle("Competition result: **Draw!**");
                                                                                        gameChannel.sendMessageEmbeds(drawEb.build()).queue();
                                                                                    }
                                                                                }
                                                                            }

                                                                            anagramHashMap.remove("<@" + participantUserId + "> anagram");
                                                                            threadChannel.delete().queue();
                                                                        }
                                                                    }
                                                                }, 1000, 1000
                                                        );
                                                    });

                                                }
                                            }
                                        }, 1000, 1000
                                );
                            }
                        }
                    }, 1000, 1000
            );
        });
    }

    @Override
    public void onButtonInteraction(ButtonInteractionEvent event) {
        event.deferReply().queue();

        String buttonName = event.getComponentId().split(" ")[0];
        String madeRequestUser = event.getComponentId().split(" ")[1];
        String requestedUser = event.getComponentId().split(" ")[2];

        if(!requestedUser.equals(String.valueOf(event.getUser().getIdLong()))) {
            event.getHook().setEphemeral(true).sendMessage("This is not your game request!").queue();
            return;
        }

        event.getMessage().getActionRows().forEach(row -> {
            row = row.asDisabled();
        });

        EmbedBuilder userChoice = new EmbedBuilder();

        madeRequestsUsers.remove(madeRequestUser);
        receivedRequestsUsers.remove(requestedUser);
        if(event.getButton().getId().startsWith("compConfirmButton")) {
            event.getMessage().editMessageEmbeds(userChoice.setTitle("**Game Accepted!**").build()).setActionRows().queue();
            event.getHook().sendMessage("Creating threads.").queue();

            String letters = "";
            String title1 = "<@" + madeRequestUser + "> anagram; ";
            String title2 = "<@" + requestedUser + "> anagram; ";
            for (int i = 0; i < NUM_LETTERS; ++i) {
                if (i < NUM_VOWELS) {
                    letters += VOWELS[(int) (Math.random() * VOWELS.length)];
                } else {
                    letters += CONSONANTS[(int) (Math.random() * CONSONANTS.length)];
                }
            }

            char tempArray[] = letters.toCharArray();
            Arrays.sort(tempArray);
            letters = new String(tempArray);

            for (int i = 0; i < NUM_LETTERS; ++i) {
                title1 += (String.valueOf(letters.charAt(i)).toUpperCase() + " ");
                title2 += (String.valueOf(letters.charAt(i)).toUpperCase() + " ");
            }

            final String threadTitle1 = title1;
            final String threadTitle2 = title2;
            final String anagramLetters = letters;

            AnagramSetting currentAnagramSetting = new AnagramSetting(GAME_TOTAL_TIME, event.getChannel(), Long.valueOf(madeRequestUser), anagramLetters, "comp");
            AnagramSetting otherAnagramSetting = new AnagramSetting(GAME_TOTAL_TIME, event.getChannel(), Long.valueOf(requestedUser), anagramLetters, "comp");

            event.getHook().sendMessage("Creating a thread for <@" + madeRequestUser + ">").queue(createThreadMessage -> {
                ((TextChannel) event.getChannel()).createThreadChannel(threadTitle1, createThreadMessage.getIdLong()).queue(threadChannel -> {
                    currentAnagramSetting.setThreadChannel(threadChannel);
                    createAnagramGame(currentAnagramSetting, otherAnagramSetting);
                });
            });

            event.getHook().sendMessage("Creating a thread for <@" + requestedUser + ">").queue(createThreadMessage -> {
                ((TextChannel) event.getChannel()).createThreadChannel(threadTitle2, createThreadMessage.getIdLong()).queue(threadChannel -> {
                    otherAnagramSetting.setThreadChannel(threadChannel);
                    createAnagramGame(otherAnagramSetting, currentAnagramSetting);
                });
            });
        }
        else if(event.getButton().getId().startsWith("compCancelButton")) {
            event.getMessage().editMessageEmbeds(userChoice.setTitle("**Game Declined.**").build()).setActionRows().queue();
            event.getHook().sendMessage("Canceling game request.").queue();
        }
    }

    public void loadSettings() {
        receivedRequestsUsers.clear();
        madeRequestsUsers.clear();
        anagramHashMap.clear();
        MongoCursor<Document> guildList = guilds.find().iterator();
        while (guildList.hasNext()) {
            Document doc = guildList.next();
            guildSettingHashMap.put(doc.getLong("_id"), GuildSetting.fromDocument(doc));

            jda.getGuildById(doc.getLong("_id")).updateCommands().addCommands(
                    Commands.slash("slashtest", "a test command")
                            .addOption(OptionType.INTEGER, "value", "converts value to string", true),

                    Commands.slash("abchelp", "Command to help you navigate Alphabot!")
                            .addOption(OptionType.INTEGER, "page", "which page of settings (1-4)"),

                    Commands.slash("abcsettings", "Current Alphabot settings"),

                    Commands.slash("abcprefreq", "Whether or not prefix is required to count")
                            .addOption(OptionType.STRING, "setting", "Prefix required (on/off)", true),

                    Commands.slash("abcanagram", "Anagram minigame")
                            .addOption(OptionType.STRING, "comp", "Competitive game of anagram against someone (enter id of opponent)", false)
                            .addOption(OptionType.STRING, "coop", "Cooperative game of anagram with someone (enter id of teammate)", false)
            ).queue();
        }
        guildList.close();
    }

    public void resetCount(MessageChannel channel, GuildSetting guildSetting, Long userId, String reason) {
        channel.sendMessage("<" + userId + "> ruined it!");
        guildSetting.setPrevCounterId(-1L);
        guildSetting.resetAlphabetCount();
        EmbedBuilder eb = new EmbedBuilder();
        eb.setTitle("Count RUINED!");
        eb.appendDescription("Count ruined by <@" + userId + ">! " + reason + "\n\nCount has been reset to 'a'");
        eb.setColor(new Color(220, 46, 68));
        channel.sendMessageEmbeds(eb.build()).queue();
    }

    public static Long getValueFromString(String str) {
        Long ret = 0L;

        for(int i = 0; i < str.length(); ++i) {
            ret += (str.charAt(i) - 'a' + 1L) * binPow(26L, str.length() - i - 1L);
        }

        return ret;
    }
    public static String getStringFromValue(Long val) {
        if(val <= 0L) {
            return "N/A";
        }
        System.err.println(val);

        if(val <= 26) {
            return ALPHABET[(int)(val - 1)];
        }

        Long exp = 0L;
        while(true) {
            if(val < binPow(27L, exp)) {
                break;
            }
            ++exp;
        }
        --exp;

        int idx = (int)(val / binPow(27L, exp)) - 1;
        String currval = ALPHABET[idx];
        return (currval) + (getStringFromValue(val % binPow(27L, exp)));
    }
    public static Long binPow(Long a, Long b) {
        if(b == 0) {
            return 1L;
        }

        Long ret = binPow(a, b / 2);
        if(b % 2 != 0) {
            return ret * ret * a;
        }
        return ret * ret;
    }
    public static boolean hasNumber(String str) {
        for(int i = 0; i < str.length(); ++i) {
            if(Character.isDigit(str.charAt(i))){
                return true;
            }
        }
        return false;
    }

    private Integer getRawScore(int len) {
        return roundToHundreds(Main.SCORE_A * len * len + Main.SCORE_B * len + Main.SCORE_C);
    }
    private Integer roundToHundreds(Double num) {
        return 100 * Math.round((int)(num / 100));
    }
}
