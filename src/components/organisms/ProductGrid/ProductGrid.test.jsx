import { render, screen } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { BrowserRouter } from 'react-router-dom';
import { describe, expect, it, vi } from 'vitest';
import { ProductGrid } from './ProductGrid';

const products = [
  { id: 1, titulo: 'Juego Uno', categoria: 'Acción', precio: 1000, imagen: '/uno.jpg', descripcion_corta: 'Primero' },
  { id: 2, titulo: 'Juego Dos', categoria: 'Aventura', precio: 2000, imagen: '/dos.jpg', descripcion_corta: 'Segundo' },
];

describe('ProductGrid', () => {
  it('muestra el mensaje configurado cuando no hay productos', () => {
    render(<ProductGrid products={[]} emptyMessage="Catálogo temporalmente vacío" />);

    expect(screen.getByText('Catálogo temporalmente vacío')).toBeInTheDocument();
  });

  it('renderiza una tarjeta por producto', () => {
    render(
      <BrowserRouter>
        <ProductGrid products={products} onAddToCart={vi.fn()} />
      </BrowserRouter>
    );

    expect(screen.getByTestId('product-card-1')).toBeInTheDocument();
    expect(screen.getByTestId('product-card-2')).toBeInTheDocument();
  });

  it('pasa el producto elegido al callback de agregar al carrito', async () => {
    const user = userEvent.setup();
    const onAddToCart = vi.fn();
    render(
      <BrowserRouter>
        <ProductGrid products={products} onAddToCart={onAddToCart} />
      </BrowserRouter>
    );

    await user.click(screen.getAllByRole('button', { name: /Agregar al Carrito/i })[1]);

    expect(onAddToCart).toHaveBeenCalledWith({
      id: 2,
      titulo: 'Juego Dos',
      precio: 2000,
      imagen: '/dos.jpg',
    });
  });
});
