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
