import { useState } from 'react';
import { checkEmailExist } from '../api/checkEmailExist';

export const useCheckEmail = () => {
  const [errorMessage, setErrorMessage] = useState('');
  const [isEmailChecked, setIsEmailChecked] = useState(false);

  const handleCheckEmail = async (value: string) => {
    try {
      const result = await checkEmailExist(value);
      const isExists = result.data.isExists;
      setErrorMessage(isExists ? '이미 존재하는 이메일입니다.' : '');
      setIsEmailChecked(true);
    } catch (error) {
      console.log(error);
      alert('중복 확인 실패');
      setIsEmailChecked(false);
    }
  };

  return { errorMessage, isEmailChecked, handleCheckEmail };
};
