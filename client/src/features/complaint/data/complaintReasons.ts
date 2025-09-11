import { COMPLAINT_REASONS, ComplaintReasonItem } from '@/features/complaint/types/complaintType';

export const COMPLAINT_REASONS_LIST: ComplaintReasonItem[] = [
  {
    value: COMPLAINT_REASONS.SPAM_OR_ADVERTISEMENT,
    label: '스팸/광고',
    description: '관련 없거나 원치 않는 광고, 홍보성 콘텐츠를 반복적으로 게시하는 경우',
  },
  {
    value: COMPLAINT_REASONS.SEXUAL_CONTENT,
    label: '선정적 콘텐츠',
    description: '음란물, 과도한 노출 등 선정적인 내용을 포함하는 경우',
  },
  {
    value: COMPLAINT_REASONS.HATE_SPEECH_OR_DISCRIMINATION,
    label: '혐오 발언/차별',
    description:
      '특정 인종, 민족, 종교, 성별, 성적 지향, 장애 등을 이유로 증오심을 부추기거나 차별하는 내용',
  },
  {
    value: COMPLAINT_REASONS.ABUSE_OR_HARASSMENT,
    label: '괴롭힘/악플',
    description: '특정인을 대상으로 한 욕설, 인신공격, 위협, 따돌림 등 괴롭힘에 해당하는 내용',
  },
  {
    value: COMPLAINT_REASONS.VIOLENT_OR_DANGEROUS_CONTENT,
    label: '폭력적/위험한 콘텐츠',
    description: '신체적 폭력, 자해, 테러 등 위험한 행위를 묘사하거나 조장하는 내용',
  },
  {
    value: COMPLAINT_REASONS.PRIVACY_VIOLATION,
    label: '개인정보 침해',
    description: '본인의 동의 없이 이름, 연락처, 주소 등 개인정보를 유출하는 경우',
  },
  {
    value: COMPLAINT_REASONS.ILLEGAL_INFORMATION,
    label: '불법 정보',
    description: '마약, 불법 도박 등 관련 법률에서 금지하는 정보를 게시하거나 거래를 유도하는 경우',
  },
];
