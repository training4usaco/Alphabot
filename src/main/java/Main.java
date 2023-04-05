import com.mongodb.client.MongoCursor;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.events.message.react.MessageReactionAddEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.components.ActionRow;

import java.util.*;

import net.dv8tion.jda.api.interactions.components.ItemComponent;
import net.dv8tion.jda.api.interactions.components.buttons.Button;

import java.awt.Color;

import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.guild.GuildJoinEvent;

import com.mongodb.client.model.Filters;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import com.mongodb.client.MongoClient;

import com.mongodb.client.MongoClients;
import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import org.slf4j.LoggerFactory;

import java.io.IOException;

import com.mongodb.ConnectionString;
import net.dv8tion.jda.api.JDABuilder;

import java.io.File;

import com.mongodb.client.model.ReplaceOptions;

import org.bson.Document;
import com.mongodb.client.MongoCollection;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

//
// Decompiled by Procyon v0.5.36
//

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
    public static final String TEXT_CYAN = "\u001b[36m";
    public static final String TEXT_RESET = "\u001b[0m";
    public static final int GAME_TOTAL_TIME = 60;
    public static final int GAME_HOTPOTATO_TIME = 30;
    public static final int GAME_LOAD_TIME = 10;
    public static final int GAME_CLOSE_TIME = 3;
    public static final int COOP_LOAD_TIME = 30;
    public static final int NUM_LETTERS = 15;
    public static final int NUM_VOWELS = 5;
    public static final int MESSAGE_RESET_LIMIT = 5;
    private static final int VALID = 1;
    private static final int INVALID = -1;
    private static final int USED = 0;
    private static MongoCollection<Document> guilds;
    private final HashMap<Long, GuildSetting> guildSettingHashMap;
    private final HashMap<String, AnagramSetting> anagramHashMap;
    private final HashMap<String, HotPotatoSetting> hotpotatoHashMap;
    private final HashSet<Long> anagramCoopHost;
    private final HashSet<Long> hotpotatoHost;
    private final HashMap<Long, AnagramSetting> coopMessageAnagramHashMap;
    private final HashMap<Long, HotPotatoSetting> messageHotPotatoHashMap;
    private final HashSet<Long> madeRequestsUsers;
    private final HashSet<Long> receivedRequestsUsers;
    private static final ArrayList<String> WordBank;
    private final ReplaceOptions options;
    private static final String PIVALUE;
    private static final String[] ALPHABET;
    private static final String[] VOWELS;
    private static final String[] CONSONANTS;

    public static void main(final String[] args) throws IOException {
        final Scanner config = new Scanner(new File("Config.txt"));
        try {
            Main.jda = JDABuilder.createDefault(config.nextLine()).build();
            System.out.println("Logged in as " + Main.jda.getSelfUser().getName() + "#" + Main.jda.getSelfUser().getDiscriminator());
        } catch (Exception e) {
            System.out.println("Login failed");
            e.printStackTrace();
        }
        final Scanner wordScanner = new Scanner(new File("words_alpha.txt"));
        while (wordScanner.hasNextLine()) {
            final String word = wordScanner.nextLine();
            Main.WordBank.add(word);
        }
        Main.jda.addEventListener(new Main(new ConnectionString(config.nextLine())));
        jda.getPresence().setActivity(Activity.playing("/abchelp"));
        System.err.println("\u001b[36mLogged in as " + Main.jda.getSelfUser().getName() + "#" + Main.jda.getSelfUser().getDiscriminator() + "\u001b[0m");
        System.err.println("\u001b[36mjvm version: " + System.getProperty("java.version") + "\u001b[0m");
    }

    private Main(final ConnectionString string) {
        this.anagramHashMap = new HashMap<String, AnagramSetting>();
        this.hotpotatoHashMap = new HashMap<String, HotPotatoSetting>();
        this.anagramCoopHost = new HashSet<Long>();
        this.hotpotatoHost = new HashSet<Long>();
        this.coopMessageAnagramHashMap = new HashMap<Long, AnagramSetting>();
        this.messageHotPotatoHashMap = new HashMap<Long, HotPotatoSetting>();
        this.madeRequestsUsers = new HashSet<Long>();
        this.receivedRequestsUsers = new HashSet<Long>();
        this.options = new ReplaceOptions().upsert(true);
        LoggerFactory.getLogger("org.mongodb");
        final LoggerContext loggerContext = (LoggerContext) LoggerFactory.getILoggerFactory();
        for (final Logger logger : loggerContext.getLoggerList()) {
            if (logger.getName().startsWith("com.mongodb") || logger.getName().startsWith("org.mongodb") || logger.getName().startsWith("net.dv8tion")) {
                logger.setLevel(Level.WARN);
            }
        }
        this.guildSettingHashMap = new HashMap<Long, GuildSetting>();
        final MongoClient client = MongoClients.create(string);
        Main.guilds = client.getDatabase("alphabot_database").getCollection("guilds");
        System.err.println("\u001b[36m" + Main.guilds.countDocuments());
        this.loadSettings();
    }

    @Override
    public void onMessageReceived(final MessageReceivedEvent event) {
        if (event.getAuthor().isBot()) {
            return;
        }
        final long guildId = event.getGuild().getIdLong();
        String message = event.getMessage().getContentRaw();
        final GuildSetting currentGuildSetting = this.guildSettingHashMap.get(guildId);
        final MessageChannel channel = event.getChannel();
        System.err.println("\u001b[36mMessage received: " + message + " in channel " + channel.getName() + " in guild " + event.getGuild().getName() + " from " + event.getMember().getNickname() + "\u001b[0m");
        if (currentGuildSetting == null) {
            channel.sendMessage("please set up guild settings").queue();
            return;
        }
        if (event.getChannelType().equals(ChannelType.GUILD_PUBLIC_THREAD)) {
            String channelKey = event.getThreadChannel().getName();
            if (this.anagramHashMap.containsKey(channelKey.substring(0, 11 + String.valueOf(event.getMember().getIdLong()).length()))) {
                channelKey = channelKey.substring(0, 11 + String.valueOf(event.getMember().getIdLong()).length());
            }
            else if (this.anagramHashMap.containsKey(channelKey.substring(0, 10 + String.valueOf(event.getMember().getIdLong()).length()))) {
                channelKey = channelKey.substring(0, 10 + String.valueOf(event.getMember().getIdLong()).length());
            }
            if (this.anagramHashMap.containsKey(channelKey)) {
                message = message.toLowerCase();
                final ThreadChannel thread = event.getThreadChannel();
                final AnagramSetting currAnagramSetting = this.anagramHashMap.get(channelKey);
                if (currAnagramSetting.isGameOver()) {
                    currAnagramSetting.resetCorrectCounter();
                    return;
                }
//                if (currAnagramSetting.getHostId() != event.getAuthor().getIdLong() && !currAnagramSetting.isParticipant(event.getAuthor().getIdLong())) {
//                    event.getThreadChannel().deleteMessageById(thread.getLatestMessageId()).queue();
//                    return;
//                }
                if (message.startsWith(currentGuildSetting.getSettingsPrefix())) {
                    message = message.substring(currentGuildSetting.getSettingsPrefix().length());
                    if (currAnagramSetting.getGameMode().equals("solo") && message.startsWith("quit")) {
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
                if (currAnagramSetting.getMessageCounter() == MESSAGE_RESET_LIMIT) {
                    final EmbedBuilder reminder = new EmbedBuilder();
                    reminder.setTitle("Letters reminder!");
                    final String anagramLetters = currAnagramSetting.getAnagramLetters();
                    String description = "Letters: ";
                    for (int i = 0; i < anagramLetters.length(); ++i) {
                        description = description + anagramLetters.charAt(i) + " ";
                    }
                    reminder.setColor(currAnagramSetting.getColor());
                    thread.sendMessageEmbeds(reminder.setDescription(description).build(), new MessageEmbed[0]).queue();
                    currAnagramSetting.resetMessageCounter();
                }
                if (!Main.WordBank.contains(message)) {
                    currAnagramSetting.resetCorrectCounter();
                    event.getMessage().addReaction("U+274C").queue();
                    return;
                }
                final int validity = currAnagramSetting.isValid(message.toLowerCase());
                if (validity == -1) {
                    currAnagramSetting.resetCorrectCounter();
                    event.getMessage().addReaction("U+274C").queue();
                }
                else if (validity == 1) {
                    currAnagramSetting.incCorrectCounter();
                    currAnagramSetting.updateScore(message.length());
                    currAnagramSetting.updateLongestStreak();
                    currAnagramSetting.setLastMessage(message);
                    currAnagramSetting.addWord(message, event.getAuthor().getName());
                    final int streak = currAnagramSetting.getCorrectCounter();
                    if (streak >= 10) {
                        event.getMessage().addReaction("U+1F4AB").queue();
                    }
                    else if (streak >= 5) {
                        event.getMessage().addReaction("U+1F31F").queue();
                    }
                    else if (streak >= 3) {
                        event.getMessage().addReaction("U+2B50").queue();
                    }
                    else {
                        event.getMessage().addReaction("U+2705").queue();
                    }
                }
                else if (validity == 0) {
                    event.getMessage().addReaction("U+1F7E1").queue();
                }
                return;
            }

            if (this.hotpotatoHashMap.containsKey(channelKey.substring(0, 13 + String.valueOf(event.getMember().getIdLong()).length()))) {
                channelKey = channelKey.substring(0, 13 + String.valueOf(event.getMember().getIdLong()).length());
            }
            else if (this.hotpotatoHashMap.containsKey(channelKey.substring(0, 12 + String.valueOf(event.getMember().getIdLong()).length()))) {
                channelKey = channelKey.substring(0, 12 + String.valueOf(event.getMember().getIdLong()).length());
            }
            if(hotpotatoHashMap.containsKey(channelKey)) {
                message = message.toLowerCase();
                final ThreadChannel thread = event.getThreadChannel();
                final HotPotatoSetting currHotpotatoSetting = hotpotatoHashMap.get(channelKey);

//                if (currHotpotatoSetting.getHostId() != event.getAuthor().getIdLong() && !currHotpotatoSetting.isParticipant(event.getAuthor().getIdLong())) {
//                    event.getThreadChannel().deleteMessageById(thread.getLatestMessageId()).queue();
//                    return;
//                }

                if(currHotpotatoSetting.isGameOver()) {
                    return;
                }
                if (!currHotpotatoSetting.isActive() && !currHotpotatoSetting.isGameOver()) {
                    thread.sendMessage("Please wait for the game to start!").queue();
                    return;
                }

                if(event.getAuthor().getIdLong() == currHotpotatoSetting.getCurrentParticipantId()) {
                    if(!WordBank.contains(message)) {
                        event.getMessage().addReaction("U+274C").queue();
                        return;
                    }

                    if(currHotpotatoSetting.isUsedWord(message)) {
                        event.getMessage().addReaction("U+274C").queue();
                        return;
                    }

                    String prevWord = currHotpotatoSetting.getPrevWord();
                    if(message.startsWith(prevWord.substring(prevWord.length() - 1))) {
                        event.getMessage().addReaction("U+1F954").queue();
                        currHotpotatoSetting.setPrevWord(message);
                        currHotpotatoSetting.incrementCurrentParticipantIndex();
                        currHotpotatoSetting.addUsedWord(message);
                        currHotpotatoSetting.incrementMessageCount();
                        if(currHotpotatoSetting.getMessageCount() % 5 == 0) {
                            currHotpotatoSetting.decrementMaxTime();
                        }
                        currHotpotatoSetting.resetCountTimer();
                        thread.sendMessage("<@" + currHotpotatoSetting.getCurrentParticipantId() + "> it is your turn!").queue();
                    }
                    else {
                        event.getMessage().addReaction("U+274C").queue();
                    }
                }
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
                final List<GuildChannel> channels = event.getGuild().getChannels();
                for (final GuildChannel currchannel : channels) {
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
                System.err.println("\u001b[36mSetting channel: " + message);
                boolean channelFound = false;
                String channelName2 = "";
                final List<GuildChannel> channels2 = event.getGuild().getChannels();
                for (final GuildChannel currchannel2 : channels2) {
                    if (Long.toString(currchannel2.getIdLong()).equals(message)) {
                        channelFound = true;
                        channelName2 = currchannel2.getName();
                        break;
                    }
                }
                if (!channelFound) {
                    channel.sendMessage("Please set to valid channel").queue();
                }
                else {
                    channel.sendMessage("Set alphabet counting channel to <#" + channelName2 + ">").queue();
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
                if (message.substring(11).equalsIgnoreCase("on")) {
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
        else if (channel.getIdLong() == currentGuildSetting.getChannel() && (message.startsWith(currentGuildSetting.getCountingPrefix()) || !this.guildSettingHashMap.get(guildId).isPrefixRequired())) {
            if (currentGuildSetting.isTrackCounting()) {
                if (this.guildSettingHashMap.get(guildId).isPrefixRequired()) {
                    message = message.substring(currentGuildSetting.getCountingPrefix().length());
                }
                message = message.split(" ", 2)[0];
                if (event.getMember().getIdLong() != 524989303206707214L && event.getMember().getIdLong() == currentGuildSetting.getPrevCounterId()) {
                    event.getMessage().addReaction("U+274C").queue();
                    this.resetCount(channel, currentGuildSetting, event.getMember().getIdLong(), "You can't count twice in a row! Let your friends count too... if you have any.");
                    Main.guilds.replaceOne(Filters.eq("_id", event.getGuild().getIdLong()), currentGuildSetting.toDocument(event.getGuild().getIdLong()), this.options);
                    return;
                }
                if (event.getMember().getIdLong() != 524989303206707214L && hasNumber(message)) {
                    event.getMessage().addReaction("U+274C").queue();
                    this.resetCount(channel, currentGuildSetting, event.getMember().getIdLong(), "Incorrect count!");
                    Main.guilds.replaceOne(Filters.eq("_id", event.getGuild().getIdLong()), currentGuildSetting.toDocument(event.getGuild().getIdLong()), this.options);
                    return;
                }
                if (getStringFromValue(currentGuildSetting.getAlphabetCount()).endsWith("z")) {
                    currentGuildSetting.incAlphabetCount();
                }
                if (!message.equals(getStringFromValue(currentGuildSetting.getAlphabetCount() + 1L))) {
                    if (event.getMember().getIdLong() != 524989303206707214L) {
                        event.getMessage().addReaction("U+274C").queue();
                        this.resetCount(channel, currentGuildSetting, event.getMember().getIdLong(), "Incorrect count!");
                        Main.guilds.replaceOne(Filters.eq("_id", event.getGuild().getIdLong()), currentGuildSetting.toDocument(event.getGuild().getIdLong()), this.options);
                    }
                }
                else {
                    currentGuildSetting.setPrevCounterId(event.getMember().getIdLong());
                    event.getMessage().addReaction("U+2705").queue();
                    currentGuildSetting.incAlphabetCount();
                    Main.guilds.replaceOne(Filters.eq("_id", event.getGuild().getIdLong()), currentGuildSetting.toDocument(event.getGuild().getIdLong()), this.options);
                }
            }
            else {
                channel.sendMessage("Counting is currently disabled").queue();
            }
        }
        else if (channel.getIdLong() == currentGuildSetting.getPiChannel() && message.startsWith("pi")) {
            message = message.split(" ")[0].substring(2);
            if (event.getMember().getIdLong() != 524989303206707214L && event.getMember().getIdLong() == currentGuildSetting.getPrevPiCounterId()) {
                event.getMessage().addReaction("U+274C").queue();

                currentGuildSetting.setPrevPiCounterId(-1L);
                currentGuildSetting.setPiCount(-1L);

                final EmbedBuilder eb = new EmbedBuilder();
                eb.setTitle("Count RUINED!");
                eb.appendDescription("Count ruined by <@" + event.getMember().getIdLong() + ">! You can't count twice. Reset back to 3");
                eb.setColor(new Color(220, 46, 68));
                channel.sendMessageEmbeds(eb.build(), new MessageEmbed[0]).queue();

                Main.guilds.replaceOne(Filters.eq("_id", event.getGuild().getIdLong()), currentGuildSetting.toDocument(event.getGuild().getIdLong()), this.options);
            }
            else if (!message.equals("" + PIVALUE.charAt((int)currentGuildSetting.getPiCount() + 1))) {
                if (event.getMember().getIdLong() != 524989303206707214L) {
                    event.getMessage().addReaction("U+274C").queue();

                    currentGuildSetting.setPrevPiCounterId(-1L);
                    currentGuildSetting.setPiCount(-1L);

                    final EmbedBuilder eb = new EmbedBuilder();
                    eb.setTitle("Count RUINED!");
                    eb.appendDescription("Count ruined by <@" + event.getMember().getIdLong() + ">! Incorrect count. Reset back to 3");
                    eb.setColor(new Color(220, 46, 68));
                    channel.sendMessageEmbeds(eb.build(), new MessageEmbed[0]).queue();

                    Main.guilds.replaceOne(Filters.eq("_id", event.getGuild().getIdLong()), currentGuildSetting.toDocument(event.getGuild().getIdLong()), this.options);
                }
            }
            else {
                currentGuildSetting.setPrevPiCounterId(event.getMember().getIdLong());
                event.getMessage().addReaction("U+2705").queue();
                currentGuildSetting.incPiCount();
                Main.guilds.replaceOne(Filters.eq("_id", event.getGuild().getIdLong()), currentGuildSetting.toDocument(event.getGuild().getIdLong()), this.options);
            }
        }
        Main.guilds.replaceOne(Filters.eq("_id", event.getGuild().getIdLong()), currentGuildSetting.toDocument(event.getGuild().getIdLong()), this.options);
        super.onMessageReceived(event);
    }

    @Override
    public void onGuildJoin(final GuildJoinEvent event) {
        final EmbedBuilder eb = new EmbedBuilder();
        eb.setTitle("Welcome to Alphabot!");
        eb.appendDescription("Hello! Someone invited me to your server! \nThis bot will help you learn your abcs in no time!\n\nidk random stuff still wip leave me alone :C. \n");
        event.getGuild().getSystemChannel().sendMessageEmbeds(eb.build(), new MessageEmbed[0]).queue();
        final GuildSetting gSetting = new GuildSetting();
        this.guildSettingHashMap.put(event.getGuild().getIdLong(), gSetting);
        Main.guilds.replaceOne(Filters.eq("_id", event.getGuild().getIdLong()), gSetting.toDocument(event.getGuild().getIdLong()), this.options);
        this.loadSettings();
        super.onGuildJoin(event);
    }

    @Override
    public void onSlashCommandInteraction(final SlashCommandInteractionEvent event) {
        final GuildSetting currentGuildSetting = this.guildSettingHashMap.get(event.getGuild().getIdLong());
        event.deferReply().queue();
        if (event.getName().equals("slashtest")) {
            final Long value = event.getOption("value").getAsLong();
            event.getHook().sendMessage(getStringFromValue(value)).queue();
        }
        else if (event.getName().equals("abcpisetchannel")) {
            final OptionMapping channel = event.getOption("channel");
            String channelName = channel.getAsString().substring(2, channel.getAsString().length() - 1);
            event.getHook().sendMessage(channelName).queue();
            boolean channelFound = false;
            Long channelName2 = 0L;
            final List<GuildChannel> channels2 = event.getGuild().getChannels();
            for (final GuildChannel currchannel2 : channels2) {
                if (Long.toString(currchannel2.getIdLong()).equals(channelName)) {
                    channelFound = true;
                    channelName2 = currchannel2.getIdLong();
                    break;
                }
            }
            if (!channelFound) {
                event.getHook().sendMessage("Please set to valid channel").queue();
            }
            else {
                event.getHook().sendMessage("Set pi counting channel to <#" + channelName2 + ">").queue();
                currentGuildSetting.setPiChannel(channelName2);
            }
        }
        else if (event.getName().equals("abchelp")) {
            final OptionMapping page = event.getOption("page");
            if (page == null || page.getAsInt() == 1) {
                final EmbedBuilder eb = new EmbedBuilder().setTitle("ALPHABOT HELP").setDescription("**Use `/abcsettings` to see current Alphabot settings and prefixes**\n\nThank you for using **Alphabot**!\nThis bot was made to help you learn your abc's and a few word minigames!\n\n**General Help**\nFor counting make sure you set a counting channel! (see page 2 for more info)\nCheckout page 3-4 for more info on Anagram and how to play!\n\n**Enjoy!**");
                eb.setFooter("Page 1").setColor(new Color(232, 29, 99));
                event.getHook().sendMessageEmbeds(eb.build(), new MessageEmbed[0]).queue();
            }
            else if (page.getAsInt() == 2) {
                final EmbedBuilder eb = new EmbedBuilder().setTitle("COUNTING HELP").setDescription("**Note that commands without slash use the prefix**\n\n**How to Count**\nJust your classic abc's...\nUntil you reach z, after which it will reset back to aa which is followed by the normal abc's with an extra a in front.\nOnce you reach az, the a will be incremented so the next number will be ba, and so on.\nEventually, once you get to zz it will reset back to aaa just like normal counting.\n\n**Counting Rules**\nYou can't count twice in a row\n\n**Setup Commands**\n\u25c8 `setChannel` sets the channel that counting will be tracked in\n\u25c8 `setCountingPrefix` sets the prefix for counting\n\u25c8 `setPrefix` sets the prefix for bot commands\n\n**Counting Commands**\n\u25c8 `start` - enables counting\n\u25c8 `end` - disables counting\n\u25c8 `/abcPrefReq on/off` - sets whether or not prefix is required to count\n");
                eb.setFooter("Page 2").setColor(new Color(248, 162, 6));
                event.getHook().sendMessageEmbeds(eb.build(), new MessageEmbed[0]).queue();
            }
            else if (page.getAsInt() == 3) {
                final EmbedBuilder eb = new EmbedBuilder().setTitle("ANAGRAM HELP").setDescription("**Scoring and Rank system is on the next page**\n\n**How to Play**\nYou will have 60 seconds to try and form as many words as possible using the letters provided the title of the thread.\nEvery 5 messages the bot will resend the letters.\n\n**Slash Commands**\n\u25c8 `abcanagram` - creates an anagram game!\n\u25c8 `abcanagram` comp - Lets you play a competitve game against someone of your choice where both people get the same letters! Type in the @ of the person you want to play a game against!\n\u25c8 `abcanagram` coop - Creates a game that people can join by reacting so multiple people can participate! Type yes for auto start (30 seconds) or no for buttons to start\n**Prefix Commands**\n\u25c8 `quit` type in the thread of the game to quit the game\n\n**Reactions**\n\u2705 - found an anagram\n\u274c - not an anagram (doesn't use right letters or isn't a word and resets your streak)\n:yellow_circle: - word already found (does not break your streak)\n\u2b50 - streak of 3 or more\n:star2: - streak of 5 or more\n:dizzy: - streak of 10 or more\n");
                eb.setFooter("Page 3").setColor(new Color(248, 201, 91));
                event.getHook().sendMessageEmbeds(eb.build(), new MessageEmbed[0]).queue();
            }
            else if (page.getAsInt() == 4) {
                final EmbedBuilder eb = new EmbedBuilder().setTitle("ANAGRAM SCORING").setDescription("**Scoring system can be seen while playing the game**\n\n\u25c8 **How to gain points**\n\u25c8 Scoring will be based on length of word and streak.\n\u25c8 Correct words increase your streak, while incorrect words reset it (note used words doesn't affect your streak).\n\n\u25c8 **Points System (Rest can be seen in game)**\n\u25c8 Words of length <= 2 are not counted.\n\u25c8 Words of length 3 are worth " + this.getRawScore(3) + " points\n\u25c8 Words of length 4 are worth " + this.getRawScore(4) + " points\n\u25c8 Words of length 5 are worth " + this.getRawScore(5) + " points\n\u25c8 Words of length 6 are worth " + this.getRawScore(6) + " points\n\u25c8 Words of length 7 are worth " + this.getRawScore(7) + " points\n\u25c8 Words of length 8 are worth " + this.getRawScore(8) + " points\n\n**Streak System**\n\u25c8 Streak of 3 adds an extra 50 points per message\n\u25c8 Streak of 5 adds an extra 150 points per message\n\u25c8 Streak of 10 adds an extra streak x 50 points per message\n\n**Ranks**\n\u25c8 ***__GOD (" + GOD_SCORE + "+)__***\n\u25c8 ***LEGENDARY (" + LEGENDARY_SCORE + "-" + (GOD_SCORE - 1) + ")***\n\u25c8 ***Mythical (" + MYTHICAL_SCORE + "-" + (GOD_SCORE - 1) + ")***\n\u25c8 ***Guru (" + GURU_SCORE + "-" + (MYTHICAL_SCORE - 1) + ")***\n\u25c8 **Master (" + MASTER_SCORE + "-" + (GURU_SCORE - 1) + ")**\n\u25c8 **Advanced (" + ADVANCED_SCORE + "-" + (MASTER_SCORE - 1) + ")**\n\u25c8 Expert (" + EXPERT_SCORE + "-" + (ADVANCED_SCORE - 1) + ")\n\u25c8 Seasoned (" + SEASONED_SCORE + "-" + (EXPERT_SCORE - 1) + ")\n\u25c8 Accomplished (" + ACCOMPLISHED_SCORE + "-" + (SEASONED_SCORE - 1) + ")\n\u25c8 Experienced (" + EXPERIENCED_SCORE + "-" + (ACCOMPLISHED_SCORE - 1) + ")\n\u25c8 Apprentice (<" + EXPERIENCED_SCORE + ")\n");
                eb.setFooter("Page 4").setColor(new Color(47, 204, 113));
                event.getHook().sendMessageEmbeds(eb.build(), new MessageEmbed[0]).queue();
            }
            else {
                event.getHook().sendMessage("Please enter a valid page number").queue();
            }
        }
        else if (event.getName().equals("abcsettings")) {
            final EmbedBuilder eb2 = new EmbedBuilder().setTitle("ALPHABOT SETTINGS");
            String currCount = getStringFromValue(this.guildSettingHashMap.get(event.getGuild().getIdLong()).getAlphabetCount());
            if (currCount.equals("")) {
                currCount = "N/A";
            }
            String isTracking;
            if (currentGuildSetting.isTrackCounting()) {
                isTracking = "Yes";
            }
            else {
                isTracking = "No";
            }
            String prevCounterString;
            if (currentGuildSetting.getPrevCounterId() == -1L) {
                prevCounterString = "N/A";
            }
            else {
                prevCounterString = "<@" + currentGuildSetting.getPrevCounterId() + ">";
            }
            String prefReq;
            if (currentGuildSetting.isPrefixRequired()) {
                prefReq = "On";
            }
            else {
                prefReq = "Off - **Be Careful**";
            }
            String piCount = "n/a";
            if(currentGuildSetting.getPiCount() != -1L) {
                piCount = PIVALUE.substring(0, ((int)currentGuildSetting.getPiCount() + 1));
            }

            if(piCount != "n/a" && piCount.length() > 1) {
                piCount = piCount.charAt(0) + "." + piCount.substring(1);
            }

            eb2.setDescription("**Current Count: " + currCount + "**\nCurrent Pi count: " + piCount + "\n\n**Guild Settings**\n\u25c8 Prefix Required: " + prefReq + "\n\u25c8 Tracking counting: " + isTracking + "\n\u25c8 Current counting prefix: `" + this.guildSettingHashMap.get(event.getGuild().getIdLong()).getCountingPrefix() + "`\n\u25c8 Current setting prefix: `" + this.guildSettingHashMap.get(event.getGuild().getIdLong()).getSettingsPrefix() + "`\n\u25c8 Counting Channel: <#" + this.guildSettingHashMap.get(event.getGuild().getIdLong()).getChannel() + ">\n\u25c8 Previous Counter: " + prevCounterString + "\n");
            event.getHook().sendMessageEmbeds(eb2.setColor(new Color(200, 244, 228)).build(), new MessageEmbed[0]).queue();
        }
        else if (event.getName().equals("abcprefreq")) {
            final String setting = event.getOption("setting").getAsString();
            if (setting.equals("on")) {
                this.guildSettingHashMap.get(event.getGuild().getIdLong()).setPrefixRequired(true);
                event.getHook().sendMessage("Prefix is now required!").queue();
            }
            else {
                this.guildSettingHashMap.get(event.getGuild().getIdLong()).setPrefixRequired(false);
                event.getHook().sendMessage("Prefix is no longer required. **Be Careful!**").queue();
            }
        }
        else if (event.getName().equals("abcanagram")) {
            if (this.anagramHashMap.containsKey("<@" + event.getMember().getIdLong() + "> anagram")) {
                event.getHook().sendMessage("You already have a game running currently!").queue();
                return;
            }
            final OptionMapping compOption = event.getOption("comp");
            final OptionMapping coopOption = event.getOption("coop");
            if (compOption != null && coopOption != null) {
                event.getHook().sendMessage("Please choose one of the two options").queue();
                return;
            }
            if (compOption == null && coopOption == null) {
                String letters = "";
                String title = "<@" + event.getMember().getIdLong() + "> anagram; ";
                for (int i = 0; i < NUM_LETTERS; ++i) {
                    if (i < NUM_VOWELS) {
                        letters += Main.VOWELS[(int) (Math.random() * Main.VOWELS.length)];
                    }
                    else {
                        letters += Main.CONSONANTS[(int) (Math.random() * Main.CONSONANTS.length)];
                    }
                }
                final char[] tempArray = letters.toCharArray();
                Arrays.sort(tempArray);
                letters = new String(tempArray);
                for (int j = 0; j < NUM_LETTERS; ++j) {
                    title = title + String.valueOf(letters.charAt(j)).toUpperCase() + " ";
                }
                final String threadTitle = title;
                final String anagramLetters = letters;
                final String s = letters;
                event.getHook().sendMessage("Creating a new thread").queue(createThreadMessage -> ((TextChannel) event.getChannel()).createThreadChannel(threadTitle, createThreadMessage.getIdLong()).queue(threadChannel -> {
                    AnagramSetting currentAnagramSetting = new AnagramSetting(GAME_TOTAL_TIME, event.getChannel(), event.getMember().getIdLong(), s, "solo");
                    AnagramSetting otherAnagramSetting = new AnagramSetting(0, event.getChannel(), event.getMember().getIdLong(), s, "solo");
                    currentAnagramSetting.setThreadChannel(threadChannel);
                    this.createAnagramGame(currentAnagramSetting, otherAnagramSetting);
                }));
            }
            else if (compOption != null) {
                if (this.madeRequestsUsers.contains(event.getMember().getIdLong())) {
                    event.getHook().sendMessage("Sorry, you currently have a pending request!").queue();
                    return;
                }
                if (this.anagramHashMap.containsKey("<@" + event.getMember().getIdLong() + "> anagram")) {
                    event.getHook().sendMessage("Sorry, you have a game currently running!").queue();
                    return;
                }
                if (this.anagramCoopHost.contains(event.getMember().getIdLong())) {
                    event.getHook().sendMessage("Sorry, you are currently hosting a coop game!").queue();
                    return;
                }
                String uid = compOption.getAsString();
                if (uid.startsWith("<@") && uid.endsWith(">")) {
                    uid = uid.substring(2, uid.length() - 1);
                }
                for (int k = 0; k < uid.length(); ++k) {
                    if (!Character.isDigit(uid.charAt(k))) {
                        event.getHook().sendMessage("Please send a valid user ID.").queue();
                        return;
                    }
                }
                final Long requestedUserId = Long.parseLong(uid);
                if (event.getGuild().retrieveMemberById(requestedUserId) == null) {
                    event.getHook().sendMessage("No user found with that ID.").queue();
                    return;
                }
                if (requestedUserId == event.getMember().getIdLong()) {
                    event.getHook().sendMessage("Sorry, you can't create a request with yourself!").queue();
                    return;
                }
                if (this.receivedRequestsUsers.contains(uid)) {
                    event.getHook().sendMessage("Sorry, they have a pending anagram request!").queue();
                    return;
                }
                if (this.anagramHashMap.containsKey("<@" + uid + "> anagram")) {
                    event.getHook().sendMessage("Sorry, they have a game running currently.").queue();
                    return;
                }
                final Button confirmButton = Button.success("anagramCompConfirmButton " + String.valueOf(event.getMember().getIdLong()) + " " + uid, "Yes");
                final Button cancelButton = Button.danger("anagramCompCancelButton " + String.valueOf(event.getMember().getIdLong()) + " " + uid, "No");
                this.receivedRequestsUsers.add(requestedUserId);
                this.madeRequestsUsers.add(event.getMember().getIdLong());
                final EmbedBuilder challengeRequestEmbed = new EmbedBuilder();
                challengeRequestEmbed.setTitle("**Anagram Challenge Request**").setDescription("<@" + event.getMember().getIdLong() + "> wants to challenge you to a 1v1 game of anagram! Do you want to accept?");
                event.getHook().sendMessageEmbeds(challengeRequestEmbed.build(), new MessageEmbed[0]).setContent("<@" + Long.parseLong(uid) + ">").addActionRow(new ItemComponent[]{confirmButton, cancelButton}).queue((message) -> {
                    Timer buttonDisableTimer = new Timer();
                    buttonDisableTimer.schedule(new TimerTask() {
                        public void run() {
                            if (!confirmButton.isDisabled() && !cancelButton.isDisabled()) {
                                Main.this.receivedRequestsUsers.remove(requestedUserId);
                                Main.this.madeRequestsUsers.remove(event.getMember().getIdLong());
                                message.editMessageEmbeds(new MessageEmbed[]{challengeRequestEmbed.build()}).setActionRows(new ActionRow[]{ActionRow.of(new ItemComponent[]{confirmButton.withDisabled(true), cancelButton.withDisabled(true)})}).queue();
                            }

                        }
                    }, 120000L);
                });
            }
            else if (coopOption != null) {
                if (this.anagramHashMap.containsKey("<@" + event.getMember().getIdLong() + "> anagram")) {
                    event.getHook().sendMessage("Sorry, you have a game currently running!").queue();
                    return;
                }
                if (this.anagramCoopHost.contains(event.getMember().getIdLong())) {
                    event.getHook().sendMessage("Sorry, you are already hosting a coop game!").queue();
                    return;
                }
                this.anagramCoopHost.add(event.getMember().getIdLong());
                if (coopOption.getAsString().toLowerCase().equals("yes")) {
                    final EmbedBuilder coopGameAnnouncementEmbed = new EmbedBuilder();
                    String letters2 = "";
                    String title2 = "<@" + event.getMember().getIdLong() + "> anagram coop; ";
                    for (int j = 0; j < NUM_LETTERS; ++j) {
                        if (j < NUM_VOWELS) {
                            letters2 += Main.VOWELS[(int) (Math.random() * Main.VOWELS.length)];
                        }
                        else {
                            letters2 += Main.CONSONANTS[(int) (Math.random() * Main.CONSONANTS.length)];
                        }
                    }
                    final char[] tempArray2 = letters2.toCharArray();
                    Arrays.sort(tempArray2);
                    letters2 = new String(tempArray2);
                    for (int l = 0; l < NUM_LETTERS; ++l) {
                        title2 = title2 + String.valueOf(letters2.charAt(l)).toUpperCase() + " ";
                    }
                    AnagramSetting currentAnagramSetting = new AnagramSetting(GAME_TOTAL_TIME, event.getChannel(), event.getMember().getIdLong(), letters2, "coop");
                    AnagramSetting otherAnagramSetting = new AnagramSetting(0, event.getChannel(), event.getMember().getIdLong(), letters2, "coop");
                    coopGameAnnouncementEmbed.setTitle("**Coop anagram game!**").setDescription("<@" + event.getMember().getIdLong() + "> is hosting a coop anagram game! React with anything to join\nStarting game in: " + COOP_LOAD_TIME + " seconds.");
                    String finalTitle = title2;
                    event.getHook().sendMessageEmbeds(coopGameAnnouncementEmbed.build(), new MessageEmbed[0]).queue((message) -> {
                        message.addReaction("U+1F44D").queue();
                        this.coopMessageAnagramHashMap.put(message.getIdLong(), currentAnagramSetting);
                        final Timer coopWaitTimer = new Timer();
                        coopWaitTimer.scheduleAtFixedRate(new TimerTask() {
                            int coopGameCountdown = 29;

                            public void run() {
                                if (this.coopGameCountdown > 0) {
                                    coopGameAnnouncementEmbed.setDescription("<@" + event.getMember().getIdLong() + "> is hosting a coop anagram game! React with anything to join!\nStarting game in: " + this.coopGameCountdown + " seconds.");
                                    message.editMessageEmbeds(new MessageEmbed[]{coopGameAnnouncementEmbed.build()}).queue();
                                    --this.coopGameCountdown;
                                }
                                else {
                                    coopWaitTimer.cancel();
                                    Main.this.coopMessageAnagramHashMap.remove(message.getIdLong());
                                    coopGameAnnouncementEmbed.setTitle("**Coop game started!**");
                                    coopGameAnnouncementEmbed.setDescription("");
                                    message.editMessageEmbeds(new MessageEmbed[]{coopGameAnnouncementEmbed.build()}).queue();
                                    event.getHook().sendMessage("Creating a new thread").queue((createThreadMessage) -> {
                                        ((TextChannel) event.getChannel()).createThreadChannel(finalTitle, createThreadMessage.getIdLong()).queue((threadChannel) -> {
                                            currentAnagramSetting.setThreadChannel(threadChannel);
                                            Main.this.createAnagramGame(currentAnagramSetting, otherAnagramSetting);
                                        });
                                    });
                                }

                            }
                        }, 1000L, 1000L);
                    });
                }
                else if (coopOption.getAsString().toLowerCase().equals("no")) {
                    final EmbedBuilder coopGameAnnouncementEmbed = new EmbedBuilder();
                    String letters2 = "";
                    String title2 = "<@" + event.getMember().getIdLong() + "> anagram coop; ";
                    for (int j = 0; j < NUM_LETTERS; ++j) {
                        if (j < NUM_VOWELS) {
                            letters2 += Main.VOWELS[(int) (Math.random() * Main.VOWELS.length)];
                        }
                        else {
                            letters2 += Main.CONSONANTS[(int) (Math.random() * Main.CONSONANTS.length)];
                        }
                    }
                    final char[] tempArray2 = letters2.toCharArray();
                    Arrays.sort(tempArray2);
                    letters2 = new String(tempArray2);
                    for (int l = 0; l < NUM_LETTERS; ++l) {
                        title2 = title2 + String.valueOf(letters2.charAt(l)).toUpperCase() + " ";
                    }
                    Button startButton = Button.success("anagramCoopStartButton " + event.getMember().getIdLong(), "Start");
                    Button cancelButton = Button.danger("anagramCoopCancelButton " + event.getMember().getIdLong(), "Cancel");
                    AnagramSetting currentAnagramSetting = new AnagramSetting(60, event.getChannel(), event.getMember().getIdLong(), letters2, "coop");
                    coopGameAnnouncementEmbed.setTitle("**Coop anagram game!**").setDescription("<@" + event.getMember().getIdLong() + "> is hosting a coop anagram game! React with anything to join\nStart the game by pressing the \"Start\" button!");
                    event.getHook().sendMessageEmbeds(coopGameAnnouncementEmbed.build(), new MessageEmbed[0]).addActionRow(new ItemComponent[]{startButton, cancelButton}).queue((message) -> {
                        message.addReaction("U+1F44D").queue();
                        this.coopMessageAnagramHashMap.put(message.getIdLong(), currentAnagramSetting);
                        Timer buttonDisableTimer = new Timer();
                        buttonDisableTimer.schedule(new TimerTask() {
                            public void run() {
                                if (!startButton.isDisabled() && !cancelButton.isDisabled()) {
                                    Main.this.anagramCoopHost.remove(event.getMember().getIdLong());
                                    message.editMessageEmbeds(new MessageEmbed[]{coopGameAnnouncementEmbed.build()}).setActionRows(new ActionRow[]{ActionRow.of(new ItemComponent[]{startButton.withDisabled(true), cancelButton.withDisabled(true)})}).queue();
                                }

                            }
                        }, 120000L);
                    });
                }
                else {
                    event.getHook().sendMessage("Please enter a valid parameter for autostart (yes/no)").queue();
                }
            }
        }
        else if(event.getName().equals("abchotpotato")) {
            if(hotpotatoHost.contains(event.getMember().getIdLong())) {
                event.getHook().sendMessage("Sorry, you are already hosting a hot potato game!").queue();
                return;
            }
            if(hotpotatoHashMap.containsKey("<@" + event.getMember().getIdLong() + "> hotpotato")) {
                event.getHook().sendMessage("Sorry, you are already have a hot potato game running!").queue();
                return;
            }
            hotpotatoHost.add(event.getMember().getIdLong());
            final EmbedBuilder hotPotatoGameAnnouncementEmbed = new EmbedBuilder();
            Random rand = new Random();
            String startWord = WordBank.get(rand.nextInt(WordBank.size()));

            Button startButton = Button.success("hotpotatoStartButton " + event.getMember().getIdLong(), "Start");
            Button cancelButton = Button.danger("hotpotatoCancelButton " + event.getMember().getIdLong(), "Cancel");
            hotPotatoGameAnnouncementEmbed.setTitle("**Hot Potato game!**").setDescription("<@" + event.getMember().getIdLong() + "> is hosting a hot potato game! React with anything to join\nStart the game by pressing the \"Start\" button!");
            HotPotatoSetting currentHotPotatoSetting = new HotPotatoSetting(event.getMember().getIdLong(), startWord, event.getChannel());

            event.getHook().sendMessageEmbeds(hotPotatoGameAnnouncementEmbed.build()).addActionRow(new ItemComponent[]{startButton, cancelButton}).queue((message) -> {
                message.addReaction("U+1F44D").queue();
                messageHotPotatoHashMap.put(message.getIdLong(), currentHotPotatoSetting);
                Timer buttonDisableTimer = new Timer();
                buttonDisableTimer.schedule(new TimerTask() {
                    public void run() {
                        if (!startButton.isDisabled() && !cancelButton.isDisabled()) {
                            hotpotatoHost.remove(event.getMember().getIdLong());
                            message.editMessageEmbeds(new MessageEmbed[]{hotPotatoGameAnnouncementEmbed.build()}).setActionRows(new ActionRow[]{ActionRow.of(new ItemComponent[]{startButton.withDisabled(true), cancelButton.withDisabled(true)})}).queue();
                        }

                    }
                }, 1000 * 60 * 5L);
            });
        }
    }

    @Override
    public void onMessageReactionAdd(final MessageReactionAddEvent event) {
        if (event.getUser().isBot()) {
            return;
        }
        if (this.coopMessageAnagramHashMap.containsKey(event.getMessageIdLong())) {
            if (event.getMember().getIdLong() == this.coopMessageAnagramHashMap.get(event.getMessageIdLong()).getHostId()) {
                return;
            }
            if (this.anagramHashMap.containsKey("<@" + event.getMember().getIdLong() + "> anagram")) {
                event.getChannel().sendMessage("Sorry, you have a game currently running!").queue();
                return;
            }
            if (this.coopMessageAnagramHashMap.get(event.getMessageIdLong()).getHostId() != event.getMessageIdLong() && !this.coopMessageAnagramHashMap.get(event.getMessageIdLong()).isParticipant(event.getUserIdLong())) {
                System.err.println("Adding " + event.getMember().getIdLong());
                event.getChannel().sendMessage("Added <@" + event.getMember().getIdLong() + "> to coop!").queue();
                this.coopMessageAnagramHashMap.get(event.getMessageIdLong()).addParticipant(event.getMember().getIdLong());
            }
        }
        if(messageHotPotatoHashMap.containsKey(event.getMessageIdLong())) {
            if (event.getMember().getIdLong() == messageHotPotatoHashMap.get(event.getMessageIdLong()).getHostId()) {
                return;
            }
            if (messageHotPotatoHashMap.get(event.getMessageIdLong()).isParticipant(event.getMember().getIdLong())) {
                return;
            }
            if (this.hotpotatoHashMap.containsKey("<@" + event.getMember().getIdLong() + "> hotpotato")) {
                event.getChannel().sendMessage("Sorry, you have a game currently running!").queue();
                return;
            }
            event.getChannel().sendMessage("Added <@" + event.getMember().getIdLong() + "> to hotpotato game!").queue();
            messageHotPotatoHashMap.get(event.getMessageIdLong()).addParticipant(event.getMember().getIdLong());
        }
    }

    public void createHotPotatoGame(final HotPotatoSetting currentHotPotatoSetting) {
        final Timer startingTimer = new Timer();
        final Long participantUserId = currentHotPotatoSetting.getHostId();
        final ThreadChannel threadChannel = currentHotPotatoSetting.getThreadChannel();
        final MessageChannel gameChannel = currentHotPotatoSetting.getGameChannel();
        final String hotPotatoWord = currentHotPotatoSetting.getPrevWord();
        threadChannel.sendMessage("<@" + participantUserId + "> Your game thread has been created").queue();
        String participantsPing = "";
        final HashSet<Long> participants = currentHotPotatoSetting.getParticipants();
        final Iterator<Long> it = participants.iterator();
        while (it.hasNext()) {
            long participant = it.next();
            participantsPing = participantsPing + "<@" + participant + ">";
        }
        if (participantsPing.length() > 0) {
            threadChannel.sendMessage(participantsPing).queue();
        }

        currentHotPotatoSetting.initParticipantOrder();
        hotpotatoHashMap.put("<@" + participantUserId + "> hotpotato", currentHotPotatoSetting);

        final EmbedBuilder hotpotatoeb = new EmbedBuilder();
        hotpotatoeb.setTitle("Hot Potato Game");

        String participantOrderList = "Participants order:\n";

        ArrayList<Long> participantOrder = currentHotPotatoSetting.getParticipantOrder();
        for (int i = 0; i < participantOrder.size(); ++i) {
            participantOrderList += "<@" + participantOrder.get(i) + ">\n";
        }

        final String participantOrderString = participantOrderList;
        hotpotatoeb.setDescription("Starting game in 10 seconds.\nTake turns to form words starting with the previous word's last letter!" +
                "\n\n**Current Word:** " + hotPotatoWord + "\n" + participantOrderList);
        hotpotatoeb.setColor(new Color(220, 46, 68));

        threadChannel.sendMessageEmbeds(hotpotatoeb.build(), new MessageEmbed[0]).queue(countdownEmbedMessage -> {
            final Long countdownMessageId = countdownEmbedMessage.getIdLong();
            startingTimer.scheduleAtFixedRate(new TimerTask() {
                Integer loadClock = GAME_LOAD_TIME - 1;

                public void run() {
                    if (this.loadClock > 0) {
                        hotpotatoeb.setDescription("Starting game in " + this.loadClock + " seconds.\nTake turns to form words starting with the previous word's last letter!" +
                                "\n\n**Current Word:** " + hotPotatoWord + "\n" + participantOrderString);
                        threadChannel.editMessageEmbedsById(countdownMessageId, new MessageEmbed[]{hotpotatoeb.build()}).queue();
                        this.loadClock = this.loadClock - 1;
                    }
                    else {
                        startingTimer.cancel();
                        final Timer gameTimer = new Timer();

                        threadChannel.editMessageEmbedsById(countdownMessageId, new MessageEmbed[]{currentHotPotatoSetting.ebBuild()}).queue();
                        currentHotPotatoSetting.decrementCountTimer();
                        currentHotPotatoSetting.setActive(true);
                        currentHotPotatoSetting.resetCountTimer();

                        gameTimer.scheduleAtFixedRate(new TimerTask() {
                            public void run() {
                                if (currentHotPotatoSetting.getCountTimer() > 0) {
                                    if (currentHotPotatoSetting.getCountTimer() <= 3) {
                                        threadChannel.sendMessage("There are " + currentHotPotatoSetting.getCountTimer() + " seconds left!").queue();
                                    }

                                    threadChannel.editMessageEmbedsById(countdownMessageId, new MessageEmbed[]{currentHotPotatoSetting.ebBuild()}).queue();
                                    currentHotPotatoSetting.decrementCountTimer();
                                }
                                else {
                                    gameTimer.cancel();
                                    currentHotPotatoSetting.setActive(false);
                                    currentHotPotatoSetting.setGameOver(true);

                                    Timer endingTimer = new Timer();
                                    new EmbedBuilder();
                                    EmbedBuilder closingEmbed = new EmbedBuilder();
                                    closingEmbed.setTitle("**Game is over!**");
                                    threadChannel.sendMessage(":boom:").queue();
                                    threadChannel.sendMessageEmbeds(closingEmbed.setDescription("Thread closing in " + GAME_CLOSE_TIME + " seconds.\nA summary of this game will be reported in <#" + gameChannel.getIdLong() + "> after this thread is closed\n").build(), new MessageEmbed[0]).queue((closingEmbedMessage) -> {
                                        final Long closingThreadMessageId = closingEmbedMessage.getIdLong();
                                        endingTimer.scheduleAtFixedRate(new TimerTask() {
                                            Integer closeClock = GAME_CLOSE_TIME - 1;

                                            public void run() {
                                                if (this.closeClock > 0) {
                                                    closingEmbed.setTitle("**Game is over!**");
                                                    closingEmbed.setDescription("Thread closing in " + this.closeClock + " seconds.\nA summary of this game will be reported in <#" + gameChannel.getIdLong() + "> after this thread is closed\n");
                                                    threadChannel.editMessageEmbedsById(closingThreadMessageId, new MessageEmbed[]{closingEmbed.build()}).queue();
                                                    this.closeClock = this.closeClock - 1;
                                                }
                                                else {
                                                    endingTimer.cancel();

                                                    EmbedBuilder eb = new EmbedBuilder();
                                                    eb.setTitle("**Game is over!** :potato:");
                                                    eb.setColor(new Color(232, 234, 255));

                                                    String ebDescription = "<@" + currentHotPotatoSetting.getCurrentParticipantId() + "> couldn't come up with a word in time!! You lose!!!\nWords Found:\n";
                                                    HashSet<String> words = currentHotPotatoSetting.getUsedWords();

                                                    for(String word : words) {
                                                        ebDescription = ebDescription + word + "\n";
                                                    }
                                                    eb.setDescription(ebDescription);
                                                    gameChannel.sendMessageEmbeds(eb.build()).queue();
                                                    hotpotatoHashMap.remove("<@" + participantUserId + "> hotpotato");
                                                    threadChannel.delete().queue();
                                                }

                                            }
                                        }, 1000L, 1000L);
                                    });
                                }
                            }
                        }, 1000L, 1000L);
                    }
                }
            }, 1000L, 1000L);
        });
    }
    public void createAnagramGame(final AnagramSetting currentAnagramSetting, final AnagramSetting otherAnagramSetting) {
        final Timer startingTimer = new Timer();
        final Long participantUserId = currentAnagramSetting.getHostId();
        final ThreadChannel threadChannel = currentAnagramSetting.getThreadChannel();
        final MessageChannel gameChannel = currentAnagramSetting.getGameChannel();
        final String anagramLetters = currentAnagramSetting.getAnagramLetters();
        threadChannel.sendMessage("<@" + participantUserId + "> Your game thread has been created").queue();
        String participantsPing = "";
        if (currentAnagramSetting.getGameMode().equals("coop")) {
            final HashSet<Long> participants = currentAnagramSetting.getParticipants();
            final Iterator<Long> it = participants.iterator();
            while (it.hasNext()) {
                participantsPing = participantsPing + "<@" + it.next() + ">";
            }
            if (participantsPing.length() > 0) {
                threadChannel.sendMessage(participantsPing).queue();
            }
        }
        this.anagramHashMap.put("<@" + participantUserId + "> anagram", currentAnagramSetting);
        final EmbedBuilder anagrameb = new EmbedBuilder();
        anagrameb.setTitle("Anagram Game");
        anagrameb.setDescription("Starting game in 10 seconds.\nTry and form as many words as possible using the letters in the title of this thread!\n**Letters: " + anagramLetters + "**");
        anagrameb.setColor(new Color(220, 46, 68));
        threadChannel.sendMessageEmbeds(anagrameb.build(), new MessageEmbed[0]).queue(countdownEmbedMessage -> {
            final Long countdownMessageId = countdownEmbedMessage.getIdLong();
            startingTimer.scheduleAtFixedRate(new TimerTask() {
                Integer loadClock = GAME_LOAD_TIME - 1;

                public void run() {
                    if (this.loadClock > 0) {
                        anagrameb.setDescription("Starting game in " + this.loadClock + " seconds.\nTry and form as many words as possible using the letters in the title of this thread!\n**Letters: " + anagramLetters + "**\nYou can use setting prefix + quit to quit at anytime (in solo mode)");
                        threadChannel.editMessageEmbedsById(countdownMessageId, new MessageEmbed[]{anagrameb.build()}).queue();
                        this.loadClock = this.loadClock - 1;
                        if (((AnagramSetting) Main.this.anagramHashMap.get("<@" + participantUserId + "> anagram")).isGameAborted()) {
                            this.loadClock = 0;
                        }
                    }
                    else {
                        startingTimer.cancel();
                        final Timer gameTimer = new Timer();
                        if (currentAnagramSetting.isGameAborted()) {
                            EmbedBuilder eb = new EmbedBuilder();
                            threadChannel.sendMessageEmbeds(eb.setTitle("**Game Aborted!**").build(), new MessageEmbed[0]).queue();
                            currentAnagramSetting.setCountTimer(0);
                        }
                        else {
                            threadChannel.editMessageEmbedsById(countdownMessageId, new MessageEmbed[]{currentAnagramSetting.ebBuild()}).queue();
                            currentAnagramSetting.decrementCountTimer();
                            currentAnagramSetting.setActive(true);
                        }

                        gameTimer.scheduleAtFixedRate(new TimerTask() {
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

                                    threadChannel.editMessageEmbedsById(countdownMessageId, new MessageEmbed[]{currentAnagramSetting.ebBuild()}).queue();
                                    currentAnagramSetting.decrementCountTimer();
                                }
                                else {
                                    gameTimer.cancel();
                                    currentAnagramSetting.setActive(false);
                                    currentAnagramSetting.setGameOver(true);
                                    Timer endingTimer = new Timer();
                                    new EmbedBuilder();
                                    EmbedBuilder closingEmbed = new EmbedBuilder();
                                    closingEmbed.setTitle("**Game is over!**");
                                    threadChannel.sendMessageEmbeds(closingEmbed.setDescription("Thread closing in " + GAME_CLOSE_TIME + " seconds.\nA summary of this game will be reported in <#" + gameChannel.getIdLong() + "> after this thread is closed\n").build(), new MessageEmbed[0]).queue((closingEmbedMessage) -> {
                                        final Long closingThreadMessageId = closingEmbedMessage.getIdLong();
                                        endingTimer.scheduleAtFixedRate(new TimerTask() {
                                            Integer closeClock = GAME_CLOSE_TIME - 1;

                                            public void run() {
                                                if (this.closeClock > 0) {
                                                    closingEmbed.setTitle("**Game is over!**");
                                                    closingEmbed.setDescription("Thread closing in " + this.closeClock + " seconds.\nA summary of this game will be reported in <#" + gameChannel.getIdLong() + "> after this thread is closed\n");
                                                    threadChannel.editMessageEmbedsById(closingThreadMessageId, new MessageEmbed[]{closingEmbed.build()}).queue();
                                                    this.closeClock = this.closeClock - 1;
                                                }
                                                else {
                                                    endingTimer.cancel();
                                                    EmbedBuilder drawEb;
                                                    if (currentAnagramSetting.isGameAborted()) {
                                                        drawEb = new EmbedBuilder();
                                                        gameChannel.sendMessageEmbeds(drawEb.setTitle("**Game Aborted!**").build(), new MessageEmbed[0]).queue();
                                                        endingTimer.cancel();
                                                        gameTimer.cancel();
                                                        startingTimer.cancel();
                                                        Main.this.anagramHashMap.remove("<@" + participantUserId + "> anagram");
                                                        threadChannel.delete().queue();
                                                        return;
                                                    }

                                                    if (!currentAnagramSetting.getGameMode().equals("solo") && !currentAnagramSetting.getGameMode().equals("coop")) {
                                                        if (currentAnagramSetting.getGameMode().equals("comp")) {
                                                            if (currentAnagramSetting.getFinalScore() > otherAnagramSetting.getFinalScore()) {
                                                                gameChannel.sendMessageEmbeds(currentAnagramSetting.endingEmbedBuild(otherAnagramSetting), new MessageEmbed[0]).queue();
                                                                drawEb = new EmbedBuilder();
                                                                drawEb.setTitle("Competition result:\n");
                                                                drawEb.setDescription("**<@" + currentAnagramSetting.getHostId() + "> is the winner!**").setFooter("Congrats!");
                                                                gameChannel.sendMessageEmbeds(drawEb.build(), new MessageEmbed[0]).queue();
                                                            }
                                                            else if (currentAnagramSetting.getFinalScore() == otherAnagramSetting.getFinalScore() && currentAnagramSetting.getHostId() < otherAnagramSetting.getHostId()) {
                                                                gameChannel.sendMessageEmbeds(currentAnagramSetting.endingEmbedBuild(otherAnagramSetting), new MessageEmbed[0]).queue();
                                                                drawEb = new EmbedBuilder();
                                                                drawEb.setTitle("Competition result: **Draw!**");
                                                                gameChannel.sendMessageEmbeds(drawEb.build(), new MessageEmbed[0]).queue();
                                                            }
                                                        }
                                                    }
                                                    else {
                                                        gameChannel.sendMessageEmbeds(currentAnagramSetting.endingEmbedBuild(), new MessageEmbed[0]).queue();
                                                    }

                                                    Main.this.anagramHashMap.remove("<@" + participantUserId + "> anagram");
                                                    threadChannel.delete().queue();
                                                }

                                            }
                                        }, 1000L, 1000L);
                                    });
                                }

                            }
                        }, 1000L, 1000L);
                    }

                }
            }, 1000L, 1000L);
        });
    }

    @Override
    public void onButtonInteraction(final ButtonInteractionEvent event) {
        event.deferReply().queue();
        if (event.getButton().getId().startsWith("anagramCoop")) {
            final String buttonName = event.getComponentId().split(" ")[0];
            final String gameHost = event.getComponentId().split(" ")[1];
            if (event.getMember().getIdLong() != Long.valueOf(gameHost)) {
                event.getHook().sendMessage("You are not the host of this coop game!").setEphemeral(true).queue();
                return;
            }
            this.anagramCoopHost.remove(event.getMember().getIdLong());
            event.getMessage().getActionRows().forEach(row -> row.getButtons().forEach(button -> button = button.asDisabled()));
            if (buttonName.startsWith("anagramCoopStartButton")) {
                EmbedBuilder eb = new EmbedBuilder();
                event.getMessage().editMessageEmbeds(eb.setTitle("**Coop game started!**").build()).setActionRows(new ActionRow[0]).queue();
                String title = "<@" + gameHost + "> anagram coop; ";
                final String letters = this.coopMessageAnagramHashMap.get(event.getMessageIdLong()).getAnagramLetters();
                for (int i = 0; i < NUM_LETTERS; ++i) {
                    title = title + String.valueOf(letters.charAt(i)).toUpperCase() + " ";
                }
                final String threadTitle = title;
                final AnagramSetting otherAnagramSetting = new AnagramSetting(0, event.getChannel(), event.getMember().getIdLong(), letters, "coop");
                event.getHook().sendMessage("Creating a new thread").queue(createThreadMessage -> ((TextChannel) event.getChannel()).createThreadChannel(threadTitle, createThreadMessage.getIdLong()).queue(threadChannel -> {
                    this.coopMessageAnagramHashMap.get(event.getMessageIdLong()).setThreadChannel(threadChannel);
                    this.createAnagramGame(this.coopMessageAnagramHashMap.get(event.getMessageIdLong()), otherAnagramSetting);
                    this.coopMessageAnagramHashMap.remove(event.getMessage().getIdLong());
                }));
            }
            else if (buttonName.startsWith("anagramCoopCancelButton")) {
                final EmbedBuilder eb = new EmbedBuilder();
                event.getMessage().editMessageEmbeds(eb.setTitle("**Coop game cancelled.**").build()).setActionRows(new ActionRow[0]).queue();
                event.getHook().sendMessage("Cancelled!").queue();
                this.coopMessageAnagramHashMap.remove(event.getMessage().getIdLong());
            }
        }
        else if (event.getButton().getId().startsWith("anagramComp")) {
            final String buttonName = event.getComponentId().split(" ")[0];
            final String madeRequestUser = event.getComponentId().split(" ")[1];
            final String requestedUser = event.getComponentId().split(" ")[2];
            if (!requestedUser.equals(String.valueOf(event.getUser().getIdLong()))) {
                event.getHook().sendMessage("This is not your game request!").setEphemeral(true).queue();
                return;
            }
            event.getMessage().getActionRows().forEach(row -> row.getButtons().forEach(button -> button = button.asDisabled()));
            final EmbedBuilder userChoice = new EmbedBuilder();
            this.madeRequestsUsers.remove(madeRequestUser);
            this.receivedRequestsUsers.remove(requestedUser);
            if (event.getButton().getId().startsWith("anagramCompConfirmButton")) {
                event.getMessage().editMessageEmbeds(userChoice.setTitle("**Game Accepted!**").build()).setActionRows(new ActionRow[0]).queue();
                event.getHook().sendMessage("Creating threads.").queue();
                String letters = "";
                String title2 = "<@" + madeRequestUser + "> anagram; ";
                String title3 = "<@" + requestedUser + "> anagram; ";
                for (int j = 0; j < NUM_LETTERS; ++j) {
                    if (j < NUM_VOWELS) {
                        letters += Main.VOWELS[(int) (Math.random() * Main.VOWELS.length)];
                    }
                    else {
                        letters += Main.CONSONANTS[(int) (Math.random() * Main.CONSONANTS.length)];
                    }
                }
                final char[] tempArray = letters.toCharArray();
                Arrays.sort(tempArray);
                letters = new String(tempArray);
                for (int k = 0; k < NUM_LETTERS; ++k) {
                    title2 = title2 + String.valueOf(letters.charAt(k)).toUpperCase() + " ";
                    title3 = title3 + String.valueOf(letters.charAt(k)).toUpperCase() + " ";
                }
                final String threadTitle2 = title2;
                final String threadTitle3 = title3;
                final String anagramLetters = letters;
                final AnagramSetting currentAnagramSetting = new AnagramSetting(60, event.getChannel(), Long.valueOf(madeRequestUser), anagramLetters, "comp");
                final AnagramSetting otherAnagramSetting = new AnagramSetting(60, event.getChannel(), Long.valueOf(requestedUser), anagramLetters, "comp");
                event.getHook().sendMessage("Creating a thread for <@" + madeRequestUser + ">").queue(createThreadMessage -> ((TextChannel) event.getChannel()).createThreadChannel(threadTitle2, createThreadMessage.getIdLong()).queue(threadChannel -> {
                    currentAnagramSetting.setThreadChannel(threadChannel);
                    this.createAnagramGame(currentAnagramSetting, otherAnagramSetting);
                }));
                event.getHook().sendMessage("Creating a thread for <@" + requestedUser + ">").queue(createThreadMessage -> ((TextChannel) event.getChannel()).createThreadChannel(threadTitle3, createThreadMessage.getIdLong()).queue(threadChannel -> {
                    currentAnagramSetting.setThreadChannel(threadChannel);
                    this.createAnagramGame(currentAnagramSetting, otherAnagramSetting);
                }));
            }
            else if (event.getButton().getId().startsWith("anagramCompCancelButton")) {
                event.getMessage().editMessageEmbeds(userChoice.setTitle("**Game Declined.**").build()).setActionRows(new ActionRow[0]).queue();
                event.getHook().sendMessage("Cancelling game request.").queue();
            }
        }
        else if (event.getButton().getId().startsWith("hotpotato")) {
            final String buttonName = event.getComponentId().split(" ")[0];
            final String gameHost = event.getComponentId().split(" ")[1];

            if (event.getMember().getIdLong() != Long.valueOf(gameHost)) {
                event.getHook().sendMessage("You are not the host of this hotpotato game!").setEphemeral(true).queue();
                return;
            }

            hotpotatoHost.remove(event.getMember().getIdLong());
            event.getMessage().getActionRows().forEach(row -> row.getButtons().forEach(button -> button = button.asDisabled()));

            if (buttonName.startsWith("hotpotatoStartButton")) {
                EmbedBuilder eb = new EmbedBuilder();
                event.getMessage().editMessageEmbeds(eb.setTitle("**Hot potato game started!**").build()).setActionRows(new ActionRow[0]).queue();
                final String threadTitle = "<@" + gameHost + "> hotpotato; ";

                event.getHook().sendMessage("Creating a new thread").queue(createThreadMessage -> ((TextChannel) event.getChannel()).createThreadChannel(threadTitle, createThreadMessage.getIdLong()).queue(threadChannel -> {
                    messageHotPotatoHashMap.get(event.getMessageIdLong()).setThreadChannel(threadChannel);
                    createHotPotatoGame(messageHotPotatoHashMap.get(event.getMessageIdLong()));
                    messageHotPotatoHashMap.remove(event.getMessageIdLong());
                }));
            }
            else if (buttonName.startsWith("hotpotatoCancelButton")) {
                final EmbedBuilder eb = new EmbedBuilder();
                event.getMessage().editMessageEmbeds(eb.setTitle("**Coop game cancelled.**").build()).setActionRows(new ActionRow[0]).queue();
                event.getHook().sendMessage("Cancelled!").queue();
                messageHotPotatoHashMap.remove(event.getMessageIdLong());
            }

        }
    }

    public void loadSettings() {
        this.receivedRequestsUsers.clear();
        this.madeRequestsUsers.clear();
        this.anagramHashMap.clear();
        final MongoCursor<Document> guildList = Main.guilds.find().iterator();
        while (guildList.hasNext()) {
            final Document doc = guildList.next();
            this.guildSettingHashMap.put(doc.getLong("_id"), GuildSetting.fromDocument(doc));
            try {
                Main.jda.getGuildById(doc.getLong("_id")).updateCommands().addCommands(
                        Commands.slash("slashtest", "a test command")
                                .addOption(OptionType.INTEGER, "value", "converts value to string", true),
                        Commands.slash("abchelp", "Command to help you navigate Alphabot!")
                                .addOption(OptionType.INTEGER, "page", "which page of settings (1-4)"),
                        Commands.slash("abcsettings", "Current Alphabot settings"),
                        Commands.slash("abcprefreq", "Whether or not prefix is required to count")
                                .addOption(OptionType.STRING, "setting", "Prefix required (on/off)", true),
                        Commands.slash("abcanagram", "Anagram minigame")
                                .addOption(OptionType.STRING, "comp", "Competitive game of anagram against someone (enter id of opponent)", false)
                                .addOption(OptionType.STRING, "coop", "Cooperative game of anagram with someone (auto start yes/no)", false),
                        Commands.slash("abcpisetchannel", "Set channel for Pi counting!")
                                .addOption(OptionType.STRING, "channel", "Channel to count in", true),
                        Commands.slash("abchotpotato", "Hot potato word game!")
                ).queue();
            } catch (Exception ex) {
            }
        }
        guildList.close();
    }

    public void resetCount(final MessageChannel channel, final GuildSetting guildSetting, final Long userId, final String reason) {
        channel.sendMessage("<" + userId + "> ruined it!");
        guildSetting.setPrevCounterId(-1L);
        guildSetting.resetAlphabetCount();
        final EmbedBuilder eb = new EmbedBuilder();
        eb.setTitle("Count RUINED!");
        eb.appendDescription("Count ruined by <@" + userId + ">! " + reason + "\n\nCount has been reset to 'a'");
        eb.setColor(new Color(220, 46, 68));
        channel.sendMessageEmbeds(eb.build(), new MessageEmbed[0]).queue();
    }

    public static String getStringFromValue(final Long val) {
        if (val <= 0L) {
            return "N/A";
        }
        System.err.println(val);
        if (val <= 26L) {
            return Main.ALPHABET[(int) (val - 1L)];
        }
        Long exp;
        for (exp = 0L; val >= binPow(27L, exp); ++exp) {
        }
        --exp;
        final int idx = (int) (val / binPow(27L, exp)) - 1;
        final String currval = Main.ALPHABET[idx];
        return currval + getStringFromValue(val % binPow(27L, exp));
    }

    public static Long binPow(final Long a, final Long b) {
        if (b == 0L) {
            return 1L;
        }
        final Long ret = binPow(a, b / 2L);
        if (b % 2L != 0L) {
            return ret * ret * a;
        }
        return ret * ret;
    }

    public static boolean hasNumber(final String str) {
        for (int i = 0; i < str.length(); ++i) {
            if (Character.isDigit(str.charAt(i))) {
                return true;
            }
        }
        return false;
    }

    private Integer getRawScore(final int len) {
        return this.roundToHundreds(71.4286 * len * len + -234.286 * len + 372.857);
    }

    private Integer roundToHundreds(final Double num) {
        return 100 * Math.round((float) (int) (num / 100.0));
    }

    static {
        WordBank = new ArrayList<String>();
        ALPHABET = new String[]{"a", "b", "c", "d", "e", "f", "g", "h", "i", "j", "k", "l", "m", "n", "o", "p", "q", "r", "s", "t", "u", "v", "w", "x", "y", "z"};
        VOWELS = new String[]{"a", "a", "e", "e", "i", "i", "o", "o", "u"};
        CONSONANTS = new String[]{"b", "c", "d", "d", "f", "g", "h", "h", "h", "j", "k", "l", "l", "m", "n", "n", "n", "p", "q", "r", "r", "s", "s", "t", "t", "v", "w", "x", "y", "z"};
        PIVALUE = "31415926535897932384626433832795028841971693993751058209749445923078164062862089986280348253421170679821480865132823066470938446095505822317253594081284811174502841027019385211055596446229489549303819644288109756659334461284756482337867831652712019091456485669234603486104543266482133936072602491412737245870066063155881748815209209628292540917153643678925903600113305305488204665213841469519415116094330572703657595919530921861173819326117931051185480744623799627495673518857527248912279381830119491298336733624406566430860213949463952247371907021798609437027705392171762931767523846748184676694051320005681271452635608277857713427577896091736371787214684409012249534301465495853710507922796892589235420199561121290219608640344181598136297747713099605187072113499999983729780499510597317328160963185950244594553469083026425223082533446850352619311881710100031378387528865875332083814206171776691473035982534904287554687311595628638823537875937519577818577805321712268066130019278766111959092164201989";
    }
}