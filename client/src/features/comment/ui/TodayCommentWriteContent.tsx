import { Card, TextArea } from '@/shared/ui';
import { YellowSquareButton } from '@/shared/ui/button/YellowSquareButton';
import { Send } from 'lucide-react';
import * as S from '../../moment/ui/TodayContent.styles';
import { useMatchMomentsQuery } from '@/features/moment/hook/useMatchMomentsQuery';

interface TodayCommentWriteContentProps {
  commentsData: { content: string; momentId: number };
  handleChange: (e: React.ChangeEvent<HTMLTextAreaElement>) => void;
  handleSubmit: () => Promise<void>;
  onSubmit: () => void;
}

export const TodayCommentWriteContent = ({
  commentsData,
  handleChange,
  handleSubmit,
  onSubmit,
}: TodayCommentWriteContentProps) => {
  const MAX_LENGTH = 300;
  const currentLength = commentsData.content.length;
  const isDisabled = commentsData.content.trim().length === 0 || currentLength > MAX_LENGTH;
  const { data: momentsData } = useMatchMomentsQuery();

  const handleFormSubmit = async () => {
    await handleSubmit();
    onSubmit();
  };

  return (
    <S.TodayContentWrapper>
      <Card.TitleContainer
        title={''} // 추후 공용 컴포넌트 추가 후 수정
        subtitle={momentsData?.data.content || ''}
      />
      <Card.Content>
        <TextArea
          placeholder="따뜻한 위로의 말을 전해주세요..."
          height="medium"
          maxLength={MAX_LENGTH}
          value={commentsData.content}
          onChange={handleChange}
        />
      </Card.Content>
      <Card.Action position="space-between">
        <p>
          {currentLength} / {MAX_LENGTH}
        </p>
        <YellowSquareButton
          Icon={Send}
          title="코멘트 보내기"
          onClick={handleFormSubmit}
          disabled={isDisabled}
        />
      </Card.Action>
    </S.TodayContentWrapper>
  );
};
