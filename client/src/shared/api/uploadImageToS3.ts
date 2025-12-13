import { FileType } from '@/shared/types/file';
import axios from 'axios';

export const uploadImageToS3 = async (presignedUrl: string, file: FileType): Promise<void> => {
  const response = await axios.put(presignedUrl, file, {
    headers: {
      'Content-Type': file.type,
    },
  });

  if (response.status !== 200) {
    throw new Error(`Failed to upload image: ${response.statusText}`);
  }
};
