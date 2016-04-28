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
        if( provider.ativo ){
          request.url = (provider.url + request.url).replace(/\/+/g,'/') ;
        }
        return request;
      }
    };
    return ref;

  }];

}]);