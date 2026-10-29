import { render, screen } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { afterEach, describe, expect, it, vi } from 'vitest';
import { ContactPage } from './ContactPage';


// npm test -- --run src/pages/ContactPage.test.jsx
describe('ContactPage', () => {
  afterEach(() => {
    vi.restoreAllMocks();
  });

  it('muestra errores al enviar el formulario vacío', async () => {
    const user = userEvent.setup();
    render(<ContactPage />);

    await user.click(screen.getByRole('button', { name: /Enviar mensaje/i }));

    // modificar valores en el getbytext
    expect(screen.getByText('Debes ingresar tu nombre.')).toBeInTheDocument();
    expect(screen.getByText('Debes ingresar tu correo.')).toBeInTheDocument();
    expect(screen.getByText('Debes ingresar un asunto.')).toBeInTheDocument();
    expect(screen.getByText('Debes escribir un mensaje.')).toBeInTheDocument();
  });

  it('rechaza dominios de correo que no están permitidos', async () => {
    const user = userEvent.setup();
    render(<ContactPage />);

    await user.type(screen.getByLabelText('Nombre completo *'), 'Ana Gamer');
    // cambiar dominio de correo por ejemplo @gmail.com
    await user.type(screen.getByLabelText('Correo electrónico *'), 'ana@hotmail.com');
    await user.type(screen.getByLabelText('Asunto *'), 'Consulta');
    await user.type(screen.getByLabelText('Mensaje *'), 'Necesito ayuda');
    await user.click(screen.getByRole('button', { name: /Enviar mensaje/i }));

    expect(screen.getByText(/Solo se permiten correos/)).toBeInTheDocument();
  });

  it('confirma el envío válido y limpia los campos', async () => {
    const user = userEvent.setup();
    const alert = vi.spyOn(window, 'alert').mockImplementation(() => {});
    render(<ContactPage />);

    await user.type(screen.getByLabelText('Nombre completo *'), 'Ana Gamer');
    await user.type(screen.getByLabelText('Correo electrónico *'), 'ana@gmail.com');
    await user.type(screen.getByLabelText('Asunto *'), 'Consulta');
    await user.type(screen.getByLabelText('Mensaje *'), 'Necesito ayuda');
    await user.click(screen.getByRole('button', { name: /Enviar mensaje/i }));

    expect(alert).toHaveBeenCalledWith(
      // cambiar mensaje esperado por cualquier cosa
      '¡Mensaje enviado con éxito! Nos pondremos en contacto contigo pronto.'
    );
    expect(screen.getByLabelText('Nombre completo *')).toHaveValue('');
  });
});
