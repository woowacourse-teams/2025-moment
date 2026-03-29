import React from 'react';
import styled from '@emotion/styled';

interface CardActionProps extends React.HTMLAttributes<HTMLElement> {
  position?: 'center' | 'space-between';
}

export const CardAction = React.forwardRef<HTMLElement, CardActionProps>(
  ({ children, position = 'center', ...props }, ref) => {
    return (
      <CardActionStyles ref={ref} $position={position} {...props}>
        {children}
      </CardActionStyles>
    );
  },
);

CardAction.displayName = 'CardAction';

const CardActionStyles = styled.section<{ $position: 'center' | 'space-between' }>`
  display: flex;
  justify-content: ${({ $position }) => ($position === 'center' ? 'center' : 'space-between')};
`;
