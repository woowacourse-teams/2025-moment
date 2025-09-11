import { useComplaintMutation } from '../api/useComplaintMutation';
import { ComplaintFormData } from '../types/complaintType';
import { addComplainedMoment } from '../utils/complainedMoments';

export const useSendComplaint = (onSuccess?: () => void, onRefetch?: () => void) => {
  const { mutate: sendComplaint, isPending, error, isError } = useComplaintMutation();

  const handleComplaintSubmit = (data: ComplaintFormData) => {
    sendComplaint(data, {
      onSuccess: () => {
        if (data.targetType === 'MOMENT') {
          addComplainedMoment(data.targetId);
        }
        onRefetch?.();
        onSuccess?.();
      },
    });
  };

  return { handleComplaintSubmit, isLoading: isPending, error, isError };
};
