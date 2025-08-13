import * as S from './LevelTable.styles';

const levelData = [
  {
    id: 1,
    level: 1,
    name: '소행성',
    story: '떠다니던 먼지들이 뭉쳐 자신만의 이야기를 품은 첫 번째 형태',
    condition: '0p',
  },
  {
    id: 2,
    level: 2,
    name: '유성',
    story: '다른 별의 궤도를 스치며 처음으로 빛의 흔적을 남기는 존재',
    condition: '60p',
  },
  {
    id: 3,
    level: 3,
    name: '혜성',
    story: '긴 꼬리를 남기며 우주를 가로지르는 방랑자',
    condition: '180p',
  },
  {
    id: 4,
    level: 4,
    name: '행성',
    story: '자신만의 색과 이야기를 가진 독립적인 존재',
    condition: '360p',
  },
  {
    id: 5,
    level: 5,
    name: '소우주',
    story: '당신의 마음속에 하나의 완전한 우주가 창조',
    condition: '720p',
  },
];

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
        {levelData.map(item => {
          return (
            <tr>
              <td>{item.level}</td>
              <td>{item.name}</td>
              <td>{item.story}</td>
              <td>{item.condition}</td>
            </tr>
          );
        })}
      </tbody>
    </S.LevelTableWrapper>
  );
};
