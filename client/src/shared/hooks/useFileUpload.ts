import { uploadImageToS3 } from '@/shared/api/uploadImageToS3';
import { usePresignedUrlMutation } from '@/shared/hooks/usePresignedUrlMutation';
import { ImageUploadData, UploadedImage } from '@/shared/types/upload';
import { useRef, useState } from 'react';
import { useCloudFrontUrl } from './useCloudFrontUrl';

interface UseFileUploadProps {
  onImageChange: (imageData: ImageUploadData | null) => void;
}

export const useFileUpload = ({ onImageChange }: UseFileUploadProps) => {
  const [uploadedImage, setUploadedImage] = useState<UploadedImage | null>(null);
  const [uploading, setUploading] = useState(false);
  const fileInputRef = useRef<HTMLInputElement>(null);

  const { mutateAsync: getPresignedUrl } = usePresignedUrlMutation();
  const { generateCloudFrontUrl } = useCloudFrontUrl();

  const handleFileSelect = async (event: React.ChangeEvent<HTMLInputElement>) => {
    const files = Array.from(event.target.files || []);

    if (files.length === 0) {
      return;
    }

    const file = files[0];

    setUploading(true);
    try {
      const { presignedUrl, filePath } = await getPresignedUrl({
        imageName: file.name,
        imageType: file.type,
      });

      // 로컬 미리보기 URL 생성
      const localPreviewUrl = window.URL.createObjectURL(file as never);

      const tempUploadedImage: UploadedImage = {
        file,
        imageUrl: filePath,
        imageName: file.name,
        previewUrl: localPreviewUrl,
      };

      setUploadedImage(tempUploadedImage);
      onImageChange({ imageUrl: filePath, imageName: file.name });

      await uploadImageToS3(presignedUrl, file);

      // CloudFront URL로 미리보기 URL 교체
      const cloudFrontUrl = generateCloudFrontUrl(filePath);

      setUploadedImage(prev => ({
        ...prev!,
        previewUrl: cloudFrontUrl,
      }));
    } catch (error) {
      console.error('Failed to process image:', error);
      alert('이미지 처리에 실패했습니다. 다시 시도해주세요.');
    } finally {
      setUploading(false);
    }

    if (fileInputRef.current) {
      fileInputRef.current.value = '';
    }
  };

  const removeImage = () => {
    if (uploadedImage) {
      if (uploadedImage.previewUrl.startsWith('blob:')) {
        window.URL.revokeObjectURL(uploadedImage.previewUrl);
      }

      setUploadedImage(null);
      onImageChange(null);
    }
  };

  const triggerFileInput = () => {
    if (fileInputRef.current) {
      fileInputRef.current.click();
    }
  };

  return {
    uploadedImage,
    uploading,
    fileInputRef,
    handleFileSelect,
    removeImage,
    triggerFileInput,
    canAddImage: !uploadedImage && !uploading,
  };
};
