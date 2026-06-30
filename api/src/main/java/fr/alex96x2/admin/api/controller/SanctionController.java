package fr.alex96x2.admin.api.controller;

import fr.alex96x2.admin.api.dto.Dtos;
import fr.alex96x2.admin.api.service.SanctionService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
public class SanctionController {

    private final SanctionService sanctionService;

    public SanctionController(SanctionService sanctionService) {
        this.sanctionService = sanctionService;
    }

    @GetMapping("/dashboard")
    public Dtos.DashboardDto dashboard() {
        return sanctionService.dashboard();
    }

    @PostMapping("/sanctions/{uuid}/ban")
    public void ban(@PathVariable String uuid, @RequestBody Dtos.SanctionRequest request) {
        sanctionService.ban(uuid, request);
    }

    @PostMapping("/sanctions/{uuid}/unban")
    public void unban(@PathVariable String uuid) {
        sanctionService.unban(uuid);
    }

    @PostMapping("/sanctions/{uuid}/mute")
    public void mute(@PathVariable String uuid, @RequestBody Dtos.SanctionRequest request) {
        sanctionService.mute(uuid, request);
    }

    @PostMapping("/sanctions/{uuid}/unmute")
    public void unmute(@PathVariable String uuid) {
        sanctionService.unmute(uuid);
    }

    @PostMapping("/sanctions/{uuid}/warn")
    public void warn(@PathVariable String uuid, @RequestBody Dtos.SanctionRequest request) {
        sanctionService.warn(uuid, request);
    }

    @PostMapping("/sanctions/{uuid}/note")
    public void note(@PathVariable String uuid, @RequestBody Dtos.NoteRequest request) {
        sanctionService.note(uuid, request);
    }
}
