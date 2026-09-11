package az.abb.embassyflow.order.controller;

import az.abb.embassyflow.order.dto.response.DocumentTypesResponse;
import az.abb.embassyflow.order.service.DocumentTypeService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/document-types")
public class DocumentTypeController {

    private final DocumentTypeService documentTypeService;

    public DocumentTypeController(DocumentTypeService documentTypeService) {
        this.documentTypeService = documentTypeService;
    }

    @GetMapping
    public DocumentTypesResponse list() {
        return new DocumentTypesResponse(documentTypeService.list());
    }
}