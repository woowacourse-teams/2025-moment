import { ReactNode } from 'react';

export type ToastVariant = 'success' | 'error' | 'warning' | 'message';
export type ToastRouteType = 'moment' | 'comment';

export interface ToastData {
  id: string;
  message: ReactNode;
  variant: ToastVariant;
  duration?: number;
  routeType?: ToastRouteType;
  link?: string;
}

export interface ToastsState {
  toasts: ToastData[];
}
