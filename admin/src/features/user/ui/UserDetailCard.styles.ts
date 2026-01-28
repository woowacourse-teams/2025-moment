import styled from "@emotion/styled";

export const Card = styled.div`
  width: 100%;
  background-color: #1f1f1f;
  border: 1px solid #333;
  border-radius: 8px;
  padding: 1.5rem;
`;

export const CardHeader = styled.div`
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 1.5rem;
`;

export const CardTitle = styled.h2`
  font-size: 1.25rem;
  font-weight: 600;
`;

export const ActionButtons = styled.div`
  display: flex;
  gap: 0.5rem;
`;

export const FieldList = styled.dl`
  display: flex;
  flex-direction: column;
  gap: 1rem;
`;

export const FieldRow = styled.div`
  display: flex;
  gap: 1rem;

  @media (max-width: 640px) {
    flex-direction: column;
    gap: 0.25rem;
  }
`;

export const FieldLabel = styled.dt`
  font-size: 0.8125rem;
  font-weight: 600;
  color: #9ca3af;
  min-width: 100px;
`;

export const FieldValue = styled.dd`
  font-size: 0.9375rem;
  color: #e5e7eb;
  margin: 0;
`;

export const StatusBadge = styled.span<{ $variant: "active" | "deleted" }>`
  display: inline-block;
  padding: 0.125rem 0.625rem;
  border-radius: 9999px;
  font-size: 0.8125rem;
  font-weight: 500;
  background-color: ${({ $variant }) =>
    $variant === "deleted"
      ? "rgba(239, 68, 68, 0.15)"
      : "rgba(34, 197, 94, 0.15)"};
  color: ${({ $variant }) => ($variant === "deleted" ? "#ef4444" : "#22c55e")};
`;

export const LoadingState = styled.div`
  display: flex;
  justify-content: center;
  padding: 3rem;
  color: #9ca3af;
`;

export const ErrorState = styled.div`
  display: flex;
  justify-content: center;
  padding: 3rem;
  color: #ef4444;
`;
