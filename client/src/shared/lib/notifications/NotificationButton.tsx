import { Button } from '@/shared/design-system/button/Button';
import { useNotification } from './useNotification';

type NotificationButtonProps = {
  onClose: VoidFunction;
};
export const NotificationButton = ({ onClose }: NotificationButtonProps) => {
  const { permission, isLoading, handleNotificationClick } = useNotification();

  const handleNotifyClick = () => {
    handleNotificationClick().then(success => {
      if (success) {
        onClose();
      }
    });
  };

  return (
    <Button
      title={isLoading ? '설정 중...' : permission === 'granted' ? '알림 허용됨' : '알림 받기'}
      onClick={handleNotifyClick}
      disabled={isLoading}
      variant={permission === 'granted' ? 'primary' : 'secondary'}
    />
  );
};
