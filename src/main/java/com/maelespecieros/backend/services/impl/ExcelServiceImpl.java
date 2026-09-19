package com.maelespecieros.backend.services.impl;

import java.io.ByteArrayOutputStream;
import java.util.List;

import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.maelespecieros.backend.entities.Producto;
import com.maelespecieros.backend.repositories.ProductoRepository;
import com.maelespecieros.backend.repositories.VentaRepository;
import com.maelespecieros.backend.services.ExcelService;
import com.maelespecieros.backend.services.BlockchainService;

@Service
@Transactional(readOnly = true)
public class ExcelServiceImpl implements ExcelService {

    private final ProductoRepository productoRepository;
    private final VentaRepository ventaRepository;
    private final BlockchainService blockchainService;

    public ExcelServiceImpl(
            ProductoRepository productoRepository,
            VentaRepository ventaRepository,
            BlockchainService blockchainService) {

        this.productoRepository = productoRepository;
        this.ventaRepository = ventaRepository;
        this.blockchainService = blockchainService;
    }

    @Override
    public byte[] exportarProductos() {

        try (XSSFWorkbook workbook = new XSSFWorkbook()) {

            XSSFSheet sheet =
                    workbook.createSheet("Productos");

            crearTitulo(sheet, "REPORTE DE PRODUCTOS");

            crearCabeceraProductos(sheet);

            int fila = 2;

            List<Producto> productos =
                    productoRepository.findByActivoTrue();

            for (Producto producto : productos) {

                Row row = sheet.createRow(fila++);

                row.createCell(0)
                        .setCellValue(producto.getCodigoProducto());

                row.createCell(1)
                        .setCellValue(producto.getNombre());

                row.createCell(2)
                        .setCellValue(producto.getCategoria().getNombre());

                row.createCell(3)
                        .setCellValue(producto.getStock());

                row.createCell(4)
                        .setCellValue(producto.getCosto().doubleValue());

                row.createCell(5)
                        .setCellValue(producto.getPrecioEfectivo().doubleValue());
            }

            autoSize(sheet, 6);

            ByteArrayOutputStream out =
                    new ByteArrayOutputStream();

            workbook.write(out);

            return out.toByteArray();

        } catch (Exception e) {

            throw new RuntimeException(
                    "Error al generar Excel.", e);

        }

    }
    @Override
    public byte[] exportarVentas() {

        try (XSSFWorkbook workbook = new XSSFWorkbook()) {

            XSSFSheet sheet =
                    workbook.createSheet("Ventas");

            crearTitulo(sheet, "REPORTE DE VENTAS");

            Row cabecera = sheet.createRow(1);

            crearCabecera(cabecera, 0, "Número");
            crearCabecera(cabecera, 1, "Fecha");
            crearCabecera(cabecera, 2, "Forma Pago");
            crearCabecera(cabecera, 3, "Estado");
            crearCabecera(cabecera, 4, "Descuento");
            crearCabecera(cabecera, 5, "Total");

            final int[] fila = {2};

            ventaRepository.findAll().forEach(venta -> {

                // Usamos fila[0] y luego lo incrementamos con ++
                Row row = sheet.createRow(fila[0]++);

                row.createCell(0)
                        .setCellValue(venta.getNumeroVenta());

                row.createCell(1)
                        .setCellValue(
                                venta.getFecha().toString());

                row.createCell(2)
                        .setCellValue(
                                venta.getFormaPago().name());

                row.createCell(3)
                        .setCellValue(
                                venta.getEstado().name());

                row.createCell(4)
                        .setCellValue(
                                venta.getDescuento().doubleValue());

                row.createCell(5)
                        .setCellValue(
                                venta.getTotal().doubleValue());

            });

            autoSize(sheet, 6);

            ByteArrayOutputStream out =
                    new ByteArrayOutputStream();

            workbook.write(out);

            return out.toByteArray();

        } catch (Exception e) {

            throw new RuntimeException(
                    "Error al exportar ventas.", e);

        }

    }

