import { Global, css } from '@emotion/react';

const globalStyles = css`
  * {
    margin: 0;
    padding: 0;
    box-sizing: border-box;
  }

  html,
  body,
  #root {
    height: 100%;
  }

  *,
  *::before,
  *::after {
    box-sizing: border-box;
  }

  @font-face {
    font-family: 'IanSui';
    src:
      url('/fonts/ongeulip.woff2') format('woff2'),
      url('/fonts/ongeulip.ttf') format('truetype');
    font-weight: 100 900;
    font-style: normal;
    font-display: swap;
  }

  html {
    font-family:
      'IanSui',
      -apple-system,
      'Segoe UI',
      system-ui,
      sans-serif;
    line-height: 1.6;
    -webkit-text-size-adjust: 100%;
    -webkit-tap-highlight-color: transparent;
    font-feature-settings: 'liga', 'kern';
    -webkit-font-smoothing: antialiased;
    -moz-osx-font-smoothing: grayscale;
  }

  body {
    margin: 0;
    font-family: inherit;
    font-size: 1rem;
    font-weight: 400;
    line-height: 1.5;
    color: #212529;
    background-color: #fff;
    -webkit-font-smoothing: antialiased;
    -moz-osx-font-smoothing: grayscale;
  }

  /* HTML5 display definitions */
  article,
  aside,
  details,
  figcaption,
  figure,
  footer,
  header,
  hgroup,
  main,
  menu,
  nav,
  section,
  summary {
    display: block;
  }

  audio,
  canvas,
  progress,
  video {
    display: inline-block;
    vertical-align: baseline;
  }

  audio:not([controls]) {
    display: none;
    height: 0;
  }

  [hidden],
  template {
    display: none;
  }

  /* Links */
  a {
    background-color: transparent;
    text-decoration: none;
    color: inherit;
  }

  a:active,
  a:hover {
    outline: 0;
  }

  /* Text-level semantics */
  abbr[title] {
    border-bottom: 1px dotted;
  }

  b,
  strong {
    font-weight: bold;
  }

  dfn {
    font-style: italic;
  }

  h1 {
    font-size: 2em;
    margin: 0;
  }

  h2,
  h3,
  h4,
  h5,
  h6 {
    margin: 0;
  }

  mark {
    background: #ff0;
    color: #000;
  }

  small {
    font-size: 80%;
  }

  sub,
  sup {
    font-size: 75%;
    line-height: 0;
    position: relative;
    vertical-align: baseline;
  }

  sup {
    top: -0.5em;
  }

  sub {
    bottom: -0.25em;
  }

  /* Embedded content */
  img {
    border: 0;
    max-width: 100%;
    height: auto;
  }

  svg:not(:root) {
    overflow: hidden;
  }

  /* Grouping content */
  figure {
    margin: 0;
  }

  hr {
    box-sizing: content-box;
    height: 0;
  }

  pre {
    overflow: auto;
  }

  code,
  kbd,
  pre,
  samp {
    font-family: monospace, monospace;
    font-size: 1em;
  }

  /* Forms */
  button,
  input,
  optgroup,
  select,
  textarea {
    color: inherit;
    font: inherit;
    margin: 0;
  }

  button {
    overflow: visible;
    background: none;
    border: none;
    cursor: pointer;
  }

  button,
  select {
    text-transform: none;
  }

  button,
  html input[type='button'],
  input[type='reset'],
  input[type='submit'] {
    -webkit-appearance: button;
    cursor: pointer;
  }

  button[disabled],
  html input[disabled] {
    cursor: default;
  }

  button::-moz-focus-inner,
  input::-moz-focus-inner {
    border: 0;
    padding: 0;
  }

  input {
    line-height: normal;
  }

  input[type='checkbox'],
  input[type='radio'] {
    box-sizing: border-box;
    padding: 0;
  }

  input[type='number']::-webkit-outer-spin-button,
  input[type='number']::-webkit-inner-spin-button {
    height: auto;
  }

  input[type='search'] {
    -webkit-appearance: textfield;
    box-sizing: content-box;
  }

  input[type='search']::-webkit-search-cancel-button,
  input[type='search']::-webkit-search-decoration {
    -webkit-appearance: none;
  }

  fieldset {
    border: 1px solid #c0c0c0;
    margin: 0 2px;
    padding: 0.35em 0.625em 0.75em;
  }

  legend {
    border: 0;
    padding: 0;
  }

  textarea {
    overflow: auto;
    resize: vertical;
  }

  optgroup {
    font-weight: bold;
  }

  /* Tables */
  table {
    border-collapse: collapse;
    border-spacing: 0;
  }

  td,
  th {
    padding: 0;
  }

  /* Lists */
  ul,
  ol {
    list-style: none;
  }

  /* fieldset */
  fieldset {
    border: none;
    padding: 0;
    margin: 0;
  }

  /* Additional utility styles */
  .sr-only {
    position: absolute;
    width: 1px;
    height: 1px;
    padding: 0;
    margin: -1px;
    overflow: hidden;
    clip: rect(0, 0, 0, 0);
    white-space: nowrap;
    border: 0;
  }
`;

const GlobalStyles = () => <Global styles={globalStyles} />;

export default GlobalStyles;
