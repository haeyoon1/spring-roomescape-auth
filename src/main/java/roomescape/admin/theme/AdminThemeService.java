package roomescape.admin.theme;

import org.springframework.stereotype.Service;
import roomescape.admin.theme.dto.AdminThemeRequest;
import roomescape.admin.theme.dto.AdminThemeResponse;
import roomescape.admin.theme.dto.AdminThemesResponse;
import roomescape.domain.store.StoreRepository;
import roomescape.exception.ErrorCode;
import roomescape.exception.RoomescapeException;
import roomescape.domain.reservation.ReservationRepository;

import java.util.List;
import roomescape.domain.theme.Theme;

@Service
public class AdminThemeService {

    private final AdminThemeRepository adminThemeRepository;
    private final ReservationRepository reservationRepository;
    private final StoreRepository storeRepository;

    public AdminThemeService(
        AdminThemeRepository adminThemeRepository,
        ReservationRepository reservationRepository,
        StoreRepository storeRepository
    ) {
        this.adminThemeRepository = adminThemeRepository;
        this.reservationRepository = reservationRepository;
        this.storeRepository = storeRepository;
    }

    public AdminThemeResponse createTheme(AdminThemeRequest request) {
        validateDuplicateTheme(request.name());
        validateStoreId(request.storeId());
        Theme theme = Theme.of(
            request.name(),
            request.description(),
            request.imageUrl(),
            request.storeId()
        );

        Theme saved = adminThemeRepository.save(theme);
        return AdminThemeResponse.from(saved);
    }

    private void validateStoreId(Long storeId) {
        if (storeId == null) {
            return;
        }
        storeRepository.findById(storeId)
            .orElseThrow(() -> new RoomescapeException(ErrorCode.STORE_ID_NOT_FOUND));
    }

    public AdminThemesResponse getAllThemes() {
        List<Theme> themes = adminThemeRepository.findAll();

        return AdminThemesResponse.from(themes);
    }

    public void deleteTheme(Long themeId) {
        validateThemeId(themeId);
        validateTimeDeletable(themeId);
        adminThemeRepository.deleteById(themeId);
    }

    private void validateThemeId(Long themeId) {
        if (!adminThemeRepository.existsById(themeId)) {
            throw new RoomescapeException(ErrorCode.THEME_ID_NOT_FOUND);
        }
    }

    private void validateTimeDeletable(Long themeId) {
        if (reservationRepository.existsByThemeId(themeId)) {
            throw new RoomescapeException(ErrorCode.TIME_DELETE_NOT_ALLOWED);
        }
    }

    private void validateDuplicateTheme(String name) {
        if (adminThemeRepository.existsByName(name)) {
            throw new RoomescapeException(ErrorCode.DUPLICATE_RESERVATION_NAME);
        }
    }
}
