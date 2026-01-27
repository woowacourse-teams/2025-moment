import type { GroupDetail } from "../types/group";
import { Button } from "@shared/ui";
import * as S from "./GroupDetailCard.styles";

interface GroupDetailCardProps {
  group: GroupDetail;
  isAdmin: boolean;
  onEdit: () => void;
  onDelete: () => void;
  onRestore: () => void;
  isRestoring: boolean;
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

export function GroupDetailCard({
  group,
  isAdmin,
  onEdit,
  onDelete,
  onRestore,
  isRestoring,
}: GroupDetailCardProps) {
  const isDeleted = group.status === "DELETED";

  return (
    <S.Card>
      <S.CardHeader>
        <S.CardTitle>Group Details</S.CardTitle>
        {isAdmin && (
          <S.ActionButtons>
            {isDeleted ? (
              <Button
                variant="primary"
                size="sm"
                onClick={onRestore}
                isLoading={isRestoring}
              >
                Restore
              </Button>
            ) : (
              <>
                <Button variant="secondary" size="sm" onClick={onEdit}>
                  Edit
                </Button>
                <Button variant="danger" size="sm" onClick={onDelete}>
                  Delete
                </Button>
              </>
            )}
          </S.ActionButtons>
        )}
      </S.CardHeader>

      <S.FieldList>
        <S.FieldRow>
          <S.FieldLabel>ID</S.FieldLabel>
          <S.FieldValue>{group.id}</S.FieldValue>
        </S.FieldRow>
        <S.FieldRow>
          <S.FieldLabel>Name</S.FieldLabel>
          <S.FieldValue>{group.name}</S.FieldValue>
        </S.FieldRow>
        <S.FieldRow>
          <S.FieldLabel>Description</S.FieldLabel>
          <S.FieldValue>{group.description}</S.FieldValue>
        </S.FieldRow>
        <S.FieldRow>
          <S.FieldLabel>Owner</S.FieldLabel>
          <S.FieldValue>{group.ownerNickname}</S.FieldValue>
        </S.FieldRow>
        <S.FieldRow>
          <S.FieldLabel>Members</S.FieldLabel>
          <S.FieldValue>{group.memberCount}</S.FieldValue>
        </S.FieldRow>
        <S.FieldRow>
          <S.FieldLabel>Moments</S.FieldLabel>
          <S.FieldValue>{group.momentCount}</S.FieldValue>
        </S.FieldRow>
        <S.FieldRow>
          <S.FieldLabel>Created</S.FieldLabel>
          <S.FieldValue>{formatDateTime(group.createdAt)}</S.FieldValue>
        </S.FieldRow>
        <S.FieldRow>
          <S.FieldLabel>Updated</S.FieldLabel>
          <S.FieldValue>{formatDateTime(group.updatedAt)}</S.FieldValue>
        </S.FieldRow>
        <S.FieldRow>
          <S.FieldLabel>Status</S.FieldLabel>
          <S.FieldValue>
            <S.StatusBadge $variant={isDeleted ? "deleted" : "active"}>
              {isDeleted ? "Deleted" : "Active"}
            </S.StatusBadge>
          </S.FieldValue>
        </S.FieldRow>
        {isDeleted && group.deletedAt && (
          <S.FieldRow>
            <S.FieldLabel>Deleted At</S.FieldLabel>
            <S.FieldValue>{formatDateTime(group.deletedAt)}</S.FieldValue>
          </S.FieldRow>
        )}
      </S.FieldList>
    </S.Card>
  );
}
