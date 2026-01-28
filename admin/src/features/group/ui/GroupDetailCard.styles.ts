import styled from "@emotion/styled";

export const Card = styled.div`
  width: 100%;
  background-color: #ffffff;
  border: 1px solid #e5e7eb;
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
  color: #1f2937;
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
  color: #6b7280;
  min-width: 120px;
`;

export const FieldValue = styled.dd`
  font-size: 0.9375rem;
  color: #1f2937;
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
      ? "rgba(239, 68, 68, 0.1)"
      : "rgba(34, 197, 94, 0.1)"};
  color: ${({ $variant }) => ($variant === "deleted" ? "#dc2626" : "#16a34a")};
`;
