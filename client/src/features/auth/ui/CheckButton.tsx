import { CustomTheme } from '@/app/styles/theme';
import { Button } from '@/shared/ui';

interface CheckButton {
  onClick: () => void;
}

export const CheckButton = ({ onClick: handleClick }: CheckButton) => {
  return <Button externalVariant={buttonVariant} title={'중복 확인'} onClick={handleClick} />;
};

const CheckButtonStyle = {
  button: (theme: CustomTheme) => `
    text-wrap: nowrap;
    font-size: 14px;
    font-weight: 600;
    color: ${theme.colors['gray-400']};
    background-color: ${theme.colors['navy-900_20']};
    border-radius: 5px;
    padding:14px 10px;
    border: 1px solid ${theme.colors['gray-700']};
    cursor: pointer;
    &:hover {
      background-color: ${theme.colors['navy-900_40']};
    }
  `,
};

const buttonVariant = (theme: CustomTheme) => CheckButtonStyle.button(theme);
