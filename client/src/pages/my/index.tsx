import { mockProfile } from '@/features/my/api/mockData';
import * as S from './index.styles';

export default function MyPage() {
  return (
    <S.MyPageWrapper>
      <S.UserInfoSection>
        <S.Email>{mockProfile.data.email}</S.Email>
        <S.UserInfo>
          <p>{mockProfile.data.nickname}</p>
          <p>{mockProfile.data.level}</p>
        </S.UserInfo>
      </S.UserInfoSection>
      <S.StatusSection>status</S.StatusSection>
      <S.SettingSection>setting</S.SettingSection>
    </S.MyPageWrapper>
  );
}
