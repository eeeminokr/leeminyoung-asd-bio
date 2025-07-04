const path = require('path')
const outputdir = process.env.OUTPUTDIR || '../target/dist';

module.exports = {
  publicPath: process.env.NODE_ENV === 'production' ? '/' : '/',
  outputDir: outputdir,
  assetsDir: '',
  configureWebpack: {
    resolve: {
      alias: {
        vue$: 'vue/dist/vue.esm-bundler.js'
      }
    }
  },
  chainWebpack: config => {
    config.resolve.alias.set(
      'vue$',
      // If using the runtime only build
      //path.resolve(__dirname, 'node_modules/vue/dist/vue.runtime.esm.js')
      //path.resolve(__dirname, 'node_modules/vue/dist/vue.runtime.esm.js')
      // Or if using full build of Vue (runtime + compiler)
      path.resolve(__dirname, 'node_modules/vue/dist/vue.esm.js')
    )
  },
  devServer: {
    port: 8081,
    proxy: { 
      '/bdsp/api': {
        target: 'http://localhost:9090',
        changeOrigin: true,
        pathRewrite: {
          '/bdsp/api': '/api',

        }
      }
    },
  }
	// css: {
	// 	loaderOptions: {
	// 		sass: {
	// 			additionalData: bootstrapSassAbstractsImports.join('\n')
	// 		},
	// 		scss: {
	// 			additionalData: [...bootstrapSassAbstractsImports, ''].join(';\n')
	// 		}
	// 	}
	// }
}
