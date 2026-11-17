package br.gov.mgi.remuneracaoglobal.infrastructure.export;

import br.gov.mgi.remuneracaoglobal.application.Modelos.Detalhe;
import br.gov.mgi.remuneracaoglobal.application.Modelos.LinhaDetalhe;
import br.gov.mgi.remuneracaoglobal.infrastructure.persistence.CasoEntity;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UncheckedIOException;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Component;

/** Planilha Excel do caso: aba de cabeçalho e aba de linhas com totais. */
@Component
public class ExportadorExcel {

    private static final String[] COLUNAS = {"Instância", "Cargo", "Membros", "Honorário vigente",
            "Honorário proposto", "Benefício vigente", "Benefício proposto", "Total vigente",
            "Total proposto", "Variação (%)"};

    public byte[] gerar(Detalhe detalhe) {
        try (XSSFWorkbook wb = new XSSFWorkbook(); ByteArrayOutputStream saida = new ByteArrayOutputStream()) {
            escreverCabecalho(wb.createSheet("Caso"), detalhe.caso());
            escreverLinhas(wb.createSheet("Linhas"), detalhe);
            wb.write(saida);
            return saida.toByteArray();
        } catch (IOException e) {
            throw new UncheckedIOException("Falha ao gerar planilha do caso.", e);
        }
    }

    private static void escreverCabecalho(Sheet sheet, CasoEntity caso) {
        String[][] campos = {
                {"Número", caso.getNumero()},
                {"Tipo", caso.getTipo().name()},
                {"Empresa", caso.getEmpresaCodigo()},
                {"Exercício", String.valueOf(caso.getExercicio())},
                {"Tipo de assembleia", caso.getTipoAssembleia()},
                {"Data da assembleia", caso.getDataAssembleia() == null ? "" : caso.getDataAssembleia().toString()},
                {"Estado", caso.getEstado().name()}};
        for (int i = 0; i < campos.length; i++) {
            Row row = sheet.createRow(i);
            row.createCell(0).setCellValue(campos[i][0]);
            row.createCell(1).setCellValue(campos[i][1]);
        }
        sheet.autoSizeColumn(0);
        sheet.autoSizeColumn(1);
    }

    private static void escreverLinhas(Sheet sheet, Detalhe detalhe) {
        Row cabecalho = sheet.createRow(0);
        for (int c = 0; c < COLUNAS.length; c++) {
            cabecalho.createCell(c).setCellValue(COLUNAS[c]);
        }
        int r = 1;
        for (LinhaDetalhe ld : detalhe.linhas()) {
            var l = ld.linha();
            Row row = sheet.createRow(r++);
            row.createCell(0).setCellValue(ld.instancia());
            row.createCell(1).setCellValue(ld.cargo());
            row.createCell(2).setCellValue(l.getQuantidadeMembros());
            row.createCell(3).setCellValue(l.getHonorarioVigente().doubleValue());
            row.createCell(4).setCellValue(l.getHonorarioProposto().doubleValue());
            row.createCell(5).setCellValue(l.getBeneficioVigente().doubleValue());
            row.createCell(6).setCellValue(l.getBeneficioProposto().doubleValue());
            row.createCell(7).setCellValue(l.getTotalVigente().doubleValue());
            row.createCell(8).setCellValue(l.getTotalProposto().doubleValue());
            row.createCell(9).setCellValue(l.getVariacaoPercentual().doubleValue());
        }
        Row totais = sheet.createRow(r);
        totais.createCell(0).setCellValue("TOTAL");
        totais.createCell(7).setCellValue(detalhe.totais().vigente().doubleValue());
        totais.createCell(8).setCellValue(detalhe.totais().proposto().doubleValue());
        totais.createCell(9).setCellValue(detalhe.totais().variacao().doubleValue());
        for (int c = 0; c < COLUNAS.length; c++) {
            sheet.autoSizeColumn(c);
        }
    }
}
