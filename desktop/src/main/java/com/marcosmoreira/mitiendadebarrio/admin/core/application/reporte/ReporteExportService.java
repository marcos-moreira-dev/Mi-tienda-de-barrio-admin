package com.marcosmoreira.mitiendadebarrio.admin.core.application.reporte;

import com.marcosmoreira.mitiendadebarrio.admin.core.domain.reporte.ReporteLinea;
import com.marcosmoreira.mitiendadebarrio.admin.core.domain.reporte.ReporteOperativo;
import com.marcosmoreira.mitiendadebarrio.admin.shared.exception.InfrastructureException;
import com.marcosmoreira.mitiendadebarrio.admin.shared.result.OperationResult;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/** Exportación local de reportes en CSV y PDF formal imprimible. */
public final class ReporteExportService {
    private static final DateTimeFormatter FILE_FORMAT = DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss");
    private static final DateTimeFormatter HUMAN_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static final int MAX_TABLE_COLUMN_CHARS = 28;
    private static final int MAX_LINE_CHARS = 112;
    private static final int PDF_LINES_PER_PAGE = 48;

    private final Path reportsDirectory;

    public ReporteExportService(Path reportsDirectory) { this.reportsDirectory = reportsDirectory; }

    public OperationResult<Path> exportarCsv(ReporteOperativo reporte) {
        if (reporte == null) return OperationResult.failure("No hay reporte para exportar.");
        try {
            Files.createDirectories(reportsDirectory);
            String baseName = reporte.tipo().name().toLowerCase() + "_" + FILE_FORMAT.format(reporte.generadoEn()) + ".csv";
            Path output = reportsDirectory.resolve(baseName);
            try (BufferedWriter writer = Files.newBufferedWriter(output, StandardCharsets.UTF_8)) {
                writer.write(String.join(",", reporte.encabezados().stream().map(this::csv).toList()));
                writer.newLine();
                for (ReporteLinea linea : reporte.lineas()) {
                    writer.write(String.join(",", linea.columnas().stream().map(this::csv).toList()));
                    writer.newLine();
                }
            }
            return OperationResult.success(output, "Reporte CSV exportado en: " + output.toAbsolutePath());
        } catch (IOException ex) {
            throw new InfrastructureException("No se pudo exportar el reporte CSV.", ex);
        }
    }

    /**
     * Mantiene el nombre anterior por compatibilidad, pero ahora genera un PDF más formal.
     */
    public OperationResult<Path> exportarPdfBasico(ReporteOperativo reporte) {
        return exportarPdfFormal(reporte);
    }

    public OperationResult<Path> exportarPdfFormal(ReporteOperativo reporte) {
        if (reporte == null) return OperationResult.failure("No hay reporte para exportar.");
        try {
            Files.createDirectories(reportsDirectory);
            String baseName = reporte.tipo().name().toLowerCase() + "_" + FILE_FORMAT.format(reporte.generadoEn()) + ".pdf";
            Path output = reportsDirectory.resolve(baseName);
            Files.writeString(output, pdfContent(reporte), StandardCharsets.ISO_8859_1);
            return OperationResult.success(output, "Reporte PDF formal exportado en: " + output.toAbsolutePath());
        } catch (IOException ex) {
            throw new InfrastructureException("No se pudo exportar el reporte PDF formal.", ex);
        }
    }

    private String csv(String value) {
        String safe = value == null ? "" : value;
        return '"' + safe.replace("\"", "\"\"") + '"';
    }

    private String pdfContent(ReporteOperativo reporte) {
        List<String> lines = new ArrayList<>();
        lines.add("MI TIENDA DE BARRIO ADMIN");
        lines.add("Reporte formal local");
        lines.add("Reporte: " + reporte.tipo().label());
        lines.add("Generado: " + HUMAN_FORMAT.format(reporte.generadoEn()));
        lines.add("Registros: " + reporte.lineas().size());
        lines.add("Formato: PDF local imprimible. CSV sigue disponible para edicion.");
        lines.add(separator());
        lines.add("RESUMEN OPERATIVO");
        lines.addAll(resumenEjecutivo(reporte));
        lines.add(separator());
        lines.add("DETALLE");
        lines.add(formatTableRow(reporte.encabezados()));
        lines.add(separator());
        if (reporte.lineas().isEmpty()) {
            lines.add("Sin registros para mostrar.");
        } else {
            for (ReporteLinea linea : reporte.lineas()) {
                lines.add(formatTableRow(linea.columnas()));
            }
        }
        lines.add(separator());
        lines.add("Generado localmente. Revise los datos antes de tomar decisiones operativas.");
        return minimalPdf(lines);
    }

