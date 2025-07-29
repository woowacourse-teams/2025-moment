import { useState } from 'react';
import { checkEmailExist } from '../api/checkEmailExist';

export const useCheckEmail = () => {
  const [errorMessage, setErrorMessage] = useState('');

  const handleCheckEmail = async (value: string) => {
    try {
      const result = await checkEmailExist(value);
      setErrorMessage(result.data.isExists ? '이미 존재하는 이메일입니다.' : '');
    } catch (error) {
      console.log(error);
      alert('중복 확인 실패');
    }
  };

  return { errorMessage, handleCheckEmail };
};
