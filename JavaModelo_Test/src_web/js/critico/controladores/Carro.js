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