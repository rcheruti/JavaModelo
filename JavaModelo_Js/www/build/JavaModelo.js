
(function(window){


var Module = angular.module('JavaModelo',['ng']);

  // Configuração dos interceptadores desse módule
Module.config(['$httpProvider',
          //'$compileProvider','$logProvider','HostInterProvider',
        function($httpProvider
          //,$compileProvider,$logProvider,HostInterProvider
            ){
    
    //$httpProvider.useApplyAsync( true );
    //$compileProvider.debugInfoEnabled( true );
    //$logProvider.debugEnabled( true );
    
    //HostInterProvider.url = 'http://127.0.0.1:8080';
    
    $httpProvider.interceptors.push( 'LoginInter' );
    $httpProvider.interceptors.push( 'HostInter' );
    
}]);


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
Module.provider('HostInter',[function(){
      /**
       * Interceptador para usar as ferramentas de desenvolvimento do Chrome
       * @type NetbeansChromeInter
       */

  var provider = this;

  provider.use = true;
  provider.url = '';

  provider.$get = ['context',function(context){

    var ref = {
      request:function( request ){
        if( provider.use ){
          request.url = provider.url + context + request.url ;
        }
        console.log('Requisição para ', request.url );
        return request;
      }
    };
    return ref;

  }];

}]);
Module.provider('LoginInter',[function(){

  var provider = this;

  provider.url = '/login.jsp';
  provider.ERRORCODE_LOGIN = 401 ;

  provider.$get = ['context','$window',function(context,$window){
    /**
     * Esse interceptador redireciona o usuário para a página de login
     * caso o servidor informe o código de erro de usuário não logado.
     * @type LoginInter
     */

    var ref = {
      response:function( response ){
        if( response.data && response.data.code === provider.ERRORCODE_LOGIN ){
          var origin = $window.location.origin ;
          $window.location = origin + context + provider.url ;
        }
        return response ;
      }
    };
    return ref;

  }];

}]);
Module.provider('context',[function(){
    
    var context = '';
    
    /**
     * 
     * @param {string} url The context url string, defaults to '' (empty string)
     * @param {boolean} prefixItRoot If that url needs to start with '/' char, defaults to TRUE
     * @param {boolean} sufixIt If that url needs to end with '/' char, defaults to FALSE
     * @returns {string} The actual context url string
     */
    this.context = function(url, prefixItRoot, sufixIt){
        if( typeof url === 'undefined' ) return context;
        if( !(typeof url === 'string') ) throw 'To configure the context url string you must pass a string as the first parameter value.';
        if( typeof prefixItRoot === 'undefined' ) prefixItRoot = true; // Defaults to true
        url = url.trim();
        
        if( prefixItRoot ){ if( url.indexOf('/') !== 0 ) url = '/'+url; }
        else{ if( url.indexOf('/') === 0 ) url = url.substring(1,url.length); }
        
        if( sufixIt ){ if( url.lastIndexOf('/') !== url.length-1 ) url = url+'/'; }
        else{ if( url.lastIndexOf('/') === url.length-1 ) url = url.substring(0,url.length-1); }
        
        context = url;
        return context;
    };
    
    this.$get = [function(){
        return context;
    }];
    
}]);

})(window);
