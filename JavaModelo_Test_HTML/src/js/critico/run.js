
Module.run(['Entidades','$window','Usuario','$q',
    function(Entidades,$window,Usuario, $q){
  
  $window.Carro = Entidades.entidade('Carro');
  $window.Cor = Entidades.entidade('Cor');
  
  Entidades.entidade('Porta');
  Entidades.entidade('Usuario'); 
  Entidades.entidade('Valor');
  Entidades.entidade('RegistroUsuario');
  
  Usuario.then(function(u){
    console.log('Usuario::: ', u);
  });
  
  
}]);
