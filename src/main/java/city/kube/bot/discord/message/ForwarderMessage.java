package city.kube.bot.discord.message;

import city.kube.bot.PlayerIcon;

public final class ForwarderMessage {
    private final String nickname;
    private final PlayerIcon icon;
    private final String side;
    private final boolean linked;
    private final String message;

    public ForwarderMessage(String nickname, PlayerIcon icon, String side, boolean linked, String message) {
        this.nickname = nickname;
        this.icon = icon;
        this.side = side;
        this.linked = linked;
        this.message = message;
    }

    public String getNickname() {
        return nickname;
    }

    public PlayerIcon getIcon() {
        return icon;
    }

    public String getSide() {
        return side;
    }

    public boolean isLinked() {
        return linked;
    }

    public String getMessage() {
        return message;
    }
}
