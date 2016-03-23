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