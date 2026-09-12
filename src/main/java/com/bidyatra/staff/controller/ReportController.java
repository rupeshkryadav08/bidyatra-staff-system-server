package com.bidyatra.staff.controller;

import com.bidyatra.staff.service.ReportService;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/reports")
public class ReportController {

    private final ReportService service;

    public ReportController(ReportService service) {
        this.service = service;
    }

    @GetMapping("/excel")
    public ResponseEntity<byte[]> excel(@RequestParam(required = false) String date, org.springframework.security.core.Authentication authentication) throws Exception {
        LocalDate d = date == null || date.isBlank() ? null : LocalDate.parse(date);
        boolean admin=authentication.getAuthorities().stream().anyMatch(a->a.getAuthority().equals("ROLE_ADMIN"));
        byte[] data = service.excelFor(d, admin?null:authentication.getName());

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=bidyatra-staff-report.xlsx")
                .contentType(MediaType.parseMediaType(
                        "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .body(data);
    }

    @GetMapping("/pdf")
    public ResponseEntity<byte[]> pdf(@RequestParam(required = false) String date, org.springframework.security.core.Authentication authentication) throws Exception {
        LocalDate d = date == null || date.isBlank() ? null : LocalDate.parse(date);
        boolean admin=authentication.getAuthorities().stream().anyMatch(a->a.getAuthority().equals("ROLE_ADMIN"));
        byte[] data = service.pdfFor(d, admin?null:authentication.getName());

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=bidyatra-staff-report.pdf")
                .contentType(MediaType.APPLICATION_PDF)
                .body(data);
    }
}
