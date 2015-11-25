Module.provider('ResourceService',[
            function(){
    
    var provider = this;
    
    this.defaults = {
        size: 20,
        page: 0,
        url: '/s/persistence/'
    } ;
    
    this.$get = ['$http','$q','context',function($http,$q,context){
        
        function constantsConstructor(){
            this.equal = '=';
            this.notEqual = '!=';
            this.lowerThanOrEqualTo = '<=';
            this.greaterThanOrEqualTo = '>=';
            this.lowerThan = '<';
            this.greaterThan = '>';
            this.notLike = 'notlike';
            this.like = 'like';
            
            this.size = provider.defaults.size ;
            this.page = provider.defaults.page ;
            this.url = context + provider.defaults.url ;
        }

        function EntityConstructor( nome, config ){
            this._etidadeNome = nome;
            if( config ){
                this.defaults = config ;
            }
        }
        EntityConstructor.prototype = new constantsConstructor();
        EntityConstructor.prototype.query = function( config ){
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

        function QueryConstructor( config ){
            this._size = config.size ;
            this._page = config.page ;
            this._order = [] ;
            this._join = [] ;
            this._url = config.url ;
            this._param = [] ;
            this.config = config ;
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
            return this;
        };
        QueryConstructor.prototype.size = function(val){ if(typeof val==='number')this._size = val; return this; };
        QueryConstructor.prototype.page = function(val){ if(typeof val==='number')this._page = val; return this; };
        QueryConstructor.prototype.join = function( vals ){
            if( vals instanceof Array ){
                for( var g in vals ){
                    var val = vals[g];
                    if(typeof val==='string') this._join.push( arguments[g] );
                }
            }else if(typeof vals==='string'){
                this._join.push( vals );
            }
            return this;
        };
        QueryConstructor.prototype.param = function( paramOrNome, comp, val, logicOp, quoteVal ){
            if( !paramOrNome ) return null;
            if( !logicOp ) logicOp = '&'; // Padão para E se for falso
            else if( logicOp !== '&' && logicOp !== '|' ) logicOp = '|'; // Padão para OU se for verdadeiro
            if( quoteVal === undefined ){ // se não for definido verificaremos o valor para tentar sempre usar as aspas
                if( val.indexOf('"') !== 0 && val.indexOf("'") !== 0 ) val = '"'+val;
                if( val.lastIndexOf('"') !== val.length-1 && val.lastIndexOf("'") !== val.length-1 ) val = val+'"';
            }else if( quoteVal ){
                val = '"'+val+'"';
            }
            this._param.push( paramOrNome+' '+comp+' '+val+logicOp );
            return this;
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
                var entidade = new EntityConstructor( nome );
            }
        };
        return ref;
    }];
    
}]);