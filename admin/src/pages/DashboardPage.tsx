import styled from '@emotion/styled';
import { GroupStatsCards } from '@features/group/ui/GroupStatsCards';

const Container = styled.div`
  padding: 2rem;
`;

const Title = styled.h1`
  font-size: 1.5rem;
  font-weight: 600;
  color: #1f2937;
  margin-bottom: 1.5rem;
`;

const Section = styled.section`
  margin-bottom: 2rem;
`;

const SectionTitle = styled.h2`
  font-size: 1.125rem;
  font-weight: 600;
  color: #374151;
  margin-bottom: 1rem;
`;

export default function DashboardPage() {
  return (
    <Container>
      <Title>Dashboard</Title>
      <Section>
        <SectionTitle>Group Statistics</SectionTitle>
        <GroupStatsCards />
      </Section>
    </Container>
  );
}
