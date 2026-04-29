import { storageService } from './storageService';

describe('storageService', () => {
  beforeEach(() => {
    sessionStorage.clear();
    localStorage.clear();
  });

  describe('session storage', () => {
    it('문자열 값을 저장하고 불러온다', () => {
      storageService.session.set('key', 'hello');
      expect(storageService.session.get('key')).toBe('hello');
    });

    it('객체 값을 직렬화해서 저장하고 불러온다', () => {
      const data = { name: '테스트', count: 42 };
      storageService.session.set('obj', data);
      expect(storageService.session.get<typeof data>('obj')).toEqual(data);
    });

    it('숫자를 저장하고 불러온다', () => {
      storageService.session.set('num', 123);
      expect(storageService.session.get<number>('num')).toBe(123);
    });

    it('존재하지 않는 키는 null을 반환한다', () => {
      expect(storageService.session.get('nonexistent')).toBeNull();
    });

    it('키를 삭제한다', () => {
      storageService.session.set('key', 'value');
      storageService.session.remove('key');
      expect(storageService.session.get('key')).toBeNull();
    });

    it('여러 키를 한 번에 삭제한다', () => {
      storageService.session.set('a', 'alpha');
      storageService.session.set('b', 'beta');
      storageService.session.set('c', 'gamma');
      storageService.session.removeMultiple(['a', 'b']);
      expect(storageService.session.get('a')).toBeNull();
      expect(storageService.session.get('b')).toBeNull();
      expect(storageService.session.get('c')).toBe('gamma');
    });

    it('clear로 전체 삭제한다', () => {
      storageService.session.set('x', '1');
      storageService.session.set('y', '2');
      storageService.session.clear();
      expect(storageService.session.get('x')).toBeNull();
      expect(storageService.session.get('y')).toBeNull();
    });
  });

  describe('local storage', () => {
    it('문자열 값을 저장하고 불러온다', () => {
      storageService.local.set('key', 'world');
      expect(storageService.local.get('key')).toBe('world');
    });

    it('객체 값을 직렬화해서 저장하고 불러온다', () => {
      const data = { id: 1, active: true };
      storageService.local.set('data', data);
      expect(storageService.local.get<typeof data>('data')).toEqual(data);
    });

    it('존재하지 않는 키는 null을 반환한다', () => {
      expect(storageService.local.get('missing')).toBeNull();
    });

    it('키를 삭제한다', () => {
      storageService.local.set('token', 'abc');
      storageService.local.remove('token');
      expect(storageService.local.get('token')).toBeNull();
    });

    it('여러 키를 한 번에 삭제한다', () => {
      storageService.local.set('p', 'alpha');
      storageService.local.set('q', 'beta');
      storageService.local.removeMultiple(['p', 'q']);
      expect(storageService.local.get('p')).toBeNull();
      expect(storageService.local.get('q')).toBeNull();
    });

    it('clear로 전체 삭제한다', () => {
      storageService.local.set('a', '1');
      storageService.local.clear();
      expect(storageService.local.get('a')).toBeNull();
    });
  });

  describe('session과 local은 독립적으로 동작한다', () => {
    it('같은 키에 다른 값을 저장할 수 있다', () => {
      storageService.session.set('key', 'session-value');
      storageService.local.set('key', 'local-value');
      expect(storageService.session.get('key')).toBe('session-value');
      expect(storageService.local.get('key')).toBe('local-value');
    });

    it('session clear가 local에 영향을 주지 않는다', () => {
      storageService.session.set('key', 'session');
      storageService.local.set('key', 'local');
      storageService.session.clear();
      expect(storageService.session.get('key')).toBeNull();
      expect(storageService.local.get('key')).toBe('local');
    });
  });
});
