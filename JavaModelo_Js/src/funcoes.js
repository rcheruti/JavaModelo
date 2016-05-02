
/*
 * Funções usadas por todo o código
 */

function __construirSetter( pro, nomeFunc, nomeAttr, defaultVal ){
  if( typeof defaultVal !== 'undefined' ) 
    pro[nomeFunc] = function( val ){
      if( typeof val === 'undefined' ) val = defaultVal;
      this[nomeAttr] = val;
      return this;
    };
  else pro[nomeFunc] = function( val ){
    this[nomeAttr] = val;
    return this;
  };
}