    @Override
    public byte[] exportarStockBajo() {

        try (XSSFWorkbook workbook = new XSSFWorkbook()) {

            XSSFSheet sheet =
                    workbook.createSheet("Stock Bajo");

            crearTitulo(sheet, "REPORTE DE STOCK BAJO");

            Row cabecera = sheet.createRow(1);

            crearCabecera(cabecera, 0, "Código");
            crearCabecera(cabecera, 1, "Producto");
            crearCabecera(cabecera, 2, "Stock");
            crearCabecera(cabecera, 3, "Stock Mínimo");

            final int[] fila = {2}; 

            productoRepository.findByActivoTrue()
                    .stream()
                    .filter(p -> p.getStock() <= p.getStockMinimo())
                    .forEach(prod -> {

                        Row row = sheet.createRow(fila[0]++); 
                        
                        

                        row.createCell(0)
                                .setCellValue(prod.getCodigoProducto());

                        row.createCell(1)
                                .setCellValue(prod.getNombre());

                        row.createCell(2)
                                .setCellValue(prod.getStock());

                        row.createCell(3)
                                .setCellValue(prod.getStockMinimo());

                    });

            autoSize(sheet, 4);

            ByteArrayOutputStream out =
                    new ByteArrayOutputStream();

            workbook.write(out);

            return out.toByteArray();

        } catch (Exception e) {

            throw new RuntimeException(
                    "Error al exportar stock.", e);

        }

    }
    @Override
    public byte[] exportarAuditoria() {

        try (XSSFWorkbook workbook = new XSSFWorkbook()) {

            XSSFSheet sheet =
                    workbook.createSheet("Auditoría");

            crearTitulo(sheet, "REPORTE DE AUDITORÍA");

            Row cabecera = sheet.createRow(1);

            crearCabecera(cabecera, 0, "Usuario");
            crearCabecera(cabecera, 1, "Acción");
            crearCabecera(cabecera, 2, "Fecha");
            crearCabecera(cabecera, 3, "Detalle");

            final int[] fila = {2};

            blockchainService.listarCadena().forEach(auditoria -> {

                Row filaActual = sheet.createRow(fila[0]++); 

                filaActual.createCell(0)
                        .setCellValue(auditoria.getUsuario());

                filaActual.createCell(1)
                        .setCellValue(auditoria.getAccion());

                filaActual.createCell(2)
                        .setCellValue(auditoria.getFecha().toString());

                filaActual.createCell(3)
                        .setCellValue(auditoria.getDescripcion());

            });

            autoSize(sheet, 4);

            ByteArrayOutputStream out =
                    new ByteArrayOutputStream();

            workbook.write(out);

            return out.toByteArray();

        } catch (Exception e) {

            throw new RuntimeException(
                    "Error al exportar auditoría.", e);

        }

    }

    private void crearTitulo(
            XSSFSheet sheet,
            String titulo) {

        Row row = sheet.createRow(0);

        Cell cell = row.createCell(0);

        cell.setCellValue(titulo);

        org.apache.poi.ss.usermodel.CellStyle style =
                sheet.getWorkbook().createCellStyle();

        Font font =
                sheet.getWorkbook().createFont();

        font.setBold(true);
        font.setFontHeightInPoints((short) 16);

        style.setFont(font);

        cell.setCellStyle(style);

    }

    private void crearCabeceraProductos(
            XSSFSheet sheet) {

        Row row = sheet.createRow(1);

        crearCabecera(row, 0, "Código");
        crearCabecera(row, 1, "Nombre");
        crearCabecera(row, 2, "Categoría");
        crearCabecera(row, 3, "Stock");
        crearCabecera(row, 4, "Costo");
        crearCabecera(row, 5, "Precio");

    }

    private void crearCabecera(
            Row row,
            int columna,
            String texto) {

        Cell cell = row.createCell(columna);

        cell.setCellValue(texto);

        org.apache.poi.ss.usermodel.CellStyle style =
                row.getSheet()
                        .getWorkbook()
                        .createCellStyle();

        Font font =
                row.getSheet()
                        .getWorkbook()
                        .createFont();

        font.setBold(true);

        style.setFont(font);

        style.setAlignment(
                HorizontalAlignment.CENTER);

        style.setFillForegroundColor(
                IndexedColors.GREY_25_PERCENT.getIndex());

        style.setFillPattern(
                FillPatternType.SOLID_FOREGROUND);

        style.setBorderTop(
                BorderStyle.THIN);

        style.setBorderBottom(
                BorderStyle.THIN);

        style.setBorderLeft(
                BorderStyle.THIN);

        style.setBorderRight(
                BorderStyle.THIN);

        cell.setCellStyle(style);

    }

    private void autoSize(
            XSSFSheet sheet,
            int columnas) {

        for (int i = 0; i < columnas; i++) {

            sheet.autoSizeColumn(i);

        }

    }

    @Override
    @Transactional(readOnly = false)
    public void importarProductos(org.springframework.web.multipart.MultipartFile file) throws Exception {
        try (java.io.InputStream is = file.getInputStream();
             XSSFWorkbook workbook = new XSSFWorkbook(is)) {
            
            XSSFSheet sheet = workbook.getSheetAt(0);
            
            // Fila 0 es título, fila 1 es cabecera. Empezamos en la fila 2.
            for (int i = 2; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                if (row == null) continue;
                
                Cell cellCodigo = row.getCell(0);
                if (cellCodigo == null || cellCodigo.getStringCellValue().trim().isEmpty()) {
                    continue;
                }
                
                String codigo = cellCodigo.getStringCellValue().trim();
                
                java.util.Optional<Producto> optProducto = productoRepository.findByCodigoProducto(codigo);
                if (optProducto.isPresent()) {
                    Producto prod = optProducto.get();
                    
                    // Columna 3: Stock
                    if (row.getCell(3) != null) {
                        prod.setStock((int) row.getCell(3).getNumericCellValue());
                    }
                    
                    // Columna 4: Costo
                    if (row.getCell(4) != null) {
                        prod.setCosto(java.math.BigDecimal.valueOf(row.getCell(4).getNumericCellValue()));
                    }
                    
                    // Columna 5: Precio
                    if (row.getCell(5) != null) {
                        prod.setPrecioEfectivo(java.math.BigDecimal.valueOf(row.getCell(5).getNumericCellValue()));
                    }
                    
                    productoRepository.save(prod);
                }
            }
        }
    }

}