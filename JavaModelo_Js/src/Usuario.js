(function(){
  
  var injector = angular.injector(['ng'],true),
      $q = injector.get('$q');
  
  var resolve, reject;
  var promise = $q(function(resolveX, rejectX){
    resolve = resolveX;
    reject = rejectX;
  });
  promise.reload = function(){ return promise; };
  
  Module.value('Usuario', promise );
  
  Module.run(['context','$http',function(context,$http){
    promise.reload = function(){
      promise.$$state.status = 0;
      $http.get(context.services +'/seguranca/usuario').then(function(data){
        if( data.data.codigo === 200 && data.data.data ) resolve( data.data.data );
        else reject( {} );
      },function(err){
        reject( err );
      });
      return promise;
    };
    promise.reload();
  }]);
  
})();


