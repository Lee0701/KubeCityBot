package city.kube.bot;

import net.dv8tion.jda.api.entities.Icon;

public class PlayerIcon {
    private final String url;
    private final Icon icon;

    public PlayerIcon(String url, Icon icon) {
        this.url = url;
        this.icon = icon;
    }

    public String getUrl() {
        return url;
    }

    public Icon getIcon() {
        return icon;
    }
}
