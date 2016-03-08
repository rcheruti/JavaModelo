Module.directive('segGrupo', ['Usuario',function(Usuario){

    return {
      link: function (scope, el, attr) {
        Usuario.then(function( u ){
          if (!u) return;

          if (u.credencial) {
            if (u.credencial.grupos) {
              var grupos = {};
              for (var gK in u.credencial.grupos) {
                var g = u.credencial.grupos[gK];
                var gNome = g.chave.trim();
                if (!grupos[gNome]) grupos[gNome] = g;
              }

              var permNome = attr.segGrupo.trim();
              if (permNome.indexOf('!') === 0) {
                if (grupos[permNome.substring(1, permNome.length)]) el.remove();
              } else {
                if (!grupos[permNome]) el.remove();
              }
            }
          }
        });
      }
    };

  }]);