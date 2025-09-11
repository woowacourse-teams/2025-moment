import { FileType } from '@/types/file';

export interface UploadedImage {
  file: FileType;
  imageUrl: string;
  imageName: string;
  previewUrl: string;
}

export interface ImageUploadData {
  imageUrl: string;
  imageName: string;
}
