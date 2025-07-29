import ReactGA from 'react-ga4';

const GA_MEASUREMENT_ID = process.env.REACT_APP_GA_ID || '';

export const initGA = () => {
  ReactGA.initialize(GA_MEASUREMENT_ID);
};

export const sendPageview = (path: string) => {
  ReactGA.send({ hitType: 'pageview', page: path });
};

export const sendEvent = (event: {
  category: string;
  action: string;
  label?: string;
  value?: number;
}) => {
  ReactGA.event(event);
};
