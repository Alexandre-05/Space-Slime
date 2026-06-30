package fr.alex96x2.admin.api.service;

import fr.alex96x2.admin.api.dto.Dtos;
import fr.alex96x2.admin.api.entity.*;
import fr.alex96x2.admin.api.repository.*;
import fr.alex96x2.admin.api.util.SanctionMapper;
import fr.alex96x2.admin.api.util.IpCryptoService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;

@Service
public class PlayerService {

    private final PlayerRepository playerRepository;
    private final BanRepository banRepository;
    private final MuteRepository muteRepository;
    private final WarnRepository warnRepository;
    private final KickRepository kickRepository;
    private final StaffNoteRepository noteRepository;
    private final SessionRepository sessionRepository;
    private final PlayerNameHistoryRepository nameHistoryRepository;
    private final IpCryptoService ipCryptoService;
    private final SanctionMapper sanctionMapper;

    public PlayerService(PlayerRepository playerRepository, BanRepository banRepository, MuteRepository muteRepository,
                         WarnRepository warnRepository, KickRepository kickRepository, StaffNoteRepository noteRepository,
                         SessionRepository sessionRepository, PlayerNameHistoryRepository nameHistoryRepository,
                         IpCryptoService ipCryptoService, SanctionMapper sanctionMapper) {
        this.playerRepository = playerRepository;
        this.banRepository = banRepository;
        this.muteRepository = muteRepository;
        this.warnRepository = warnRepository;
        this.kickRepository = kickRepository;
        this.noteRepository = noteRepository;
        this.sessionRepository = sessionRepository;
        this.nameHistoryRepository = nameHistoryRepository;
        this.ipCryptoService = ipCryptoService;
        this.sanctionMapper = sanctionMapper;
    }

    public Dtos.PageResponse<Dtos.PlayerSummaryDto> list(String search, int page, int size) {
        Page<PlayerEntity> result = playerRepository.search(search,
                PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "lastSeen")));
        List<Dtos.PlayerSummaryDto> content = result.getContent().stream().map(this::toSummary).toList();
        return new Dtos.PageResponse<>(content, page, size, result.getTotalElements(), result.getTotalPages());
    }

    public Dtos.PlayerDetailDto getDetail(String uuid) {
        PlayerEntity player = playerRepository.findById(uuid)
                .orElseThrow(() -> new IllegalArgumentException("Joueur introuvable"));
        boolean founder = isFounder();
        String ip = founder ? ipCryptoService.decrypt(player.getLastIpEncrypted()) : null;

        return new Dtos.PlayerDetailDto(
                player.getUuid(),
                player.getCurrentName(),
                player.getFirstSeen(),
                player.getLastSeen(),
                player.getTotalPlaytime(),
                ip,
                nameHistoryRepository.findByUuidOrderByChangedAtDesc(uuid).stream()
                        .map(n -> new Dtos.NameHistoryDto(n.getName(), n.getChangedAt())).toList(),
                banRepository.findByUuidOrderByCreatedAtDesc(uuid).stream().map(b -> sanctionMapper.toDto(b, "bans")).toList(),
                muteRepository.findByUuidOrderByCreatedAtDesc(uuid).stream().map(m -> sanctionMapper.toDto(m, "mutes")).toList(),
                kickRepository.findByUuidOrderByCreatedAtDesc(uuid).stream()
                        .map(k -> new Dtos.KickDto(k.getId(), k.getReason(), k.getStaffName(), k.getCreatedAt(), k.getSource().name())).toList(),
                warnRepository.findByUuidOrderByCreatedAtDesc(uuid).stream()
                        .map(w -> new Dtos.WarnDto(w.getId(), w.getReason(), w.getStaffName(), w.getCreatedAt(), w.isActive(), w.getSource().name())).toList(),
                noteRepository.findByUuidOrderByCreatedAtDesc(uuid).stream()
                        .map(n -> new Dtos.NoteDto(n.getId(), n.getContent(), n.getStaffName(), n.getCreatedAt())).toList(),
                sessionRepository.findByUuidOrderByJoinAtDesc(uuid).stream()
                        .map(s -> new Dtos.SessionDto(s.getId(), s.getJoinAt(), s.getQuitAt(), founder ? s.getIpHash() : null)).toList()
        );
    }

    private Dtos.PlayerSummaryDto toSummary(PlayerEntity p) {
        boolean banned = banRepository.findFirstByUuidAndActiveTrueOrderByCreatedAtDesc(p.getUuid()).isPresent();
        boolean muted = muteRepository.findFirstByUuidAndActiveTrueOrderByCreatedAtDesc(p.getUuid()).isPresent();
        int warns = warnRepository.findByUuidAndActiveTrueOrderByCreatedAtDesc(p.getUuid()).size();
        return new Dtos.PlayerSummaryDto(p.getUuid(), p.getCurrentName(), p.getFirstSeen(), p.getLastSeen(),
                p.getTotalPlaytime(), banned, muted, warns);
    }

    private boolean isFounder() {
        var auth = SecurityContextHolder.getContext().getAuthentication();
        return auth != null && auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_FONDATEUR"));
    }
}
