// this is a test
import com.mongodb.ConnectionString;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.model.ReplaceOptions;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.GuildChannel;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.events.ReadyEvent;
import net.dv8tion.jda.api.events.guild.GuildJoinEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import org.bson.Document;
import org.jetbrains.annotations.NotNull;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Scanner;

import static com.mongodb.client.model.Filters.eq;

public class Main extends ListenerAdapter {
    public static JDA jda;
    public static final String TEXT_CYAN = "\u001B[36m";
    public static final String TEXT_RESET = "\u001B[0m";
    private static MongoCollection<Document> guilds;
    private final HashMap<Long, GuildSetting> guildSettingHashMap;
    private final ReplaceOptions options = new ReplaceOptions().upsert(true);
    private static String[] ALPHABET = new String[]{"a", "b", "c", "d", "e", "f", "g", "h", "i", "j", "k", "l", "m", "n", "o", "p", "q", "r", "s", "t", "u", "v", "w", "x", "y", "z"};

    public static void main(String[] args) throws IOException {
        Scanner config = new Scanner(new File("Config.txt"));
        try {
            jda = JDABuilder.createDefault(config.nextLine()).build();
            System.out.println("Logged in as " + jda.getSelfUser().getName() + "#" + jda.getSelfUser().getDiscriminator());
        } catch (Exception e) {
            System.out.println("Login failed");
            e.printStackTrace();
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
            else if (message.startsWith("start")) {
                channel.sendMessage("Counting will now be enabled").queue();
                currentGuildSetting.setTrackCounting(true);
            }
        }
        else {
            if(message.startsWith(currentGuildSetting.getCountingPrefix())) {
                if (channel.getIdLong() == currentGuildSetting.getChannel()) {
                    if (currentGuildSetting.isTrackCounting()) {
                        message = message.substring(currentGuildSetting.getCountingPrefix().length()).split(" ", 2)[0];

                        if (event.getMember().getIdLong() == currentGuildSetting.getPrevCounterId()) {
                            event.getMessage().addReaction("U+274C").queue();
                            resetCount(channel, currentGuildSetting, event.getMember().getIdLong(), "You can't count twice in a row! Let your friends count too... if you have any.");
                            guilds.replaceOne(eq("_id", event.getGuild().getIdLong()), currentGuildSetting.toDocument(event.getGuild().getIdLong()), options);
                            return;
                        }

                        if (hasNumber(message)) {
                            event.getMessage().addReaction("U+274C").queue();
                            resetCount(channel, currentGuildSetting, event.getMember().getIdLong(), "Incorrect count!");
                            guilds.replaceOne(eq("_id", event.getGuild().getIdLong()), currentGuildSetting.toDocument(event.getGuild().getIdLong()), options);
                            return;
                        }

                        currentGuildSetting.setPrevCounterId(event.getMember().getIdLong());

                        if (getValueFromString(message) != (currentGuildSetting.getAlphabetCount() + 1L)) {
                            event.getMessage().addReaction("U+274C").queue();
                            resetCount(channel, currentGuildSetting, event.getMember().getIdLong(), "Incorrect count!");
                            guilds.replaceOne(eq("_id", event.getGuild().getIdLong()), currentGuildSetting.toDocument(event.getGuild().getIdLong()), options);
                        }
                        else {
                            event.getMessage().addReaction("U+2705").queue();
                            currentGuildSetting.incAlphabetCount();
                            guilds.replaceOne(eq("_id", event.getGuild().getIdLong()), currentGuildSetting.toDocument(event.getGuild().getIdLong()), options);
                        }
                    }
                    else {
                        channel.sendMessage("Counting is currently disabled").queue();
                    }
                }
                else {
                    channel.sendMessage("Please setup a channel for counting!").queue();
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
            String message = event.getOption("print_message").getAsString();

            if(message != null) {
                event.getHook().sendMessage(message).queue();
            }
        }
        else if(event.getName().equals("abchelp")) {
            EmbedBuilder eb = new EmbedBuilder().setTitle("ALPHABOT HELP").setDescription("**Use `/abcsettings` to see current Alphabot settings and prefixes**\n\n"
                    + "**Setup Commands**\n"
                    + "◈ `getChannel` prints the channel that counting is tracked in\n"
                    + "◈ `getCountingPrefix` prints the prefix for counting\n"
                    + "◈ `getPrefix` prints the prefix for bot commands\n"
                    + "◈ `setChannel` sets the channel that counting will be tracked in\n"
                    + "◈ `setCountingPrefix` sets the prefix for counting\n"
                    + "◈ `setPrefix` sets the prefix for bot commands\n\n"
                    + "**Counting Commands**\n"
                    + "◈ `start` - enables counting\n"
                    + "◈ `end` - disables counting\n");
            eb.setFooter("coming to a alphabot near you");

            event.getHook().sendMessageEmbeds(eb.build()).queue();;
        }
        else if(event.getName().equals("abcsettings")) {
            EmbedBuilder eb = new EmbedBuilder().setTitle("ALPHABOT SETTINGS");
            String currCount = getStringFromValue(guildSettingHashMap.get(event.getGuild().getIdLong()).getAlphabetCount());
            String isTracking = "";
            String prevCounterString = "";

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

            eb.setDescription("**Current Count: " + currCount + "**\n\n"
                    + "**Guild Settings**\n"
                    + "◈ Tracking counting: " + isTracking + "\n"
                    + "◈ Current counting prefix: `" + guildSettingHashMap.get(event.getGuild().getIdLong()).getCountingPrefix() + "`\n"
                    + "◈ Current setting prefix: `" + guildSettingHashMap.get(event.getGuild().getIdLong()).getSettingsPrefix() + "`\n"
                    + "◈ Counting Channel: " + "<#" + guildSettingHashMap.get(event.getGuild().getIdLong()).getChannel() + ">" + "\n"
                    + "◈ Previous Counter: " + prevCounterString + "\n");

            event.getHook().sendMessageEmbeds(eb.build()).queue();
        }
    }

    public void loadSettings() {
        MongoCursor<Document> guildList = guilds.find().iterator();
        while (guildList.hasNext()) {
            Document doc = guildList.next();
            guildSettingHashMap.put(doc.getLong("_id"), GuildSetting.fromDocument(doc));

            jda.getGuildById(doc.getLong("_id")).updateCommands().addCommands(
                    Commands.slash("slashtest", "a test command")
                            .addOption(OptionType.STRING, "print_message", "prints a given message", true),

                    Commands.slash("abchelp", "Command to help you navigate Alphabot!"),

                    Commands.slash("abcsettings", "Current Alphabot settings")
            ).queue();
        }
        guildList.close();
    }

    @Override
    public void onReady(@NotNull ReadyEvent event) {
        System.err.println(TEXT_CYAN + "Setting up Slash commmands");
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
        System.err.println(val);
        if(val == 0L) {
            return "";
        }
        Long exp = 0L;
        while(true) {
            if(val < binPow(26L, exp)) {
                break;
            }
            ++exp;
        }
        --exp;

        int idx = (int)(val / binPow(26L, exp)) - 1;
        String currval = ALPHABET[idx];
        return (currval) + (getStringFromValue(val % binPow(26L, exp)));
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
