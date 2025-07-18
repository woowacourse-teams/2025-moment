export default {
  presets: [
    ['@babel/preset-env', { targets: { node: 'current' } }],
    ['@babel/preset-react', { runtime: 'automatic', importSource: '@emotion/react' }],
    '@babel/preset-typescript',
  ],
  plugins: ['@emotion/babel-plugin'],
};
