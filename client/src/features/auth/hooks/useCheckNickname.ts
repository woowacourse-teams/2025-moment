import { useState } from 'react';
import { checkNicknameExist } from '../api/checkNicknameExist';

export const useCheckNickname = () => {
  const [errorMessage, setErrorMessage] = useState('');
  const [isNicknameChecked, setIsNicknameChecked] = useState(false);

  const handleCheckNickname = async (value: string) => {
    try {
      const result = await checkNicknameExist(value);
      setErrorMessage(result.data.isExists ? '이미 존재하는 닉네임입니다.' : '');
      setIsNicknameChecked(true);
    } catch (error) {
      console.error(error);
      alert('중복 확인 실패');
      setIsNicknameChecked(false);
    }
  };

  return { errorMessage, isNicknameChecked, handleCheckNickname };
};
