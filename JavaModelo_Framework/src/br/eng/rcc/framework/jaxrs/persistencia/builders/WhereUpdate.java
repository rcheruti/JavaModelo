
package br.eng.rcc.framework.jaxrs.persistencia.builders;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaDelete;
import javax.persistence.criteria.CriteriaUpdate;
import javax.persistence.criteria.Expression;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

public class WhereUpdate implements WhereBuilderInterface{
    
    private final Map<String, CriteriaWhereBuilder> map;
    private CriteriaBuilder cbObj;
    private CriteriaUpdate query;
    private List<Predicate> exps;
    
    
    WhereUpdate(CriteriaBuilder cb_, 
                CriteriaUpdate query,
                Map<String, CriteriaWhereBuilder> stcMap
            ){
        this.map = stcMap;
        this.cbObj = cb_;
        this.query = query;
        this.exps = new ArrayList<>();
    }
    
    
    
    
    public WhereUpdate add(String[] vet){
        Root root = query.getRoot();
                String[] vet0 = vet[0].split("\\.");
                Path exp1 = root.get(vet0[0]);
                for(int i = 1; i < vet0.length; i++) exp1 = exp1.get(vet0[i]);
                exps.add( map.get(vet[1]).apply( cbObj, exp1, vet[2] ) );
        return this;
    }
    public WhereUpdate addArray( String[][] arr ){
        Root root = query.getRoot();
        Expression exp = null;
        for(String[] vet : arr){
            if( vet == null 
                || vet[0] == null
                || vet[1] == null
                || vet[2] == null
                )continue;
            try{
                String[] vet0 = vet[0].split("\\.");
                Path exp1 = root.get(vet0[0]);
                for(int i = 1; i < vet0.length; i++) exp1 = exp1.get(vet0[i]);
                exps.add( map.get(vet[1]).apply( cbObj, exp1, vet[2] ) );
            }catch(IllegalArgumentException ex){}
        }
        return this;
    }
    public Predicate[] build(){
        return exps.toArray(new Predicate[0]);
    }
    
    
}
