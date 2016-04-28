Module.provider('HostInter',[function(){
      /**
       * Interceptador para resirecionar as requisições para outro
       * endereço.
       * (ferramentas de desenvolvimento do Chrome)
       * @type NetbeansChromeInter
       */

  var provider = this;

  provider.ativo = false;
  provider.url = '';

  provider.$get = ['context',function(context){

    var ref = {
      request:function( request ){
        console.log( request );
        if( provider.ativo && !request.noHostInter ){
          request.url = (provider.url + request.url).replace(/\/+/g,'/')
            .replace(/(\w):\/+/g,'$1://') ;
        }
        return request;
      }
    };
    return ref;

  }];

}]);