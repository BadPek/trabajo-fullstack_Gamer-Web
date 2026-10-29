import { render, screen } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { describe, it, expect, vi } from 'vitest';
import { Button } from './Button';

describe('Button Component', () => {
  it('renderiza el texto o elementos hijos pasados', () => {
    render(<Button>Agregar al Carrito</Button>);
    expect(screen.getByRole('button', { name: /agregar al carrito/i })).toBeInTheDocument();
  });

  it('llama a la función onClick cuando el usuario hace clic', async () => {
    const handleClick = vi.fn();
    const user = userEvent.setup();

    render(<Button onClick={handleClick}>Enviar</Button>);
    await user.click(screen.getByRole('button'));

    expect(handleClick).toHaveBeenCalledTimes(1);
  });

});