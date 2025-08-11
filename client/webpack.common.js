import CopyWebpackPlugin from 'copy-webpack-plugin';
import dotenv from 'dotenv';
import ForkTsCheckerWebpackPlugin from 'fork-ts-checker-webpack-plugin';
import HtmlWebpackPlugin from 'html-webpack-plugin';
import path from 'path';
import { fileURLToPath } from 'url';
import webpack from 'webpack';

dotenv.config();

const __filename = fileURLToPath(import.meta.url);
const __dirname = path.dirname(__filename);

/** @type {import('webpack').Configuration} */
const config = {
  entry: './src/index.tsx',
  resolve: {
    extensions: ['.ts', '.tsx', '.js'],
    alias: {
      '@': path.resolve(__dirname, 'src'),
    },
    fallback: {
      path: false,
      fs: false,
    },
  },
  module: {
    rules: [
      {
        test: /\.(ts|tsx)$/,
        exclude: /node_modules/,
        use: 'babel-loader',
      },
      {
        test: /\.css$/,
        use: ['style-loader', 'css-loader'],
      },
    ],
  },
  plugins: [
    new HtmlWebpackPlugin({ template: './public/index.html' }),
    new ForkTsCheckerWebpackPlugin(),
    new webpack.DefinePlugin({
      'process.env.REACT_APP_BASE_URL': JSON.stringify(
        process.env.REACT_APP_BASE_URL || 'http://localhost:8080/api/v1',
      ),
      'process.env.REACT_APP_GOOGLE_LOGIN_REDIRECTION_URL': JSON.stringify(
        process.env.REACT_APP_GOOGLE_LOGIN_REDIRECTION_URL ||
          'http://localhost:8080/api/v1/auth/login/google',
      ),
      'process.env.REACT_APP_GA_ID': JSON.stringify(process.env.REACT_APP_GA_ID || ''),
      'process.env.REACT_APP_SENTRY_DSN': JSON.stringify(process.env.REACT_APP_SENTRY_DSN || ''),
    }),

    new CopyWebpackPlugin({
      patterns: [
        {
          from: 'public',
          to: '',
          globOptions: {
            ignore: ['**/index.html'],
          },
        },
      ],
    }),
  ],
};

export default config;
