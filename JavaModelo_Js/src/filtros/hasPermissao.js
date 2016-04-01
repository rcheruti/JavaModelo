Module.filter('hasPermissao',['Usuario',function(Usuario){
  
  var permissoes = {};
  Usuario.then(function(u){
    if (!u) return;

    if (u.credencial) {
      if (u.credencial.grupos) {
        for (var gK in u.credencial.grupos) {
          var g = u.credencial.grupos[gK];
          if (!g.permissoes) continue;
          for (var pK in g.permissoes) {
            var p = g.permissoes[pK];
            var pNome = p.nome.trim();
            if (!permissoes[pNome]) permissoes[pNome] = p;
          }
        }
        for (var pK in u.credencial.permissoes) {
          var p = u.credencial.permissoes[pK];
          var pNome = p.nome.trim();
          if (!permissoes[pNome])
            permissoes[pNome] = p;
        }
      }
    }
  });
  
  return function( str ){
    return !!permissoes[ str ];
  };
}]);