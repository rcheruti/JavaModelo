Module.controller('Carro',['$scope','entidades',
    function($scope,entidades){
  
  $scope.carros = [];
  $scope.cores = [];
  
  
  entidades.Cor.query().order('nome').get().then(function(data){
    $scope.cores = data.data.data;
  }) ; 
  entidades.Carro.query().join('cores').order('nome').get().then(function(data){
    $scope.carros = data.data.data ;
  });
  
}]);