package kr.kubecity.bot;

import java.util.Date;

public record Vote(
        int buildingId,
        String voterUuid,
        Date date
) {
}
