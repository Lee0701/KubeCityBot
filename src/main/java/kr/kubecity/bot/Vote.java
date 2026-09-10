package kr.kubecity.bot;

import java.util.Date;

public record Vote(
        int id,
        int buildingId,
        String voterUuid,
        Date date
) {
}
