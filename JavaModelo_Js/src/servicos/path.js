(function(){
  
  Module.provider('path',[function(){
    var that = this;
    
    that.protocol = '';
    that.host = '';
    that.port = '';
    that.context = '';
    
    that.hasHost = false;
    
    that.root = '';
    that.servico = '';
    that.websocket = '';
    
    
    this.$get = ['$window',function($window){
      // tratamentos do HOST:
      var wl = $window.location;
      var host = that.host;
      var hostParsed = null;
      if( that.host ){
        hostParsed = /(\w+:\/+)?([^\/:]+)(:\d+)?(\/.*)?/i.exec( host );
        that.hasHost = true;
      }else{
        hostParsed = [
          wl+'',
          wl.protocol+'//',
          wl.host.replace(/:.*/,''),
          wl.port,
          wl.pathname
        ];
      }
      
      if(!that.protocol) that.protocol = hostParsed[1] || 'http://';
      if(that.hasHost || !that.host) that.host = hostParsed[2];
      
      if(!that.port) that.port = parseInt(hostParsed[3].replace(':','')) || 80;
      else that.port = parseInt(that.port);
      
      if(!that.context) that.context = hostParsed[4];
      
      console.log( 'path:this,pathProvider:this', this, that );
      
      
      var pathFunc = function( pathName, path ){
        var str = that.context+that[pathName] + path ;
        if( that.hasHost ){
          str = that.protocol + (that.host+':'+that.port+str).replace(/\/+/g,'/');
        }
        return str;
      };
      
      pathFunc.host = function(){};
      pathFunc.root = function(){};
      pathFunc.servico = function(){};
      pathFunc.websocket = function(){};
      
      return pathFunc;
    }];
    
  }]);
  
})();