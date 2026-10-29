import { act, cleanup, fireEvent, render, screen } from '@testing-library/react';
import { afterEach, describe, expect, it, vi } from 'vitest';
import { BrowserRouter } from 'react-router-dom';
import { juegosData } from '@/data/juegosData';
import { HomePage } from './HomePage';


//npm test --run src\pages\HomePage.test.jsx


describe('HomePage', () => {
  afterEach(() => {
    cleanup();
    vi.useRealTimers();
  });

  it('muestra ofertas navegables y los juegos destacados', () => {
    render(
      <BrowserRouter>
        <HomePage onAddToCart={vi.fn()} />
      </BrowserRouter>
    );

    expect(screen.getByRole('heading', { name: 'Grandes Ofertas de Temporada' })).toBeInTheDocument();
    expect(screen.getByRole('button', { name: 'Mostrar oferta 1' })).toHaveAttribute('aria-current', 'true');
    expect(screen.getByRole('button', { name: 'Mostrar oferta 2' })).toHaveAttribute('aria-current', 'false');
    expect(screen.getByText('Destacados Gamer')).toBeInTheDocument();
    expect(screen.getByRole('list', { name: 'Catálogo de videojuegos' })).toBeInTheDocument();
    expect(screen.getAllByTestId(/^product-card-/)).toHaveLength(juegosData.length);
    // añadir
    // expect(screen.getByRole('heading', { name: 'Oferta inexistente' })).toBeInTheDocument();
  });

  it('cambia la oferta con los controles y los indicadores', () => {
    render(
      <BrowserRouter>
        <HomePage onAddToCart={vi.fn()} />
      </BrowserRouter>
    );

    fireEvent.click(screen.getByRole('button', { name: 'Oferta siguiente' }));
    // cambiar por expect(screen.getByRole('heading', { name: 'Grandes Ofertas de Temporada' })).toBeInTheDocument();
    expect(screen.getByRole('heading', { name: 'Títulos Galardonados y Accesorios Pro' })).toBeInTheDocument();
    expect(screen.getByRole('button', { name: 'Mostrar oferta 2' })).toHaveAttribute('aria-current', 'true');

    fireEvent.click(screen.getByRole('button', { name: 'Oferta anterior' }));
    expect(screen.getByRole('heading', { name: 'Grandes Ofertas de Temporada' })).toBeInTheDocument();

    fireEvent.click(screen.getByRole('button', { name: 'Mostrar oferta 2' }));
    expect(screen.getByRole('heading', { name: 'Títulos Galardonados y Accesorios Pro' })).toBeInTheDocument();
  });

  it('avanza automáticamente a la oferta siguiente después de seis segundos', () => {
    vi.useFakeTimers();
    render(
      <BrowserRouter>
        <HomePage onAddToCart={vi.fn()} />
      </BrowserRouter>
    );

    act(() => vi.advanceTimersByTime(6000));

    // cambiar por expect(screen.getByRole('heading', { name: 'Grandes Ofertas de Temporada' })).toBeInTheDocument();
    expect(screen.getByRole('heading', { name: 'Títulos Galardonados y Accesorios Pro' })).toBeInTheDocument();
  });
});
