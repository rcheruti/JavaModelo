
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


(function(){
  
  var injector = angular.injector(['ng'],true),
      $http = injector.get('$http'),
      $q = injector.get('$q');
  
  function _getObjByPath( obj, path ){
    if( !path ) return obj;
    if( typeof path === 'string' ) path = path.split('.');
    for(var i =0; i < path.length; i++){
      obj = obj[path[i]] || (obj[path[i]] = {});
    }
    return  obj;
  }
  function _apply( query, scope ){
    if( query.$scope && !query.$scope.$$phase ) query.$scope.$apply(  );
    else if( scope && scope.$apply && !scope.$$phase ) scope.$apply(  );
  }
  
  //===========================================================================
  
  var defaults = {
        contentType: 'application/json',
        dataPath: 'data.data',
        size: 20,
        page: 0,
        url: '/persistencia'
      },
      headers = {
        'Content-Type': defaults.contentType
      },
      tiposCache = {},
      entidadesCache = {};

  //===========================================================================
  
  function Entidade( nome, config ){
    if( !config ) config = {};
    this.nome = nome;
    this.dataPath = config.dataPath || defaults.dataPath;
    this.size = config.size || defaults.size;
    this.page = config.page || defaults.page;
    this.url = config.url || defaults.url ;
  }
  
  function Query( ents ){
    this.entidade = ents instanceof Array? ents[0] : ents;
    this._size = this.entidade.size;
    this._page = this.entidade.page;
    this._param = [];
    this._join = [];
    this._order = [];
    this._data = null;
    this._url = this.entidade.url;
    this._method = '';
    this.$scope = null;
    this._build = false;
  }
  var proto = Query.prototype;
    
  proto.size = function( x ){
    if( typeof x === 'number' ) this._size = x;
    return this;
  };
  proto.page = function( x ){
    if( typeof x === 'number' ) this._page = x;
    return this;
  };
  proto.order = function (vals) {
    if (vals instanceof Array) {
      for (var g in vals) {
        var val = vals[g];
        if (typeof val === 'string')
          this._order.push( val );
      }
    } else if (typeof vals === 'string') {
      this._order.push(vals);
    }
    return this;
  };
  proto.join = function (vals) {
    if (vals instanceof Array) {
      for (var g in vals) {
        var val = vals[g];
        if (typeof val === 'string')
          this._join.push( val );
      }
    } else if (typeof vals === 'string') {
      this._join.push(vals);
    }
    return this;
  };
  proto.param = function (nome, comp, val, logicOp, quoteVal) {
    if (!nome)
      return null;
    if (!logicOp)
      logicOp = '&'; // Padão para E se for falso
    else if (logicOp !== '&' && logicOp !== '|')
      logicOp = '|'; // Padão para OU se for verdadeiro
    if (quoteVal === undefined) { // se não for definido verificaremos o valor para tentar sempre usar as aspas
      val = '' + val;
      if (val.indexOf('"') !== 0 && val.indexOf("'") !== 0)
        val = '"' + val;
      if (val.lastIndexOf('"') !== val.length - 1 && val.lastIndexOf("'") !== val.length - 1)
        val = val + '"';
    } else if (quoteVal) {
      val = '"' + val + '"';
    }
    this._param.push(nome + ' ' + comp + ' ' + val + logicOp);
    return this;
  };
  proto.data = function( x ){
    this._data = x;
    return this;
  };
  proto.method = function( x ){
    this._method = x;
    return this;
  };
  
  proto.build = function( force ){
    if( this._build && !force ) return this;
    // Montar Query String:
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
    
    // Montar url:
    var url = this._url +'/' +this.entidade.nome ;
    this._url = url + matrix + queryStr ;
    this._build = true;
    return this;
  };
  proto.request = function(){
    var that = this;
    return $http({
      method: this._method,
      url: this._url,
      headers: headers,
      data: this._data || {}
    }).then(function(data){
      return _getObjByPath( data, that.entidade.dataPath );
    });
  };
  proto.apply = function( $scope ){
    this.$scope = $scope;
    return this;
  };
  
  proto.tipo = function( override ){
    var that = this;
    var cached = tiposCache[that.entidade.nome] ;
    if( !cached || override  ){
      that._url += '/tipo';
      return that.method('GET').build().request().then(function(data){
        return tiposCache[that.entidade.nome] = data;
      });
    }
    return $q.resolve( cached );
  };
  
  __construirRequisicao( 'get','GET' );
  __construirRequisicao( 'put','PUT' );
  __construirRequisicao( 'post','POST' );
  __construirRequisicao( 'delete','DELETE' );
  
  __construirRequisicaoIn( 'tipoIn','tipo' );
  __construirRequisicaoIn( 'getIn','get' );
  __construirRequisicaoIn( 'putIn','put' );
  __construirRequisicaoIn( 'postIn','post' );
  __construirRequisicaoIn( 'deleteIn','delete' );
  
  function __construirRequisicao( nomeFunc, method ){
    proto[nomeFunc] = function( _data ){
      return this.data( _data ).method( method ).build().request();
    };
  }
  function __construirRequisicaoIn( nomeFunc, methodFunc ){
    proto[nomeFunc] = function( obj, key, _data ){
      if( !key ) key = this.entidade.nome;
      key = key.split('.');
      var objToBind = _getObjByPath( obj, key.slice(0,key.length-1) );
      key = key[key.length-1];
      var that = this;
      return this[methodFunc]( _data ).then(function(data){
        objToBind[key] = data;
        _apply( that, obj );
        return data;
      });
    };
  }

//============================================================================
Module.provider('Entidades',[function(){
    
  this.defaults = defaults;
  this.$get = ['context',function(context){
    var that = this;
    var ref = {
      query: function( entOrList ){
        if( !(entOrList instanceof Array) ) entOrList = [ entOrList ];
        for( var i = 0; i < entOrList.length; i++ ){
          var entOrStr = entOrList[i];
          if( typeof entOrStr === 'string' ) entOrList[i] = entidadesCache[entOrStr] ;
        }
        return new Query( entOrList );
      },
      entidade: function( nome, config, override ){
        var ent = entidadesCache[nome];
        if( ent && !override ) return ent;
        if( !config ) config = {};
        if( !config.url ) config.url = context.services+ defaults.url ;
        ent = new Entidade( nome, config );
        that[nome] = entidadesCache[nome] = ent;
        return ent;
      },
      get: function( nome ){
        return entidadesCache[nome] ;
      }
    };
    ref.eq = ref.equal = '=';
    ref.ne = ref.notEqual = '!=';
    ref.le = ref.lowerThanOrEqualTo = '<=';
    ref.ge = ref.greaterThanOrEqualTo = '>=';
    ref.lt = ref.lowerThan = '<';
    ref.gt = ref.greaterThan = '>';
    ref.nl = ref.notLike = 'notlike';
    ref.lk = ref.like = 'like';
    return ref;
  }];

}]);

})();

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
      return str.replace(/\/\/+/g,'/');
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
