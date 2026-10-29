import { render, screen } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { BrowserRouter } from 'react-router-dom';
import { describe, it, expect, vi } from 'vitest';
import { ProductCard } from './ProductCard';

const mockProduct = {
  id: 1,
  titulo: 'Elden Ring',
  categoria: 'Action RPG',
  precio: 59990,
  imagen: 'elden_ring.jpg',
  descripcionCorta: 'Un épico juego de rol de acción.',
};

describe('ProductCard Component', () => {
  it('debe mostrar el título, la categoría y el precio formateado en CLP', () => {
    render(
      <BrowserRouter>
        <ProductCard {...mockProduct} onAddToCart={() => {}} />
      </BrowserRouter>
    );

    expect(screen.getByText('Elden Ring')).toBeInTheDocument();
    expect(screen.getByText('Action RPG')).toBeInTheDocument();
    expect(screen.getByText('$59.990')).toBeInTheDocument();
  });

  it('debe ejecutar la función onAddToCart con los datos del producto al hacer clic', async () => {
    const handleAddToCart = vi.fn();
    const user = userEvent.setup();

    render(
      <BrowserRouter>
        <ProductCard {...mockProduct} onAddToCart={handleAddToCart} />
      </BrowserRouter>
    );

    const btn = screen.getByRole('button', { name: /agregar al carrito/i });
    await user.click(btn);

    expect(handleAddToCart).toHaveBeenCalledWith({
      id: 1,
      titulo: 'Elden Ring',
      precio: 59990,
      imagen: 'elden_ring.jpg',
    });
  });
});