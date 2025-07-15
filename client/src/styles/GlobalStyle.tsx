import { Global } from '@emotion/react';
import { reset } from './reset';

export const GlobalStyle = () => {
  return <Global styles={reset} />;
};
