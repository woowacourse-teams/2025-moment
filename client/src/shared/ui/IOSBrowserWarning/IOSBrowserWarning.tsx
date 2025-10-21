import React, { useState, useEffect } from 'react';
import { Share, Smartphone } from 'lucide-react';
import * as S from './IOSBrowserWarning.styles';
import { isIOS, isPWA } from '@/shared/utils/device';

const IOS_BROWSER_WARNING_DISMISSED_KEY = 'ios_browser_warning_dismissed';

export const IOSBrowserWarning: React.FC = () => {
  const [isVisible, setIsVisible] = useState(false);

  useEffect(() => {
    const shouldShow = isIOS() && !isPWA();
    const isDismissed = localStorage.getItem(IOS_BROWSER_WARNING_DISMISSED_KEY);

    if (shouldShow && !isDismissed) {
      setIsVisible(true);
    }
  }, []);

  const handleDismiss = () => {
    setIsVisible(false);
    localStorage.setItem(IOS_BROWSER_WARNING_DISMISSED_KEY, 'true');
  };

  const handleLearnMore = () => {
    alert(
      '📱 Safari에서 알림 받는 방법:\n\n' +
        '1. Safari 브라우저로 이 사이트를 여세요\n' +
        '2. 하단의 공유 버튼을 탭하세요\n' +
        '3. "홈 화면에 추가"를 선택하세요\n' +
        '4. 추가된 앱 아이콘을 탭하여 실행하세요\n\n' +
        '홈 화면에서 실행한 앱은 푸시 알림을 받을 수 있어요!',
    );
  };

  if (!isVisible) return null;

  return (
    <S.Banner>
      <S.Content>
        <S.IconWrapper>
          <Smartphone size={24} />
        </S.IconWrapper>
        <S.TextWrapper>
          <S.Title>알림을 받으려면 Safari를 사용하세요!</S.Title>
          <S.Description>
            현재 브라우저에서는 푸시 알림이 지원되지 않아요. Safari로 홈 화면에 추가하면 알림을 받을
            수 있어요.
          </S.Description>
        </S.TextWrapper>
        <S.ButtonGroup>
          <S.Button variant="primary" onClick={handleLearnMore}>
            <Share size={16} />
            자세히 보기
          </S.Button>
          <S.Button variant="secondary" onClick={handleDismiss}>
            닫기
          </S.Button>
        </S.ButtonGroup>
      </S.Content>
    </S.Banner>
  );
};
