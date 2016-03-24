Module.controller('Janela',['$scope','Entidades',
    function($scope,Entidades){
    
  function recarregar(){
    Entidades.query( 'Janela' ).param('porta',Entidades.nnl).getIn($scope,'janelasPortas'); 
    Entidades.query( 'Janela' ).param('porta',Entidades.nl).getIn($scope,'janelasSemPortas'); 
  }
  recarregar();
  
  
  
}]);