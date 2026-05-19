package roomescape.domain.reservation.dto;

import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;

public record ReservationRequest(
    @NotNull(message = "예약 날짜는 필수 입력 값입니다.")
    LocalDate date,

    @NotNull(message = "예약 시간 id는 필수 입력 값입니다.")
    Long timeId,

    @NotNull(message = "예약 테마 id는 필수 입력 값입니다.")
    Long themeId
) {

}
