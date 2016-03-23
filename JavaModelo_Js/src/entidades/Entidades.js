(function(){
  
  var injector = angular.injector(['ng'],true),
      $http = injector.get('$http'),
      $q = injector.get('$q')
      ;
  
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
        url: '/persistencia',
        cacheTimeout: {
          "POST/tipo": -1 , // -1 para nunca expirar
          "POST/buscar": 180000 , // 3min timeout
          "POST": 180000 ,
          "PUT": 180000 ,
          "DELETE": 180000 
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
    this._param = [];
    this._join = [];
    this._order = [];
    this._data = null;
    this._url = this.entidade.url;
    this._method = '';
    this.$scope = null;
    this._build = false;
    this._path = '';
    this._cache = false;
    this._clearCache = false;
    
    this._buscarUm = true;
    this._buscarMuitos = false;
    this._buscarId = false;
  }
  
  var proto = Query.prototype;
    
  __construirSetter( proto, 'page', '_page' );
  __construirSetter( proto, 'size', '_size' );
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
    if( !this._path ) this._path = '';
    var url = '' +(this._buscarUm?'/um':this._buscarMuitos?'/muitos':'') + 
            (this._buscarId?'/id':'') ;
    url = this._url +url + (this._buscarMuitos?'':'/'+this.entidade.nome) 
            +this._path ;
    this._url = url + matrix + queryStr ;
    this._build = true;
    return this;
  };
  proto.send = function(){
    var entidade = this.entidade;
    var cache = entidade.cache;
    var cacheKey = this._method + this._path;
    if( this._clearCache 
      || (entidade.ultimoCache[cacheKey] 
        && entidade.cacheTimeout[cacheKey] > 0
        && (Date.now() - entidade.ultimoCache[cacheKey]) > entidade.cacheTimeout[cacheKey]
      ) ) cache[cacheKey] = null;
    else if( cache[cacheKey] ) return $q.resolve( cache[cacheKey] );
    
    var that = this; 
    return $http({
      method: this._method,
      url: this._url,
      headers: headers,
      data: this._data || {}
    }).then(function(data){
      data = _getObjByPath( data, entidade.dataPath );
      if( that._cache ){
        entidade.ultimoCache[cacheKey] = Date.now();
        cache[cacheKey] = data;
      }
      return data;
    });
  };
  
  proto.clearBuild = function(){
    this._build = false;
    return this;
  };
  __construirSetter( proto, 'cache', '_cache', true );
  __construirSetter( proto, 'clearCache', '_clearCache', true );
  __construirSetter( proto, 'path', '_path' );
  __construirSetter( proto, 'method', '_method' );
  __construirSetter( proto, 'data', '_data' );
  __construirSetter( proto, 'apply', '_apply' );
  __construirSetter( proto, 'id', '_id', true );
  
  
  __construirRequisicao( proto, 'tipo','POST', '/tipo' );
  __construirRequisicao( proto, 'get','POST', '/buscar' );
  __construirRequisicao( proto, 'post','POST' );
  __construirRequisicao( proto, 'put','PUT' );
  __construirRequisicao( proto, 'delete','DELETE' );
  
  __construirRequisicaoIn( proto, 'tipoIn','tipo' );
  __construirRequisicaoIn( proto, 'getIn','get' );
  __construirRequisicaoIn( proto, 'postIn','post' );
  __construirRequisicaoIn( proto, 'putIn','put' );
  __construirRequisicaoIn( proto, 'deleteIn','delete' );
  
  function __construirRequisicao( pro, nomeFunc, method, path ){
    pro[nomeFunc] = function( _data ){
      return this.path( path ).data( _data ).method( method ).build().send();
    };
  }
  function __construirRequisicaoIn( pro, nomeFunc, methodFunc ){
    pro[nomeFunc] = function( obj, key, _data ){
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
  
  
//============================================================================
  
  function MuitosQuery(){
    this._querys = [];
    this._method = '';
    this.$scope = null;
    this._build = false;
    this._path = '';
    this._buscarId = false;
  }
  
  var muitosProto = MuitosQuery.prototype;
  muitosProto.clearBuild = proto.clearBuild;
  muitosProto.apply = proto.apply ;
  muitosProto.id = proto.id ;
  muitosProto.path = proto.path ;
  muitosProto.method = proto.method ;
  
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
  
  muitosProto.build = function(){
    for( var i = 0; i < this._querys.length; i++ ){
      this._querys[i].build();
    }
    this._build = true;
    return this;
  } ;
  muitosProto.send = function(){} ;
  
  
  __construirRequisicao( muitosProto, 'get','POST', '/buscar' );
  __construirRequisicao( muitosProto, 'put','PUT' );
  __construirRequisicao( muitosProto, 'post','POST' );
  __construirRequisicao( muitosProto, 'delete','DELETE' );
  
  __construirRequisicaoIn( muitosProto, 'tipoIn','tipo' );
  __construirRequisicaoIn( muitosProto, 'getIn','get' );
  __construirRequisicaoIn( muitosProto, 'putIn','put' );
  __construirRequisicaoIn( muitosProto, 'postIn','post' );
  __construirRequisicaoIn( muitosProto, 'deleteIn','delete' );
  
  
//============================================================================
Module.provider('Entidades',[function(){
    
  this.defaults = defaults;
  this.$get = ['context',function(context){
    var that = this;
    var ref = {
      query: function( ent ){
        if( typeof ent === 'string' ) ent = ref.entidade(ent);
        return new Query( ent );
      },
      muitosQuery: function( arrQ ){
        if( !arrQ ) return new MuitosQuery(  );
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
