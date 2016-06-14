Module.controller('Janela',['$scope','Entidades',
    function($scope,Entidades){
    
  function recarregar(){
    Entidades.from( 'Janela' ).where('porta',Entidades.nnl).getIn($scope,'janelasPortas'); 
    Entidades.from( 'Janela' ).where('porta',Entidades.nl).getIn($scope,'janelasSemPortas'); 
  }
  recarregar();
  
  
  
}]);