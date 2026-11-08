import { beforeEach, describe, expect, it, vi } from 'vitest';
import {
  dismissStorageWarning,
  getStorageWarning,
  readSessionJson,
  readSessionValue,
  readValidatedSessionValue,
  writeSessionValue,
} from './sessionStorageUtils';

beforeEach(() => {
  vi.restoreAllMocks();
  sessionStorage.clear();
  dismissStorageWarning();
});

describe('sessionStorageUtils', () => {
  it('descarta y reporta JSON dañado en lugar de interrumpir la aplicación', () => {
    sessionStorage.setItem('gz_test', '{');

    expect(readSessionJson('gz_test', {
      fallback: [],
      isValid: Array.isArray,
      label: 'los datos de prueba',
    })).toEqual([]);
    expect(sessionStorage.getItem('gz_test')).toBeNull();
    expect(getStorageWarning()).toMatch(/datos guardados para los datos de prueba estaban dañados/);
  });

  it('descarta y reporta datos con una estructura incorrecta', () => {
    sessionStorage.setItem('gz_test', JSON.stringify({ value: 'no es una lista' }));

    expect(readSessionJson('gz_test', {
      fallback: [],
      isValid: Array.isArray,
      label: 'los datos de prueba',
    })).toEqual([]);
    expect(sessionStorage.getItem('gz_test')).toBeNull();
    expect(getStorageWarning()).toMatch(/formato inválido/);
  });

  it('usa el valor inicial y muestra un aviso cuando el almacenamiento no se puede leer', () => {
    vi.spyOn(Object.getPrototypeOf(window.sessionStorage), 'getItem').mockImplementation(() => {
      throw new DOMException('Storage access denied', 'SecurityError');
    });

    expect(readSessionValue('gz_test')).toBeNull();
    expect(getStorageWarning()).toMatch(/no está disponible/);
  });

  it('muestra un aviso cuando no se pueden guardar los cambios', () => {
    vi.spyOn(Object.getPrototypeOf(window.sessionStorage), 'setItem').mockImplementation(() => {
      throw new DOMException('Quota exceeded', 'QuotaExceededError');
    });

    expect(writeSessionValue('gz_test', 'value')).toBe(false);
    expect(getStorageWarning()).toMatch(/No se pudieron guardar/);
  });

  it('descarta y reporta valores inválidos guardados sin serialización JSON', () => {
    sessionStorage.setItem('gz_test', 'CUPON_INVALIDO');

    expect(readValidatedSessionValue('gz_test', {
      fallback: '',
      isValid: (value) => value === 'GAMER10',
      label: 'el cupón',
    })).toBe('');
    expect(sessionStorage.getItem('gz_test')).toBeNull();
    expect(getStorageWarning()).toMatch(/valor guardado para el cupón no es válido/);
  });
});
