package roomescape.domain.reservation;

import jakarta.validation.Valid;
import java.net.URI;
import java.time.LocalDate;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import roomescape.auth.Auth;
import roomescape.domain.reservation.dto.ReservationFixRequest;
import roomescape.domain.reservation.dto.ReservationRequest;
import roomescape.domain.reservation.dto.MyReservationsResponse;
import roomescape.domain.reservation.dto.ReservationResponse;
import roomescape.domain.reservationtime.dto.TimeResponse;
import roomescape.domain.user.User;

@Validated
@RestController
public class ReservationController {

    private final ReservationService reservationService;

    public ReservationController(ReservationService reservationService) {
        this.reservationService = reservationService;
    }

    @PostMapping("/reservations")
    public ResponseEntity<ReservationResponse> createReservation(
        @Auth User user,
        @RequestBody @Valid ReservationRequest request
    ) {
        ReservationResponse response = reservationService.createReservation(user, request);
        URI location = URI.create("/reservations/" + response.id());
        return ResponseEntity.created(location).body(response);
    }

    @GetMapping("/reservations")
    public ResponseEntity<List<TimeResponse>> getReservations(
        @RequestParam LocalDate date, @RequestParam Long themeId
    ) {
        List<TimeResponse> responses = reservationService.getReservations(date, themeId);
        return ResponseEntity.ok(responses);
    }

    @GetMapping("/reservations/mine")
    public ResponseEntity<MyReservationsResponse> getMyReservations(
        @Auth User user
    ) {
        MyReservationsResponse response = reservationService.getMyReservations(user);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/reservation/{id}")
    public ResponseEntity<Void> deleteReservation(
        @Auth User user,
        @PathVariable Long id
    ) {
        reservationService.deleteReservation(user, id);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/reservation/{id}")
    public ResponseEntity<Void> updateMyReservation(
        @Auth User user,
        @PathVariable Long id,
        @RequestBody ReservationFixRequest request
    ) {
        reservationService.updateMyReservation(user, id, request);
        return ResponseEntity.noContent().build();
    }
}
