
(function(window){


/*
 * Funções usadas por todo o código
 */

function __construirSetter( pro, nomeFunc, nomeAttr, defaultVal ){
  if( typeof defaultVal !== 'undefined' ) 
    pro[nomeFunc] = function( val ){
      if( typeof val === 'undefined' ) val = defaultVal;
      this[nomeAttr] = val;
      return this;
    };
  else pro[nomeFunc] = function( val ){
    this[nomeAttr] = val;
    return this;
  };
}


var Module = angular.module('JavaModelo',['ng','ui.router']);

var urlContext = 'persistencia/context',
    hiProvider = null,
    ctxProvider = null;
  
Module.run(['$templateCache','$rootElement',  'path',
    function($templateCache,$rootElement,  path){
  
  console.log( 'Testando path', path );
  console.log( "path('servico','persistencia')", path('servico','persistencia') );
  
  if( !hiProvider.ativo ){
    // Pegar o context;
    var url = urlContext;
    var xhttp = new XMLHttpRequest();
    xhttp.withCredentials = true;
    xhttp.onreadystatechange = function() {
      if (xhttp.readyState === 4 && xhttp.status === 200) {
        //console.log( 'novo context', xhttp.responseText );
        window.contextRoot = xhttp.responseText;
      }
    };
    xhttp.open("GET", url, false);
    xhttp.send();
    ctxProvider.root( window.contextRoot );
  }else{
    var scripts = $rootElement[0].querySelectorAll('script[type="text/ng-template"]');
    var url = hiProvider.url.replace(/\/$/,'');
    for(var i = 0; i < scripts.length; i++){
      var key = scripts[i].id ;
      var nome = url+ '/'+ key.replace(/^\//,'') ;
      $templateCache.put( nome, scripts[i].textContent );
    }
  }
  
  
  
}]);
  
  // Configuração dos interceptadores desse módule
Module.config(['$httpProvider','contextProvider','HostInterProvider', 'pathProvider',
          //'$compileProvider','$logProvider',
        function($httpProvider,contextProvider, HostInterProvider , pathProvider
          //,$compileProvider,$logProvider
            ){
            
  //$httpProvider.useApplyAsync( true );
  //$compileProvider.debugInfoEnabled( true );
  //$logProvider.debugEnabled( true );
  
  //HostInterProvider.ativo = true;
  //HostInterProvider.url = 'http://127.0.0.1:8080';
  
  pathProvider.host = 'https://www.aqui.com.br:9090/Depois';
  pathProvider.servico = '/servicos';
  
  ctxProvider = contextProvider;
  hiProvider = HostInterProvider;
  
  $httpProvider.interceptors.push( 'LoginInter' );
  $httpProvider.interceptors.push( 'HostInter' );

  $httpProvider.defaults.headers.common['X-Requested-With'] = 'XMLHttpRequest';
  $httpProvider.defaults.withCredentials = true;
  
  
}]);


Module.directive('segGrupo', ['Usuario',function(Usuario){

    return {
      link: function (scope, el, attr) {
        Usuario.then(function( u ){
          if (!u) return;

          if (u.credencial) {
            if (u.credencial.grupos) {
              var grupos = {};
              for (var gK in u.credencial.grupos) {
                var g = u.credencial.grupos[gK];
                var gNome = g.chave.trim();
                if (!grupos[gNome]) grupos[gNome] = g;
              }

              var permNome = attr.segGrupo.trim();
              if (permNome.indexOf('!') === 0) {
                if (grupos[permNome.substring(1, permNome.length)]) el.remove();
              } else {
                if (!grupos[permNome]) el.remove();
              }
            }
          }
        });
      }
    };

  }]);
Module.directive('segPermissao', ['Usuario',function(Usuario){

    return {
      link: function (scope, el, attr) {
        Usuario.then(function(u){
          if (!u) return;

          if (u.credencial) {
            if (u.credencial.grupos) {
              var permissoes = {};
              for (var gK in u.credencial.grupos) {
                var g = u.credencial.grupos[gK];
                if (!g.permissoes) continue;
                for (var pK in g.permissoes) {
                  var p = g.permissoes[pK];
                  var pNome = p.nome.trim();
                  if (!permissoes[pNome]) permissoes[pNome] = p;
                }
              }
              for (var pK in u.credencial.permissoes) {
                var p = u.credencial.permissoes[pK];
                var pNome = p.nome.trim();
                if (!permissoes[pNome])
                  permissoes[pNome] = p;
              }

              //------------------------------------------------------
              var permNome = attr.segPermissao.trim();
              if (permNome.indexOf('!') === 0) {
                if (permissoes[permNome.substring(1, permNome.length)]) el.remove();
              } else {
                if (!permissoes[permNome]) el.remove();
              }
            }
          }
        });
      }
    };

  }]);
(function(){
  var $http = null, $q = null, Entidades = null;
  
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
        dataPath: 'data.0',
        size: 20,
        page: 0,
        url: '/persistencia',
          // o cache usa no indece o número da AÇÃO:
        cacheTimeout: {
          "5": -1 , // -1 para nunca expirar
          "1": 180000 , // 3min timeout
          "2": 180000 ,
          "3": 180000 ,
          "4": 180000 
        } // ms
      },
      headers = {
        'Content-Type': defaults.contentType,
        'X-Requested-With':"XMLHttpRequest"
      },
      entidadesCache = {};

  //===========================================================================
  
  function Entidade( nome, config ){
    if( !config ) config = {};
    this.nome = nome;
    this.dataPath = config.dataPath || defaults.dataPath;
    this.size = config.size || defaults.size;
    this.page = config.page || defaults.page;
    this.url = config.url || defaults.url ;
    this.cache = {};
    this.ultimoCache = {};
    this.cacheTimeout = angular.extend({}, 
        defaults.cacheTimeout , config.cacheTimeout ); 
  }
  
  function Query( ent ){
    this.entidade = ent || {};
    this._size = this.entidade.size;
    this._page = this.entidade.page;
    this._where = []; // query
    this._join = [];
    this._order = [];
    this._data = null;
    this._id = false;
    this._acao = 1;
    
    this._url = this.entidade.url;
    this.$scope = null;
    this._build = null;
    this._cache = false;
    this._clearCache = false;
    this._inObj = null;
    this._inPath = this.entidade.nome;
  }
  
  var proto = Query.prototype;
    
  __construirSetter( proto, 'page', '_page' );
  __construirSetter( proto, 'size', '_size' );
  __construirSetter( proto, 'id', '_id', true );
  __construirSetter( proto, 'data', '_data' );
  __construirSetter( proto, 'acao', '_acao' );
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
  proto.where = function (nome, comp, val, logicOp, quoteVal) {
    if (!nome || !comp)
      return this;
    comp = comp.toLowerCase();
    if( comp === 'isnull' || comp === 'isnotnull' ){
      val = '-';
      quoteVal = true;
    }
    if (!logicOp)
      logicOp = '&'; // Padão para E se for falso
    else if (logicOp !== '&' && logicOp !== '|')
      logicOp = '|'; // Padão para OU se for verdadeiro
    if (typeof quoteVal === 'undefined') { // se não for definido verificaremos o valor para tentar sempre usar as aspas
      val = '' + val;
      if (val.indexOf('"') !== 0 && val.indexOf("'") !== 0)
        val = '"' + val;
      if (val.lastIndexOf('"') !== val.length - 1 && val.lastIndexOf("'") !== val.length - 1)
        val = val + '"';
    } else if (quoteVal) {
      val = '"' + val + '"';
    }
    this._where.push(nome + ' ' + comp + ' ' + val + logicOp);
    return this;
  };
  
  proto.in = function( obj, path ){
    this._inObj = obj;
    this._inPath = path;
    return this;
  };
  proto.build = function( force ){
    if( this._build && !force ) return this;
    // Montar Query String:
    var queryStr = '';
    for (var g in this._where) {
      queryStr += this._where[g];
    }
    queryStr = queryStr.replace(/[&\|\s]+$/, '');
    //if (queryStr) queryStr = '?' + queryStr;
    
    this._build = {
      entidade: this.entidade.nome,
      page: this._page,
      size: this._size,
      where: queryStr,
      join: this._join,
      order: this._order,
      data: this._data,
      id: this._id,
      acao: this._acao
    };
    return this;
  };
  proto.send = function(){
    var cached = this.getCache();
    if( cached ) return $q.resolve( _send.call(that, cached) );
    
    var that = this; 
    return $http({
      method: 'POST',
      url: this._url,
      headers: headers,
      data: this._build
    }).then(function(data){ return _send.call(that, data.data); });
  };
  function _send(data){
    if( this._cache ){
      this.entidade.ultimoCache[ this._acao ] = Date.now();
      this.entidade.cache[ this._acao ] = data;
    }
    if( this._inObj ){
      var key = this._inPath.split('.');
      var objToBind = _getObjByPath( this._inObj, key.slice(0,key.length-1) );
      key = key[key.length-1];
      objToBind[key] = _getObjByPath( data, this.entidade.dataPath );
      _apply( this, this._inObj );
    }
    return data; 
  }
  
  proto.getCache = function(){
    var entidade = this.entidade;
    var cache = entidade.cache;
    var cacheKey = this._acao;
    if( this._clearCache 
      || (entidade.ultimoCache[cacheKey] 
        && entidade.cacheTimeout[cacheKey] > 0
        && (Date.now() - entidade.ultimoCache[cacheKey]) > entidade.cacheTimeout[cacheKey]
      ) ) return cache[cacheKey] = null;
    else if( cache[cacheKey] ) return cache[cacheKey];
  };
  proto.clearBuild = function(){
    this._build = false;
    return this;
  };
  __construirSetter( proto, 'cache', '_cache', true );
  __construirSetter( proto, 'clearCache', '_clearCache', true );
  __construirSetter( proto, 'apply', '_apply' );
  
  
  __construirRequisicao( proto, 'tipo',5 );
  __construirRequisicao( proto, 'get',1 );
  __construirRequisicao( proto, 'post',2 );
  __construirRequisicao( proto, 'put',3 );
  __construirRequisicao( proto, 'delete',4 );
  __construirRequisicao( proto, 'paginacao',10 );
  
  __construirRequisicaoIn( proto, 'tipoIn','tipo' );
  __construirRequisicaoIn( proto, 'getIn','get' );
  __construirRequisicaoIn( proto, 'postIn','post' );
  __construirRequisicaoIn( proto, 'putIn','put' );
  __construirRequisicaoIn( proto, 'deleteIn','delete' );
  __construirRequisicaoIn( proto, 'paginacaoIn','paginacao' );
  
  function __construirRequisicao( pro, nomeFunc, acao ){
    pro[nomeFunc] = function( _data ){
      return this.data( _data || this._data ).acao( acao ).build().send();
    };
  }
  function __construirRequisicaoIn( pro, nomeFunc, methodFunc ){
    pro[nomeFunc] = function( obj, key, _data ){
      return this.in( obj, key )[methodFunc]( _data );
    };
  }
  
  
  
//============================================================================
  
  function MuitosQuery( listaQuerys ){
    var arr = listaQuerys || [];
    if( !(arr instanceof Array) ) arr = [ arr ];
    this._querys = arr;
  }
  
  var muitosProto = MuitosQuery.prototype;
  
  muitosProto.add = function( query ){
    this._querys.push( query );
    return this;
  };
  muitosProto.remove = function( query ){
    for( var i = 0; i < this._querys.length; i++ ){
      if( this._querys[i] === query ){
        this._querys.splice( i, 1 );
        break;
      }
    }
    return this;
  };
  
  muitosProto.send = function(){
    if( !this._querys.length ) return $q(function(res,rej){});
    var _build = [], _cacheds = [];
    for( var i = 0; i < this._querys.length; i++ ){
      var q = this._querys[i];
      q.build();
      var cached = q.getCache();
      if( cached ) _cacheds[i] = cached ;
      else _build.push( q._build );
    }
    if( !_build.length ) return;
    
    var that = this;
    return $http({
      method: 'POST',
      url: defaults.url,
      headers: headers,
      data: _build
    }).then(function(data){ 
      data = data.data ;
      var arr = data.data ;
      // restaurar o vetor completo da requisição JS:
      for(var g in _cacheds){
        arr.splice( g, 0, _cacheds[g].data );
      }
      // copia da resposta JsonResponse:
      for(var g in arr){
        var copia = _cacheds[g];
        if( !copia ){
          var copia = {};
          for(var k in data){
            copia[k] = data[k];
          }
          copia.data = [ arr[g] ]; // novo array, para garantir index 0
          copia.size = that._querys[g]._size;
          copia.page = that._querys[g]._page;
        }
        _send.call( that._querys[g], copia );
      }
      
      return data;
    });
  };
  
  
//============================================================================
    // Auxiliares:
  
  function _copiarCom( objArr, arrParam ){
    var retornarVetor = true;
    if( !(objArr instanceof Array) ){
      objArr = [ objArr ];
      retornarVetor = false;
    }
      // montar estrutura de busca
    var arr = [];
    for( var g = 0; g < arrParam.length; g++ ){
      var val = arrParam[g] ;
      if( typeof val === 'number' ) arr.push( [val] );
      else arr.push( val.split('.') );
    }
      // iniciar busca e copiar
    var arrCopia = [],copia, obj, tempObj, tempCopia, vet, k;
    for( var ok = 0; ok < objArr.length; ok++ ){
      obj = objArr[ok];
      copia = {};
      arrCopia.push( copia );
      for(var g = 0; g < arr.length; g++ ){
        vet = arr[g];
        tempObj = obj;
        tempCopia = copia;
        for( k = 0; k < vet.length -1; k++ ){
          tempObj = tempObj[ vet[k] ];
          if( !tempCopia[ vet[k] ] ) tempCopia[ vet[k] ] = {};
          tempCopia = tempCopia[ vet[k] ];
        }
        tempCopia[ vet[k] ] = tempObj[ vet[k] ];
      }
    }
    
    if( retornarVetor ) return arrCopia;
    return copia;
  }
  
  function _copiarSem( objArr, arrParam ){
    var retornarVetor = true;
    if( !(objArr instanceof Array) ){
      objArr = [ objArr ];
      retornarVetor = false;
    }
      // montar estrutura de busca
    var arr = [];
    for( var g = 0; g < arrParam.length; g++ ){
      var val = arrParam[g] ;
      if( typeof val === 'number' ) arr.push( [val] );
      else arr.push( val.split('.') );
    }
    var excluir = {}, tempExc;
    for( var g = 0; g < arr.length; g++ ){
      var vet = arr[g];
      tempExc = excluir;
      for( k = 0; k < vet.length -1; k++ ){
        tempExc = tempExc[ vet[k] ];
      }
      tempExc[ vet[k] ] = true; // para ser verdadeiro na comparação
    }
      // iniciar busca e copiar
    var resp = [];
    for( var ok = 0; ok < objArr.length; ok++ ){
      resp.push( _copiaSem_Deep( {}, objArr[ok], excluir ) );
    }
    
    if( retornarVetor ) return resp;
    return resp[0];
  }
  function _copiaSem_Deep( para, de, comp ){
    for( var g in de ){
      if( !comp[g] ){
        para[g] = de[g];
        continue;
      }
      // CONTINUAR AQUI
    }
  }
  
//============================================================================
Module.provider('Entidades',[function(){
  
  this.defaults = defaults;
  var that = this;
  
  this.$get = ['context','$http','$q',function(context,inj$http, inj$q){
    
    $http = inj$http;
    $q = inj$q;

    that.defaults.url = context.root+ that.defaults.url;
    var ref = {
      query: function( ent ){
        if( typeof ent === 'string' ) ent = ref.entidade(ent);
        return new Query( ent );
      },
      queryMuitos: function( arrQ ){
        return new MuitosQuery( arrQ );
      },
      entidade: function( nome, config, override ){
        var ent = entidadesCache[nome];
        if( ent && !override ) return ent;
        if( !config ) config = {};
        if( !config.url ) config.url = defaults.url ;
        ent = new Entidade( nome, config );
        that[nome] = entidadesCache[nome] = ent;
        return ent;
      },
      get: function( nome ){
        return entidadesCache[nome] ;
      },
      
        // Shallow copy
      copiarCom: _copiarCom,
        // Shallow copy
      copiarSem: _copiarSem
    };
    
    ref.eq = ref.equal = '=';
    ref.ne = ref.notEqual = '!=';
    ref.le = ref.lowerThanOrEqualTo = '<=';
    ref.ge = ref.greaterThanOrEqualTo = '>=';
    ref.lt = ref.lowerThan = '<';
    ref.gt = ref.greaterThan = '>';
    ref.nl = ref.notLike = 'notlike';
    ref.lk = ref.like = 'like';
    ref.nl = ref.isNull = 'isnull';
    ref.nnl = ref.isNotNull = 'isnotnull';
    
    ref.BUSCAR =      1;
    ref.CRIAR =       2;
    ref.EDITAR =      3;
    ref.DELETAR =     4;
    ref.TIPO =        5;
    ref.ADICIONAR =   6;
    ref.REMOVER =     7;
    ref.PAGINACAO =   10;
    
    return Entidades = ref;
  }];

}]);

})();

(function(){
  
  var $http = null, $q = null, path = null, $rootElement = null ;
  
  
  function Exportar(  ){
    this._data = null; // deve ser no formato de "Busca";
    this._tipo = 'xlsx';
    this._nome = '';
    this._titulos = [];
    this._atributos = [];
    this._entidade = ''; // nome da entidade
    
    this._target = '_blank';
    this._build = false;
  }

  var proto = Exportar.prototype;
  
  __construirSetter( proto, 'nome', '_nome' );
  __construirSetter( proto, 'tipo', '_tipo' );
  __construirSetter( proto, 'entidade', '_entidade' );
  __construirSetter( proto, 'target', '_target' );
  proto.titulos = function( vals ){
    if(!(vals instanceof Array)) vals = [ vals ];
    this._titulos = vals;
    return this;
  };
  proto.dados = function( vals ){
    if(!(vals instanceof Array)) vals = [ vals ];
    this._atributos = vals;
    return this;
  };
  
  
  proto.clearBuild = function(){
    this._build = false;
    return this;
  };
  proto.build = function(){
    if( this._build ) return this;
    
    this._data = {
      entidade: this._entidade,
      data: {
        nome: this._nome,
        titulos: this._titulos,
        atributos: this._atributos
      }
    };
    
    return this;
  };
  proto.send = function(){
    this.build();
    
      // fazer a requisição:
    var form = document.createElement("form");
    form.action = 'exportar';
    form.method = 'post';
    form.target = this._target;
    var input = document.createElement("textarea");
    input.name = 'json';
    input.value = JSON.stringify( this._data );
    form.appendChild(input);
    form.style.display = 'none';
    $rootElement[0].appendChild(form);
    form.submit();
    $rootElement[0].removeChild(form);
  };
  
  
//============================================================================
  Module.provider('Exportar',[function(){
    
    var that = this;
    
    this.$get = ['$http','$q','path','$rootElement',
        function(inj$http,inj$q, injpath, inj$rootElement){
      
      $http = inj$http;
      $q = inj$q;
      path = injpath;
      $rootElement = inj$rootElement;
      
      return {
        query: function( entidadeNome ){
          return new Exportar().entidade( entidadeNome );
        }
      };
    }];
    
  }]);
  
})();
Module.filter('',['Usuario',function(Usuario){
  
  var grupos = {};
  Usuario.then(function( u ){
    if (!u) return;

    if (u.credencial) {
      if (u.credencial.grupos) {
        for (var gK in u.credencial.grupos) {
          var g = u.credencial.grupos[gK];
          var gNome = g.chave.trim();
          if (!grupos[gNome]) grupos[gNome] = g;
        }
      }
    }
  });
  
  return function( str ){
    return !!grupos[ str ];
  };
}]);
Module.filter('hasPermissao',['Usuario',function(Usuario){
  
  var permissoes = {};
  Usuario.then(function(u){
    if (!u) return;

    if (u.credencial) {
      if (u.credencial.grupos) {
        for (var gK in u.credencial.grupos) {
          var g = u.credencial.grupos[gK];
          if (!g.permissoes) continue;
          for (var pK in g.permissoes) {
            var p = g.permissoes[pK];
            var pNome = p.nome.trim();
            if (!permissoes[pNome]) permissoes[pNome] = p;
          }
        }
        for (var pK in u.credencial.permissoes) {
          var p = u.credencial.permissoes[pK];
          var pNome = p.nome.trim();
          if (!permissoes[pNome])
            permissoes[pNome] = p;
        }
      }
    }
  });
  
  return function( str ){
    return !!permissoes[ str ];
  };
}]);
Module.provider('HostInter',[function(){
      /**
       * Interceptador para resirecionar as requisições para outro
       * endereço.
       * (ferramentas de desenvolvimento do Chrome)
       * @type NetbeansChromeInter
       */

  var provider = this;

  provider.ativo = false;
  provider.url = '';

  provider.$get = ['context',function(context){

    var ref = {
      request:function( request ){
        if( provider.ativo && !request.noHostInter ){
          request.url = (provider.url + request.url).replace(/\/+/g,'/')
            .replace(/(\w):\/+/g,'$1://') ;
        }
        return request;
      }
    };
    return ref;

  }];

}]);
Module.provider('LoginInter',[function(state){ // '$state'

  var provider = this;
  
  provider.handler = null;
  provider.state = ''; // login
  provider.url = '/login.html';
  provider.ativo = true;
  provider.ERRORCODE_LOGIN = 401 ;

  provider.$get = ['context','$window',function(context,$window){
    /**
     * Esse interceptador redireciona o usuário para a página de login
     * caso o servidor informe o código de erro de usuário não logado.
     * @type LoginInter
     */

    var ref = {
      response:function( response ){
        var data = response.data || {};
        if( provider.ativo && data.codigo === provider.ERRORCODE_LOGIN ){
          if( provider.handler ){
            provider.handler( response );
            //-----------------------------------------------------------------
          }else{
            if( provider.state && false ){
              //state.go( provider.state );
            }else{
              var origin = $window.location.origin ;
              $window.location = origin + context.root + provider.url ;
            }
          }
        }
        return response ;
      }
    };
    return ref;

  }];

}]);
(function(){
  
  var injector = angular.injector(['ng'],true),
      $q = injector.get('$q'),
      provider = null;
  
  var resolve, reject;
  var promise = $q(function(resolveX, rejectX){
    resolve = resolveX;
    reject = rejectX;
  });
  promise._recarregar = false;
  promise.recarregar = function(){ return promise; };
  
  var oldThen = promise.then ;
  promise.then = function(){
    if( !promise.$$state.status && !promise._recarregar ) promise.recarregar();
    oldThen.apply( promise, arguments );
  };
  
  //=========================================================================
  Module.provider('Usuario', function(){
    provider = this;
    this.carregarAoIniciar = false;
    this.$get = function(){ return promise; };
  });
  
  //=========================================================================
  Module.run(['context','$http',function(context,$http){
    promise.recarregar = function(){
      if( promise._recarregar ) return promise;
      promise.$$state.status = 0;
      promise._recarregar = true;
      $http.get(context.services +'/seguranca/usuario').then(function(data){
        if( data.data.codigo === 200 && data.data.data ) resolve( data.data.data );
        else reject( {} );
      },function(err){
        reject( err );
      }).finally(function(){
        promise._recarregar = false;
      });
      return promise;
    };
    if( provider.carregarAoIniciar ) promise.recarregar();
  }]);
  
})();



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
        provider.context.root = 'x'; // necessário!
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
      str = str.trim().replace(/\/\/+/g,'/');
      if( !provider.context.root ){
        str = str.replace(/^\s*\//,'');
      }
      return str;
    }
    
    this.$get = [function(){
      
      return provider.context;
    }];
    
}]);

