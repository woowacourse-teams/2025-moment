import { useFileUpload } from '@/shared/hooks';
import { ImageUploadData } from '@/shared/types';
import { ImagePlus, X } from 'lucide-react';
import * as S from './FileUpload.styles';

interface FileUploadProps {
  onImageChange: (imageData: ImageUploadData | null) => void;
  accept?: string;
  disabled?: boolean;
}

export function FileUpload({
  onImageChange,
  accept = 'image/*',
  disabled = false,
}: FileUploadProps) {
  const {
    uploadedImage,
    uploading,
    fileInputRef,
    handleFileSelect,
    removeImage,
    triggerFileInput,
    canAddImage,
  } = useFileUpload({ onImageChange });

  return (
    <S.FileUploadContainer>
      <S.FileInputContainer>
        <S.HiddenFileInput
          ref={fileInputRef}
          type="file"
          accept={accept}
          onChange={handleFileSelect}
          disabled={disabled || uploading}
        />

        {canAddImage && (
          <S.FileUploadButton
            onClick={() => !disabled && !uploading && triggerFileInput()}
            disabled={disabled || uploading}
          >
            <ImagePlus size={20} />
            <S.FileUploadText>{uploading ? '업로드 중...' : '이미지 추가'}</S.FileUploadText>
          </S.FileUploadButton>
        )}
      </S.FileInputContainer>

      {uploadedImage && (
        <S.PreviewContainer>
          <S.PreviewItem>
            <S.PreviewImage src={uploadedImage.previewUrl} alt="미리보기" />
            <S.RemoveButton onClick={removeImage} disabled={disabled || uploading}>
              <X size={16} />
            </S.RemoveButton>
            <S.FileName>{uploadedImage.file.name}</S.FileName>
          </S.PreviewItem>
        </S.PreviewContainer>
      )}
    </S.FileUploadContainer>
  );
}
