package city.kobaya.kobayabot.discord.message;

import city.kobaya.kobayabot.KobayaBotPlugin;
import city.kobaya.kobayabot.features.TranslatorForwarder;
import io.github.ranolp.rattranslate.Locale;
import io.github.ranolp.rattranslate.RatTranslate;
import io.github.ranolp.rattranslate.translator.Translator;
import net.dv8tion.jda.core.MessageBuilder;
import net.dv8tion.jda.core.entities.Icon;
import net.dv8tion.jda.core.entities.TextChannel;

import java.util.EnumMap;
import java.util.stream.Collectors;

public class TranslatedMessage extends WebhookMessge {
    private static final EnumMap<Locale, String> DISCORD_PREFIXES = new EnumMap<>(Locale.class);

    static {
        DISCORD_PREFIXES.put(Locale.AFRIKAANS, ":flag_za:");
        DISCORD_PREFIXES.put(Locale.ARABIC, ":flag_sa:");
        DISCORD_PREFIXES.put(Locale.ASTURIAN, ":flag_es:");
        DISCORD_PREFIXES.put(Locale.AZERBAIJANI, ":flag_az:");
        DISCORD_PREFIXES.put(Locale.BELARUSIAN, ":flag_by:");
        DISCORD_PREFIXES.put(Locale.BULGARIAN, ":flag_bg:");
        DISCORD_PREFIXES.put(Locale.BRETON, ":flag_fr:");
        DISCORD_PREFIXES.put(Locale.CATALAN, ":flag_es:");
        DISCORD_PREFIXES.put(Locale.CZECH, ":flag_cz:");
        DISCORD_PREFIXES.put(Locale.WELSH, ":flag_gb:");
        DISCORD_PREFIXES.put(Locale.DANISH, ":flag_dk:");
        DISCORD_PREFIXES.put(Locale.AUSTRIAN_GERMAN, ":flag_at:");
        DISCORD_PREFIXES.put(Locale.GERMAN, ":flag_de:");
        DISCORD_PREFIXES.put(Locale.GREEK, ":flag_gr:");
        DISCORD_PREFIXES.put(Locale.AUSTRALIAN_ENGLISH, ":flag_au:");
        DISCORD_PREFIXES.put(Locale.CANADIAN_ENGLISH, ":flag_ca:");
        DISCORD_PREFIXES.put(Locale.BRITISH_ENGLISH, ":flag_gb:");
        DISCORD_PREFIXES.put(Locale.NEW_ZEALAND_ENGLISH, ":flag_nz:");
        DISCORD_PREFIXES.put(Locale.BRITISH_ENGLISH_UPSIDE_DOWN, ":flag_gb:");
        DISCORD_PREFIXES.put(Locale.PIRATE_ENGLISH, ":skull_crossbones:");
        DISCORD_PREFIXES.put(Locale.AMERICAN_ENGLISH, ":flag_us:");
        DISCORD_PREFIXES.put(Locale.ESPERANTO, ":flag_uy:");
        DISCORD_PREFIXES.put(Locale.ARGENTINIAN_SPANISH, ":flag_ar:");
        DISCORD_PREFIXES.put(Locale.SPANISH, ":flag_es:");
        DISCORD_PREFIXES.put(Locale.MEXICAN_SPANISH, ":flag_mx:");
        DISCORD_PREFIXES.put(Locale.URUGUAYAN_SPANISH, ":flag_uy:");
        DISCORD_PREFIXES.put(Locale.VENEZUELAN_SPANISH, ":flag_ve:");
        DISCORD_PREFIXES.put(Locale.ESTONIAN, ":flag_ee:");
        DISCORD_PREFIXES.put(Locale.BASQUE, ":flag_es:");
        DISCORD_PREFIXES.put(Locale.PERSIAN, ":flag_ir:");
        DISCORD_PREFIXES.put(Locale.FINNISH, ":flag_fi:");
        DISCORD_PREFIXES.put(Locale.FILIPINO, ":flag_ph:");
        DISCORD_PREFIXES.put(Locale.FAROESE, ":flag_fo:");
        DISCORD_PREFIXES.put(Locale.FRENCH, ":flag_fr:");
        DISCORD_PREFIXES.put(Locale.CANADIAN_FRENCH, ":flag_ca:");
        DISCORD_PREFIXES.put(Locale.FRISIAN, ":flag_nl:");
        DISCORD_PREFIXES.put(Locale.IRISH, ":flag_ie:");
        DISCORD_PREFIXES.put(Locale.SCOTTISH_GAELIC, ":flag_gb:");
        DISCORD_PREFIXES.put(Locale.GALICIAN, ":flag_es:");
        DISCORD_PREFIXES.put(Locale.MANX, ":flag_im:");
        DISCORD_PREFIXES.put(Locale.HAWAIIAN, "Hawaiian");
        DISCORD_PREFIXES.put(Locale.HEBREW, ":flag_il:");
        DISCORD_PREFIXES.put(Locale.HINDI, ":flag_in:");
        DISCORD_PREFIXES.put(Locale.CROATIAN, ":flag_hr:");
        DISCORD_PREFIXES.put(Locale.HUNGARIAN, ":flag_hu:");
        DISCORD_PREFIXES.put(Locale.ARMENIAN, ":flag_am:");
        DISCORD_PREFIXES.put(Locale.INDONESIAN, ":flag_id:");
        DISCORD_PREFIXES.put(Locale.ICELANDIC, ":flag_is:");
        DISCORD_PREFIXES.put(Locale.IDO, ":flag_io:");
        DISCORD_PREFIXES.put(Locale.ITALIAN, ":flag_it:");
        DISCORD_PREFIXES.put(Locale.JAPANESE, ":flag_jp:");
        DISCORD_PREFIXES.put(Locale.LOJBAN, "Lojban");
        DISCORD_PREFIXES.put(Locale.GEORGIAN, ":flag_ge:");
        DISCORD_PREFIXES.put(Locale.KOREAN, ":flag_kr:");
        DISCORD_PREFIXES.put(Locale.KOLSCH_OR_RIPUARIAN, ":flag_de:");
        DISCORD_PREFIXES.put(Locale.CORNISH, ":flag_gb:");
        DISCORD_PREFIXES.put(Locale.LATIN, ":flag_va:");
        DISCORD_PREFIXES.put(Locale.LUXEMBOURGISH, ":flag_lu:");
        DISCORD_PREFIXES.put(Locale.LIMBURGISH, ":flag_li:");
        DISCORD_PREFIXES.put(Locale.LOLCAT, ":flag_us: LOLCAT");
        DISCORD_PREFIXES.put(Locale.LITHUANIAN, ":flag_lt:");
        DISCORD_PREFIXES.put(Locale.LATVIAN, ":flag_lv:");
        DISCORD_PREFIXES.put(Locale.MAORI, ":flag_nz:");
        DISCORD_PREFIXES.put(Locale.MACEDONIAN, ":flag_mk:");
        DISCORD_PREFIXES.put(Locale.MONGOLIAN, ":flag_mn:");
        DISCORD_PREFIXES.put(Locale.MALAY, ":flag_my:");
        DISCORD_PREFIXES.put(Locale.MALTESE, ":flag_mt:");
        DISCORD_PREFIXES.put(Locale.LOW_GERMAN, ":flag_de:");
        DISCORD_PREFIXES.put(Locale.DUTCH, ":flag_nl:");
        DISCORD_PREFIXES.put(Locale.NORWEGIAN_NYNORSK, ":flag_no:");
        DISCORD_PREFIXES.put(Locale.NORWEGIAN1, ":flag_no:");
        DISCORD_PREFIXES.put(Locale.NORWEGIAN2, ":flag_no:");
        DISCORD_PREFIXES.put(Locale.OCCITAN, ":flag_fr:");
        DISCORD_PREFIXES.put(Locale.POLISH, ":flag_pl:");
        DISCORD_PREFIXES.put(Locale.BRAZILIAN_PORTUGUESE, ":flag_br:");
        DISCORD_PREFIXES.put(Locale.PORTUGUESE, ":flag_pt:");
        DISCORD_PREFIXES.put(Locale.QUENYA, "Quenya");
        DISCORD_PREFIXES.put(Locale.ROMANIAN, ":flag_ro:");
        DISCORD_PREFIXES.put(Locale.RUSSIAN, ":flag_ru:");
        DISCORD_PREFIXES.put(Locale.NORTHERN_SAMI, ":flag_no:");
        DISCORD_PREFIXES.put(Locale.SLOVAK, ":flag_sk:");
        DISCORD_PREFIXES.put(Locale.SLOVENIAN, ":flag_si:");
        DISCORD_PREFIXES.put(Locale.SOMALI, ":flag_so:");
        DISCORD_PREFIXES.put(Locale.ALBANIAN, ":flag_al:");
        DISCORD_PREFIXES.put(Locale.SERBIAN, ":flag_rs:");
        DISCORD_PREFIXES.put(Locale.SWEDISH, ":flag_se:");
        DISCORD_PREFIXES.put(Locale.SWABIAN_GERMAN, ":flag_de:");
        DISCORD_PREFIXES.put(Locale.THAI, ":flag_th:");
        DISCORD_PREFIXES.put(Locale.TAGALOG, ":flag_ph:");
        DISCORD_PREFIXES.put(Locale.KLINGON, "`Klingon");
        DISCORD_PREFIXES.put(Locale.TURKISH, ":flag_tr:");
        DISCORD_PREFIXES.put(Locale.TALOSSAN, "Talossan");
        DISCORD_PREFIXES.put(Locale.UKRAINIAN, ":flag_ua:");
        DISCORD_PREFIXES.put(Locale.VALENCIAN, ":flag_es:");
        DISCORD_PREFIXES.put(Locale.VIETNAMESE, ":flag_vi:");
        DISCORD_PREFIXES.put(Locale.SIMPLIFIED_CHINESE, ":flag_cn:");
        DISCORD_PREFIXES.put(Locale.TRADITIONAL_CHINESE, ":flag_tw:");
    }

