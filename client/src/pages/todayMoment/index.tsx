import { useSendMoments } from '@/features/moment/hook/useSendMoments';
import { TodayMomentForm } from '@/features/moment/ui/TodayMomentForm';
import { TitleContainer } from '@/shared/design-system/titleContainer/TitleContainer';
import { useEffect } from 'react';
import { useLocation, useParams } from 'react-router';
import * as S from './index.styles';
import { track } from '@/shared/lib/ga/track';
import { useDwell } from '@/shared/lib/ga/hooks/useDwell';

export default function TodayMomentPage() {
  const { groupId } = useParams<{ groupId: string }>();
  const { handleContentChange, handleImageChange, handleSendContent, content } =
    useSendMoments(groupId);

  const location = useLocation();
  useEffect(() => {
    track('open_composer', {
      entry: location.state?.entry ?? 'nav',
      composer: 'moment',
    });
  }, []);

  useDwell({ item_type: 'moment', surface: 'composer' });

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
      />
    </S.TodayPageWrapper>
  );
}
