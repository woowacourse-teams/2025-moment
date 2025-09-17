import { LazyImage } from '@/shared/ui/lazyImage/LazyImage';
import { levelTableData } from './data/levelTableData';
import * as S from './LevelTable.styles';

export const LevelTable = () => {
  return (
    <S.LevelTableWrapper>
      <thead>
        <tr>
          <th>명칭</th>
          <th>레벨</th>
          <th>달성 조건</th>
          <th>셩장 서사</th>
        </tr>
      </thead>

      <tbody>
        {levelTableData.map(item => {
          return (
            <tr key={item.id} className={item.name.includes('3단계') ? 'last-stage' : ''}>
              <td>
                <LazyImage
                  src={item.level}
                  alt={item.name}
                  variant="levelIcon"
                  width="50px"
                  height="50px"
                  borderRadius="4px"
                />
              </td>
              <td>{item.name}</td>
              <td>{item.condition}</td>
              <td>{item.story}</td>
            </tr>
          );
        })}
      </tbody>
    </S.LevelTableWrapper>
  );
};