    public TranslatedMessage(String side, TextChannel channel, String nickname, String message, Locale fromLocale, Icon icon, boolean linked) {
        super(channel, nickname);

        String translatedMessage;
        try {
            TranslatorForwarder forwarder = KobayaBotPlugin.getInstance().getFeature(TranslatorForwarder.class).orElseThrow(NullPointerException::new);
            Translator translator = RatTranslate.getInstance().getTranslator();
            boolean auto = fromLocale == null;
            translatedMessage = forwarder.getLanguages().stream()
                    .map(Locale::getByCode)
                    .map(toLocale -> {
                        String translated = auto
                                ? translator.translateAuto(message, toLocale)
                                : translator.translate(message, fromLocale, toLocale);
                        if(DISCORD_PREFIXES.containsKey(toLocale)) {
                            translated = DISCORD_PREFIXES.get(toLocale) + " " + translated;
                        }
                        return  translated;
                    })
                    .collect(Collectors.joining("\n"));
        } catch(NoClassDefFoundError error) {
            KobayaBotPlugin.getInstance().getLogger().warning("Translator forwarder requires RatTranslate.");
            error.printStackTrace();
            translatedMessage = message;
        } catch(NullPointerException ignore) {
            return;
        }
        setAvatar(icon);
        setMessage(
                new MessageBuilder().setContent(String.format("[%s]%s\n%s", side, linked ? " :link:" : "", translatedMessage))
                        .build());
    }
}
