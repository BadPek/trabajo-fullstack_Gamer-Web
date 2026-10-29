import { render, screen, within } from '@testing-library/react';
import { BrowserRouter } from 'react-router-dom';
import { describe, expect, it, vi } from 'vitest';
import { juegosData } from '@/data/juegosData';
import { CatalogPage } from './CatalogPage';

//npm test --run src\pages\CatalogPage.test.jsx
describe('CatalogPage', () => {
  it('muestra la cantidad total de juegos y las tarjetas del catálogo', () => {
    render(
      <BrowserRouter>
        <CatalogPage onAddToCart={vi.fn()} />
      </BrowserRouter>
    );

    const catalog = screen.getByRole('list', { name: 'Catálogo de videojuegos' });
    expect(screen.getByText(`${juegosData.length} Juegos Disponibles`)).toBeInTheDocument();
    //cambiar por expect(within(catalog).getAllByRole('listitem')).toHaveLength(juegosData.length + 1);
    expect(within(catalog).getAllByRole('listitem')).toHaveLength(juegosData.length);
  });

  it('presenta las categorías declaradas para los juegos', () => {
    render(
      <BrowserRouter>
        <CatalogPage onAddToCart={vi.fn()} />
      </BrowserRouter>
    );

    const catalog = screen.getByRole('list', { name: 'Catálogo de videojuegos' });
    [...new Set(juegosData.map((game) => game.categoria))].forEach((category) => {
      // cambiar por expect(within(catalog).getByText('Categoría inexistente')).toBeInTheDocument();
      expect(within(catalog).getAllByText(category).length).toBeGreaterThan(0);
    });
  });
});
