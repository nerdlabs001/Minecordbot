package us.cyrien.minecordbot.chat.listeners.discordListeners;

import net.dv8tion.jda.core.entities.ChannelType;
import net.dv8tion.jda.core.entities.TextChannel;
import net.dv8tion.jda.core.entities.User;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;
import net.dv8tion.jda.core.hooks.ListenerAdapter;
import org.bukkit.ChatColor;
import org.json.JSONArray;
import us.cyrien.minecordbot.Minecordbot;
import us.cyrien.minecordbot.chat.Messenger;
import us.cyrien.minecordbot.configuration.MCBConfig;
import us.cyrien.minecordbot.prefix.PrefixParser;

public abstract class TextChannelListener extends ListenerAdapter {

    private Minecordbot mcb;
    private Messenger messenger;
    protected MCBChannelType channelType;

    public TextChannelListener(Minecordbot mcb) {
        this.mcb = mcb;
        messenger = mcb.getMessenger();
        this.channelType = MCBChannelType.DEFAULT_CHANNEL;
    }

    public void onMessageReceived(MessageReceivedEvent event) {
        boolean self = event.getAuthor().equals(mcb.getJDA().getSelfUser());
        boolean blocked = prefixIsBlocked(event.getMessage().getContent()) || botIsBlocked(event.getAuthor().getId());
        if(self || blocked)
            return;
        switch (channelType) {
            case MOD_CHANNEL:
                String tcID = MCBConfig.get("mod_channel");
                TextChannel tc = mcb.getJDA().getTextChannelById(tcID);
                if(tc != null)
                    execute(event);
                break;
            case BOUND_CHANNEL:
                if (containsChannel(event.getTextChannel().getId()))
                    execute(event);
                break;
            case DEFAULT_CHANNEL:
                execute(event);
                break;
            case PRIVATE_CHANNEL:
                if (event.getChannelType() == ChannelType.PRIVATE)
                    execute(event);
                break;
        }
    }

    public abstract void execute(MessageReceivedEvent event);

    protected boolean botIsBlocked(String id) {
        JSONArray blockedB = MCBConfig.get("blocked_bots");
        if (mcb.getJDA().getUserById(id) != null) {
            User user = mcb.getJDA().getUserById(id);
            if (user.isBot()) {
                for (Object s : blockedB) {
                    if (user.getId().equals(s.toString().trim()))
                        return true;
                }
            }
        }
        return false;
    }

    protected boolean prefixIsBlocked(String message) {
        JSONArray blockedP = MCBConfig.get("blocked_command_prefix");
        for (Object p : blockedP) {
            if (message.startsWith(p.toString()))
                return true;
        }
        return false;
    }

    protected boolean containsChannel(String id) {
        JSONArray tcArray = MCBConfig.get("text_channels");
        if (tcArray != null) {
            for (Object s : tcArray)
                if (s.toString().equalsIgnoreCase(id))
                    return true;
        }
        return false;
    }

    protected void relayMessage(MessageReceivedEvent event) {
        String msg = event.getMessage().getContent();
        String prefix = PrefixParser.parseDiscordPrefixes(MCBConfig.get("message_prefix_minecraft"), event);
        getMessenger().sendGlobalMessageToMC(ChatColor.translateAlternateColorCodes('&', prefix + (MCBConfig.get("message_format") + msg)).trim());
    }

    public Messenger getMessenger() {
        return messenger;
    }

    public Minecordbot getMcb() {
        return mcb;
    }

public enum MCBChannelType {
    PRIVATE_CHANNEL,
    BOUND_CHANNEL,
    MOD_CHANNEL,
    DEFAULT_CHANNEL
}
}
