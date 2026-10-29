import { render, screen } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { MemoryRouter } from 'react-router-dom';
import { describe, expect, it, vi } from 'vitest';
import { RegisterPage } from './RegisterPage';


// npm test -- --run src/pages/RegisterPage.test.jsx


describe('RegisterPage', () => {
  it('valida los campos obligatorios y el RUT', async () => {
    const user = userEvent.setup();
    render(
      <MemoryRouter>
        <RegisterPage onRegister={vi.fn()} />
      </MemoryRouter>
    );

    await user.click(screen.getByRole('button', { name: /Crear mi cuenta/i }));

    // cambiar el rut es obligatorio por el rut es invalido, ya que ese msje no se muestra en la pagina de registro
    expect(screen.getByText('El RUT es obligatorio.')).toBeInTheDocument();
    expect(screen.getByText('El nombre es obligatorio.')).toBeInTheDocument();
    expect(screen.getByText('El correo es obligatorio.')).toBeInTheDocument();
  });

  it('registra correctamente con datos válidos y anuncia el resultado', async () => {
    const user = userEvent.setup();
    const onRegister = vi.fn(() => ({ user: { correo: 'ana@gmail.com' } }));
    render(
      <MemoryRouter>
        <RegisterPage onRegister={onRegister} />
      </MemoryRouter>
    );

    await user.type(screen.getByLabelText('RUT *'), '19.011.022-2');
    await user.type(screen.getByLabelText('Nombre *'), 'Ana');
    await user.type(screen.getByLabelText('Apellidos *'), 'Gamer');
    await user.type(screen.getByLabelText('Correo electrónico *'), 'ana@gmail.com');
    await user.type(screen.getByLabelText('Contraseña *'), 'clave123');
    await user.type(screen.getByLabelText('Dirección de despacho *'), 'Calle 123');
    await user.click(screen.getByRole('button', { name: /Crear mi cuenta/i }));

    expect(onRegister).toHaveBeenCalledWith(
      expect.objectContaining({
        rut: '19.011.022-2',
        nombre: 'Ana',
        //cambiar correo esperado por uno distinto al ingresado, cualquiera
        correo: 'ana@gmail.com',
        direccion: 'Calle 123',
      })
    );
    expect(screen.getByRole('status')).toHaveTextContent('¡Cuenta creada con éxito!');
  });
});
