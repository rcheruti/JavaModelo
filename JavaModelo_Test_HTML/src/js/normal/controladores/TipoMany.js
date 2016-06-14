Module.controller('TipoMany',['$scope','Entidades',
    function($scope,Entidades){
  
  $scope.tipo = null;
  $scope.entidade = null;
  $scope.override = false;
  $scope.entidadeLista = [
    'Carro','Porta','Usuario','Valor','Cor'
  ];
  
  var q = Entidades.from([]); // para pegar uma busca de muitos
  for(var g in $scope.entidadeLista){
    q.add( Entidades.from($scope.entidadeLista[g])
      .in( $scope, 'tipo.'+g ).acao( Entidades.TIPO ) );
  }
  q.send();
  console.log( 'Entidades::from([...])', q );
  console.log( '$scope', $scope );
  
  $scope.mostrarTipo = function(x){
    //Entidades.query( $scope.entidade ).tipoIn( $scope, 'tipo', $scope.override );
  };
  
}]);