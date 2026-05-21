package roomescape.domain.reservation;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.LocalDate;
import java.time.LocalTime;
import org.junit.jupiter.api.Test;
import roomescape.domain.reservationtime.ReservationTime;
import roomescape.domain.theme.Theme;
import roomescape.domain.user.Role;
import roomescape.domain.user.User;
import roomescape.exception.ErrorCode;
import roomescape.exception.RoomescapeException;

class ReservationTest {

    private static final ReservationTime RESERVED_TIME = ReservationTime.of(
        1L,
        LocalTime.of(10, 0),
        LocalTime.of(11, 0)
    );
    private static final Theme STORE_1_THEME = Theme.of(
        1L,
        "테마1",
        "설명",
        "https://example.com/image.png",
        1L
    );

    @Test
    void 예약자_본인이면_정상_처리() {
        Reservation reservation = Reservation.of(
            1L,
            "카키",
            LocalDate.of(2999, 12, 31),
            RESERVED_TIME,
            STORE_1_THEME
        );
        User owner = User.of(1L, "카키", "password");

        assertThatCode(() -> reservation.validateAccessibleBy(owner))
            .doesNotThrowAnyException();
    }

    @Test
    void 예약자가_아닌_일반_유저면_FORBIDDEN() {
        Reservation reservation = Reservation.of(
            1L,
            "카키",
            LocalDate.of(2999, 12, 31),
            RESERVED_TIME,
            STORE_1_THEME
        );
        User other = User.of(2L, "브리", "password");

        assertThatThrownBy(() -> reservation.validateAccessibleBy(other))
            .isInstanceOf(RoomescapeException.class)
            .extracting("errorCode").isEqualTo(ErrorCode.FORBIDDEN_RESERVATION);
    }

    @Test
    void 같은_매장_매니저면_정상_처리() {
        Reservation reservation = Reservation.of(
            1L,
            "카키",
            LocalDate.of(2999, 12, 31),
            RESERVED_TIME,
            STORE_1_THEME
        );
        User manager = User.of(3L, "강남매니저", "password", Role.MANAGER, 1L);

        assertThatCode(() -> reservation.validateAccessibleBy(manager))
            .doesNotThrowAnyException();
    }

    @Test
    void 다른_매장_매니저면_FORBIDDEN() {
        Reservation reservation = Reservation.of(
            1L,
            "카키",
            LocalDate.of(2999, 12, 31),
            RESERVED_TIME,
            STORE_1_THEME
        );
        User manager = User.of(4L, "홍대매니저", "password", Role.MANAGER, 2L);

        assertThatThrownBy(() -> reservation.validateAccessibleBy(manager))
            .isInstanceOf(RoomescapeException.class)
            .extracting("errorCode").isEqualTo(ErrorCode.FORBIDDEN_RESERVATION);
    }
}
