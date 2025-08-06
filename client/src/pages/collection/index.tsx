import { MyCommentsList } from '@/features/comment/ui/MyCommentsList';
import { MyMomentsList } from '@/features/moment/ui/MyMomentsList';
import { Button } from '@/shared/ui';
import { TitleContainer } from '@/shared/ui/titleContainer/TitleContainer';
import { useState } from 'react';
import * as S from './index.styles';

type SelectedPage = 'myMoments' | 'postComments';

export default function Collection() {
  const [selectedPage, setSelectedPage] = useState<SelectedPage>('myMoments');

  const handleToggle = (page: SelectedPage) => {
    setSelectedPage(page);
  };

  const pageConfig = {
    myMoments: {
      title: '나의 모멘트',
      subtitle: '내가 공유한 모멘트와 받은 공감을 확인해보세요',
    },
    postComments: {
      title: '보낸 코멘트',
      subtitle: '내가 보낸 코멘트를 확인해보세요',
    },
  };

  const currentConfig = pageConfig[selectedPage];
  return (
    <S.CollectionContainer>
      <S.SelectButtonContainer>
        <Button
          title={pageConfig.myMoments.title}
          variant="primary"
          onClick={() => handleToggle('myMoments')}
        />
        <Button
          title={pageConfig.postComments.title}
          variant="primary"
          onClick={() => handleToggle('postComments')}
        />
      </S.SelectButtonContainer>
      <TitleContainer title={currentConfig.title} subtitle={currentConfig.subtitle} />

      {selectedPage === 'myMoments' ? <MyMomentsList /> : <MyCommentsList />}
    </S.CollectionContainer>
  );
}
