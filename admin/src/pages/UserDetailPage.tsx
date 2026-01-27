import styled from '@emotion/styled';
import { useParams, useNavigate } from 'react-router-dom';
import { UserDetailCard } from '@features/user/ui/UserDetailCard';
import { UserEditModal } from '@features/user/ui/UserEditModal';
import { UserDeleteModal } from '@features/user/ui/UserDeleteModal';
import { useUserDetail } from '@features/user/hooks/useUserDetail';
import { useAuth } from '@shared/auth/useAuth';
import { useState } from 'react';

const Container = styled.div`
  padding: 2rem;
  max-width: 800px;
`;

const BackLink = styled.button`
  padding: 0.5rem 0;
  margin-bottom: 1rem;
  font-size: 0.875rem;
  color: #9ca3af;
  background: none;
  border: none;
  cursor: pointer;

  &:hover {
    color: white;
  }
`;

const LoadingState = styled.div`
  display: flex;
  justify-content: center;
  padding: 3rem;
  color: #9ca3af;
`;

const ErrorState = styled.div`
  display: flex;
  justify-content: center;
  padding: 3rem;
  color: #ef4444;
`;

export default function UserDetailPage() {
  const { id } = useParams<{ id: string }>();
  const navigate = useNavigate();
  const { user: authUser } = useAuth();
  const isAdmin = authUser?.role === 'ADMIN';

  const {
    user,
    isLoading,
    isError,
    isDeleteModalOpen,
    openDeleteModal,
    closeDeleteModal,
    deleteReason,
    setDeleteReason,
    handleDelete,
    isDeleting,
  } = useUserDetail(id!);

  const [isEditModalOpen, setEditModalOpen] = useState(false);

  if (isLoading) {
    return (
      <Container>
        <LoadingState>Loading...</LoadingState>
      </Container>
    );
  }

  if (isError || !user) {
    return (
      <Container>
        <ErrorState>Failed to load user details.</ErrorState>
      </Container>
    );
  }

  return (
    <Container>
      <BackLink onClick={() => navigate('/users')}>&larr; Back to Users</BackLink>

      <UserDetailCard
        user={user}
        isAdmin={isAdmin}
        onEdit={() => setEditModalOpen(true)}
        onDelete={openDeleteModal}
      />

      <UserEditModal
        isOpen={isEditModalOpen}
        onClose={() => setEditModalOpen(false)}
        userId={id!}
        currentNickname={user.nickname}
      />

      <UserDeleteModal
        isOpen={isDeleteModalOpen}
        onClose={closeDeleteModal}
        reason={deleteReason}
        onReasonChange={setDeleteReason}
        onConfirm={handleDelete}
        isLoading={isDeleting}
      />
    </Container>
  );
}
