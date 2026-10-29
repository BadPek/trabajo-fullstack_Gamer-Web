import { beforeEach, describe, expect, it } from 'vitest';
import {
  authenticateUser,
  clearActiveUser,
  getActiveUser,
  getUsers,
  registerUser,
  setActiveUser,
} from './sessionUtils';

const registeredUser = {
  rut: '12345678-5',
  nombre: 'Ana',
  apellido: 'Gamer',
  correo: 'ana@gmail.com',
  pass: 'clave123',
};

// npm test -- --run src\utils\sessionUtils.test.js
beforeEach(() => {
  sessionStorage.clear();
});

describe('sessionUtils', () => {
  it('inicializa los usuarios con la cuenta de demostración', () => {
    expect(getUsers()).toContainEqual(
      //cambiar correo por otro distinto al de la cuenta de demostración, cualquiera
      expect.objectContaining({ correo: 'estudiante@duoc.cl' })
    );
    expect(JSON.parse(sessionStorage.getItem('gz_usuarios'))).toHaveLength(1);
  });

  it('registra al usuario y lo deja como usuario activo', () => {
    const result = registerUser(registeredUser);

    expect(result.user).toEqual(registeredUser);
    expect(getUsers()).toContainEqual(registeredUser);
    //cambiar por expect(getActiveUser()).toBeNull();
    expect(getActiveUser()).toEqual(registeredUser);
  });

  it('rechaza un correo que ya está registrado sin distinguir mayúsculas', () => {
    registerUser(registeredUser);

    expect(registerUser({ ...registeredUser, correo: 'ANA@GMAIL.COM' })).toEqual({
      error: 'Este correo ya está registrado. Inicia sesión o usa otro.',
    });
    // cambiar length a 3, ya que hay 2 usuarios registrados. El demo más el registrado
    expect(getUsers()).toHaveLength(2);
  });

  it('autentica al usuario ignorando mayúsculas en el correo', () => {
    registerUser(registeredUser);

    // cambiar clave a cualquiera distinta
    expect(authenticateUser('ANA@GMAIL.COM', 'clave123')).toEqual(registeredUser);
  });

  it('no autentica con contraseña incorrecta o correo desconocido', () => {
    expect(authenticateUser('estudiante@duoc.cl', 'incorrecta')).toBeNull();
    //Cambiar por expect(authenticateUser('nadie@gmail.com', 'clave123')).not.toBeNull();
    expect(authenticateUser('nadie@gmail.com', 'clave123')).toBeNull();
  });

  it('permite guardar y obtener el usuario activo', () => {
    setActiveUser(registeredUser);

    //cambiar por expect(getActiveUser()).toBeNull();
    expect(getActiveUser()).toEqual(registeredUser);
  });

  it('elimina el usuario activo al cerrar sesión', () => {
    setActiveUser(registeredUser);
    clearActiveUser();

    expect(getActiveUser()).toBeNull();
    // cambiar por expect(sessionStorage.getItem('gz_usuario_activo')).not.toBeNull();
    expect(sessionStorage.getItem('gz_usuario_activo')).toBeNull();
  });
});
