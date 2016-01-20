
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
    
    $httpProvider.defaults.headers.common['X-Requested-With'] = 'XMLHttpRequest';
    
}]);


Module.service('entidades',[function(){
  
  return {};
  
}]);

Module.provider('ResourceService', [
  function () {

    var provider = this;

    provider.defaults = {
      size: 20,
      page: 0,
      url: null //'/s/persistence/'
    };

    this.$get = ['$http', '$q', 'context', 'entidades',
      function ($http, $q, context, entidades) {

        if (provider.defaults.url === null) {
          provider.defaults.url = context.services + '/persistencia';
        }

        function constantsConstructor() {
          this.equal = '=';
          this.notEqual = '!=';
          this.lowerThanOrEqualTo = '<=';
          this.greaterThanOrEqualTo = '>=';
          this.lowerThan = '<';
          this.greaterThan = '>';
          this.notLike = 'notlike';
          this.like = 'like';

          this.size = provider.defaults.size;
          this.page = provider.defaults.page;
          this.url = provider.defaults.url;
        }

        function EntityConstructor(nome, config) {
          config = config||{};
          this._entidadeNome = nome;
          
          this.size = config.size||this.size;
          this.page = config.page||this.page;
          this.url = config.url|| (this.url+'/'+this._entidadeNome);
        }
        EntityConstructor.prototype = new constantsConstructor();
        var proto = EntityConstructor.prototype;
        proto.query = function (config) {
          config = angular.extend( {}, this, config ); 
          var query = new QueryConstructor( config );

          //query.size(config.size);
          //query.page(config.page);
          if (config.url)
            query._url = config.url;
          else
            query._url += this._entidadeNome;

          var entidades = config.entidades;
          if (entidades instanceof Array) {
            for (var g in entidades) {
              query.param(g, this.equal, entidades[g]);
            }
          } else {
            for (var g in config) {
              //if (g === 'size' || g === 'page' || g === 'url')
              if( g in this ) continue;
              query.param(g, this.equal, config[g]);
            }
          }
          return query;
        };

        function QueryConstructor(config) {
          if( !config ) config = {};
          this._size = config.size || 0;
          this._page = config.page || 0;
          this._order = [];
          this._join = [];
          this._url = config.url || '';
          this._param = [];
          //this.config = config;
        }
        var proto = QueryConstructor.prototype; 
        proto.order = function (vals) {
          if (vals instanceof Array) {
            for (var g in vals) {
              var val = vals[g];
              if (typeof val === 'string')
                this._order.push(arguments[g]);
            }
          } else if (typeof vals === 'string') {
            this._order.push(vals);
          }
          return this;
        };
        proto.size = function (val) {
          if (typeof val === 'number')
            this._size = val;
          return this;
        };
        proto.page = function (val) {
          if (typeof val === 'number')
            this._page = val;
          return this;
        };
        proto.join = function (vals) {
          if (vals instanceof Array) {
            for (var g in vals) {
              var val = vals[g];
              if (typeof val === 'string')
                this._join.push(arguments[g]);
            }
          } else if (typeof vals === 'string') {
            this._join.push(vals);
          }
          return this;
        };
        proto.param = function (paramOrNome, comp, val, logicOp, quoteVal) {
          if (!paramOrNome)
            return null;
          if (!logicOp)
            logicOp = '&'; // Padão para E se for falso
          else if (logicOp !== '&' && logicOp !== '|')
            logicOp = '|'; // Padão para OU se for verdadeiro
          if (quoteVal === undefined) { // se não for definido verificaremos o valor para tentar sempre usar as aspas
            if (val.indexOf('"') !== 0 && val.indexOf("'") !== 0)
              val = '"' + val;
            if (val.lastIndexOf('"') !== val.length - 1 && val.lastIndexOf("'") !== val.length - 1)
              val = val + '"';
          } else if (quoteVal) {
            val = '"' + val + '"';
          }
          this._param.push(paramOrNome + ' ' + comp + ' ' + val + logicOp);
          return this;
        };
        proto.get = function (params) {

          // Montar Query String:
          //this.param(params);
          var queryStr = '';
          for (var g in this._param) {
            queryStr += this._param[g];
          }
          queryStr = queryStr.replace(/[&\|\s]+$/, '');
          if (queryStr) queryStr = '?' + queryStr;

          // Montar Matrix Params:
          var matrix = [
            'size=' + this._size,
            'page=' + this._page
          ];
          if (this._join.length > 0)
            matrix.push('join=' + this._join.join(','));
          if (this._order.length > 0)
            matrix.push('order=' + this._order.join(','));
          matrix = matrix.join(';');
          if (matrix)
            matrix = ';' + matrix;

          // Buscar:
          return $http.get(this._url + matrix + queryStr);
        };

        var ref = {
          entidade: function (nome, config) {
            entidades[nome] = new EntityConstructor(nome, config);
            return entidades[nome];
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

  provider.use = false;
  provider.url = '';

  provider.$get = ['context',function(context){

    var ref = {
      request:function( request ){
        if( provider.use ){
          request.url = provider.url + context.services + request.url ;
        }
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
          $window.location = origin + context.services + provider.url ;
        }
        return response ;
      }
    };
    return ref;

  }];

}]);
Module.provider('context',[function(){
    
    var provider = this;
        
    
    /**
     * 
     * @param {string} url The context url string, defaults to '' (empty string)
     * @param {boolean} prefixItRoot If that url needs to start with '/' char, defaults to TRUE
     * @param {boolean} sufixIt If that url needs to end with '/' char, defaults to FALSE
     * @returns {string} The actual context url string
     */
    function _context(url, prefixItRoot, sufixIt){
        if( !url ) return '';
        if( !(typeof url === 'string') ) throw 'To configure the context url string you must pass a string as the first parameter value.';
        if( typeof prefixItRoot === 'undefined' ) prefixItRoot = true; // Defaults to true
        url = url.trim();
        
        if( prefixItRoot ){ if( url.indexOf('/') !== 0 ) url = '/'+url; }
        else{ if( url.indexOf('/') === 0 ) url = url.substring(1,url.length); }
        
        if( sufixIt ){ if( url.lastIndexOf('/') !== url.length-1 ) url = url+'/'; }
        else{ if( url.lastIndexOf('/') === url.length-1 ) url = url.substring(0,url.length-1); }
        
        return corrigirUrl(url);
    };
    var _funcs = {
      root: function(/*, params _context */){
        provider.context.root = _context.apply(this, arguments);
        provider.context.services = corrigirUrl( provider.context.root + provider.context.path.services );
        provider.context.websocket = corrigirUrl( provider.context.root + provider.context.path.websocket );
        return provider ;
      } ,
      services: function(/*, params _context */){
        provider.context.services = corrigirUrl( 
          provider.context.root + 
          (provider.context.path.services = _context.apply(this, arguments) )
          );
        return provider ;
      } ,
      websocket: function(/*, params _context */){
        provider.context.websocket = corrigirUrl( 
          provider.context.root + 
          (provider.context.path.websocket = _context.apply(this, arguments) )
          );
        return provider ;
      } ,
      put: function(nome /*, params _context */){
        provider.context[nome] = _context.call(this, arguments[1], arguments[2], arguments[3]);
        return provider ;
      },
      context: { root: '', path: {} }
    };
    angular.extend( this, _funcs );
    
    function corrigirUrl(str){
      return str.replace(/\/\//g,'/');
    }
    
    this.$get = [function(){
      
      return provider.context;
    }];
    
}]);

/*


      var atual, anterior, itensAtuais, 
          continuosRef = {
        put: function(){
          
          return continuosRef;
        },
        in: function( url ){
          if( !url ){
            return continuosRef;
          }
          url = corrigirUrl(url);
          if( url[0] === '/' ) url = url.substr(1);
          url = url.split('/');
          anterior.push( atual );
          for(var i=0; i<url.length; i++){
            if( !atual[url[i]] ) atual[url[i]] = {};
            atual = atual[url];
          }
          return continuosRef;
        },
        clearIn: function(){
          ref.clearIn();
          return continuosRef;
        },
        end: function(){
          atual = anterior.pop();
          return continuosRef;
        },
        done: function(){
          //////////
          ref.clearIn();
          return continuosRef;
        }
      };
      


        // Ex.: put( 'logout', '/sistema/logout', context.services );
        // irá criar no Attr "context.logout" a URL "/s/sistema/logout"
        put: function( nomeAttr, url , urlPrefix ){
          if( !urlPrefix ) urlPrefix = ref.services ;
          ref[ nomeAttr ] = corrigirUrl(urlPrefix + url);
        },
        in: function( url ){
          continuosRef.in( url );
          return continuosRef ;
        },
        clearIn: function(){
          anterior = [];
          atual = itensAtuais = {};
        }

*/


})(window);
