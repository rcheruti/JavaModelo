Module.provider('context',[function(){
    
    var provider = this;
    
    /**
     * 
     * @param {string} url The context url string, defaults to '' (empty string)
     * @param {boolean} prefixItRoot If that url needs to start with '/' char, defaults to TRUE
     * @param {boolean} sufixIt If that url needs to end with '/' char, defaults to FALSE
     * @returns {string} The actual context url string
     */
    function _context(url, prefixItRoot, sufixIt){
        if( !url ) return '';
        if( !(typeof url === 'string') ) throw 'To configure the context url string you must pass a string as the first parameter value.';
        if( typeof prefixItRoot === 'undefined' ) prefixItRoot = true; // Defaults to true
        url = url.trim();
        
        if( prefixItRoot ){ if( url.indexOf('/') !== 0 ) url = '/'+url; }
        else{ if( url.indexOf('/') === 0 ) url = url.substring(1,url.length); }
        
        if( sufixIt ){ if( url.lastIndexOf('/') !== url.length-1 ) url = url+'/'; }
        else{ if( url.lastIndexOf('/') === url.length-1 ) url = url.substring(0,url.length-1); }
        
        return corrigirUrl(url);
    };
    var _funcs = {
      root: function(/*, params _context */){
        provider.context.root = _context.apply(this, arguments);
        provider.context.services = corrigirUrl( provider.context.root + provider.context.path.services );
        provider.context.websocket = corrigirUrl( provider.context.root + provider.context.path.websocket );
        return provider ;
      } ,
      services: function(/*, params _context */){
        provider.context.services = corrigirUrl( 
          provider.context.root + 
          (provider.context.path.services = _context.apply(this, arguments) )
          );
        return provider ;
      } ,
      websocket: function(/*, params _context */){
        provider.context.websocket = corrigirUrl( 
          provider.context.root + 
          (provider.context.path.websocket = _context.apply(this, arguments) )
          );
        return provider ;
      } ,
      put: function(nome /*, params _context */){
        provider.context[nome] = _context.call(this, arguments[1], arguments[2], arguments[3]);
        return provider ;
      },
      context: { root: '', path: {} }
    };
    angular.extend( this, _funcs );
    
    function corrigirUrl(str){
      return str.replace(/\/\/+/g,'/');
    }
    
    this.$get = [function(){
      
      return provider.context;
    }];
    
}]);

/*


      var atual, anterior, itensAtuais, 
          continuosRef = {
        put: function(){
          
          return continuosRef;
        },
        in: function( url ){
          if( !url ){
            return continuosRef;
          }
          url = corrigirUrl(url);
          if( url[0] === '/' ) url = url.substr(1);
          url = url.split('/');
          anterior.push( atual );
          for(var i=0; i<url.length; i++){
            if( !atual[url[i]] ) atual[url[i]] = {};
            atual = atual[url];
          }
          return continuosRef;
        },
        clearIn: function(){
          ref.clearIn();
          return continuosRef;
        },
        end: function(){
          atual = anterior.pop();
          return continuosRef;
        },
        done: function(){
          //////////
          ref.clearIn();
          return continuosRef;
        }
      };
      


        // Ex.: put( 'logout', '/sistema/logout', context.services );
        // irá criar no Attr "context.logout" a URL "/s/sistema/logout"
        put: function( nomeAttr, url , urlPrefix ){
          if( !urlPrefix ) urlPrefix = ref.services ;
          ref[ nomeAttr ] = corrigirUrl(urlPrefix + url);
        },
        in: function( url ){
          continuosRef.in( url );
          return continuosRef ;
        },
        clearIn: function(){
          anterior = [];
          atual = itensAtuais = {};
        }

*/
