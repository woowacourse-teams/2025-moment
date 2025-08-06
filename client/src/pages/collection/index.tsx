import { MyMomentsList } from '@/features/moment/ui/MyMomentsList';
import { TitleContainer } from '@/shared/ui/titleContainer/TitleContainer';
import * as S from './index.styles';
import { Button } from '@/shared/ui';
import { useState } from 'react';
import PostCommentsList from '../postComments';
import { IconBar } from '@/widgets/icons/IconBar';

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

      {selectedPage === 'myMoments' ? <MyMomentsList /> : <PostCommentsList />}
    </S.CollectionContainer>
  );
}
