import { HttpResponse, http } from 'msw';
import dummy from './dummy.json';

export const handlers = [
  http.get('/api/user', () => {
    return HttpResponse.json(dummy);
  }),
];
