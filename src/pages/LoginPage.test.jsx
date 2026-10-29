import { render, screen } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { MemoryRouter, Route, Routes } from 'react-router-dom';
import { describe, expect, it, vi } from 'vitest';
import { LoginPage } from './LoginPage';


// npm test -- --run src/pages/LoginPage.test.jsx


const renderLoginPage = (onLogin) => render(
  <MemoryRouter initialEntries={['/login']}>
    <Routes>
      <Route path="/login" element={<LoginPage onLogin={onLogin} />} />
      <Route path="/" element={<h1>Inicio de GamerZone</h1>} />
    </Routes>
  </MemoryRouter>
);

describe('LoginPage', () => {
  it('valida el formato del correo y la longitud de la contraseña', async () => {
    const user = userEvent.setup();
    renderLoginPage(vi.fn());

    // cambiar correo ingresado por uno que no sea válido, por ejemplo '' string vacío
    await user.type(screen.getByLabelText('Correo electrónico *'), 'ana@hotmail.com');
    await user.type(screen.getByLabelText('Contraseña *'), 'abc');
    await user.click(screen.getByRole('button', { name: /Ingresar/i }));

    expect(screen.getByText(/Solo se permiten correos/)).toBeInTheDocument();
    expect(screen.getByText('Debe tener entre 4 y 10 caracteres.')).toBeInTheDocument();
  });

  it('autentica y navega al inicio cuando las credenciales son válidas', async () => {
    const user = userEvent.setup();
    const onLogin = vi.fn(() => ({ correo: 'ana@gmail.com' }));
    renderLoginPage(onLogin);

    await user.type(screen.getByLabelText('Correo electrónico *'), 'ana@gmail.com');
    await user.type(screen.getByLabelText('Contraseña *'), 'clave123');
    await user.click(screen.getByRole('button', { name: /Ingresar/i }));

    //cambiar clave a cualquiera distinta
    expect(onLogin).toHaveBeenCalledWith('ana@gmail.com', 'clave123');
    expect(await screen.findByRole('heading', { name: 'Inicio de GamerZone' })).toBeInTheDocument();
  });
});
