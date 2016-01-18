
module.exports = function (grunt) {
  
  grunt.loadNpmTasks('grunt-contrib-clean');
  grunt.loadNpmTasks('grunt-contrib-concat');
  grunt.loadNpmTasks('grunt-contrib-uglify');
  //grunt.loadNpmTasks('grunt-contrib-cssmin');
  //grunt.loadNpmTasks('grunt-contrib-htmlmin');
  grunt.loadNpmTasks('grunt-contrib-copy');
  
  // caminhos do processo de criação:
  var p = {
    www:'www/',
    www_build:'www/build/',
    src:'src/',
    temp:'www_t/',
    dist:'dist/',
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
    clean:{
      all:{
        src: [ p.www_build, p.dist ]
      }
    },
    concat:{
      build:{
        options:{
          banner: lf+'(function(window){'+lf+lf,
          footer: lf+lf+'})(window);'+lf
        },
        src:[
          p.src+ 'config.js',
          //p.src+ '**',
          p.src+ '**/*.js'
        ],
        dest: c.jsBuild
      }
    },
    uglify:{
      build:{
        files:[{
          src: c.jsBuild ,
          dest: c.jsMin
        }]
      }
    },
    copy:{
      test:{
        files:[{
          expand: true,
          cwd: p.dist ,
          src: jsName ,
          dest: p.www_build
        }]
      }
    }
  });
  
  
  grunt.registerTask('default',['clean','concat','uglify','copy']);
  
};
