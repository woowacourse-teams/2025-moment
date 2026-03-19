import { useComplaintMutation } from '../api/useComplaintMutation';
import { useBlockMutation } from '@/features/block/api/useBlockMutation';
import { ComplaintFormData } from '../types/complaintType';
import { queryClient } from '@/app/lib/queryClient';
import { queryKeys } from '@/shared/lib/queryKeys';

export const useSendComplaint = (onComplete?: () => void) => {
  const { mutate: sendComplaint, isPending, error, isError } = useComplaintMutation();
  const { mutate: blockMember } = useBlockMutation();

  const handleComplaintSubmit = (data: ComplaintFormData) => {
    sendComplaint(data, {
      onSuccess: () => {
        if (data.blockMemberId) {
          blockMember(data.blockMemberId);
        }

        if (data.targetType === 'MOMENT') {
          queryClient.invalidateQueries({
            queryKey: queryKeys.commentableMoments.all,
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
