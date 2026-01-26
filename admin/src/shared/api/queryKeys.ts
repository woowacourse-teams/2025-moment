// Hierarchical query keys for React Query
// Following the pattern: [entity, scope, ...params]

export const queryKeys = {
  // Users
  users: {
    all: ['users'] as const,
    lists: () => [...queryKeys.users.all, 'list'] as const,
    list: (filters: Record<string, unknown>) => [...queryKeys.users.lists(), filters] as const,
    details: () => [...queryKeys.users.all, 'detail'] as const,
    detail: (id: string) => [...queryKeys.users.details(), id] as const,
  },

  // Groups
  groups: {
    all: ['groups'] as const,
    lists: () => [...queryKeys.groups.all, 'list'] as const,
    list: (filters: Record<string, unknown>) => [...queryKeys.groups.lists(), filters] as const,
    details: () => [...queryKeys.groups.all, 'detail'] as const,
    detail: (id: string) => [...queryKeys.groups.details(), id] as const,
  },

  // Moments
  moments: {
    all: ['moments'] as const,
    lists: () => [...queryKeys.moments.all, 'list'] as const,
    list: (filters: Record<string, unknown>) => [...queryKeys.moments.lists(), filters] as const,
    details: () => [...queryKeys.moments.all, 'detail'] as const,
    detail: (id: string) => [...queryKeys.moments.details(), id] as const,
  },

  // Complaints
  complaints: {
    all: ['complaints'] as const,
    lists: () => [...queryKeys.complaints.all, 'list'] as const,
    list: (filters: Record<string, unknown>) => [...queryKeys.complaints.lists(), filters] as const,
    details: () => [...queryKeys.complaints.all, 'detail'] as const,
    detail: (id: string) => [...queryKeys.complaints.details(), id] as const,
  },

  // Audit Logs
  auditLogs: {
    all: ['auditLogs'] as const,
    lists: () => [...queryKeys.auditLogs.all, 'list'] as const,
    list: (filters: Record<string, unknown>) => [...queryKeys.auditLogs.lists(), filters] as const,
  },
};
