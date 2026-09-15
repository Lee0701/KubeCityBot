package kr.kubecity.bot.minecraft;

import kr.kubecity.bot.BuildingApproval;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jspecify.annotations.NonNull;

public class BuildingApproveEvent extends Event {
    private static final HandlerList handlers = new HandlerList();

    @Override
    public @NonNull HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }

    private BuildingApproval approval;

    public BuildingApproveEvent(BuildingApproval approval) {
        this.approval = approval;
    }

    public BuildingApproval getApproval() {
        return approval;
    }

    public void setApproval(BuildingApproval approval) {
        this.approval = approval;
    }
}
