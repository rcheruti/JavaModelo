Module.controller('Carro',['$scope','entidades',
    function($scope,entidades){
  
  //$scope.carros = [];
  $scope.cores = [];
  
  $scope.carro = {};
  
  function recarregar(){
    entidades.Cor.query().order('nome').get().then(function(data){
      $scope.cores = data.data.data;
    }) ; 
    entidades.Carro.query().join('cores','portas').order('nome').getIn( $scope, 'coisas.carros' );
  }
  recarregar();
  
  
  $scope.postCarro = function(){
    entidades.Carro.post( $scope.carro ).then(function(data){
      //$scope.carro = {};
      recarregar();
    });
  };
  
}]);