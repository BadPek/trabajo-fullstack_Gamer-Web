import {
  readSessionJson,
  removeSessionValue,
  writeSessionValue,
} from '@/utils/sessionStorageUtils';

const USERS_KEY = 'gz_usuarios';
const ACTIVE_USER_KEY = 'gz_usuario_activo';

const demoUser = {
  rut: '19011022-2',
  nombre: 'Estudiante',
  apellido: 'Duoc',
  correo: 'estudiante@duoc.cl',
  pass: '1234',
  region: 'metropolitana',
  comuna: 'Santiago Centro',
  direccion: 'Av. España 8',
};

const isStoredUser = (user) =>
  user
  && typeof user === 'object'
  && typeof user.nombre === 'string'
  && typeof user.correo === 'string'
  && typeof user.pass === 'string';

const isStoredUsers = (users) => Array.isArray(users) && users.every(isStoredUser);

export const getUsers = () => {
  return readSessionJson(USERS_KEY, {
    fallback: [demoUser],
    isValid: isStoredUsers,
    label: 'los usuarios',
    onMissing: (users) => writeSessionValue(USERS_KEY, JSON.stringify(users)),
    onInvalid: (users) => writeSessionValue(USERS_KEY, JSON.stringify(users)),
  });
};

export const getActiveUser = () => {
  return readSessionJson(ACTIVE_USER_KEY, {
    fallback: null,
    isValid: isStoredUser,
    label: 'la sesión de usuario',
  });
};

export const setActiveUser = (user) => {
  writeSessionValue(ACTIVE_USER_KEY, JSON.stringify(user));
};

export const clearActiveUser = () => {
  removeSessionValue(ACTIVE_USER_KEY);
};

export const registerUser = (user) => {
  const users = getUsers();
  const emailExists = users.some(
    (registeredUser) => registeredUser.correo.toLowerCase() === user.correo.toLowerCase()
  );

  if (emailExists) return { error: 'Este correo ya está registrado. Inicia sesión o usa otro.' };

  const updatedUsers = [...users, user];
  writeSessionValue(USERS_KEY, JSON.stringify(updatedUsers));
  setActiveUser(user);
  return { user };
};

export const authenticateUser = (email, password) =>
  getUsers().find(
    (user) => user.correo.toLowerCase() === email.toLowerCase() && user.pass === password
  ) || null;
