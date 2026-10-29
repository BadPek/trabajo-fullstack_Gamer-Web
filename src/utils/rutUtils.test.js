import { describe, it, expect } from 'vitest';
import { cleanRut, validateRut } from './rutUtils';

describe('rutUtils', () => {
  it('cleanRut debe limpiar puntos y guiones dejando solo números y dígito verificador', () => {
    expect(cleanRut('19.011.022-2')).toBe('190110222');
    expect(cleanRut('12.345.678-k')).toBe('12345678K');
  });

  it('validateRut debe validar correctamente RUTs chilenos válidos', () => {
    expect(validateRut('19.011.022-2')).toBe(true);
    expect(validateRut('190110222')).toBe(true);
  });

  it('validateRut debe rechazar RUTs inválidos o mal formateados', () => {
    expect(validateRut('11.111.111-1')).toBe(false);
    expect(validateRut('123')).toBe(false);
    expect(validateRut('')).toBe(false);
  });

});