import { Button, ButtonProps } from '@/shared/design-system/button';

type YellowSquareButtonProps = Omit<ButtonProps, 'variant'>;

export const YellowSquareButton = ({ children, ...props }: YellowSquareButtonProps) => {
  return (
    <Button variant="tertiary" {...props}>
      {children}
    </Button>
  );
};
