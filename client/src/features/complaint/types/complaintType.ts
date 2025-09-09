export enum ComplaintReason {
  SPAM_OR_ADVERTISEMENT = 'SPAM_OR_ADVERTISEMENT',
  SEXUAL_CONTENT = 'SEXUAL_CONTENT',
  HATE_SPEECH_OR_DISCRIMINATION = 'HATE_SPEECH_OR_DISCRIMINATION',
  ABUSE_OR_HARASSMENT = 'ABUSE_OR_HARASSMENT',
  VIOLENT_OR_DANGEROUS_CONTENT = 'VIOLENT_OR_DANGEROUS_CONTENT',
  PRIVACY_VIOLATION = 'PRIVACY_VIOLATION',
  ILLEGAL_INFORMATION = 'ILLEGAL_INFORMATION',
}

export interface ComplaintReasonItem {
  value: ComplaintReason;
  label: string;
  description: string;
}

export interface ComplaintFormData {
  reason: ComplaintReason;
  targetId: number;
  targetType: 'MOMENT' | 'COMMENT';
}
