import { MyMomentsItem } from './moments';

export interface MomentWithNotifications extends MyMomentsItem {
  notificationId: number | null;
  read: boolean;
}
