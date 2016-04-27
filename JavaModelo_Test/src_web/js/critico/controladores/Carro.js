Module.controller('Carro',['$scope','Entidades','$http',
    function($scope,Entidades, $http){
  
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
  
  $scope.exportarCarros = function(){
    var data = { 
      entidade:'Carro', 
      data: { nome:'Carros - Export', 
        titulos:['ID','Nome'] ,
        atributos:['id','nome'] 
      }
    };
    
    var form = document.createElement("form");
    form.action = 'exportar';
    form.method = 'post';
    form.target = "_blank";
    var input = document.createElement("textarea");
    input.name = 'json';
    input.value = JSON.stringify( data );
    form.appendChild(input);
    form.style.display = 'none';
    document.body.appendChild(form);
    form.submit();
    document.body.removeChild(form);
    
  };
  
}]);