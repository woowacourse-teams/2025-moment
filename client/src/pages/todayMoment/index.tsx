import { useSendMoments } from '@/features/moment/hook/useSendMoments';
import { TodayMomentForm } from '@/features/moment/ui/TodayMomentForm';
import { TitleContainer } from '@/shared/design-system/titleContainer';
import { useEffect } from 'react';
import { useLocation, useParams } from 'react-router';
import * as S from './index.styles';
import { track } from '@/shared/lib/ga/track';
import { useDwell } from '@/shared/lib/ga/hooks/useDwell';
import { useABVariant } from '@/shared/hooks/useABVariant';

export default function TodayMomentPage() {
  const { groupId } = useParams<{ groupId: string }>();
  const { handleContentChange, handleImageChange, handleSendContent, content } =
    useSendMoments(groupId);

  const variant = useABVariant('submit-btn-position');
  const location = useLocation();

  useEffect(() => {
    (window as any).__AB_VARIANT__ = variant;
    track('open_composer', {
      entry: location.state?.entry ?? 'nav',
      composer: 'moment',
    });
    return () => {
      (window as any).__AB_VARIANT__ = undefined;
    };
  }, [variant]);

  useDwell('composer');

  return (
    <S.TodayPageWrapper>
      <TitleContainer
        title="오늘의 모멘트"
        subtitle="하루에 한 번, 당신의 특별한 모멘트를 공유해보세요"
      />
      <TodayMomentForm
        handleContentChange={handleContentChange}
        handleImageChange={handleImageChange}
        handleSendContent={handleSendContent}
        content={content}
        variant={variant}
      />
    </S.TodayPageWrapper>
  );
}
