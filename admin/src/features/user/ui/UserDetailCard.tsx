import type { User } from "../api/useUsersQuery";
import { Button } from "@shared/ui";
import * as S from "./UserDetailCard.styles";

interface UserDetailCardProps {
  user: User;
  isAdmin: boolean;
  onEdit: () => void;
  onDelete: () => void;
}

const formatDateTime = (dateString: string) => {
  return new Date(dateString).toLocaleString("ko-KR", {
    year: "numeric",
    month: "2-digit",
    day: "2-digit",
    hour: "2-digit",
    minute: "2-digit",
  });
};

export function UserDetailCard({
  user,
  isAdmin,
  onEdit,
  onDelete,
}: UserDetailCardProps) {
  const isDeleted = !!user.deletedAt;

  return (
    <S.Card>
      <S.CardHeader>
        <S.CardTitle>User Details</S.CardTitle>
        {isAdmin && !isDeleted && (
          <S.ActionButtons>
            <Button variant="secondary" size="sm" onClick={onEdit}>
              Edit
            </Button>
            <Button variant="danger" size="sm" onClick={onDelete}>
              Delete
            </Button>
          </S.ActionButtons>
        )}
      </S.CardHeader>

      <S.FieldList>
        <S.FieldRow>
          <S.FieldLabel>ID</S.FieldLabel>
          <S.FieldValue>{user.id}</S.FieldValue>
        </S.FieldRow>
        <S.FieldRow>
          <S.FieldLabel>Email</S.FieldLabel>
          <S.FieldValue>{user.email}</S.FieldValue>
        </S.FieldRow>
        <S.FieldRow>
          <S.FieldLabel>Nickname</S.FieldLabel>
          <S.FieldValue>{user.nickname}</S.FieldValue>
        </S.FieldRow>
        <S.FieldRow>
          <S.FieldLabel>Provider</S.FieldLabel>
          <S.FieldValue>{user.providerType}</S.FieldValue>
        </S.FieldRow>
        <S.FieldRow>
          <S.FieldLabel>Created</S.FieldLabel>
          <S.FieldValue>{formatDateTime(user.createdAt)}</S.FieldValue>
        </S.FieldRow>
        <S.FieldRow>
          <S.FieldLabel>Status</S.FieldLabel>
          <S.FieldValue>
            <S.StatusBadge $variant={isDeleted ? "deleted" : "active"}>
              {isDeleted ? "Deleted" : "Active"}
            </S.StatusBadge>
          </S.FieldValue>
        </S.FieldRow>
        {isDeleted && user.deletedAt && (
          <S.FieldRow>
            <S.FieldLabel>Deleted At</S.FieldLabel>
            <S.FieldValue>{formatDateTime(user.deletedAt)}</S.FieldValue>
          </S.FieldRow>
        )}
      </S.FieldList>
    </S.Card>
  );
}
