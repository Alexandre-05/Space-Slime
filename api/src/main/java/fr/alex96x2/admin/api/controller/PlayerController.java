package fr.alex96x2.admin.api.controller;

import fr.alex96x2.admin.api.dto.Dtos;
import fr.alex96x2.admin.api.service.PlayerService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/players")
public class PlayerController {

    private final PlayerService playerService;

    public PlayerController(PlayerService playerService) {
        this.playerService = playerService;
    }

    @GetMapping
    public Dtos.PageResponse<Dtos.PlayerSummaryDto> list(
            @RequestParam(required = false) String search,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return playerService.list(search, page, size);
    }

    @GetMapping("/{uuid}")
    public Dtos.PlayerDetailDto detail(@PathVariable String uuid) {
        return playerService.getDetail(uuid);
    }
}
