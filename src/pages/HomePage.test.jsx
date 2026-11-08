import { act, cleanup, fireEvent, render, screen } from '@testing-library/react';
import { afterEach, describe, expect, it, vi } from 'vitest';
import { BrowserRouter } from 'react-router-dom';
import { juegosData } from '@/data/juegosData';
import { HomePage } from './HomePage';


//npm test --run src\pages\HomePage.test.jsx


describe('HomePage', () => {
  afterEach(() => {
    cleanup();
    sessionStorage.removeItem('gz_bienvenida_cupon_mostrada');
    vi.useRealTimers();
  });

  it('muestra ofertas navegables y los juegos destacados', () => {
    render(
      <BrowserRouter>
        <HomePage onAddToCart={vi.fn()} />
      </BrowserRouter>
    );

    expect(screen.queryByRole('dialog')).not.toBeInTheDocument();
    expect(screen.getByRole('heading', { name: 'Grandes Ofertas de Temporada' })).toBeInTheDocument();
    expect(screen.getByRole('button', { name: 'Mostrar oferta 1' })).toHaveAttribute('aria-current', 'true');
    expect(screen.getByRole('button', { name: 'Mostrar oferta 2' })).toHaveAttribute('aria-current', 'false');
    expect(screen.getByText('Destacados Gamer')).toBeInTheDocument();
    expect(screen.getByRole('list', { name: 'Catálogo de videojuegos' })).toBeInTheDocument();
    expect(screen.getAllByTestId(/^product-card-/)).toHaveLength(juegosData.length);
    // añadir
    // expect(screen.getByRole('heading', { name: 'Oferta inexistente' })).toBeInTheDocument();
  });

  it('muestra el cupón GAMER10 después de 800 ms y permite cerrarlo', () => {
    vi.useFakeTimers();
    render(
      <BrowserRouter>
        <HomePage onAddToCart={vi.fn()} />
      </BrowserRouter>
    );

    act(() => vi.advanceTimersByTime(800));

    expect(screen.getByRole('dialog', { name: 'Una oferta para tu próxima partida' })).toBeInTheDocument();
    expect(screen.getByLabelText('Cupón GAMER10')).toHaveTextContent('GAMER10');
    expect(sessionStorage.getItem('gz_bienvenida_cupon_mostrada')).toBe('true');

    fireEvent.click(screen.getByRole('button', { name: 'Cerrar promoción' }));
    expect(screen.queryByRole('dialog')).not.toBeInTheDocument();
  });

  it('no vuelve a mostrar el modal durante la misma sesión', () => {
    vi.useFakeTimers();
    const renderHome = () => render(
      <BrowserRouter>
        <HomePage onAddToCart={vi.fn()} />
      </BrowserRouter>
    );
    const firstRender = renderHome();

    act(() => vi.advanceTimersByTime(800));
    expect(screen.getByRole('dialog')).toBeInTheDocument();
    firstRender.unmount();

    renderHome();
    act(() => vi.advanceTimersByTime(800));

    expect(screen.queryByRole('dialog')).not.toBeInTheDocument();
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
