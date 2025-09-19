import path from 'path';
import { fileURLToPath } from 'url';
import { merge } from 'webpack-merge';
import common from './webpack.common.js';
import { BundleAnalyzerPlugin } from 'webpack-bundle-analyzer';

const __filename = fileURLToPath(import.meta.url);
const __dirname = path.dirname(__filename);

export default merge(common, {
  mode: 'development',
  devtool: 'eval-source-map',
  output: {
    filename: 'bundle.js',
    path: path.resolve(__dirname, 'dist'),
    publicPath: '/',
    clean: true,
  },
  devServer: {
    static: {
      directory: path.join(__dirname, 'public'),
    },
    port: 3000,
    open: true,
    hot: true,
    historyApiFallback: {
      rewrites: [
        {
          from: /^\/moment(-dev)?\/images\//,
          to: function () {
            return false;
          },
        },
        {
          from: /^\/api\//,
          to: function () {
            return false;
          },
        },
        { from: /./, to: '/index.html' },
      ],
    },
    client: {
      overlay: true,
    },
  },
  plugins: [...(process.env.ANALYZE_BUNDLE === 'true' ? [new BundleAnalyzerPlugin()] : [])],
});
