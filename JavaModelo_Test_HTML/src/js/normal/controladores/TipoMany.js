Module.controller('TipoMany',['$scope','Entidades',
    function($scope,Entidades){
  
  $scope.tipo = null;
  $scope.entidade = null;
  $scope.override = false;
  $scope.entidadeLista = [
    'Carro','Porta','Usuario','Valor','Cor'
  ];
  
  var q = Entidades.queryMuitos();
  for(var g in $scope.entidadeLista){
    q.add( Entidades.query($scope.entidadeLista[g])
      .in( $scope, 'tipo.'+g ).acao( Entidades.TIPO ) );
  }
  q.send();
  console.log( 'Entidades::queryMuitos', q );
  console.log( '$scope', $scope );
  
  $scope.mostrarTipo = function(x){
    //Entidades.query( $scope.entidade ).tipoIn( $scope, 'tipo', $scope.override );
  };
  
}]);