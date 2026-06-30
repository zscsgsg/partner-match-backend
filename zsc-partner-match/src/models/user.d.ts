/**
 * 用户类别
 */
export type UserType = {
  id: number;
  /** 智能匹配返回的用户ID字段（MatchResultVO 使用 userId） */
  userId?: number;
  username: string;
  userAccount: string;
  avatarUrl?: string;
  profile?: string;
  gender: number;
  phone: string;
  email: string;
  userStatus: number;
  role: number;
  tags: string[];
  createTime: Date;
  /** 智能匹配综合得分 (0~1) */
  totalScore?: number;
  /** 智能匹配共同标签 */
  commonTags?: string[];
  /** AI 推荐理由 */
  explanation?: string;
};
