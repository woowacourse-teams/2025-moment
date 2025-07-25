import { ErrorBoundaryProps, ErrorBoundaryState } from '@/shared/types/errorBoundary';
import React, { Component, ReactNode } from 'react';
import { ErrorFallback } from './ErrorFallback';

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
