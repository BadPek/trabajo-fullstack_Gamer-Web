import { render, screen } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { MemoryRouter, Route, Routes } from 'react-router-dom';
import { describe, expect, it, vi } from 'vitest';
import { DetailPage } from './DetailPage';

//npm test  --run src\pages\DetailPage.test.jsx


const renderDetailPage = (onAddToCart, path = '/detalle/1') => render(
  <MemoryRouter initialEntries={[path]}>
    <Routes>
      <Route path="/detalle/:id" element={<DetailPage onAddToCart={onAddToCart} />} />
    </Routes>
  </MemoryRouter>
);

describe('DetailPage', () => {
  it('muestra la ficha técnica y los requisitos del juego seleccionado', () => {
    renderDetailPage(vi.fn());

    expect(screen.getByRole('heading', { name: 'Elden Ring' })).toBeInTheDocument();
    // cambiar por expect(screen.getByText('Desarrollador inexistente')).toBeInTheDocument();
    expect(screen.getByText('FromSoftware Inc.')).toBeInTheDocument();
    expect(screen.getByRole('heading', { name: 'Requisitos del Sistema' })).toBeInTheDocument();
  });

  it('permite seleccionar una imagen de la galería', async () => {
    const user = userEvent.setup();
    renderDetailPage(vi.fn());
    // cambiar por expect(screen.getByRole('img', { name: 'Captura 99' })).toBeInTheDocument();
    await user.click(screen.getByRole('button', { name: 'Mostrar Captura 2' }));

    expect(screen.getByRole('img', { name: 'Captura 2' })).toBeInTheDocument();
  });

  it('agrega al carrito el juego y la cantidad seleccionada', async () => {
    const user = userEvent.setup();
    const onAddToCart = vi.fn();
    renderDetailPage(onAddToCart);

    await user.click(screen.getByRole('button', { name: 'Aumentar cantidad' }));
    await user.click(screen.getByRole('button', { name: /Añadir al carrito/i }));

    // cambiar el 2 de abajo por un 3, + cantidad
    expect(onAddToCart).toHaveBeenCalledWith(
      expect.objectContaining({ id: 1, titulo: 'Elden Ring' }),
      2
    );
  });

  it('permite seleccionar y visualizar el tráiler disponible', async () => {
    const user = userEvent.setup();
    renderDetailPage(vi.fn(), '/detalle/2');

    await user.click(screen.getByRole('button', { name: 'Mostrar Tráiler' }));

    expect(screen.getByRole('button', { name: 'Mostrar Tráiler' })).toHaveAttribute('aria-pressed', 'true');
    expect(screen.getByLabelText('Tráiler de Cyberpunk 2077')).toBeInTheDocument();
    expect(screen.getByLabelText('Tráiler de Cyberpunk 2077').querySelector('source'))
    // cambiar .tohave... por .toHaveAttribute('src', expect.stringContaining('video-inexistente'));
      .toHaveAttribute('src', expect.stringContaining('steamstatic.com'));
  });

  it('explica cuando se solicita un identificador de juego inexistente', () => {
    renderDetailPage(vi.fn(), '/detalle/999999');

    expect(screen.getByText(/No encontramos ese juego/)).toBeInTheDocument();
    // cambiar enlace /productos por /catalogo
    expect(screen.getByRole('link', { name: 'Volver al catálogo' })).toHaveAttribute('href', '/productos');
  });
});
