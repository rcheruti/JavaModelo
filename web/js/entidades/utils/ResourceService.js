Module.service('ResourceService',[function(){
    /**
     * Esse é um recurso auxiliar para criar entidades e serviços que têm
     * chamadas padrão ao lado do servidor. Ex.: Entidades de banco
     * 
     * post:{
     *      data: { dados para criar a entidade }
     * }
     * put:{
     *      id: { chaves da entidade },
     *      data: { dados para atualizar a entidade }
     * }
     * delete:{
     *      id: { chaves da entidade }
     * }
     * get:{
     *      
     * }
     * 
     * @type ResourceService
     */
    
    var defaults = {
        
    };
    
    var ref = {
        create:function( url ){
            
        }
    };
    return ref;
    
}]);