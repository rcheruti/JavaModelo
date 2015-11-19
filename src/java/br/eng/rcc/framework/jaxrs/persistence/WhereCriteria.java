
package br.eng.rcc.framework.jaxrs.persistence;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Expression;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

public class WhereCriteria implements WhereBuilderInterface{
    
    private final Map<String, CriteriaWhereBuilder> map;
    private CriteriaBuilder cbObj;
    private CriteriaQuery query;
    private List<Predicate> exps;
    
    
    WhereCriteria(CriteriaBuilder cb_, 
                CriteriaQuery query,
                Map<String, CriteriaWhereBuilder> stcMap
            ){
        this.map = stcMap;
        this.cbObj = cb_;
        this.query = query;
        this.exps = new ArrayList<>();
    }
    
    
    
    
    public WhereCriteria add(String[] vet){
        Root root = (Root)query.getRoots().iterator().next();
        exps.add( map.get(vet[1]).apply( cbObj, root.get(vet[0]), vet[2] ) );
        return this;
    }
    public WhereCriteria addArray( String[][] arr ){
        Root root = (Root)query.getRoots().iterator().next();
        Expression exp = null;
        for(String[] vet : arr){
            if( vet == null 
                || vet[0] == null
                || vet[1] == null
                || vet[2] == null
                )continue;
            try{
                exps.add( map.get(vet[1]).apply( cbObj, root.get(vet[0]), vet[2] ) );
            }catch(IllegalArgumentException ex){}
        }
        return this;
    }
    public Predicate[] build(){
        return exps.toArray(new Predicate[0]);
    }
    
    
}
