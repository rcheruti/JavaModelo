Module.controller('Janela',['$scope','Entidades',
    function($scope,Entidades){
    
  function recarregar(){
    Entidades.query( 'Janela' ).where('porta',Entidades.nnl).getIn($scope,'janelasPortas'); 
    Entidades.query( 'Janela' ).where('porta',Entidades.nl).getIn($scope,'janelasSemPortas'); 
  }
  recarregar();
  
  
  
}]);