(function(){
  
  Module.provider('path',[function(){
    var that = this;
    
    that.protocol = '';
    that.host = '';
    that.port = '';
    that.context = '';
    
    that.hasHost = false;
    
    that.root = '';
    that.servico = '';
    that.websocket = '';
    
    
    this.$get = ['$window',function($window){
      // tratamentos do HOST:
      var wl = $window.location;
      var host = that.host;
      var hostParsed = null;
      if( that.host ){
        hostParsed = /(\w+:\/+)?([^\/:]+)(:\d+)?(\/.*)?/i.exec( host );
        that.hasHost = true;
      }else{
        hostParsed = [
          wl+'',
          wl.protocol+'//',
          wl.host.replace(/:.*/,''),
          wl.port,
          wl.pathname
        ];
      }
      
      if(!that.protocol) that.protocol = hostParsed[1] || 'http://';
      if(that.hasHost || !that.host) that.host = hostParsed[2];
      
      if(!that.port) that.port = parseInt(hostParsed[3].replace(':','')) || 80;
      else that.port = parseInt(that.port);
      
      if(!that.context) that.context = hostParsed[4];
      
      // tratamentos dos nomes padrão:
      that.r = that.root;
      that.s = that.servico;
      that.ws = that.websocket;
      
      console.log( 'path:this,pathProvider:this', this, that );
      
      
      var pathFunc = function( pathName, path ){
        if( !pathName ) pathName = 'r';
        var str = that.context+that[pathName] + path ;
        if( that.hasHost ){
          str = that.protocol + (that.host+':'+that.port+str).replace(/\/+/g,'/');
        }
        return str;
      };
      
      pathFunc.get = function( pathName ){
        return that[pathName];
      };
      pathFunc.hasHost = function(  ){
        return that.hasHost;
      };
      
      return pathFunc;
    }];
    
  }]);
  
})();

})(window);
