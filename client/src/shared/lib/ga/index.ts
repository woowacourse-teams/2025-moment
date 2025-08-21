import ReactGA from 'react-ga4';

const GA_MEASUREMENT_ID = process.env.REACT_APP_GA_ID || '';

export const initGA = () => {
  if (process.env.NODE_ENV === 'development') {
    return;
  }

  const hostname = window.location.hostname;
  if (hostname !== 'connectingmoment.com') {
    return;
  }

  if (!GA_MEASUREMENT_ID) {
    console.warn('GA_MEASUREMENT_ID is not set');
    return;
  }

  ReactGA.initialize(GA_MEASUREMENT_ID);
};

export const sendPageview = (path: string) => {
  if (
    process.env.NODE_ENV === 'development' ||
    window.location.hostname !== 'connectingmoment.com'
  ) {
    return;
  }

  ReactGA.send({ hitType: 'pageview', page: path });
};

export const sendEvent = (event: {
  category: string;
  action: string;
  label?: string;
  value?: number;
}) => {
  if (
    process.env.NODE_ENV === 'development' ||
    window.location.hostname !== 'connectingmoment.com'
  ) {
    return;
  }

  ReactGA.event(event);
};
