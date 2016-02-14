Module.controller('Tipo',['$scope','entidades',
    function($scope,entidades){
  
  $scope.tipo = null;
  $scope.entidade = null;
  $scope.override = false;
  $scope.entidadeLista = [
    'Carro','Porta','Usuario','Valor','Cor'
  ];
  
  $scope.mostrarTipo = function(x){
    //$scope.entidade = x;
    var ent = entidades[ $scope.entidade ];
    if(ent) ent.tipoIn( $scope, 'tipo', $scope.override );
  };
  
}]);