    private List<String> resumenEjecutivo(ReporteOperativo reporte) {
        List<String> resumen = new ArrayList<>();
        if (reporte.lineas().isEmpty()) {
            resumen.add("- No se encontraron registros para este reporte.");
            resumen.add("- No hay acciones operativas inmediatas derivadas de esta consulta.");
            return resumen;
        }
        resumen.add("- Registros encontrados: " + reporte.lineas().size() + ".");
        switch (reporte.tipo()) {
            case PRODUCTOS_POR_COMPRAR -> resumen.add("- Priorice reposicion de productos con stock por debajo del minimo.");
            case BAJO_STOCK -> resumen.add("- Revise estos productos antes de que queden agotados.");
            case AGOTADOS -> resumen.add("- Estos productos requieren decision inmediata de compra o desactivacion.");
            case PROXIMOS_A_VENCER -> resumen.add("- Revise promociones, merma o retiro antes de la fecha de vencimiento.");
            case INVENTARIO_VALORIZADO -> resumen.add("- Use este reporte como referencia interna del valor aproximado del inventario.");
            case COMPRAS_RECIENTES -> resumen.add("- Verifique que las compras recientes hayan aumentado stock y tengan respaldo documental.");
            case VENTAS_INTERNAS_RECIENTES -> resumen.add("- Recuerde que la venta interna no reemplaza comprobante autorizado por el SRI.");
            case MERMAS_RETIROS_RECIENTES -> resumen.add("- Revise si las mermas se repiten por proveedor, categoria o fecha de vencimiento.");
            case CIERRE_CAJA_RECIENTE -> resumen.add("- Compare saldo esperado, contado y diferencias antes de cerrar la operacion diaria.");
            case FIADO_PENDIENTE -> resumen.add("- Priorice el seguimiento de clientes con mayor saldo pendiente.");
            case ABONOS_RECIENTES -> resumen.add("- Confirme que cada abono quede conectado con caja cuando corresponda.");
            case GASTOS_OPERATIVOS -> resumen.add("- Revise gastos frecuentes para detectar salidas que afecten caja.");
            case CUENTAS_POR_PAGAR -> resumen.add("- Priorice proveedores con saldos pendientes y fechas de vencimiento cercanas.");
        }
        return resumen;
    }

    private String formatTableRow(List<String> values) {
        return truncate(values.stream()
                .map(value -> truncate(value == null ? "" : value.replace('\n', ' '), MAX_TABLE_COLUMN_CHARS))
                .reduce((a, b) -> a + " | " + b)
                .orElse(""), MAX_LINE_CHARS);
    }

    private String separator() {
        return "-".repeat(112);
    }

    private String minimalPdf(List<String> lines) {
        List<List<String>> pages = paginate(lines);
        StringBuilder pdf = new StringBuilder("%PDF-1.4\n");
        List<String> objects = new ArrayList<>();
        objects.add("1 0 obj\n<< /Type /Catalog /Pages 2 0 R >>\nendobj\n");
        StringBuilder kids = new StringBuilder();
        int firstPageObject = 3;
        int fontObject = firstPageObject + pages.size() * 2;
        int contentObjectBase = firstPageObject + pages.size();
        for (int i = 0; i < pages.size(); i++) {
            int pageObject = firstPageObject + i;
            int contentObject = contentObjectBase + i;
            kids.append(pageObject).append(" 0 R ");
            objects.add(pageObject + " 0 obj\n<< /Type /Page /Parent 2 0 R /MediaBox [0 0 595 842] /Resources << /Font << /F1 " + fontObject + " 0 R >> >> /Contents " + contentObject + " 0 R >>\nendobj\n");
        }
        objects.add(1, "2 0 obj\n<< /Type /Pages /Kids [" + kids + "] /Count " + pages.size() + " >>\nendobj\n");
        for (int i = 0; i < pages.size(); i++) {
            int contentObject = contentObjectBase + i;
            String stream = pageStream(pages.get(i), i + 1, pages.size());
            int length = stream.getBytes(StandardCharsets.ISO_8859_1).length;
            objects.add(contentObject + " 0 obj\n<< /Length " + length + " >>\nstream\n" + stream + "endstream\nendobj\n");
        }
        objects.add(fontObject + " 0 obj\n<< /Type /Font /Subtype /Type1 /BaseFont /Helvetica >>\nendobj\n");

        List<Integer> offsets = new ArrayList<>();
        for (String object : objects) {
            offsets.add(pdf.toString().getBytes(StandardCharsets.ISO_8859_1).length);
            pdf.append(object);
        }
        int xrefOffset = pdf.toString().getBytes(StandardCharsets.ISO_8859_1).length;
        int size = objects.size() + 1;
        pdf.append("xref\n0 ").append(size).append("\n0000000000 65535 f \n");
        for (Integer offset : offsets) pdf.append(String.format("%010d 00000 n \n", offset));
        pdf.append("trailer\n<< /Size ").append(size).append(" /Root 1 0 R >>\nstartxref\n").append(xrefOffset).append("\n%%EOF\n");
        return pdf.toString();
    }

    private List<List<String>> paginate(List<String> lines) {
        List<List<String>> pages = new ArrayList<>();
        List<String> current = new ArrayList<>();
        for (String line : lines) {
            current.add(line);
            if (current.size() >= PDF_LINES_PER_PAGE) {
                pages.add(current);
                current = new ArrayList<>();
            }
        }
        if (!current.isEmpty() || pages.isEmpty()) pages.add(current);
        return pages;
    }

    private String pageStream(List<String> lines, int page, int totalPages) {
        StringBuilder text = new StringBuilder();
        text.append("BT\n/F1 10 Tf\n40 805 Td\n");
        int count = 0;
        for (String line : lines) {
            if (count > 0) text.append("0 -14 Td\n");
            text.append("(").append(pdfEscape(truncate(line, MAX_LINE_CHARS))).append(") Tj\n");
            count++;
        }
        text.append("ET\n");
        text.append("BT\n/F1 8 Tf\n40 28 Td\n(Pagina ").append(page).append(" de ").append(totalPages)
                .append(" - Documento local generado por MiTienda de Barrio Admin) Tj\nET\n");
        return text.toString();
    }

    private String pdfEscape(String value) {
        return normalizeForPdf(value).replace("\\", "\\\\").replace("(", "\\(").replace(")", "\\)");
    }

    private String normalizeForPdf(String value) {
        if (value == null) return "";
        return value
                .replace("–", "-")
                .replace("—", "-")
                .replace("“", "\"")
                .replace("”", "\"")
                .replace("‘", "'")
                .replace("’", "'")
                .replace("→", "->");
    }

    private String truncate(String value, int max) {
        if (value == null) return "";
        return value.length() <= max ? value : value.substring(0, max - 3) + "...";
    }
}
