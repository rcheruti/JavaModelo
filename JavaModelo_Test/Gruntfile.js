
module.exports = function (grunt) {
  
  grunt.loadNpmTasks('grunt-contrib-clean');
  grunt.loadNpmTasks('grunt-contrib-concat');
  grunt.loadNpmTasks('grunt-contrib-uglify');
  grunt.loadNpmTasks('grunt-contrib-cssmin');
  grunt.loadNpmTasks('grunt-contrib-htmlmin');
  grunt.loadNpmTasks('grunt-contrib-copy');
  grunt.loadNpmTasks('grunt-contrib-less');
  grunt.loadNpmTasks('grunt-replace');
  
  // caminhos do processo de criação:
  var p = {
    dist:'web/',
    src:'src_web/',
    srcCssC:'src_web/css/critico/',
    srcCssN:'src_web/css/normal/',
    srcJsC:'src_web/js/critico/',
    srcJsN:'src_web/js/normal/',
    htmlPaginas:'src_web/paginas/',
    htmlIndex:'src_web/index.html',
    temp:'web_temp/',
    test:'test/'
  };
  
  var jsName = 'JavaModelo.js';
  // nomes de arquivos do processo:
  var c = {
    jsBuild: p.dist+jsName,
    jsMin: p.dist+'JavaModelo.min.js'
  };
  
  var lf = grunt.util.linefeed;
  
  // Project configuration.
  grunt.initConfig({
    // ---------------  limpeza
    clean:{
      temp:{
        src: [ p.temp ]
      },
      dist:{
        src: [ p.dist ]
      }
    },
    // ----------------  juntando os arquivos
    concat:{
      cssCritico:{
        src:[ p.srcCssC+ 'index.less', p.srcCssC+ '**/*.less' ],
        dest: p.temp+'cssCritico.less'
      },
      cssNormal:{
        src:[ p.srcCssN+ '**/*.less' ],
        dest: p.temp+'cssNormal.less'
      },
      jsCritico:{
        src:[ 
          p.srcJsC+ 'libs/angular/angular.min.js',
          p.srcJsC+ 'libs/angular/*.js',
          p.srcJsC+ 'libs/*.js',
          p.srcJsC+ 'config.js',
          p.srcJsC+ '**/*.js'
        ],
        dest: p.temp+'jsCritico.js'
      },
      jsCriticoLogin:{
        src:[ 
          p.srcJsC+ 'libs/angular/angular.min.js',
          p.srcJsC+ 'libs/angular/*.js',
          p.srcJsC+ 'libs/*.js',
          //p.srcJsC+ '**/*.js'
        ],
        dest: p.temp+'jsCriticoLogin.js'
      },
      jsNormal:{
        src:[ p.srcJsN+ '**/*.js' ],
        dest: p.temp+'jsNormal.js'
      },
      html:{
        options:{
          process: function(src, filepath){
            var id = filepath.replace(/.*\/paginas\//i,'');
            return '<script type="text/ng-template" id="'+ id +'">'+ src +'</script>'; 
          }
        },
        files:[{
          src:[ p.htmlPaginas+ '**/*.html' ],
          dest: p.temp+'htmlPaginas.html'
        }]
      }
    },
    
    //-------------  interpretando LESS
    less:{
      cssCritico:{
        files:[{
          src:[ p.temp+'cssCritico.less' ],
          dest: p.temp+'cssCritico.css'  
        }]
      },
      cssNormal:{
        files:[{
          src:[ p.temp+'cssNormal.less' ],
          dest: p.temp+'cssNormal.css'
        }]
      }
    },
    
    //-------------  minimizando os arquivos
    cssmin:{
      cssCritico:{
        files:[{
          src: p.temp+'cssCritico.css' ,
          dest: p.temp+'cssCritico.min.css'
        }]
      },
      cssNormal:{
        files:[{
          src: p.temp+'cssNormal.css' ,
          dest: p.temp+'css.min.css'
        }]
      }
    },
    uglify:{
      jsCritico:{
        files:[{
          src: p.temp+'jsCritico.js' ,
          dest: p.temp+'jsCritico.min.js'
        }]
      },
      jsCriticoLogin:{
        files:[{
          src: p.temp+'jsCriticoLogin.js' ,
          dest: p.temp+'jsCriticoLogin.min.js'
        }]
      },
      jsNormal:{
        files:[{
          src: p.temp+'jsNormal.js' ,
          dest: p.temp+'js.min.js'
        }]
      }
    },
    htmlmin:{
      html:{
        options:{
          removeComments: true,
          collapseWhitespace: true
        },
        files:[{
          src: p.temp+'htmlPaginas.html' ,
          dest: p.temp+'htmlPaginas.min.html'
        }]
      }
    },
    
    //-------------  copiando os arquivos finais
    copy:{
      test:{
        files:[{
          expand: true,
          cwd: p.temp ,
          src:[
            'css.min.css',
            'js.min.js'
          ],
          dest: p.dist
        }]
      }
    },
    
    //-------------  replace 
    replace:{
      dist:{
        options:{
          detail: false,
          patterns:[{
            match: 'htmlPaginas',
            replacement: function(){
              return grunt.file.read(p.temp+"/htmlPaginas.html");
            }
          },{
            match: 'cssCritico',
            replacement: function(){
              return grunt.file.read(p.temp+"/cssCritico.css");
            }
          },{
            match: 'jsCritico',
            replacement: function(){
              return grunt.file.read(p.temp+"/jsCritico.js");
            }
          },{
            match: 'jsCriticoLogin',
            replacement: function(){
              return grunt.file.read(p.temp+"/jsCriticoLogin.js");
            }
          }]
        },
        files:[{
          expand:true,
          flatten:true,
          src: [p.src+ 'index.html', p.src+ 'login.html'],
          dest: p.dist 
        }]
      }
    }
    
    
  });
  
  
  grunt.registerTask('default',['clean','concat','less',
                            //'cssmin',
                            //'uglify',
                            //'htmlmin',
                            'copy','replace'
                            //,'clean:temp'
                          ]);
  
};
