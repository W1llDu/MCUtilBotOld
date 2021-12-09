package io.github.w1ll_du.MCUtilsBot;

import javax.annotation.Nonnull;

import io.github.w1ll_du.MCUtilsBot.command.commands.fdLinkHandler;
import me.duncte123.botcommons.BotCommons;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.events.ReadyEvent;
import org.apache.commons.collections4.BidiMap;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.io.IOException;
import java.util.Map;

public class Listener extends ListenerAdapter {

    private static final Logger LOGGER = LoggerFactory.getLogger(Listener.class);
    private final CommandManager manager = new CommandManager();
    Map<String, String> conf;
    private final String prefix;
    private final String owner_id;
    private BidiMap<String, String> playerMap;

    public Listener(Map<String, String> conf, BidiMap<String, String> playerMap) {
        this.conf = conf;
        prefix = conf.get("prefix");
        owner_id = conf.get("owner_id");
        this.playerMap = playerMap;
    }

    @Override
    public void onReady(@Nonnull ReadyEvent event) {
        LOGGER.info("{} is ready.", event.getJDA().getSelfUser().getAsTag());
    }

    @Override
    public void onGuildMessageReceived(@NotNull GuildMessageReceivedEvent event) {
        String raw = event.getMessage().getContentRaw();
        if (event.getAuthor().getId().equals(conf.get("fdlink_bot_id"))) {
            fdLinkHandler.handle(event, playerMap, conf);
        }
        if (event.getChannel().getId().equals(conf.get("changelog_channel_id"))
            && ! event.getAuthor().getId().equals(owner_id)) {
            // TODO: only allow java and filter out bedrock
        }
        if (event.getAuthor().isBot()) {
            return;
        }
        if (event.isWebhookMessage()) {
            return;
        }
        if (! event.getGuild().getId().equals(conf.get("server_id"))) {
            return;
        }
        if (! event.getChannel().getId().equals(conf.get("bot_channel_id"))) {
            return;
        }
        if (! raw.startsWith(prefix)) return;
        if (raw.equals(prefix + "shutdown") && event.getAuthor().getId().equals(owner_id)) {
            LOGGER.info("Shutting down");
            event.getJDA().shutdown();
            BotCommons.shutdown(event.getJDA());
        }
        try {
            manager.handle(event, prefix, playerMap, conf);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}