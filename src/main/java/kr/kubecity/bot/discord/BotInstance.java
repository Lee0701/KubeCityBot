package kr.kubecity.bot.discord;

import kr.kubecity.bot.KubeCityBotPlugin;
import kr.kubecity.bot.discord.message.DiscordMessage;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.utils.ChunkingFilter;
import net.dv8tion.jda.api.utils.MemberCachePolicy;

import java.util.List;
import java.util.function.Function;

public class BotInstance {
    private JDA jda;
    private DiscordMessageSender discordMessageSender;

    public void launch(String token) {
        if(jda != null) jda.shutdown();
        jda = JDABuilder.createDefault(token)
                .enableIntents(GatewayIntent.GUILD_MEMBERS)
                .setMemberCachePolicy(MemberCachePolicy.ALL)
                .setChunkingFilter(ChunkingFilter.ALL)
                .build();
        jda.addEventListener(new ReadyListener());
        jda.addEventListener(new DiscordChatListener());

        discordMessageSender = new DiscordMessageSender();
        discordMessageSender.runTaskTimerAsynchronously(KubeCityBotPlugin.getInstance(), 0, 20);
    }

    public void shutdown() {
        jda.shutdown();
        jda = null;
    }

    public void sendDiscordMessage(DiscordMessage message) {
        discordMessageSender.offerMessage(message);
    }

    public void sendDiscordMessages(List<String> channels, Function<TextChannel, DiscordMessage> messageGenerator) {
        Guild guild = getGuild();
        for(String channelId : channels) {
            TextChannel textChannel = guild.getTextChannelById(channelId);
            sendDiscordMessage(messageGenerator.apply(textChannel));
        }
    }

    public Guild getGuild() {
        return jda.getGuildById(KubeCityBotPlugin.getInstance().getServerId());
    }

    public JDA getJda() {
        return jda;
    }

}
