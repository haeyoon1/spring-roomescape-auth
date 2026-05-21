package roomescape.domain.reservation;

import java.time.LocalDate;
import java.util.List;

import org.springframework.stereotype.Service;
import roomescape.domain.reservation.dto.ReservationFixRequest;
import roomescape.domain.reservation.dto.MyReservationsResponse;
import roomescape.domain.reservation.dto.ReservationResponse;
import roomescape.domain.user.User;
import roomescape.exception.ErrorCode;
import roomescape.exception.RoomescapeException;
import roomescape.domain.theme.Theme;
import roomescape.admin.theme.AdminThemeRepository;
import roomescape.domain.reservationtime.ReservationTime;
import roomescape.domain.reservation.dto.ReservationRequest;
import roomescape.domain.reservationtime.dto.TimeResponse;
import roomescape.domain.reservationtime.ReservationTimeRepository;

@Service
public class ReservationService {

    private final ReservationRepository reservationRepository;
    private final ReservationTimeRepository reservationTimeRepository;
    private final AdminThemeRepository adminThemeRepository;

    public ReservationService(
        ReservationRepository reservationRepository,
        ReservationTimeRepository reservationTimeRepository,
        AdminThemeRepository adminThemeRepository
    ) {
        this.reservationRepository = reservationRepository;
        this.reservationTimeRepository = reservationTimeRepository;
        this.adminThemeRepository = adminThemeRepository;
    }

    public ReservationResponse createReservation(User user, ReservationRequest request) {
        ReservationTime time = reservationTimeRepository.findById(request.timeId())
            .orElseThrow(() -> new RoomescapeException(ErrorCode.TIME_ID_NOT_FOUND));
        Theme theme = adminThemeRepository.findById(request.themeId())
            .orElseThrow(() -> new RoomescapeException(ErrorCode.THEME_ID_NOT_FOUND));

        validateDuplicateReservation(request.date(), request.timeId(), request.themeId());
        time.validateIfTimePast(request.date());

        Reservation reservation = Reservation.of(
            user.getName(),
            request.date(),
            time,
            theme
        );
        Reservation saved = reservationRepository.save(reservation);
        return ReservationResponse.of(saved);
    }

    public List<TimeResponse> getReservations(LocalDate date, Long themeId) {
        List<ReservationTime> reservationTimes = reservationTimeRepository.findAll();
        List<Long> bookedTimeIds = reservationRepository.findTimeByDateAndThemeId(date, themeId);

        return reservationTimes.stream()
            .filter(reservationTime -> !bookedTimeIds.contains(reservationTime.getId()))
            .map(TimeResponse::of)
            .toList();
    }

    public MyReservationsResponse getMyReservations(User user) {
        List<Reservation> reservations = reservationRepository.findByName(user.getName());
        return MyReservationsResponse.from(reservations);
    }

    public void deleteReservation(User user, Long id) {
        Reservation reservation = reservationRepository.findById(id)
            .orElseThrow(() -> new RoomescapeException(ErrorCode.RESERVATION_ID_NOT_FOUND));
        reservation.validateAccessibleBy(user);
        reservationRepository.deleteById(id);
    }

    public void updateMyReservation(User user, Long id, ReservationFixRequest fixRequest) {
        Reservation reservation = reservationRepository.findById(id)
            .orElseThrow(() -> new RoomescapeException(ErrorCode.RESERVATION_ID_NOT_FOUND));
        reservation.validateAccessibleBy(user);
        validateFixRequest(reservation.getTheme(), fixRequest);

        reservationRepository.updateDateAndTime(id, fixRequest.date(), fixRequest.timeId());
    }

    private void validateFixRequest(Theme theme, ReservationFixRequest newRequest) {
        ReservationTime newTime = reservationTimeRepository.findById(newRequest.timeId())
            .orElseThrow(() -> new RoomescapeException(ErrorCode.TIME_ID_NOT_FOUND));
        newTime.validateIfTimePast(newRequest.date());
        validateDuplicateReservation(newRequest.date(), newRequest.timeId(), theme.getId());
    }

    private void validateDuplicateReservation(LocalDate date, Long timeId, Long themeId) {
        boolean isDuplicated = reservationRepository.existsByDateAndTimeIdAndThemeId(date, timeId, themeId);
        if (isDuplicated) {
            throw new RoomescapeException(ErrorCode.DUPLICATE_RESERVATION);
        }
    }

}
