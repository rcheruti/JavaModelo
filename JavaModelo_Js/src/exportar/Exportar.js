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