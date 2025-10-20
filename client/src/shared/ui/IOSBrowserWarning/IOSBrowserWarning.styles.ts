import styled from '@emotion/styled';

export const Banner = styled.div`
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  z-index: 9999;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  padding: 16px 20px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.15);
  animation: slideDown 0.3s ease-out;

  @keyframes slideDown {
    from {
      transform: translateY(-100%);
    }
    to {
      transform: translateY(0);
    }
  }
`;

export const Content = styled.div`
  max-width: 1200px;
  margin: 0 auto;
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
`;

export const IconWrapper = styled.div`
  flex-shrink: 0;
  display: flex;
  align-items: center;
  opacity: 0.9;
`;

export const TextWrapper = styled.div`
  flex: 1;
`;

export const Title = styled.div`
  font-size: 14px;
  font-weight: 600;
  margin-bottom: 4px;
`;

export const Description = styled.div`
  font-size: 12px;
  opacity: 0.9;
  line-height: 1.4;
`;

export const ButtonGroup = styled.div`
  display: flex;
  gap: 8px;
  flex-shrink: 0;
`;

export const Button = styled.button<{ variant?: 'primary' | 'secondary' }>`
  padding: 8px 16px;
  border-radius: 6px;
  font-size: 13px;
  font-weight: 500;
  cursor: pointer;
  border: none;
  white-space: nowrap;
  transition: all 0.2s;
  display: flex;
  align-items: center;
  gap: 6px;

  ${({ variant = 'primary' }) =>
    variant === 'primary'
      ? `
    background: white;
    color: #667eea;
    &:hover {
      background: rgba(255, 255, 255, 0.9);
    }
  `
      : `
    background: rgba(255, 255, 255, 0.2);
    color: white;
    &:hover {
      background: rgba(255, 255, 255, 0.3);
    }
  `}

  &:active {
    transform: scale(0.98);
  }
`;
