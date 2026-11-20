import { useSyncExternalStore } from 'react';

const listeners = new Set();
let warning = '';

const notifyListeners = () => {
  listeners.forEach((listener) => listener());
};

const reportStorageWarning = (message) => {
  warning = message;
  notifyListeners();
};

export const subscribeToStorageWarning = (listener) => {
  listeners.add(listener);
  return () => listeners.delete(listener);
};

export const useStorageWarning = () =>
  useSyncExternalStore(subscribeToStorageWarning, () => warning, () => '');

export const getStorageWarning = () => warning;

export const dismissStorageWarning = () => {
  warning = '';
  notifyListeners();
};

export const readSessionValue = (key) => {
  try {
    return window.sessionStorage.getItem(key);
  } catch {
    reportStorageWarning('El almacenamiento de esta pestaña no está disponible. Los cambios no persistirán al recargar.');
    return null;
  }
};

export const writeSessionValue = (key, value) => {
  try {
    window.sessionStorage.setItem(key, value);
    return true;
  } catch {
    reportStorageWarning('No se pudieron guardar los cambios en esta pestaña. Seguirán disponibles solo mientras no recargues.');
    return false;
  }
};

export const removeSessionValue = (key) => {
  try {
    window.sessionStorage.removeItem(key);
    return true;
  } catch {
    reportStorageWarning('No se pudo limpiar el almacenamiento de esta pestaña.');
    return false;
  }
};

export const readSessionJson = (key, { fallback, isValid, label, onMissing, onInvalid }) => {
  const storedValue = readSessionValue(key);
  if (storedValue === null) {
    onMissing?.(fallback);
    return fallback;
  }

  try {
    const parsedValue = JSON.parse(storedValue);
    if (isValid(parsedValue)) return parsedValue;
  } catch {
    removeSessionValue(key);
    reportStorageWarning(`Los datos guardados para ${label} estaban dañados y se restablecieron.`);
    onInvalid?.(fallback);
    return fallback;
  }

  removeSessionValue(key);
  reportStorageWarning(`Los datos guardados para ${label} tenían un formato inválido y se restablecieron.`);
  onInvalid?.(fallback);
  return fallback;
};

export const readValidatedSessionValue = (key, { fallback, isValid, label }) => {
  const storedValue = readSessionValue(key);
  if (storedValue === null) return fallback;
  if (isValid(storedValue)) return storedValue;

  removeSessionValue(key);
  reportStorageWarning(`El valor guardado para ${label} no es válido y se descartó.`);
  return fallback;
};
