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

export const getUsers = () => {
  const storedUsers = sessionStorage.getItem(USERS_KEY);
  if (storedUsers === null) {
    sessionStorage.setItem(USERS_KEY, JSON.stringify([demoUser]));
    return [demoUser];
  }

  const users = JSON.parse(storedUsers);
  if (!Array.isArray(users)) {
    throw new Error('Los usuarios guardados en esta sesión tienen un formato inválido.');
  }

  return users;
};

export const getActiveUser = () => {
  const storedUser = sessionStorage.getItem(ACTIVE_USER_KEY);
  return storedUser ? JSON.parse(storedUser) : null;
};

export const setActiveUser = (user) => {
  sessionStorage.setItem(ACTIVE_USER_KEY, JSON.stringify(user));
};

export const clearActiveUser = () => {
  sessionStorage.removeItem(ACTIVE_USER_KEY);
};

export const registerUser = (user) => {
  const users = getUsers();
  const emailExists = users.some(
    (registeredUser) => registeredUser.correo.toLowerCase() === user.correo.toLowerCase()
  );

  if (emailExists) return { error: 'Este correo ya está registrado. Inicia sesión o usa otro.' };

  const updatedUsers = [...users, user];
  sessionStorage.setItem(USERS_KEY, JSON.stringify(updatedUsers));
  setActiveUser(user);
  return { user };
};

export const authenticateUser = (email, password) =>
  getUsers().find(
    (user) => user.correo.toLowerCase() === email.toLowerCase() && user.pass === password
  ) || null;
