Module.controller('Carro',['$scope','Entidades',
    function($scope,Entidades){
  
  $scope.cores = [];
  $scope.carro = {};
  
  function recarregar(){
    Entidades.query( 'Cor' ).order(['nome']).get().then(function(data){
      $scope.cores = data;
    }) ; 
    Entidades.query('Carro').join(['cores','portas','valor','portas.janelas','registroUsuario'])
      .order(['nome']).getIn( $scope, 'coisas.carros' );
  }
  recarregar();
  
  
  $scope.postCarro = function(){
    Entidades.query('Carro').post( $scope.carro ).then( recarregar );
  };
  
  console.log( Entidades );
  $scope.deleteCarro = function( carro ){
    var sim = confirm('Deletar?');
    sim && Entidades.query('Carro').param('id',Entidades.eq, carro.id ).delete().then( recarregar );
  };
  
}]);
Module.controller('Menu',['$scope','$http','$window','context',
    function($scope,$http,$window,context){
  
  $scope.logoutMsg = '';
  
  $scope.logout = function(){
    $http.post( context.services+ '/seguranca/logout').then(function(data){
      if( data.data.status ){
        var url = $window.location.origin + context.root ;
        $window.location = url;
      }else{
        $scope.logoutMsg = 'Falha no logout.';
      }
    });
  };
  
}]);
Module.controller('Tipo',['$scope','Entidades',
    function($scope,Entidades){
  
  $scope.tipo = null;
  $scope.entidade = null;
  $scope.override = false;
  $scope.entidadeLista = [
    'Carro','Porta','Usuario','Valor','Cor','RegistroUsuario'
  ];
  
  $scope.mostrarTipo = function(x){
    Entidades.query( $scope.entidade ).cache(true).clearCache( $scope.override )
      .tipoIn( $scope, 'tipo' );
  };
  
}]);
Module.controller('TipoMany',['$scope','Entidades',
    function($scope,Entidades){
  
  $scope.tipo = null;
  $scope.entidade = null;
  $scope.override = false;
  $scope.entidadeLista = [
    'Carro','Porta','Usuario','Valor','Cor'
  ];
  
  $scope.mostrarTipo = function(x){
    Entidades.query( $scope.entidade ).tipoIn( $scope, 'tipo', $scope.override );
  };
  
}]);