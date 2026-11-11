package ua.chytalnia;

import java.util.List;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/import")
public class ExcelImportController {
    private final ExcelService excelService;

    ExcelImportController(ExcelService excelService) {
        this.excelService = excelService;
    }

    @PostMapping(path = "/excel", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    Map<String, Integer> importExcel(@RequestParam("file") MultipartFile file) {
        return Map.of("imported", excelService.save(file));
    }

    @ExceptionHandler(ResponseStatusException.class)
    ResponseEntity<Map<String, List<String>>> invalidWorkbook(ResponseStatusException exception) {
        return ResponseEntity.status(exception.getStatusCode())
            .body(Map.of("errors", List.of(exception.getReason())));
    }
}
