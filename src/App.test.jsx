import { fireEvent, render, screen } from '@testing-library/react';
import { afterEach, describe, expect, it } from 'vitest';
import { App } from './App';
import { dismissStorageWarning } from '@/utils/sessionStorageUtils';

describe('App', () => {
  afterEach(() => {
    sessionStorage.clear();
    dismissStorageWarning();
  });

  it('informa que se restableció el carrito guardado con datos dañados', () => {
    sessionStorage.setItem('gz_carrito', '{');

    render(<App />);

    expect(screen.getByRole('alert')).toHaveTextContent(
      'Los datos guardados para el carrito estaban dañados y se restablecieron.'
    );
    expect(sessionStorage.getItem('gz_carrito')).toBe('[]');
  });

  it('permite cerrar el aviso de almacenamiento', async () => {
    sessionStorage.setItem('gz_carrito', '{');
    render(<App />);

    fireEvent.click(screen.getByRole('button', { name: 'Cerrar aviso de almacenamiento' }));

    expect(screen.queryByRole('alert')).not.toBeInTheDocument();
  });
});
