Module.filter('',['Usuario',function(Usuario){
  
  var grupos = {};
  Usuario.then(function( u ){
    if (!u) return;

    if (u.credencial) {
      if (u.credencial.grupos) {
        for (var gK in u.credencial.grupos) {
          var g = u.credencial.grupos[gK];
          var gNome = g.chave.trim();
          if (!grupos[gNome]) grupos[gNome] = g;
        }
      }
    }
  });
  
  return function( str ){
    return !!grupos[ str ];
  };
}]);