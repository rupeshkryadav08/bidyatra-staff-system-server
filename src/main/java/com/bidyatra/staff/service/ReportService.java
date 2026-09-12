package com.bidyatra.staff.service;

import com.bidyatra.staff.model.Activity;
import com.bidyatra.staff.repository.ActivityRepository;

import com.lowagie.text.Document;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.FontFactory;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.time.LocalDate;
import java.util.List;

@Service
public class ReportService {

    private final ActivityRepository repository;

    public ReportService(ActivityRepository repository) {
        this.repository = repository;
    }

    // =========================================================
    // EXCEL REPORT - ADMIN / ALL RECORDS
    // =========================================================

    public byte[] excel(LocalDate date) throws Exception {

        List<Activity> list;

        if (date == null) {
            list = repository.findAllByOrderByDateDescCreatedAtDesc();
        } else {
            list = repository.findByDateOrderByCreatedAtDesc(date);
        }

        return generateExcel(list);
    }

    // =========================================================
    // EXCEL REPORT - STAFF SPECIFIC
    // =========================================================

    public byte[] excelFor(LocalDate date, String username) throws Exception {

        List<Activity> list;

        if (username == null || username.isBlank()) {

            // ADMIN
            if (date == null) {
                list = repository.findAllByOrderByDateDescCreatedAtDesc();
            } else {
                list = repository.findByDateOrderByCreatedAtDesc(date);
            }

        } else {

            // STAFF
            if (date == null) {
                list = repository
                        .findByCreatedByOrderByDateDescCreatedAtDesc(username);
            } else {
                list = repository
                        .findByCreatedByAndDateOrderByCreatedAtDesc(
                                username,
                                date
                        );
            }
        }

        return generateExcel(list);
    }

    // =========================================================
    // GENERATE EXCEL
    // =========================================================

    private byte[] generateExcel(List<Activity> list) throws Exception {

        try (
                Workbook workbook = new XSSFWorkbook();
                ByteArrayOutputStream outputStream =
                        new ByteArrayOutputStream()
        ) {

            Sheet sheet = workbook.createSheet("Daily Activity");

            String[] headers = {
                    "Date",
                    "Staff Name",
                    "Location",
                    "Drivers Met",
                    "Registration",
                    "Document Issues",
                    "Remarks",
                    "Created By"
            };

            Row headerRow = sheet.createRow(0);

            CellStyle headerStyle = workbook.createCellStyle();

            org.apache.poi.ss.usermodel.Font excelFont =
                    workbook.createFont();

            excelFont.setBold(true);

            headerStyle.setFont(excelFont);

            for (int i = 0; i < headers.length; i++) {

                Cell cell = headerRow.createCell(i);

                cell.setCellValue(headers[i]);

                cell.setCellStyle(headerStyle);
            }

            int rowNumber = 1;

            for (Activity activity : list) {

                Row row = sheet.createRow(rowNumber++);

                row.createCell(0)
                        .setCellValue(
                                activity.getDate() != null
                                        ? activity.getDate().toString()
                                        : ""
                        );

                row.createCell(1)
                        .setCellValue(
                                safe(activity.getStaffName())
                        );

                row.createCell(2)
                        .setCellValue(
                                safe(activity.getVisitingLocation())
                        );

                row.createCell(3)
                        .setCellValue(
                                activity.getMeetDriver()
                        );

                row.createCell(4)
                        .setCellValue(
                                activity.getLogin()
                        );

                row.createCell(5)
                        .setCellValue(
                                activity.getDocumentsIssues()
                        );

                row.createCell(6)
                        .setCellValue(
                                safe(activity.getOther())
                        );

                row.createCell(7)
                        .setCellValue(
                                safe(activity.getCreatedBy())
                        );
            }

            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
            }

            workbook.write(outputStream);

            return outputStream.toByteArray();
        }
    }

    // =========================================================
    // PDF REPORT - ADMIN / ALL RECORDS
    // =========================================================

    public byte[] pdf(LocalDate date) throws Exception {

        List<Activity> list;

        if (date == null) {
            list = repository.findAllByOrderByDateDescCreatedAtDesc();
        } else {
            list = repository.findByDateOrderByCreatedAtDesc(date);
        }

        return generatePdf(list, date);
    }

    // =========================================================
    // PDF REPORT - STAFF SPECIFIC
    // =========================================================

    public byte[] pdfFor(LocalDate date, String username) throws Exception {

        List<Activity> list;

        if (username == null || username.isBlank()) {

            // ADMIN
            if (date == null) {
                list = repository.findAllByOrderByDateDescCreatedAtDesc();
            } else {
                list = repository.findByDateOrderByCreatedAtDesc(date);
            }

        } else {

            // STAFF
            if (date == null) {
                list = repository
                        .findByCreatedByOrderByDateDescCreatedAtDesc(username);
            } else {
                list = repository
                        .findByCreatedByAndDateOrderByCreatedAtDesc(
                                username,
                                date
                        );
            }
        }

        return generatePdf(list, date);
    }

    // =========================================================
    // GENERATE PDF
    // =========================================================

    private byte[] generatePdf(
            List<Activity> list,
            LocalDate date
    ) throws Exception {

        try (
                ByteArrayOutputStream outputStream =
                        new ByteArrayOutputStream()
        ) {

            Document document = new Document(
                    PageSize.A4.rotate(),
                    25,
                    25,
                    25,
                    25
            );

            PdfWriter.getInstance(
                    document,
                    outputStream
            );

            document.open();

            // =================================================
            // TITLE
            // =================================================

            Font titleFont =
                    FontFactory.getFont(
                            FontFactory.HELVETICA_BOLD,
                            18
                    );

            Paragraph title =
                    new Paragraph(
                            "BIDYATRA - DAILY STAFF ACTIVITY REPORT",
                            titleFont
                    );

            title.setAlignment(Element.ALIGN_CENTER);

            document.add(title);

            // =================================================
            // DATE
            // =================================================

            String reportDate =
                    date == null
                            ? "All Records"
                            : date.toString();

            Paragraph dateParagraph =
                    new Paragraph(
                            "Report Date: " + reportDate + "\n\n"
                    );

            document.add(dateParagraph);

            // =================================================
            // TABLE
            // =================================================

            PdfPTable table = new PdfPTable(8);

            table.setWidthPercentage(100);

            String[] headers = {
                    "Date",
                    "Staff",
                    "Location",
                    "Drivers",
                    "Registration",
                    "Docs",
                    "Remarks",
                    "Created By"
            };

            for (String header : headers) {
                table.addCell(header);
            }

            for (Activity activity : list) {

                table.addCell(
                        activity.getDate() != null
                                ? activity.getDate().toString()
                                : ""
                );

                table.addCell(
                        safe(activity.getStaffName())
                );

                table.addCell(
                        safe(activity.getVisitingLocation())
                );

                table.addCell(
                        String.valueOf(
                                activity.getMeetDriver()
                        )
                );

                table.addCell(
                        String.valueOf(
                                activity.getLogin()
                        )
                );

                table.addCell(
                        String.valueOf(
                                activity.getDocumentsIssues()
                        )
                );

                table.addCell(
                        safe(activity.getOther())
                );

                table.addCell(
                        safe(activity.getCreatedBy())
                );
            }

            document.add(table);

            document.close();

            return outputStream.toByteArray();
        }
    }

    // =========================================================
    // NULL SAFE STRING
    // =========================================================

    private String safe(String value) {

        return value == null ? "" : value;
    }
}