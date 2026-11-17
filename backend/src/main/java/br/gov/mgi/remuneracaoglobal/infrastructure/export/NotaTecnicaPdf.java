package br.gov.mgi.remuneracaoglobal.infrastructure.export;

import br.gov.mgi.remuneracaoglobal.application.Modelos.Detalhe;
import br.gov.mgi.remuneracaoglobal.application.Modelos.LinhaDetalhe;
import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Element;
import com.lowagie.text.FontFactory;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.util.Locale;
import org.springframework.stereotype.Component;

/** Nota técnica em PDF com cabeçalho do caso, linhas propostas e totais. */
@Component
public class NotaTecnicaPdf {

    private static final Locale PT_BR = Locale.of("pt", "BR");

    public byte[] gerar(Detalhe detalhe) {
        var caso = detalhe.caso();
        ByteArrayOutputStream saida = new ByteArrayOutputStream();
        Document doc = new Document(PageSize.A4.rotate(), 36, 36, 36, 36);
        try {
            PdfWriter.getInstance(doc, saida);
            doc.open();
            doc.add(new Paragraph("Nota Técnica - " + caso.getNumero(),
                    FontFactory.getFont(FontFactory.HELVETICA_BOLD, 16)));
            doc.add(new Paragraph("Empresa: " + caso.getEmpresaCodigo() + "    Exercício: " + caso.getExercicio()
                    + "    Tipo: " + caso.getTipo().name() + "    Estado: " + caso.getEstado().name()));
            doc.add(new Paragraph("Tipo de assembleia: " + caso.getTipoAssembleia() + "    Data: "
                    + (caso.getDataAssembleia() == null ? "-" : caso.getDataAssembleia().toString())));
            doc.add(new Paragraph(" "));

            PdfPTable tabela = new PdfPTable(8);
            tabela.setWidthPercentage(100);
            tabela.setWidths(new float[]{3f, 2f, 1f, 2f, 2f, 2f, 2f, 1.5f});
            for (String titulo : new String[]{"Instância", "Cargo", "Membros", "Total vigente",
                    "Total proposto", "Honorário proposto", "Benefício proposto", "Variação (%)"}) {
                tabela.addCell(titulo);
            }
            for (LinhaDetalhe ld : detalhe.linhas()) {
                var l = ld.linha();
                tabela.addCell(ld.instancia());
                tabela.addCell(ld.cargo());
                tabela.addCell(String.valueOf(l.getQuantidadeMembros()));
                tabela.addCell(moeda(l.getTotalVigente()));
                tabela.addCell(moeda(l.getTotalProposto()));
                tabela.addCell(moeda(l.getHonorarioProposto()));
                tabela.addCell(moeda(l.getBeneficioProposto()));
                tabela.addCell(percentual(l.getVariacaoPercentual()));
            }
            doc.add(tabela);
            doc.add(new Paragraph(" "));

            var totais = detalhe.totais();
            Paragraph resumo = new Paragraph("Total vigente: " + moeda(totais.vigente())
                    + "    Total proposto: " + moeda(totais.proposto())
                    + "    Variação: " + percentual(totais.variacao()) + "%",
                    FontFactory.getFont(FontFactory.HELVETICA_BOLD, 11));
            resumo.setAlignment(Element.ALIGN_RIGHT);
            doc.add(resumo);

            if (caso.getJustificativa() != null) {
                doc.add(new Paragraph("Justificativa: " + caso.getJustificativa()));
            }
        } catch (DocumentException e) {
            throw new IllegalStateException("Falha ao gerar nota técnica do caso " + caso.getNumero(), e);
        } finally {
            doc.close();
        }
        return saida.toByteArray();
    }

    private static String moeda(BigDecimal valor) {
        return String.format(PT_BR, "R$ %,.2f", valor);
    }

    private static String percentual(BigDecimal valor) {
        return String.format(PT_BR, "%,.2f", valor);
    }
}
