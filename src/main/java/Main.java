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
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import org.bson.Document;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.*;

import static com.mongodb.client.model.Filters.eq;

public class Main extends ListenerAdapter {
    public static JDA jda;
    private static final Long ADMIN = 524989303206707214L;
    public static final String TEXT_CYAN = "\u001B[36m";
    public static final String TEXT_RESET = "\u001B[0m";
    public static final Integer GAME_TOTAL_TIME = 60;
    public static final Integer GAME_LOAD_TIME = 10;
    public static final Integer GAME_CLOSE_TIME = 5;
    public static final Integer NUM_LETTERS = 15;
    public static final Integer NUM_VOWELS = 5;
    private static final int VALID = 1;
    private static final int INVALID = -1;
    private static final int USED = 0;
    private static MongoCollection<Document> guilds;
    private static final Random rand = new Random();

    private final HashMap<Long, GuildSetting> guildSettingHashMap;
    private final HashMap<String, AnagramSetting> anagramHashMap = new HashMap<>();
    private static final HashSet<String> Words = new HashSet<>();
    private final ReplaceOptions options = new ReplaceOptions().upsert(true);
    private static String[] ALPHABET = new String[]{"a", "b", "c", "d", "e", "f", "g", "h", "i", "j", "k", "l", "m", "n", "o", "p", "q", "r", "s", "t", "u", "v", "w", "x", "y", "z"};
    private static String[] VOWELS = new String[]{"a", "e", "i", "o", "u"};
    private static String[] CONSONANTS = new String[]{"b", "c", "d", "f", "g", "h", "j", "k", "l", "m", "n", "p", "q", "r", "s", "t", "v", "w", "x", "y", "z"};
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
        GuildChannel guildChannel = event.getGuildChannel();

        if (message == null) {
            return;
        }
        System.err.println(TEXT_CYAN + "Message received: " + message + " in channel " + channel.getIdLong());

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
                    return;
                }
                if (currAnagramSetting.getMemberId() != event.getAuthor().getIdLong()) {
                    event.getThreadChannel().deleteMessageById(thread.getLatestMessageId()).queue();
                    return;
                }

                if (!currAnagramSetting.isActive()) {
                    thread.sendMessage("Please wait for the game to start!").queue();
                    return;
                }

                if(message.startsWith(currentGuildSetting.getSettingsPrefix())) {
                    message = message.substring(currentGuildSetting.getSettingsPrefix().length());
                    if(message.startsWith("quit")) {
                        currAnagramSetting.setCountTimer(0);
                        return;
                    }
                }

//                currAnagram.setLastMessage(message);
                if(!Words.contains(message)) {
                    event.getMessage().addReaction("U+274C").queue();
                    return;
                }

                Integer validity = currAnagramSetting.isValid(message.toLowerCase());
                if(validity == INVALID) {
                    event.getMessage().addReaction("U+274C").queue();
                }
                else if(validity == VALID) {
                    currAnagramSetting.updateScore(message.length());
                    currAnagramSetting.setLastMessage(message);
                    event.getMessage().addReaction("U+2705").queue();
                }
                else if(validity == USED) {
                    event.getMessage().addReaction("U+1F7E1").queue();
                }

