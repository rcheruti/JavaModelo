package br.eng.rcc.framework.export.excel;

import br.eng.rcc.framework.jaxrs.JsonResponse;
import br.eng.rcc.framework.jaxrs.MsgException;
import br.eng.rcc.framework.utils.ClassCache;
import br.eng.rcc.framework.persistencia.EntidadesService;
import br.eng.rcc.framework.utils.ClasseAtributoUtil;
import br.eng.rcc.framework.utils.BuscaInfo;
import br.eng.rcc.framework.utils.ClasseUtil;
import br.eng.rcc.framework.utils.IBeanUtil;
import com.fasterxml.jackson.databind.JsonNode;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import javax.annotation.PostConstruct;
import javax.enterprise.context.RequestScoped;
import javax.enterprise.inject.Any;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

/**
 * Com essa classe será possível preparar a exportação para arquivos <b>".xlsx"</b>
 * <br><br>
 * 
 * 
 * @author rcheruti
 */

@RequestScoped
public class UtilsDownloadExcel {
  
  @Inject
  protected EntidadesService entServ;
  
  @Inject
  protected ClassCache cache;
  
  @Inject @Any
  protected Instance<IExcelStyle> injectEstilos;
  
  protected IExcelStyle estilos;
  
  @PostConstruct
  public void postConstruct(){
    if( !injectEstilos.isUnsatisfied() ){
      estilos = injectEstilos.get();
    }else{
      estilos = new DefaultExcelEstilo();
    }
  }
  
  //------------------------------------------------------------------------
  
  /*
  public static Response download( 
          EntityManager em, Class klass, 
          String sheetName ,
          List<String> cabs,
          List<String> selecionar,
          String[] ordem, boolean asc ){
    
    ...
    
    //=========================================================================
    
    ...
  
    StreamingOutput sOut = ( out )->{ workbook.write( out ); };
    Response.ResponseBuilder resp = Response.ok( sOut ); 
    resp.header("Content-Disposition", "attachment; filename=\""+ sheetName +".xlsx\"");
    return resp.build();
  }
  /* */
  
  
  
  /**
   * Este método criará um {@link XSSFWorkbook}.
   * <br><br>
   * O formato do objeto em <code>info.data[0]</code> é:
   * <br>
   * <pre>
   * {
   *  "nome": string ,
   *  "titulos": [ string ... ] ,
   *  "atributos": [ string ... ]
   * }
   * </pre>
   * <br><br>
   * O índice em <code>"titulos"</code> e <code>"atributos"</code> será usado para
   * sincronizar qual é o título para qual atributo.
   * 
   * @param info O objeto com as informações da busca que foi feita
   * @return XSSFWorkbook
   * @throws Exception
   */
  public XSSFWorkbook exportarEntidade( BuscaInfo info ) throws Exception{
    
    JsonNode node = info.data.path(0);
    JsonNode nodeAttr = node.path("atributos");
    JsonNode nodeTitu = node.path("titulos");
    JsonNode nodeNome = node.path("nome");
    if( !nodeAttr.isArray() || !nodeTitu.isArray() ) 
      throw new MsgException(JsonResponse.ERROR_EXCECAO, null, 
            "Informe o objeto com as configs de exportação no atributo 'data'!");
    
    ClasseUtil classUtil = cache.getUtil( info.entidade );
    for( JsonNode nodeString : nodeAttr ){
      Object x = classUtil.path( nodeString.asText() );
      if( x == null ) throw new MsgException(JsonResponse.ERROR_EXCECAO, null,
            String.format("O atributo '%s' não existe na entidade %s.", 
                    nodeString.asText(), info.entidade ));
    }
    
      // busca:
    BuscaInfo clone = info.clone();
    clone.acao = BuscaInfo.ACAO_BUSCAR;
    List<String> joins = new ArrayList<>();
    for( JsonNode nodeString : nodeAttr ){
      String[] strs = nodeString.asText().split("\\.");
      if( strs.length > 1 ){
        for(int i = 1; i < strs.length ; i++){
          joins.add( String.join(".", Arrays.copyOfRange(strs, 0, i ) ) );
        }
      }
    }
    if( joins.size() > 0 ){
      joins.addAll( Arrays.asList(clone.join) );
      clone.join = joins.toArray(new String[0]);
    }
    List<Object> lista = (List<Object>)entServ.processar(clone);
    
      // exportar:
    XSSFWorkbook workbook = new XSSFWorkbook();
    XSSFSheet sheet = workbook.createSheet( nodeNome.asText() );
    XSSFCellStyle styleHeader = estilos.titulos( workbook );
    XSSFCellStyle styleBody = estilos.atributos( workbook );
    
    int i;
    XSSFRow row = sheet.createRow( 0 );
    i = 0;
    for( JsonNode tNode : nodeTitu ){
      Cell cell = row.createCell( i );
      cell.setCellStyle(styleHeader);
      cell.setCellValue( tNode.asText() );
      i++;
    }
    
    int nextRow = 1 ;
    XSSFCell cell;
    for(Object objEnt : lista){
      row = sheet.createRow( nextRow++ );
      i = 0;
      for( JsonNode nodeString : nodeAttr ){
        Object valor = classUtil.get(objEnt, nodeString.asText());
        cell = row.createCell( i );
        cell.setCellStyle(styleBody);
        cell.setCellValue( valor==null? null : valor.toString() );
        i++;
      }
    }
    
    for( i = nodeAttr.size()-1; i >= 0; i-- ) sheet.autoSizeColumn( i );
    
    return workbook;
  }
  
  
}
