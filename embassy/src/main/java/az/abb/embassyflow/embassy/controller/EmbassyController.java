package az.abb.embassyflow.embassy.controller;

import az.abb.embassyflow.embassy.dto.response.EmbassiesResponse;
import az.abb.embassyflow.embassy.service.EmbassyService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/embassies")
public class EmbassyController {

    private final EmbassyService embassyService;

    public EmbassyController(EmbassyService embassyService) {
        this.embassyService = embassyService;
    }

    @GetMapping
    public EmbassiesResponse list() {
        return new EmbassiesResponse(embassyService.listActive());
    }
}