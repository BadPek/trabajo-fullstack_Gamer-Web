import { render, screen } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { MemoryRouter } from 'react-router-dom';
import { describe, expect, it, vi } from 'vitest';
import { Navbar } from './Navbar';

describe('Navbar', () => {
  it('muestra el menú del usuario autenticado y permite cerrar sesión', async () => {
    const user = userEvent.setup();
    const onLogout = vi.fn();
    render(
      <MemoryRouter>
        <Navbar
          cartCount={0}
          currentUser={{ nombre: 'Ana', correo: 'ana@gmail.com' }}
          onLogout={onLogout}
        />
      </MemoryRouter>
    );

    const menuButton = screen.getByRole('button', { name: 'Hola, Ana' });
    const logoutButton = screen.getByRole('button', { name: 'Cerrar sesión' });
    expect(menuButton).toHaveAttribute('aria-expanded', 'false');
    expect(logoutButton.parentElement).not.toHaveClass('show');

    await user.click(menuButton);
    expect(menuButton).toHaveAttribute('aria-expanded', 'true');
    expect(logoutButton.parentElement).toHaveClass('show');

    await user.keyboard('{Escape}');
    expect(menuButton).toHaveAttribute('aria-expanded', 'false');
    expect(menuButton).toHaveFocus();

    await user.click(menuButton);
    await user.click(logoutButton);
    expect(onLogout).toHaveBeenCalledOnce();
    expect(menuButton).toHaveAttribute('aria-expanded', 'false');
  });
});
