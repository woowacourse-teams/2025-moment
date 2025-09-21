import { useComplaintMutation } from '../api/useComplaintMutation';
import { ComplaintFormData } from '../types/complaintType';
import { queryClient } from '@/app/lib/queryClient';

export const useSendComplaint = (onComplete?: () => void) => {
  const { mutate: sendComplaint, isPending, error, isError } = useComplaintMutation();

  const handleComplaintSubmit = (data: ComplaintFormData) => {
    sendComplaint(data, {
      onSuccess: () => {
        if (data.targetType === 'MOMENT') {
          queryClient.invalidateQueries({
            queryKey: ['commentableMoments'],
          });
        } else {
          queryClient.invalidateQueries({ queryKey: ['comments'] });
          queryClient.invalidateQueries({ queryKey: ['moments'] });
        }

        onComplete?.();
      },
    });
  };

  return { handleComplaintSubmit, isLoading: isPending, error, isError };
};
