Module.provider('LoginInter',[function(state){ // '$state'

  var provider = this;
  
  provider.handler = null;
  provider.state = ''; // login
  provider.url = '/login.jsp';
  provider.ativo = true;
  provider.ERRORCODE_LOGIN = 401 ;

  provider.$get = ['context','$window',function(context,$window){
    /**
     * Esse interceptador redireciona o usuário para a página de login
     * caso o servidor informe o código de erro de usuário não logado.
     * @type LoginInter
     */

    var ref = {
      response:function( response ){
        var data = response.data || {};
        console.log( 'LoginInter',data );
        if( provider.ativo && data.codigo === provider.ERRORCODE_LOGIN ){
          if( provider.handler ){
            console.log( 'LoginInter handler',provider.handler );
            provider.handler( response );
            //-----------------------------------------------------------------
          }else{
            if( provider.state && false ){
            console.log( 'LoginInter state',provider.state );
              //state.go( provider.state );
            }else{
              var origin = $window.location.origin ;
            console.log( 'LoginInter location', origin + context.services + provider.url );
              $window.location = origin + context.root + provider.url ;
            }
          }
        }
        return response ;
      }
    };
    return ref;

  }];

}]);