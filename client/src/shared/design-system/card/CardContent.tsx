import React from 'react';
import styled from '@emotion/styled';

interface CardContentProps extends React.HTMLAttributes<HTMLElement> {}

export const CardContent = React.forwardRef<HTMLElement, CardContentProps>(
  ({ children, ...props }, ref) => {
    return <CardContentStyles ref={ref} {...props}>{children}</CardContentStyles>;
  },
);

CardContent.displayName = 'CardContent';

const CardContentStyles = styled.section`
  display: flex;
  flex-direction: column;
  text-align: center;
  gap: 5px;
  margin: 10px 0;
`;
