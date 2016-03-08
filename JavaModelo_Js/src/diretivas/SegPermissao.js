Module.directive('segPermissao', [function () {

    return {
      link: function (scope, el, attr) {
        var u = window.Usuario;
        if (!u) return;

        if (u.credencial) {
          if (u.credencial.grupos) {
            var permissoes = {};
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

            //------------------------------------------------------
            var permNome = attr.segPermissao.trim();
            if (permNome.indexOf('!') === 0) {
              if (permissoes[permNome.substring(1, permNome.length)]) el.remove();
            } else {
              if (!permissoes[permNome]) el.remove();
            }
          }
        }
      }
    };

  }]);