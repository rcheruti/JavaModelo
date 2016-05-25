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
  Module.run(['path','$http',function(path,$http){
    promise.recarregar = function(){
      if( promise._recarregar ) return promise;
      promise.$$state.status = 0;
      promise._recarregar = true;
      $http.get( path('r','/seguranca/usuario') ).then(function(data){
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


