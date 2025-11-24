package com.samvitex.servicios;

import com.lowagie.text.*;
import com.lowagie.text.pdf.BaseFont;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import com.samvitex.modelos.entidades.Venta;
import com.samvitex.modelos.entidades.VentaDetalle;
import org.springframework.stereotype.Service;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.text.SimpleDateFormat;

@Service
public class ServicioImpresion {

    public byte[] generarComprobanteEnMemoria(Venta venta) {
        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Document document = new Document(PageSize.A4);
            PdfWriter writer = PdfWriter.getInstance(document, out);

            document.open();

            // --- SOLUCIÓN CRÍTICA: CARGAR FUENTE FÍSICA DE WINDOWS ---
            // Cargamos Arial directamente del sistema y la INCRUSTAMOS (EMBEDDED = true)
            BaseFont baseFont;
            try {
                // Intentamos cargar Arial de Windows
                baseFont = BaseFont.createFont("C:\\Windows\\Fonts\\arial.ttf", BaseFont.CP1252, BaseFont.EMBEDDED);
            } catch (Exception e) {
                // Si falla (ej. no es Windows), usamos COURIER que es más compatible que Helvetica
                baseFont = BaseFont.createFont(BaseFont.COURIER, BaseFont.CP1252, BaseFont.NOT_EMBEDDED);
            }

            Font fontTitulo = new Font(baseFont, 18, Font.BOLD, Color.BLACK);
            Font fontCabecera = new Font(baseFont, 12, Font.BOLD, Color.DARK_GRAY);
            Font fontNormal = new Font(baseFont, 10, Font.NORMAL, Color.BLACK);
            Font fontNegrita = new Font(baseFont, 10, Font.BOLD, Color.BLACK);
            // ---------------------------------------------------------

            // --- Cabecera ---
            Paragraph titulo = new Paragraph("SAMVITEX S.A.C.", fontTitulo);
            titulo.setAlignment(Element.ALIGN_CENTER);
            document.add(titulo);

            Paragraph datosEmpresa = new Paragraph("RUC: 20123456789\nJr. Gamarra 123, La Victoria, Lima\nTelf: (01) 555-1234", fontNormal);
            datosEmpresa.setAlignment(Element.ALIGN_CENTER);
            document.add(datosEmpresa);
            document.add(new Paragraph(" "));

            // --- Datos Venta (Tabla Superior) ---
            PdfPTable tablaDatos = new PdfPTable(2);
            tablaDatos.setWidthPercentage(100);

            // Lado Izquierdo
            PdfPCell celdaCliente = new PdfPCell();
            celdaCliente.setBorder(Rectangle.NO_BORDER);
            celdaCliente.addElement(new Paragraph("Cliente: " + venta.getCliente().getNombreCompleto(), fontNormal));
            celdaCliente.addElement(new Paragraph("DOC: " + venta.getCliente().getDniRuc(), fontNormal));
            String direccion = (venta.getCliente().getDireccion() != null) ? venta.getCliente().getDireccion() : "-";
            celdaCliente.addElement(new Paragraph("Dirección: " + direccion, fontNormal));
            tablaDatos.addCell(celdaCliente);

            // Lado Derecho
            PdfPCell celdaVenta = new PdfPCell();
            celdaVenta.setBorder(Rectangle.NO_BORDER);
            celdaVenta.setHorizontalAlignment(Element.ALIGN_RIGHT);

            String tipoDoc = venta.getTipoComprobante() != null ? venta.getTipoComprobante().toString().replace("_", " ") : "NOTA VENTA";
            Paragraph pTipo = new Paragraph(tipoDoc, fontCabecera);
            pTipo.setAlignment(Element.ALIGN_RIGHT);
            celdaVenta.addElement(pTipo);

            Paragraph pNum = new Paragraph("N°: E001-" + String.format("%06d", venta.getId()), fontTitulo);
            pNum.setAlignment(Element.ALIGN_RIGHT);
            celdaVenta.addElement(pNum);

            String fecha = new SimpleDateFormat("dd/MM/yyyy HH:mm").format(java.util.Date.from(venta.getFechaVenta()));
            Paragraph pFecha = new Paragraph("Fecha: " + fecha, fontNormal);
            pFecha.setAlignment(Element.ALIGN_RIGHT);
            celdaVenta.addElement(pFecha);

            tablaDatos.addCell(celdaVenta);
            document.add(tablaDatos);
            document.add(new Paragraph(" "));

            // --- Tabla Productos ---
            PdfPTable tablaProductos = new PdfPTable(new float[]{1, 4, 2, 2});
            tablaProductos.setWidthPercentage(100);
            tablaProductos.setHeaderRows(1);

            String[] headers = {"CANT.", "DESCRIPCIÓN", "P. UNIT", "IMPORTE"};
            for (String h : headers) {
                PdfPCell cell = new PdfPCell(new Phrase(h, fontNegrita));
                cell.setBackgroundColor(Color.LIGHT_GRAY);
                cell.setHorizontalAlignment(Element.ALIGN_CENTER);
                cell.setPadding(5);
                tablaProductos.addCell(cell);
            }

            if (venta.getDetalles() != null) {
                for (VentaDetalle det : venta.getDetalles()) {
                    PdfPCell c1 = new PdfPCell(new Phrase(String.valueOf(det.getCantidad()), fontNormal));
                    c1.setHorizontalAlignment(Element.ALIGN_CENTER); c1.setPadding(4);
                    tablaProductos.addCell(c1);

                    PdfPCell c2 = new PdfPCell(new Phrase(det.getProducto().getNombre(), fontNormal));
                    c2.setPadding(4);
                    tablaProductos.addCell(c2);

                    PdfPCell c3 = new PdfPCell(new Phrase("S/ " + det.getPrecioUnitario().toString(), fontNormal));
                    c3.setHorizontalAlignment(Element.ALIGN_RIGHT); c3.setPadding(4);
                    tablaProductos.addCell(c3);

                    PdfPCell c4 = new PdfPCell(new Phrase("S/ " + det.getSubtotalLinea().toString(), fontNormal));
                    c4.setHorizontalAlignment(Element.ALIGN_RIGHT); c4.setPadding(4);
                    tablaProductos.addCell(c4);
                }
            }
            document.add(tablaProductos);

            // --- Total ---
            document.add(new Paragraph(" "));
            PdfPTable tablaTotal = new PdfPTable(new float[]{7, 2});
            tablaTotal.setWidthPercentage(100);

            PdfPCell cVacia = new PdfPCell(new Phrase(""));
            cVacia.setBorder(Rectangle.NO_BORDER);
            tablaTotal.addCell(cVacia);

            PdfPCell cTotal = new PdfPCell(new Phrase("TOTAL: S/ " + venta.getTotal(), fontTitulo));
            cTotal.setHorizontalAlignment(Element.ALIGN_RIGHT);
            cTotal.setBorder(Rectangle.TOP);
            cTotal.setPaddingTop(10);
            tablaTotal.addCell(cTotal);

            document.add(tablaTotal);

            document.close();
            return out.toByteArray();
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Error PDF: " + e.getMessage());
        }
    }
}