Module.controller('Carro',['$scope','Entidades',
    function($scope,Entidades){
  
  $scope.cores = [];
  $scope.carro = {};
  
  function recarregar(){
    Entidades.query( 'Cor' ).order(['nome']).get().then(function(data){
      console.log( 'data', data );
      $scope.cores = data.data[0];
    }) ; 
    Entidades.query('Carro').join(['cores','portas','valor','portas.janelas',
        'registroUsuario'])
      .order(['nome']).getIn( $scope, 'coisas.carros' );
  }
  recarregar();
  
  
  $scope.postCarro = function(){
    Entidades.query('Carro').post( $scope.carro ).then( recarregar );
  };
  
  $scope.deleteCarro = function( carro ){
    var sim = confirm('Deletar?');
    sim && Entidades.query('Carro').id().delete( carro ).then( recarregar );
  };
  
}]);