import storybook from 'eslint-plugin-storybook';

import reactPlugin from '@eslint-react/eslint-plugin';
import prettierConfig from 'eslint-config-prettier';
import typescriptEslint from 'typescript-eslint';

export default [
  {
    ignores: [
      'node_modules/',
      'dist/',
      'build/',
      '*.bundle.js',
      '*.chunk.js',
      '*.log',
      '.env*',
      '.vscode/',
      '.idea/',
      '.DS_Store',
      'Thumbs.db',
      'pnpm-lock.yaml',
    ],
  },
  ...typescriptEslint.configs.recommended,
  {
    files: ['**/*.{js,jsx,ts,tsx}'],
    plugins: {
      '@eslint-react': reactPlugin,
    },
    languageOptions: {
      parserOptions: {
        ecmaVersion: 2022,
        sourceType: 'module',
        ecmaFeatures: {
          jsx: true,
        },
      },
      globals: {
        // Jest globals
        describe: 'readonly',
        it: 'readonly',
        test: 'readonly',
        expect: 'readonly',
        beforeEach: 'readonly',
        afterEach: 'readonly',
        beforeAll: 'readonly',
        afterAll: 'readonly',
        jest: 'readonly',
        // Browser globals
        window: 'readonly',
        document: 'readonly',
        console: 'readonly',
        EventSource: 'readonly',
        MessageEvent: 'readonly',
        alert: 'readonly',
        setTimeout: 'readonly',
        clearTimeout: 'readonly',
        setInterval: 'readonly',
        clearInterval: 'readonly',
        URLSearchParams: 'readonly',
        // DOM types
        Node: 'readonly',
        Element: 'readonly',
        EventTarget: 'readonly',
        // Node.js globals
        process: 'readonly',
        Buffer: 'readonly',
        global: 'readonly',
        // React/TypeScript globals
        React: 'readonly',
        JSX: 'readonly',
        HTMLElement: 'readonly',
        HTMLButtonElement: 'readonly',
        HTMLInputElement: 'readonly',
        HTMLTextAreaElement: 'readonly',
        HTMLDivElement: 'readonly',
        HTMLFormElement: 'readonly',
        Event: 'readonly',
        MouseEvent: 'readonly',
        KeyboardEvent: 'readonly',
        alert: 'readonly',
      },
    },
    rules: {
      '@typescript-eslint/no-unused-vars': 'warn',
      '@typescript-eslint/no-explicit-any': 'warn',
      '@typescript-eslint/no-empty-object-type': 'off',
      'react/react-in-jsx-scope': 'off',
      'no-unused-vars': 'off', // TypeScript handles this better
      'no-undef': 'warn',
    },
  },
  prettierConfig,
  ...storybook.configs['flat/recommended'],
];
