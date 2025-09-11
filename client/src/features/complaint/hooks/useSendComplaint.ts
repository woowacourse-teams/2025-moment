import { useComplaintMutation } from '../api/useComplaintMutation';
import { ComplaintFormData } from '../types/complaintType';
import { addComplainedMoment } from '../utils/complainedMoments';

export const useSendComplaint = (onComplete?: () => void, onRefetch?: () => void) => {
  const { mutate: sendComplaint, isPending, error, isError } = useComplaintMutation();

  const handleComplaintSubmit = (data: ComplaintFormData) => {
    sendComplaint(data, {
      onSuccess: () => {
        if (data.targetType === 'MOMENT') {
          addComplainedMoment(data.targetId);
        }
        onRefetch?.();
        onComplete?.();
      },
    });
  };

  return { handleComplaintSubmit, isLoading: isPending, error, isError };
};
