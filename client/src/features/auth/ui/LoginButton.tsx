import { colors } from '@/app/styles/colors';
import { Button } from '@/shared/ui/Button';
import { css } from '@emotion/react';

const loginStyle = css`
  background-color: transparent;
  color: #fff;
  border: 1px solid ${colors.border.primary};
  border-radius: 50px;
  padding: 10px 20px;
  font-size: 16px;
  font-weight: 600;

  &:hover {
    transform: scale(1.05);
    transition: transform 0.3s ease;
  }
`;

export const LoginButton = () => {
  return <Button title="로그인" onClick={() => {}} css={loginStyle} />;
};
