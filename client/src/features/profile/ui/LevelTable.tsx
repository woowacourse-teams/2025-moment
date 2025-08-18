import { levelTableData } from '@/features/profile/data/levelTableData';
import * as S from './LevelTable.styles';

export const LevelTable = () => {
  return (
    <S.LevelTableWrapper>
      <thead>
        <tr>
          <th>레벨</th>
          <th>명칭</th>
          <th>성장 서사</th>
          <th>달성 조건</th>
        </tr>
      </thead>

      <tbody>
        {levelTableData.map(item => (
          <tr key={item.level}>
            <td>{item.level}</td>
            <td>{item.name}</td>
            <td>{item.story}</td>
            <td>{item.condition}</td>
          </tr>
        ))}
      </tbody>
    </S.LevelTableWrapper>
  );
};
