import { describe, expect, it } from 'vitest';
import { getEmailSuggestions, isAllowedEmail } from './emailUtils';

describe('emailUtils', () => {
  it.each([
    'usuario@duoc.cl',
    'docente@profesor.duoc.cl',
    'jugador@gmail.com',
  ])('acepta el correo permitido %s', (email) => {
    expect(isAllowedEmail(email)).toBe(true);
  });

  it.each(['usuario@hotmail.com', 'correo-sin-dominio', '', 'usuario@duoc.cl.evil.com'])(
    'rechaza el correo no permitido %s',
    (email) => {
      expect(isAllowedEmail(email)).toBe(false);
    }
  );

  it('sugiere dominios compatibles con lo que se ha escrito', () => {
    expect(getEmailSuggestions('ana@gm')).toEqual(['ana@gmail.com']);
  });

  it('devuelve sugerencias de todos los dominios desde un nombre sin arroba', () => {
    expect(getEmailSuggestions('ana')).toEqual([
      'ana@duoc.cl',
      'ana@profesor.duoc.cl',
      'ana@gmail.com',
    ]);
  });

  it('no sugiere direcciones si falta el nombre de usuario', () => {
    expect(getEmailSuggestions('@gmail')).toEqual([]);
  });
});
