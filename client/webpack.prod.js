import { merge } from 'webpack-merge';
import common from './webpack.common.js';
import path from 'path';

export default merge(common, {
  mode: 'production',
  output: {
    filename: 'bundle.[contenthash].js',
    path: path.resolve(__dirname, '../dist'),
    clean: true,
  },
  devtool: false,
});
