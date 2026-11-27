import { render, screen } from '@testing-library/react';
import { describe, expect, it } from 'vitest';
import { Badge } from './Badge';

describe('Badge', () => {
  it('renderiza el contenido recibido', () => {
    render(<Badge>Disponible</Badge>);

    expect(screen.getByText('Disponible')).toBeInTheDocument();
  });

  it('aplica la variante y las clases adicionales', () => {
    render(<Badge variant="success" className="custom-badge">Éxito</Badge>);

    expect(screen.getByText('Éxito')).toHaveClass('bg-success-subtle', 'custom-badge');
  });
});
