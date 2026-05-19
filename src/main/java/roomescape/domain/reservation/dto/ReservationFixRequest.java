package roomescape.domain.reservation.dto;

import java.time.LocalDate;

public record ReservationFixRequest(
    LocalDate date,
    Long timeId
) {

}
