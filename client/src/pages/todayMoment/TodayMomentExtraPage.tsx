import { useSendExtraMoments } from '@/features/moment/hook/useSendExtraMoments';
import { TodayMomentForm } from '@/features/moment/ui/TodayMomentForm';
import { TitleContainer } from '@/shared/design-system/titleContainer/TitleContainer';
import * as S from './index.styles';
import { useDwell } from '@/shared/lib/ga/hooks/useDwell';
import { track } from '@/shared/lib/ga/track';
import { useEffect } from 'react';
import { useLocation } from 'react-router';

export default function TodayMomentExtraPage() {
  const {
    handleExtraContentChange,
    handleExtraImageChange,
    handleTagNameClick,
    handleSendExtraContent,
    content,
    tagNames,
  } = useSendExtraMoments();
  const location = useLocation();
  useEffect(() => {
    track('open_composer', {
      entry: location.state?.entry ?? 'nav',
      composer: 'extra',
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
        handleContentChange={handleExtraContentChange}
        handleImageChange={handleExtraImageChange}
        handleTagNameClick={handleTagNameClick}
        handleSendContent={handleSendExtraContent}
        content={content}
        tagNames={tagNames}
      />
    </S.TodayPageWrapper>
  );
}
