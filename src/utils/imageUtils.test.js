import { describe, expect, it } from 'vitest';
import { getImageUrl } from './imageUtils';

describe('imageUtils', () => {
  it('devuelve una cadena vacía si no recibe una ruta', () => {
    expect(getImageUrl('')).toBe('');
    expect(getImageUrl(null)).toBe('');
  });

  it('agrega una barra inicial a rutas locales relativas', () => {
    expect(getImageUrl('src/img/juego.jpg')).toBe('/src/img/juego.jpg');
  });

  it('conserva rutas locales que ya son absolutas', () => {
    expect(getImageUrl('/src/img/juego.jpg')).toBe('/src/img/juego.jpg');
  });

  it.each([
    'https://example.com/juego.jpg',
    'http://example.com/juego.jpg',
  ])('conserva URL externa %s', (url) => {
    expect(getImageUrl(url)).toBe(url);
  });
});
