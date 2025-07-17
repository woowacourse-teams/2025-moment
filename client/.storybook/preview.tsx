import React from 'react';
import { ThemeProvider } from '@emotion/react';
import type { Preview } from '@storybook/react-webpack5';
import { theme } from '../src/app/styles/theme';

const preview: Preview = {
  parameters: {
    actions: { argTypesRegex: '^on[A-Z].*' },
    controls: {
      matchers: {
        color: /(background|color)$/i,
        date: /Date$/,
      },
    },
    backgrounds: {
      default: 'primary',
      values: [{ name: 'primary', value: '#0F172A' }],
    },
  },
  decorators: [
    Story => (
      <ThemeProvider theme={theme}>
        <div style={{ backgroundColor: '#0F172A', minHeight: '100vh', padding: '20px' }}>
          <Story />
        </div>
      </ThemeProvider>
    ),
  ],
};

export default preview;
