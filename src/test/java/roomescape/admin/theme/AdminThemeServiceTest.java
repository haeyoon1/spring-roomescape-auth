package roomescape.admin.theme;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import roomescape.admin.theme.dto.AdminThemeRequest;
import roomescape.admin.theme.dto.AdminThemeResponse;
import roomescape.admin.theme.dto.AdminThemesResponse;
import roomescape.domain.reservation.ReservationRepository;
import roomescape.domain.store.StoreRepository;
import roomescape.domain.theme.Theme;
import roomescape.domain.user.Role;
import roomescape.domain.user.User;
import roomescape.exception.ErrorCode;
import roomescape.exception.RoomescapeException;

@ExtendWith(MockitoExtension.class)
class AdminThemeServiceTest {

    @Mock
    private AdminThemeRepository adminThemeRepository;

    @Mock
    private ReservationRepository reservationRepository;

    @Mock
    private StoreRepository storeRepository;

    @InjectMocks
    private AdminThemeService adminThemeService;

    private static User manager(Long storeId) {
        return User.of(1L, "매니저", "pw", Role.MANAGER, storeId);
    }

    private static User regularUser() {
        return User.of(2L, "일반유저", "pw");
    }

    @Test
    void createTheme_정상_생성() {
        AdminThemeRequest request = new AdminThemeRequest("테마1", "설명", "https://example.com/image.jpg", 1L);
        Theme saved = Theme.of(1L, "테마1", "설명", "https://example.com/image.jpg", 1L);

        when(adminThemeRepository.existsByName("테마1")).thenReturn(false);
        when(storeRepository.findById(1L)).thenReturn(Optional.of(roomescape.domain.store.Store.of(1L, "강남점")));
        when(adminThemeRepository.save(any(Theme.class))).thenReturn(saved);
        AdminThemeResponse response = adminThemeService.createTheme(manager(1L), request);

        assertAll(
            () -> assertThat(response.id()).isEqualTo(1L),
            () -> assertThat(response.name()).isEqualTo("테마1"),
            () -> assertThat(response.storeId()).isEqualTo(1L)
        );
    }

    @Test
    void createTheme_일반_유저면_403_예외() {
        AdminThemeRequest request = new AdminThemeRequest("테마1", "설명", "https://example.com/image.jpg", 1L);

        assertThatThrownBy(() -> adminThemeService.createTheme(regularUser(), request))
            .isInstanceOf(RoomescapeException.class)
            .extracting("errorCode").isEqualTo(ErrorCode.FORBIDDEN_THEME);
    }

    @Test
    void createTheme_다른_매장_매니저면_403_예외() {
        AdminThemeRequest request = new AdminThemeRequest("테마1", "설명", "https://example.com/image.jpg", 2L);

        assertThatThrownBy(() -> adminThemeService.createTheme(manager(1L), request))
            .isInstanceOf(RoomescapeException.class)
            .extracting("errorCode").isEqualTo(ErrorCode.FORBIDDEN_THEME);
    }

    @Test
    void createTheme_중복된_이름이면_예외() {
        AdminThemeRequest request = new AdminThemeRequest("테마1", "설명", "https://example.com/image.jpg", 1L);

        when(adminThemeRepository.existsByName("테마1")).thenReturn(true);

        assertThatThrownBy(() -> adminThemeService.createTheme(manager(1L), request))
            .isInstanceOf(RoomescapeException.class)
            .extracting("errorCode").isEqualTo(ErrorCode.DUPLICATE_RESERVATION_NAME);
    }

    @Test
    void createTheme_존재하지_않는_storeId면_예외() {
        AdminThemeRequest request = new AdminThemeRequest("테마1", "설명", "https://example.com/image.jpg", 999L);

        when(adminThemeRepository.existsByName("테마1")).thenReturn(false);
        when(storeRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> adminThemeService.createTheme(manager(999L), request))
            .isInstanceOf(RoomescapeException.class)
            .extracting("errorCode").isEqualTo(ErrorCode.STORE_ID_NOT_FOUND);
    }

    @Test
    void getAllThemes_정상_조회() {
        Theme theme1 = Theme.of(1L, "테마1", "설명1", "url1");
        Theme theme2 = Theme.of(2L, "테마2", "설명2", "url2");

        when(adminThemeRepository.findAll()).thenReturn(List.of(theme1, theme2));

        AdminThemesResponse response = adminThemeService.getAllThemes();

        assertAll(
            () -> assertThat(response.themes()).hasSize(2),
            () -> assertThat(response.themes().get(0).name()).isEqualTo("테마1")
        );
    }

    @Test
    void deleteTheme_정상_삭제() {
        Theme theme = Theme.of(1L, "테마1", "설명", "url", 1L);
        when(adminThemeRepository.findById(1L)).thenReturn(Optional.of(theme));
        when(reservationRepository.existsByThemeId(1L)).thenReturn(false);

        adminThemeService.deleteTheme(manager(1L), 1L);

        verify(adminThemeRepository, times(1)).deleteById(1L);
    }

    @Test
    void deleteTheme_다른_매장_테마면_403_예외() {
        Theme theme = Theme.of(1L, "테마1", "설명", "url", 2L);
        when(adminThemeRepository.findById(1L)).thenReturn(Optional.of(theme));

        assertThatThrownBy(() -> adminThemeService.deleteTheme(manager(1L), 1L))
            .isInstanceOf(RoomescapeException.class)
            .extracting("errorCode").isEqualTo(ErrorCode.FORBIDDEN_THEME);
    }

    @Test
    void deleteTheme_일반_유저면_403_예외() {
        Theme theme = Theme.of(1L, "테마1", "설명", "url", 1L);
        when(adminThemeRepository.findById(1L)).thenReturn(Optional.of(theme));

        assertThatThrownBy(() -> adminThemeService.deleteTheme(regularUser(), 1L))
            .isInstanceOf(RoomescapeException.class)
            .extracting("errorCode").isEqualTo(ErrorCode.FORBIDDEN_THEME);
    }

    @Test
    void deleteTheme_존재하지_않는_id면_예외() {
        when(adminThemeRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> adminThemeService.deleteTheme(manager(1L), 99L))
            .isInstanceOf(RoomescapeException.class)
            .extracting("errorCode").isEqualTo(ErrorCode.THEME_ID_NOT_FOUND);
    }

    @Test
    void deleteTheme_예약이_존재하면_예외() {
        Theme theme = Theme.of(1L, "테마1", "설명", "url", 1L);
        when(adminThemeRepository.findById(1L)).thenReturn(Optional.of(theme));
        when(reservationRepository.existsByThemeId(1L)).thenReturn(true);

        assertThatThrownBy(() -> adminThemeService.deleteTheme(manager(1L), 1L))
            .isInstanceOf(RoomescapeException.class)
            .extracting("errorCode").isEqualTo(ErrorCode.TIME_DELETE_NOT_ALLOWED);
    }
}