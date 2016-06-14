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
        url: '',
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
    this._chaves = false;
    this._acao = 1;
    this._id = null;
    
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
  __construirSetter( proto, 'id', '_id' );
  __construirSetter( proto, 'chaves', '_chaves', true );
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
      id: this._id,
      from: this.entidade.nome,
      page: this._page,
      size: this._size,
      where: queryStr,
      join: this._join,
      order: this._order,
      data: this._data,
      chaves: this._chaves,
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
  
  this.$get = ['path','$http','$q',function(path,inj$http, inj$q){
    
    $http = inj$http;
    $q = inj$q;

    that.defaults.url = path('p', that.defaults.url) ;
    var ref = {
      from: function( ent ){
        if( ent instanceof Array ) new MuitosQuery( ent );
        if( typeof ent === 'string' ) ent = ref.entidade.apply(ref, ent, arguments);
        return new Query( ent );
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
    ref.REFS =        15;
    
    return Entidades = ref;
  }];

}]);

})();
