export interface RewardHistoryItem {
  id: number;
  changeStar: number;
  reason: string;
  createdAt: string;
}

export interface RewardHistoryResponse {
  status: number;
  data: {
    items: RewardHistoryItem[];
    currentPageNum: number;
    pageSize: number;
    totalPages: number;
  };
}

export interface RewardHistoryData {
  items: RewardHistoryItem[];
  currentPageNum: number;
  pageSize: number;
  totalPages: number;
}

export interface UseRewardHistoryQueryOptions {
  pageNum?: number;
  pageSize?: number;
  enabled?: boolean;
}
export interface GetRewardHistoryParams {
  pageNum?: number;
  pageSize?: number;
}
