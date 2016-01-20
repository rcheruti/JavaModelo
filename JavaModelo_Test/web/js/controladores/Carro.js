Module.controller('Carro',['$scope','entidades',
    function($scope,entidades){
  
  $scope.carros = [];
  $scope.cores = [];
  
  console.log('entidades', entidades);
  
  entidades.Cor.query().order('nome').get().then(function(data){
    console.log( 'cores', data );
    $scope.cores = data.data.data;
  }) ; 
  
}]);