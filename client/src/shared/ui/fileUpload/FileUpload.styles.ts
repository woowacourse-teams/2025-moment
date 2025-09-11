import styled from '@emotion/styled';

export const FileUploadContainer = styled.div`
  width: 100%;
  display: flex;
  flex-direction: column;
  gap: 12px;
`;

export const FileInputContainer = styled.div`
  width: 100%;
  display: flex;
  align-items: center;
  gap: 8px;
`;

export const HiddenFileInput = styled.input`
  display: none;
`;

export const FileUploadButton = styled.button<{ disabled?: boolean }>`
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 8px 12px;
  border: 1px dashed ${({ theme }) => theme.colors['gray-400']};
  border-radius: 6px;
  background-color: transparent;
  color: ${({ theme }) => theme.colors['gray-400']};
  cursor: ${({ disabled }) => (disabled ? 'not-allowed' : 'pointer')};
  transition: all 0.2s ease;
  font-size: 14px;
  font-weight: 500;

  &:hover:not(:disabled) {
    border-color: ${({ theme }) => theme.colors['yellow-500']};
    color: ${({ theme }) => theme.colors['yellow-500']};
    background-color: ${({ theme }) => theme.colors['yellow-300_10']};
  }

  &:disabled {
    opacity: 0.5;
  }
`;

export const FileUploadText = styled.span`
  white-space: nowrap;
`;

export const PreviewContainer = styled.div`
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  max-height: 200px;
  overflow-y: auto;
  padding: 8px 0;
`;

export const PreviewItem = styled.div`
  position: relative;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 4px;
  padding: 8px;
  border: 1px solid ${({ theme }) => theme.colors['gray-600']};
  border-radius: 6px;
  background-color: ${({ theme }) => theme.colors['gray-800_80']};
  width: 80px;
`;

export const PreviewImage = styled.img`
  width: 60px;
  height: 60px;
  object-fit: cover;
  border-radius: 4px;
`;

export const RemoveButton = styled.button<{ disabled?: boolean }>`
  position: absolute;
  top: -6px;
  right: -6px;
  display: flex;
  align-items: center;
  justify-content: center;
  width: 20px;
  height: 20px;
  border: none;
  border-radius: 50%;
  background-color: ${({ theme }) => theme.colors['red-500']};
  color: white;
  cursor: ${({ disabled }) => (disabled ? 'not-allowed' : 'pointer')};
  transition: all 0.2s ease;

  &:hover:not(:disabled) {
    background-color: ${({ theme }) => theme.colors['red-500']};
    transform: scale(1.1);
  }

  &:disabled {
    opacity: 0.5;
  }
`;

export const FileName = styled.span`
  font-size: 10px;
  color: ${({ theme }) => theme.colors['gray-400']};
  text-align: center;
  word-break: break-all;
  line-height: 1.2;
  max-width: 100%;
  overflow: hidden;
  text-overflow: ellipsis;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
`;
