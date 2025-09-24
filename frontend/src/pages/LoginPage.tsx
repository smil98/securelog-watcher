import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import axios from 'axios';

const LoginPage = () => {
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [otp, setOtp] = useState('');
  const [isMfaRequired, setIsMfaRequired] = useState(false);
  const [error, setError] = useState('');
  const [isLoading, setIsLoading] = useState(false);
  const { login } = useAuth();
  const navigate = useNavigate();

  const handleLogin = async (e: React.FormEvent) => {
    e.preventDefault();
    setIsLoading(true);
    setError('');
    try {
      // 1. 로그인 요청
      const response = await axios.post('http://localhost:8080/api/v1/auth/login', {
        email,
        password
      });

      // 2. 백엔드 응답에 2단계 인증 여부 확인
      if (response.data.isMfaRequired) {
        setIsMfaRequired(true);
        setError('2단계 인증을 위해 OTP 코드를 입력해주세요.');
      } else {
        await login(response.data.token);
        navigate('/dashboard');
      }
    } catch (err) {
      if (axios.isAxiosError(err)) {
        setError('로그인 실패: ' + (err.response?.data?.message || err.message));
      } else {
        setError('알 수 없는 오류가 발생했습니다.');
      }
    } finally {
      setIsLoading(false);
    }
  };

  const handleMfaVerify = async (e: React.FormEvent) => {
    e.preventDefault();
    setIsLoading(true);
    setError('');
    try {
      // 3. OTP 인증 요청
      const response = await axios.post('http://localhost:8080/api/v1/auth/mfa-verify', {
        email,
        otp
      });
      await login(response.data.token);
      navigate('/dashboard');
    } catch (err) {
      if (axios.isAxiosError(err)) {
        setError('OTP 인증 실패: ' + (err.response?.data?.message || err.message));
      } else {
        setError('알 수 없는 오류가 발생했습니다.');
      }
    } finally {
      setIsLoading(false);
    }
  };

  return (
    <div className="flex flex-col items-center justify-center p-6 bg-white rounded-lg shadow-md">
      <h2 className="text-2xl font-bold mb-4 text-gray-800">로그인</h2>
      <form className="w-full max-w-sm" onSubmit={isMfaRequired ? handleMfaVerify : handleLogin}>
        <div className="mb-4">
          <label className="block text-gray-700 text-sm font-bold mb-2" htmlFor="email">
            이메일
          </label>
          <input
            className="shadow appearance-none border rounded w-full py-2 px-3 text-gray-700 leading-tight focus:outline-none focus:shadow-outline"
            id="email"
            type="email"
            placeholder="이메일"
            value={email}
            onChange={(e) => setEmail(e.target.value)}
            required
            disabled={isMfaRequired}
          />
        </div>
        <div className="mb-6">
          <label className="block text-gray-700 text-sm font-bold mb-2" htmlFor="password">
            비밀번호
          </label>
          <input
            className="shadow appearance-none border rounded w-full py-2 px-3 text-gray-700 mb-3 leading-tight focus:outline-none focus:shadow-outline"
            id="password"
            type="password"
            placeholder="******************"
            value={password}
            onChange={(e) => setPassword(e.target.value)}
            required
            disabled={isMfaRequired}
          />
        </div>

        {isMfaRequired && (
          <div className="mb-6">
            <label className="block text-gray-700 text-sm font-bold mb-2" htmlFor="otp">
              OTP 코드
            </label>
            <input
              className="shadow appearance-none border rounded w-full py-2 px-3 text-gray-700 mb-3 leading-tight focus:outline-none focus:shadow-outline"
              id="otp"
              type="text"
              placeholder="Google Authenticator의 6자리 코드"
              value={otp}
              onChange={(e) => setOtp(e.target.value)}
              required
            />
          </div>
        )}

        {error && <p className="text-red-500 text-xs italic mb-4">{error}</p>}
        <div className="flex items-center justify-between">
          <button
            className="bg-blue-500 hover:bg-blue-700 text-white font-bold py-2 px-4 rounded focus:outline-none focus:shadow-outline disabled:bg-gray-400"
            type="submit"
            disabled={isLoading}
          >
            {isLoading ? '인증 중...' : isMfaRequired ? 'OTP 인증' : '로그인'}
          </button>
        </div>
      </form>
    </div>
  );
};

export default LoginPage;