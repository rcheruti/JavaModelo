Module.provider('ResourceService',[
            function(){
    
    var provider = this;
    
    this.defaults = {
        size: 20,
        page: 0,
        url: (context?context:'') +'/s/persistence/'
    } ;
    
    this.$get = ['$http','$q',function($http,$q){
        
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
        
        function constantesComparacao(){
            this.equal = '=';
            this.notEqual = '!=';
            this.lowerThanOrEqualTo = '<=';
            this.greaterThanOrEqualTo = '>=';
            this.lowerThan = '<';
            this.greaterThan = '>';
            this.notLike = 'notlike';
            this.like = 'like';
            this.defaults = provider.defaults ;
        }

        function EntidadeConstructor( nome, config ){
            this._etidadeNome = nome;
            if( config ){
                this.defaults = config ;
            }
        }
        EntidadeConstructor.prototype = new constantesComparacao();
        EntidadeConstructor.prototype.query = function( config ){
            var query = new QueryConstructor();
            
            query.size( config.size );
            query.page( config.page );
            if( config.url ) query._url = config.url ;
            else query._url += this._etidadeNome ;
            
            var entidades = config.entidades ;
            if( entidades instanceof Array ){
                for( var g in entidades ){
                    query.param( g, this.equal , entidades[g] );
                }
            }else{
                for( var g in config ){
                    if( g==='size'||g==='page'||g==='url' ) continue;
                    query.param( g, this.equal , config[g] );
                }
            }
            return query ;
        };

        function QueryConstructor(){
            this._size = this.defaults.size ;
            this._page = this.defaults.page ;
            this._order = [] ;
            this._join = [] ;
            this._url = this.defaults.url ;
            this._param = [] ;
        }
        QueryConstructor.prototype.order = function( vals ){
            if( vals instanceof Array ){
                for( var g in vals ){
                    var val = vals[g];
                    if(typeof val==='string') this._order.push( arguments[g] );
                }
            }else if(typeof vals==='string'){
                this._order.push( vals );
            }
        };
        QueryConstructor.prototype.size = function(val){ this._size = (typeof val==='number')? val : this.defaults.size ; };
        QueryConstructor.prototype.page = function(val){ this._page = (typeof val==='number')? val : this.defaults.page ; };
        QueryConstructor.prototype.join = function( vals ){
            if( vals instanceof Array ){
                for( var g in vals ){
                    var val = vals[g];
                    if(typeof val==='string') this._join.push( arguments[g] );
                }
            }else if(typeof vals==='string'){
                this._join.push( vals );
            }
        };
        QueryConstructor.prototype.param = function( paramOrNome, comp, val, logicOp, quoteVal ){
            if( !paramOrNome ) return null;
            if( !logicOp ) logicOp = '&'; // Padão para E se for falso
            else if( logicOp !== '&' && logicOp !== '|' ) logicOp = '|'; // Padão para OU se for verdadeiro
            if( quoteVal === undefined ){ // se não for definido verificaremos o valor para tentar sempre usar as aspas
                if( val.indexOf('"') != 0 && val.indexOf("'") != 0 ) val = '"'+val;
                if( val.lastIndexOf('"') != val.length-1 && val.lastIndexOf("'") != val.length-1 ) val = val+'"';
            }else if( quoteVal ){
                val = '"'+val+'"';
            }
            this._param.push( paramOrNome+' '+comp+' '+val+logicOp );
        };
        QueryConstructor.prototype.get = function( params ){
            
                // Montar Query String:
            this.param( params );
            var queryStr = '';
            for( var g in this._param ){
                queryStr += this._param[g];
            }
            queryStr.replace( /[&\|]+\s*$/, '' );
            if( queryStr ) queryStr = '?'+queryStr ;
            
                // Montar Matrix Params:
            var matrix = [
                'size='+ this._size ,
                'page='+ this._page 
            ];
            if( this._join.length > 0 ) matrix.push( 'join='+this._join.join(',') );
            if( this._order.length > 0 ) matrix.push( 'join='+this._order.join(',') );
            matrix = matrix.join(';');
            if( matrix ) matrix = ';'+matrix ;
                
                // Buscar:
            return $http.get(this._url+matrix+queryStr);
        };

        var ref = {
            entidade:function( nome ){
                var entidade = new EntidadeConstructor( nome );
            }
        };
        return ref;
    }];
    
}]);