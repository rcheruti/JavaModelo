Module.controller('Carro',['$scope','entidades',
    function($scope,entidades){
  
  //$scope.carros = [];
  $scope.cores = [];
  
  $scope.carro = {};
  
  entidades.Cor.query().order('nome').get().then(function(data){
    $scope.cores = data.data.data;
  }) ; 
  entidades.Carro.query().join('cores').order('nome').getIn( $scope, 'coisas.carros' );
  
  $scope.postCarro = function(){
    entidades.Carro.post( $scope.carro ).then(function(data){
      $scope.carro = {};
    });
  };
  
}]);