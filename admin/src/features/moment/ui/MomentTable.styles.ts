import styled from "@emotion/styled";

export const TableContainer = styled.div`
  width: 100%;
  overflow-x: auto;
  border: 1px solid #e5e7eb;
  border-radius: 8px;
  background-color: #ffffff;
`;

export const Table = styled.table`
  width: 100%;
  border-collapse: collapse;
  min-width: 500px;
`;

export const Thead = styled.thead`
  background-color: #f9fafb;
`;

export const Th = styled.th`
  padding: 0.75rem 1rem;
  text-align: left;
  font-size: 0.8125rem;
  font-weight: 600;
  color: #6b7280;
  white-space: nowrap;
  border-bottom: 1px solid #e5e7eb;
`;

export const Tbody = styled.tbody``;

export const Tr = styled.tr`
  cursor: pointer;
  transition: background-color 0.15s ease;

  &:hover {
    background-color: #f9fafb;
  }

  &:not(:last-child) {
    border-bottom: 1px solid #f3f4f6;
  }
`;

export const Td = styled.td`
  padding: 0.75rem 1rem;
  font-size: 0.875rem;
  color: #1f2937;
  white-space: nowrap;
`;

export const ContentTd = styled(Td)`
  max-width: 300px;
  overflow: hidden;
  text-overflow: ellipsis;
  color: #6b7280;
`;

export const ActionCell = styled.div`
  display: flex;
  gap: 0.5rem;
`;

export const EmptyState = styled.div`
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 2rem;
  color: #9ca3af;
`;

export const LoadingState = styled.div`
  display: flex;
  justify-content: center;
  padding: 2rem;
  color: #9ca3af;
`;
