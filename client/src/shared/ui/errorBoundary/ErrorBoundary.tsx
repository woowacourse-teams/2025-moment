import { ErrorBoundaryProps, ErrorBoundaryState } from '@/shared/types/errorBoundary';
import React, { Component, ReactNode } from 'react';
import { ErrorFallback } from './ErrorFallback';
import * as Sentry from '@sentry/react';

export class ErrorBoundary extends Component<ErrorBoundaryProps, ErrorBoundaryState> {
  constructor(props: ErrorBoundaryProps) {
    super(props);
    this.state = { hasError: false };
  }

  static getDerivedStateFromError(error: Error): Partial<ErrorBoundaryState> {
    return { hasError: true, error };
  }

  componentDidCatch(error: Error, errorInfo: React.ErrorInfo) {
    console.error('ErrorBoundary caught an error:', error, errorInfo);

    Sentry.captureException(error, {
      level: 'error',
      tags: {
        errorBoundary: true,
        error_type: error.constructor.name,
        priority: 'high',
      },
      contexts: {
        react: {
          componentStack: errorInfo.componentStack,
        },
        error_details: {
          message: error.message,
          stack: error.stack?.split('\n').slice(0, 5).join('\n'),
          component: this.constructor.name,
        },
      },
    });
  }

  private resetErrorBoundary = () => {
    this.setState({ hasError: false, error: undefined });
  };

  render(): ReactNode {
    const { hasError, error } = this.state;
    const { children, fallback: FallbackComponent } = this.props;

    if (hasError && error) {
      if (FallbackComponent) {
        return <FallbackComponent error={error} resetError={this.resetErrorBoundary} />;
      }

      return <ErrorFallback error={error} resetError={this.resetErrorBoundary} />;
    }

    return children;
  }
}
