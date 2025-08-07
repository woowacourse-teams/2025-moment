import { MyMoments } from './moments';

export interface MomentWithNotifications extends MyMoments {
  notificationId: number | null;
  read: boolean;
}
