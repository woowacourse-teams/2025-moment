import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { useUserDetailQuery } from '../api/useUserDetailQuery';
import { useDeleteUserMutation } from '../api/useDeleteUserMutation';

export function useUserDetail(userId: string) {
  const navigate = useNavigate();
  const { data: user, isLoading, isError } = useUserDetailQuery(userId);
  const deleteUserMutation = useDeleteUserMutation(userId);

  const [isDeleteModalOpen, setDeleteModalOpen] = useState(false);
  const [deleteReason, setDeleteReason] = useState('');

  const openDeleteModal = () => setDeleteModalOpen(true);
  const closeDeleteModal = () => {
    setDeleteModalOpen(false);
    setDeleteReason('');
  };

  const handleDelete = async () => {
    if (!deleteReason.trim()) return;

    await deleteUserMutation.mutateAsync({ reason: deleteReason.trim() });
    closeDeleteModal();
    navigate('/users');
  };

  return {
    user,
    isLoading,
    isError,
    isDeleteModalOpen,
    openDeleteModal,
    closeDeleteModal,
    deleteReason,
    setDeleteReason,
    handleDelete,
    isDeleting: deleteUserMutation.isPending,
  };
}
