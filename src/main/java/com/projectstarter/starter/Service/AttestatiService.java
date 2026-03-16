package com.projectstarter.starter.Service;

import com.projectstarter.starter.Dto.Request.AttestatiRequest;
import com.projectstarter.starter.Entity.Atleta;
import com.projectstarter.starter.Repository.AtletaRepository;
import jakarta.annotation.PostConstruct;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.sf.jasperreports.engine.*;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import java.io.*;
import java.nio.file.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

@Slf4j
@Service
@RequiredArgsConstructor
public class AttestatiService {

    private final AtletaRepository atletaRepository;

    private Path tempDir;
    private String jasperBasePath;
    private String jasperValutazionePath;

    @PostConstruct
    public void init() throws Exception {
        tempDir = Files.createTempDirectory("jasper-attestati");

        String[] resources = {
            "jasper/attestato_template.jasper",
            "jasper/attestato_template.jpg",
            "jasper/attestato_valutazione_disciplinare_template.jasper",
            "jasper/attestato_valutazione_disciplinare_template.jpg"
        };

        for (String resource : resources) {
            ClassPathResource cpr = new ClassPathResource(resource);
            Path dest = tempDir.resolve(cpr.getFilename());
            try (InputStream in = cpr.getInputStream()) {
                Files.copy(in, dest, StandardCopyOption.REPLACE_EXISTING);
            }
        }

        jasperBasePath = tempDir.resolve("attestato_template.jasper").toString();
        jasperValutazionePath = tempDir.resolve("attestato_valutazione_disciplinare_template.jasper").toString();

        log.info("AttestatiService: template JasperReports compilati in {}", tempDir);
    }

    public byte[] generateZip(AttestatiRequest request) throws Exception {
        String data = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try (ZipOutputStream zos = new ZipOutputStream(baos)) {
            for (Long atletaId : request.getAtletaIds()) {
                Atleta atleta = atletaRepository.findById(atletaId)
                        .orElseThrow(() -> new EntityNotFoundException("Atleta non trovato con id: " + atletaId));

                String valutazione = request.getValutazioni() != null
                        ? request.getValutazioni().getOrDefault(String.valueOf(atletaId), "")
                        : "";

                byte[] pdf = generatePdf(atleta, request.getCommissioneTecnica(), valutazione);

                String filename = String.format("%d_%s_%s_%s.pdf",
                        atleta.getId(),
                        atleta.getCognome().toUpperCase(),
                        atleta.getNome().toUpperCase(),
                        data);

                zos.putNextEntry(new ZipEntry(filename));
                zos.write(pdf);
                zos.closeEntry();
            }
        }
        return baos.toByteArray();
    }

    private byte[] generatePdf(Atleta atleta, String commissioneTecnica, String valutazione) throws Exception {
        String nominativo = capitalize(atleta.getNome()) + " " + capitalize(atleta.getCognome());

        Map<String, Object> params = new HashMap<>();
        params.put("nominativo", nominativo);
        params.put("cintura", atleta.getCintura() != null ? atleta.getCintura() : "");
        params.put("commissioneTecnica", commissioneTecnica != null ? commissioneTecnica : "");

        String jasperPath;
        if (valutazione != null && !valutazione.isBlank()) {
            params.put("valutazione", valutazione);
            jasperPath = jasperValutazionePath;
        } else {
            jasperPath = jasperBasePath;
        }

        JasperPrint print = JasperFillManager.fillReport(jasperPath, params, new JREmptyDataSource());

        return JasperExportManager.exportReportToPdf(print);
    }

    private static String capitalize(String s) {
        if (s == null || s.isEmpty()) return s;
        return Character.toUpperCase(s.charAt(0)) + s.substring(1).toLowerCase();
    }
}
