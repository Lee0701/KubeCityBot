package kr.kubecity.bot;

import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.jspecify.annotations.NonNull;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class BuildingApproval implements ConfigurationSerializable {
    public static final Map<Integer, BuildingApproval> BUILDING_APPROVALS = new HashMap<>();

    private int buildingId;
    private int rating;
    private int experience;
    private String approverUuid;
    private Date date;

    public BuildingApproval(int buildingId) {
        this.buildingId = buildingId;
    }

    public static BuildingApproval of(int buildingId) {
        return BUILDING_APPROVALS.computeIfAbsent(buildingId, BuildingApproval::new);
    }

    public static BuildingApproval deserialize(Map<String, Object> args) {
        int buildingId = (int) args.get("buildingId");
        BuildingApproval result = BuildingApproval.of(buildingId);
        Object rating = args.get("rating");
        if(rating instanceof Integer) {
            result.rating = (Integer) rating;
        }
        Object experience =  args.get("experience");
        if(experience instanceof Integer) {
            result.experience = (Integer) experience;
        }
        Object approverUuid = args.get("approverUuid");
        if(approverUuid instanceof String) {
            result.approverUuid = (String) approverUuid;
        }
        Object date = args.get("date");
        if(date instanceof Date) {
            result.date = (Date) date;
        }
        return result;
    }

    @Override
    public @NonNull Map<String, Object> serialize() {
        Map<String, Object> result = new HashMap<>();
        result.put("buildingId", buildingId);
        result.put("rating", rating);
        result.put("experience", experience);
        result.put("approverUuid", approverUuid);
        result.put("date", date);
        return result;
    }

    public int getBuildingId() {
        return buildingId;
    }

    public void setBuildingId(int buildingId) {
        this.buildingId = buildingId;
    }

    public int getRating() {
        return rating;
    }

    public void setRating(int rating) {
        this.rating = rating;
    }

    public int getExperience() {
        return experience;
    }

    public void setExperience(int experience) {
        this.experience = experience;
    }

    public String getApproverUuid() {
        return approverUuid;
    }

    public void setApproverUuid(String approverUuid) {
        this.approverUuid = approverUuid;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }
}
