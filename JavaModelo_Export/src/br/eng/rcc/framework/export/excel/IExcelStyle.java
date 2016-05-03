
package br.eng.rcc.framework.export.excel;

import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

/**
 * Objetos dessa interface são injetados nas ferramentas de exportação para excel
 * para que sejá possível definir os estilos padrão da exportação.
 * <br><br>
 * 
 * @author rcheruti
 */
public interface IExcelStyle {
  
  default XSSFCellStyle titulos(XSSFWorkbook workbook){return new DefaultExcelEstilo().titulos(workbook); };
  default XSSFCellStyle atributos(XSSFWorkbook workbook){return new DefaultExcelEstilo().atributos(workbook); };
  
}