//                event.getChannel().deleteMessageById(thread.getLatestMessageId()).queue();
            }
        }

        if (message.startsWith(currentGuildSetting.getSettingsPrefix())) {
            message = message.substring(currentGuildSetting.getSettingsPrefix().length());
            if (message.startsWith("anagram")) {
                if(anagramHashMap.containsKey("<@" + event.getMember().getIdLong() + "> anagram")) {
                    channel.sendMessage("You already have a game running currently!").queue();
                    return;
                }
                channel.sendMessage("Creating a new thread").queue();
                Long threadMessageId = event.getChannel().getLatestMessageIdLong();


                Timer startingTimer = new java.util.Timer();

                String letters = "";
                String title = "<@" + event.getMember().getIdLong() + "> anagram; ";
                for(int i = 0; i < NUM_LETTERS; ++i) {
                    if(i < NUM_VOWELS) {
                        letters += VOWELS[rand.nextInt(5)];
                    }
                    else {
                        letters += CONSONANTS[rand.nextInt(21)];
                    }
                }

                char tempArray[] = letters.toCharArray();
                Arrays.sort(tempArray);
                letters = new String(tempArray);

                for(int i = 0; i < NUM_LETTERS; ++i) {
                    title += (String.valueOf(letters.charAt(i)).toUpperCase() + " ");
                }

                final String anagramLetters = letters;

                ((TextChannel) event.getChannel()).createThreadChannel(title, threadMessageId).queue(threadChannel -> {
                    anagramHashMap.put("<@" + event.getMember().getIdLong() + "> anagram", new AnagramSetting(GAME_TOTAL_TIME, threadChannel, event.getMember().getIdLong(), anagramLetters));

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
                                        if(loadClock != 0) {
                                            anagrameb.setDescription("Starting game in " + loadClock + " seconds.\nTry and form as many words as possible using the letters in the title of this thread!\n**Letters: " + anagramLetters + "**");
                                            threadChannel.editMessageEmbedsById(countdownMessageId, anagrameb.build()).queue();
                                            --loadClock;
                                        }
                                        else {
                                            Timer gameTimer = new java.util.Timer();
                                            AnagramSetting currentAnagramSetting = anagramHashMap.get("<@" + event.getMember().getIdLong() + "> anagram");
                                            threadChannel.editMessageEmbedsById(countdownMessageId, currentAnagramSetting.ebBuild()).queue();
                                            currentAnagramSetting.decrementCountTimer();
                                            currentAnagramSetting.setActive(true);
                                            gameTimer.scheduleAtFixedRate(
                                                    new java.util.TimerTask() {
                                                        @Override
                                                        public void run() {
                                                            if(currentAnagramSetting.getCountTimer() != 0) {
                                                                if(currentAnagramSetting.getCountTimer() == GAME_TOTAL_TIME / 2) {
                                                                    threadChannel.sendMessage("You have " + currentAnagramSetting.getCountTimer() + " seconds left!").queue();
                                                                }
                                                                if(currentAnagramSetting.getCountTimer() <= 5) {
                                                                    threadChannel.sendMessage("You have " + currentAnagramSetting.getCountTimer() + " seconds left!").queue();
                                                                }
//                                                                threadChannel.deleteMessageById(countdownMessageId).queue();
//                                                                threadChannel.sendMessageEmbeds(currentAnagramSetting.ebBuild()).queue();
                                                                threadChannel.editMessageEmbedsById(countdownMessageId, currentAnagramSetting.ebBuild()).queue();
                                                                currentAnagramSetting.decrementCountTimer();
                                                            }
                                                            else {
                                                                currentAnagramSetting.setActive(false);
                                                                currentAnagramSetting.setGameOver(true);
                                                                Timer endingTimer = new java.util.Timer();
                                                                EmbedBuilder endingEmbed = new EmbedBuilder();
                                                                EmbedBuilder closingEmbed = new EmbedBuilder();
                                                                closingEmbed.setTitle("**Game is over!**");
                                                                threadChannel.sendMessageEmbeds(closingEmbed.setDescription("Thread closing in " + GAME_CLOSE_TIME + " seconds.\nA summary of this game will be reported in <#" + event.getChannel().getIdLong() + "> after this thread is closed\n").build()).queue(closingEmbedMessage -> {
                                                                    Long closingThreadMessageId = closingEmbedMessage.getIdLong();
                                                                    endingTimer.scheduleAtFixedRate(
                                                                            new java.util.TimerTask() {
                                                                                Integer closeClock = GAME_CLOSE_TIME - 1;

                                                                                @Override
                                                                                public void run() {
                                                                                    if (closeClock != 0) {
                                                                                        closingEmbed.setTitle("**Game is over!**");
                                                                                        closingEmbed.setDescription("Thread closing in " + closeClock + " seconds.\nA summary of this game will be reported in <#" + event.getChannel().getIdLong() + "> after this thread is closed\n");
                                                                                        threadChannel.editMessageEmbedsById(closingThreadMessageId, closingEmbed.build()).queue();
                                                                                        --closeClock;
                                                                                    } else {
                                                                                        endingEmbed.setTitle("**Game is over!**");
                                                                                        endingEmbed.setColor(currentAnagramSetting.getColor());
                                                                                        String embedDescription = "<@" + event.getMember().getId() + "> Your final score was: " + currentAnagramSetting.getScore() + "\nYour rank is: " + currentAnagramSetting.getRank() +
                                                                                                "\n\nWords you found were: \n";
                                                                                        Iterator<String> it = currentAnagramSetting.getUsedWords().iterator();
                                                                                        while (it.hasNext()) {
                                                                                            embedDescription += it.next() + "\n";
                                                                                        }
                                                                                        System.err.println(currentAnagramSetting.getRank());

                                                                                        endingEmbed.setFooter("Congrats!");
                                                                                        event.getChannel().sendMessageEmbeds(endingEmbed.setDescription(embedDescription).build()).queue();
                                                                                        anagramHashMap.remove("<@" + event.getMember().getIdLong() + "> anagram");
                                                                                        threadChannel.delete().queue();
                                                                                        endingTimer.cancel();
                                                                                    }
                                                                                }
                                                                            }, 1000, 1000
                                                                    );
                                                                    gameTimer.cancel();
                                                                });
                                                            }
                                                        }
                                                    }, 1000, 1000
                                            );
                                            startingTimer.cancel();
                                        }
                                    }
                                }, 1000, 1000
                        );
                    });
                });
            }
            else if (message.startsWith("end")) {
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

                        String pattern = "([a-zA-Z])\\1*";

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
            EmbedBuilder eb = new EmbedBuilder().setTitle("ALPHABOT HELP").setDescription("**Use `/abcsettings` to see current Alphabot settings and prefixes (note that commands without slash use the prefix)**\n\n"
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
            eb.setFooter("coming to a alphabot near you");

            event.getHook().sendMessageEmbeds(eb.build()).queue();;
        }
        else if(event.getName().equals("abcsettings")) {
            EmbedBuilder eb = new EmbedBuilder().setTitle("ALPHABOT SETTINGS");
            String currCount = getStringFromValue(guildSettingHashMap.get(event.getGuild().getIdLong()).getAlphabetCount());
            String isTracking = "";
            String prevCounterString = "";
            String prefReq = "";

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

            event.getHook().sendMessageEmbeds(eb.build()).queue();
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
    }

    public void loadSettings() {
        MongoCursor<Document> guildList = guilds.find().iterator();
        while (guildList.hasNext()) {
            Document doc = guildList.next();
            guildSettingHashMap.put(doc.getLong("_id"), GuildSetting.fromDocument(doc));

            jda.getGuildById(doc.getLong("_id")).updateCommands().addCommands(
                    Commands.slash("slashtest", "a test command")
                            .addOption(OptionType.INTEGER, "value", "converts value to string", true),

                    Commands.slash("abchelp", "Command to help you navigate Alphabot!"),

                    Commands.slash("abcsettings", "Current Alphabot settings"),

                    Commands.slash("abcprefreq", "Whether or not prefix is required to count")
                            .addOption(OptionType.STRING, "setting", "Prefix required (on/off)", true)
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
}
