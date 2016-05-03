
package br.eng.rcc.framework.export.excel;

import java.awt.Color;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFColor;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

public class DefaultExcelEstilo implements IExcelStyle{

  @Override
  public XSSFCellStyle titulos(XSSFWorkbook workbook) {
    Font font = workbook.createFont();
    font.setBold(true);

    // Cor da celula: rgb( 142 , 169 , 219 )
    XSSFCellStyle style = (XSSFCellStyle) workbook.createCellStyle();
    style.setFillPattern(XSSFCellStyle.SOLID_FOREGROUND);
    style.setFillForegroundColor(new XSSFColor(new Color(142, 169, 219)));
    style.setFillBackgroundColor(new XSSFColor(new Color(142, 169, 219)));
    style.setFont(font);
    style.setAlignment(XSSFCellStyle.ALIGN_CENTER);

    return style;
  }

  @Override
  public XSSFCellStyle atributos(XSSFWorkbook workbook) {
    XSSFCellStyle style = (XSSFCellStyle) workbook.createCellStyle();
    return style;
  }
  
}
