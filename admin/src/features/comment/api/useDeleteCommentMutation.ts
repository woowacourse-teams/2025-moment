import { useMutation, useQueryClient } from "@tanstack/react-query";
import { apiClient } from "@shared/api";
import { queryKeys } from "@shared/api/queryKeys";

export const useDeleteCommentMutation = (groupId: string, momentId: string) => {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: (commentId: number) =>
      deleteComment(groupId, momentId, commentId),
    onSuccess: () => {
      queryClient.invalidateQueries({
        queryKey: queryKeys.groups.detail(groupId),
      });
    },
  });
};

const deleteComment = async (
  groupId: string,
  momentId: string,
  commentId: number,
): Promise<void> => {
  await apiClient.delete(
    `/groups/${groupId}/moments/${momentId}/comments/${commentId}`,
  );
};
