import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { useGroupDetailQuery } from '../api/useGroupDetailQuery';
import { useDeleteGroupMutation } from '../api/useDeleteGroupMutation';
import { useRestoreGroupMutation } from '../api/useRestoreGroupMutation';
import { useUpdateGroupMutation } from '../api/useUpdateGroupMutation';
import type { UpdateGroupRequest } from '../types/group';

export function useGroupDetail(groupId: string) {
  const navigate = useNavigate();
  const { data: group, isLoading, isError } = useGroupDetailQuery(groupId);
  const deleteMutation = useDeleteGroupMutation(groupId);
  const restoreMutation = useRestoreGroupMutation(groupId);
  const updateMutation = useUpdateGroupMutation(groupId);

  const [isDeleteModalOpen, setDeleteModalOpen] = useState(false);
  const [deleteReason, setDeleteReason] = useState('');
  const [isEditModalOpen, setEditModalOpen] = useState(false);

  const openDeleteModal = () => setDeleteModalOpen(true);
  const closeDeleteModal = () => {
    setDeleteModalOpen(false);
    setDeleteReason('');
  };

  const handleDelete = async () => {
    if (!deleteReason.trim()) return;
    await deleteMutation.mutateAsync({ reason: deleteReason.trim() });
    closeDeleteModal();
    navigate('/groups');
  };

  const handleRestore = async () => {
    await restoreMutation.mutateAsync();
  };

  const handleUpdate = async (data: UpdateGroupRequest) => {
    await updateMutation.mutateAsync(data);
    setEditModalOpen(false);
  };

  return {
    group,
    isLoading,
    isError,
    isDeleteModalOpen,
    openDeleteModal,
    closeDeleteModal,
    deleteReason,
    setDeleteReason,
    handleDelete,
    isDeleting: deleteMutation.isPending,
    handleRestore,
    isRestoring: restoreMutation.isPending,
    isEditModalOpen,
    setEditModalOpen,
    handleUpdate,
    isUpdating: updateMutation.isPending,
  };
}
