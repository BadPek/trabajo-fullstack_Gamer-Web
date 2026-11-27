import { render, screen } from '@testing-library/react';
import { describe, expect, it } from 'vitest';
import { FormField } from './FormField';
import { Input } from '@/components/atoms/Input/Input';

describe('FormField', () => {
  it('asocia la etiqueta con el control y agrega indicación de requerido', () => {
    render(
      <FormField id="email" label="Correo" required control={<Input type="email" />} />
    );

    expect(screen.getByLabelText('Correo *')).toHaveAttribute('id', 'email');
  });

  it('vincula el mensaje de error con el control y lo marca como inválido', () => {
    render(
      <FormField id="name" label="Nombre" error="Campo obligatorio" control={<Input />} />
    );
    const input = screen.getByLabelText('Nombre');

    expect(input).toHaveAttribute('aria-invalid', 'true');
    expect(input).toHaveAttribute('aria-describedby', 'name-error');
    expect(screen.getByText('Campo obligatorio')).toHaveAttribute('id', 'name-error');
  });

  it('muestra la ayuda y la agrega a aria-describedby', () => {
    render(
      <FormField id="password" label="Clave" hint="Entre 4 y 10 caracteres" control={<Input />} />
    );

    expect(screen.getByLabelText('Clave')).toHaveAttribute('aria-describedby', 'password-hint');
    expect(screen.getByText('Entre 4 y 10 caracteres')).toBeInTheDocument();
  });
});
