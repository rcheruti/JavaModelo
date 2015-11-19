
package br.eng.rcc.framework.utils;

import br.eng.rcc.framework.jaxrs.persistence.EntidadesService;
import java.lang.reflect.Field;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.enterprise.context.ApplicationScoped;
import javax.persistence.EntityManager;
import javax.persistence.metamodel.EntityType;
import javax.persistence.metamodel.Metamodel;
import javax.persistence.metamodel.PluralAttribute;

@ApplicationScoped
public class EntidadesUtils {
    
    public void nullifyLazy(EntityManager em, Object[] lista, 
            String[] params){
        nullifyLazy(em, lista, params, 0);
    }
    public void nullifyLazy(EntityManager em, Object[] lista, 
            String[] params, int secureLevel ){
            // Se a árvore descer demais, podemos estar em recursão infinita,
            // temos que isso aconteça
        if( secureLevel++ >= 50 ){
            throw new IllegalArgumentException("Um dos parâmetros esta fazendo com que entremos em recursão infinita!");
        }
        if( lista.length > 0 ){
            List<Field> fieldsToNullify = new LinkedList<>();
            List<Field> fieldsDownNullify = new LinkedList<>();
            
            Metamodel meta = em.getMetamodel();
            EntityType type = meta.entity( lista[0].getClass() );
            Set<PluralAttribute> pluralAttrs = type.getDeclaredPluralAttributes();
            for(PluralAttribute attr : pluralAttrs){
                String name = attr.getName();
                Field member = (Field)attr.getJavaMember();
                if( !constainsInArray(params, name) ){
                    fieldsToNullify.add( member );
                }else{
                    // Descer a arvore para nullify
                    fieldsDownNullify.add(member);
                }
            }
            
            // Aplicar valor NULL nas lista que são LAZY:
            for(Field field : fieldsToNullify){
                for(Object obj : lista){
                    try {
                        field.set(obj, null);
                    } catch (IllegalArgumentException | IllegalAccessException ex) {
                        Logger.getLogger(EntidadesService.class.getName()).log(Level.SEVERE, null, ex);
                        return;
                    }
                }
            }
            // Descer a arvore para nullify:
            for(Field field : fieldsDownNullify){
                for(Object obj : lista){
                    try {
                        nullifyLazy( em, ((List<Object>)field.get(obj)).toArray(), 
                                new String[0], secureLevel );
                    } catch (IllegalArgumentException | IllegalAccessException ex) {
                        Logger.getLogger(EntidadesService.class.getName()).log(Level.SEVERE, null, ex);
                        return;
                    }
                }
            }
        }
    }
    public boolean constainsInArray(Object[] arr, Object obj){
        if(arr == null || obj == null)return false;
        int hash = obj.hashCode();
        for(Object x : arr){
            if( hash == x.hashCode() && obj.equals(x) )return true;
        }
        return false;
    }
    
    
}
