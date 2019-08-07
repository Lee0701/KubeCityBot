package city.kobaya.kobayabot.features;

import city.kobaya.kobayabot.IconStorage;
import city.kobaya.kobayabot.KobayaBotPlugin;
import city.kobaya.kobayabot.KobayaPlayer;
import city.kobaya.kobayabot.discord.message.ForwarderMessage;
import city.kobaya.kobayabot.discord.message.TranslatedMessage;
import io.github.ranolp.rattranslate.Locale;
import io.github.ranolp.rattranslate.RatPlayer;
import io.github.ranolp.rattranslate.RatTranslate;
import io.github.ranolp.rattranslate.lang.LangStorage;
import io.github.ranolp.rattranslate.lang.Variable;
import io.github.ranolp.rattranslate.translator.Translator;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.TextChannel;
import net.dv8tion.jda.core.entities.User;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class TranslatorForwarder extends Forwarder {

    private List<String> languages = new ArrayList<>();

    @Override
    public void reload(JavaPlugin plugin) {
        super.reload(plugin);
        languages = getConfigurationSection().getStringList("languages");
    }

    @Override
    public void save() {

    }

    @Override
    public void forwardFromMinecraft(Player player, String message) {
        String name = player.getName();
        KobayaBotPlugin.getInstance().getBot().sendDiscordMessages(
                channels,
                channel -> new TranslatedMessage("Minecraft", channel, name, message, RatPlayer.of(player).getLocale(), IconStorage.getIconFor(player.getUniqueId()), false)
        );
    }

    @Override
    public void forwardFromDiscord(Message message) {
        TextChannel channel = message.getTextChannel();
        User author = message.getAuthor();
        String text = message.getContentDisplay();

        if(!channels.contains(channel.getId())) return;

        String username = message.getMember().getEffectiveName();
        String minecraftName = username;

        Bukkit.getLogger()
                .info(String.format("[%s](%s)<%s>: %s", "Discord", channel.getName(), username, text));

        String format = "[Discord] <%s> %s";

        KobayaPlayer kobayaPlayer = KobayaPlayer.of(author.getId());
        if(kobayaPlayer.getUuid() != null) {
            if(kobayaPlayer.getChatFormat() != null) format = "[Discord] " + kobayaPlayer.getChatFormat();
            if(kobayaPlayer.getNickname() != null) minecraftName = kobayaPlayer.getNickname();
        }
        String finalFormat = format;
        String finalName = minecraftName;
        String originalMessage = String.format(format, minecraftName, text);

        Set<RatPlayer> recipients = Bukkit.getServer().getOnlinePlayers().stream()
                .map(RatPlayer::of)
                .collect(Collectors.toSet());

        try {
            LangStorage langStorage = RatTranslate.getInstance().getLangStorage();
            Translator translator = RatTranslate.getInstance().getTranslator();

            // Translate message.
            Map<Locale, String> translateMap = recipients.stream()
                    .map(RatPlayer::getLocale)
                    .distinct()
                    .collect(Collectors.toMap(locale -> locale,
                            locale -> String.format(finalFormat, finalName, translator.translateAuto(text, locale))
                    ));

            // Send minecraft messages.
            for(RatPlayer recipient : recipients) {
                if(recipient.getTranslateMode()) {
                    String translated = translateMap.get(recipient.getLocale());
                    if(RatTranslate.getInstance().isJsonMessageAvailable()) {
                        String hover = recipient.format(langStorage, "chat.original",
                                Variable.ofAny("hover", "text", text),
                                Variable.ofAny("hover", "lang", "auto")
                        );
                        recipient.sendHoverableMessage(translated, hover);
                    } else {
                        recipient.sendMessage(translated);
                    }
                } else {
                    recipient.sendMessage(originalMessage);
                }
            }

            // Send discord message.
            KobayaBotPlugin.getInstance().getBot().sendDiscordMessage(
                    new TranslatedMessage("Discord", channel, username, text, null, IconStorage.getIconFor(author), false));

            // Delete original message.
            message.delete().queue();

        } catch(NoClassDefFoundError error) {
            KobayaBotPlugin.getInstance().getLogger().warning("Translator forwarder requires RatTranslate.");
            error.printStackTrace();
            return;
        }
    }

    @Override
    public ConfigurationSection getConfigurationSection() {
        return KobayaBotPlugin.getInstance().getConfig().getConfigurationSection("translator-forwarder");
    }

    public List<String> getLanguages() {
        return languages;
    }
}
