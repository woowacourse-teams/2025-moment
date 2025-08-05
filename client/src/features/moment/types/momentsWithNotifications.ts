import { MyMoments } from './moments';

export interface MomentWithNotifications extends MyMoments {
  read: boolean;
}
