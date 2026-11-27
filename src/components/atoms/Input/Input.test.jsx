import { render, screen } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { describe, expect, it } from 'vitest';
import { Input } from './Input';

describe('Input', () => {
  it('renderiza un input estándar con clase de control', () => {
    render(<Input aria-label="Correo" />);

    expect(screen.getByRole('textbox', { name: 'Correo' })).toHaveClass('form-control');
  });

  it('permite renderizar un área de texto', () => {
    render(<Input as="textarea" aria-label="Mensaje" />);

    expect(screen.getByRole('textbox', { name: 'Mensaje' })).toBeInTheDocument();
  });

  it('aplica estado inválido y acepta escritura', async () => {
    const user = userEvent.setup();
    render(<Input aria-label="Nombre" isInvalid />);
    const input = screen.getByRole('textbox', { name: 'Nombre' });

    await user.type(input, 'Ana');

    expect(input).toHaveClass('is-invalid');
    expect(input).toHaveValue('Ana');
  });
});
