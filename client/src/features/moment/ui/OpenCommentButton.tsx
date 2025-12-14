import { CustomTheme } from '@/shared/styles/theme';
import { Button } from '@/shared/design-system/button';

export const OpenCommentButton = ({
  hasComment,
  onClick: handleClick,
}: {
  hasComment: boolean;
  onClick: () => void;
}) => {
  return (
    <Button
      externalVariant={theme => buttonVariant(theme, hasComment)}
      title={hasComment ? '코멘트 보기' : '아직 응답이 없어요.'}
      disabled={!hasComment}
      onClick={handleClick}
    />
  );
};

const buttonVariant = (theme: CustomTheme, hasComment: boolean) => `
  border: 1px solid ${hasComment ? theme.colors['yellow-500'] : theme.colors.white};
  color: ${hasComment ? theme.colors['yellow-500'] : theme.colors.white};
  border-radius: 25px;
  padding: 8px 16px;
  font-size: 14px;
  font-weight: bold;
  width: 100%;
`;
