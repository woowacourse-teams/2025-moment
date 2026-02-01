import { createStore, useStore } from './core';
import { ToastData, ToastRouteType, ToastsState } from '@/shared/types/toast';

const toastStore = createStore<ToastsState>({ toasts: [] });
const timers = new Map<string, ReturnType<typeof setTimeout>>();
const generateId = () => `toast-${Date.now()}-${Math.random().toString(36).slice(2, 11)}`;

export const useToasts = () => useStore(toastStore);

function addToast(toast: Omit<ToastData, 'id'>) {
  const currentToasts = toastStore.getState().toasts;
  const duplicate = currentToasts.find(
    t => t.message === toast.message && t.variant === toast.variant,
  );
  if (duplicate) {
    return duplicate.id;
  }

  const id = generateId();
  const newToast = { ...toast, id };

  toastStore.setState(state => ({ toasts: [...state.toasts, newToast] }));

  const duration = toast.duration ?? 3000;
  if (duration > 0) {
    const prev = timers.get(id);
    if (prev) clearTimeout(prev);

    const handle = setTimeout(() => {
      timers.delete(id);
      removeToast(id);
    }, duration);

    timers.set(id, handle);
  }

  return id;
}

function removeToast(id: string) {
  const t = timers.get(id);
  if (t) {
    clearTimeout(t);
    timers.delete(id);
  }
  toastStore.setState(state => ({ toasts: state.toasts.filter(t => t.id !== id) }));
}

export const toasts = {
  success: (message: string, duration?: number) =>
    addToast({ message, variant: 'success', duration }),

  error: (message: string, duration?: number) => addToast({ message, variant: 'error', duration }),

  warning: (message: string, duration?: number) =>
    addToast({ message, variant: 'warning', duration }),

  message: (message: string, routeType?: ToastRouteType, duration?: number) =>
    addToast({ message, variant: 'message', routeType, duration }),

  hide: (id: string) => removeToast(id),
  clear: () => {
    timers.forEach(t => clearTimeout(t));
    timers.clear();
    toastStore.setState({ toasts: [] });
  },
} as const;
