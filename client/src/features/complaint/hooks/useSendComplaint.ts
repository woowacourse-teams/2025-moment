import { useComplaintMutation } from '../api/useComplaintMutation';
import { ComplaintFormData } from '../types/complaintType';

export const useSendComplaint = (onSuccess?: () => void, onRefetch?: () => void) => {
  const { mutate: sendComplaint, isPending, error, isError } = useComplaintMutation();

  const handleComplaintSubmit = (data: ComplaintFormData) => {
    sendComplaint(data, {
      onSuccess: () => {
        onRefetch?.();
        onSuccess?.();
      },
    });
  };

  return { handleComplaintSubmit, isLoading: isPending, error, isError };
};
