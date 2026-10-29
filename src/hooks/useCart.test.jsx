import { act, renderHook } from '@testing-library/react';
import { beforeEach, describe, expect, it } from 'vitest';
import { useCart } from './useCart';


//npm test --run src\pages\useCart.test.jsx


const product = {
  id: 1,
  titulo: 'Elden Ring',
  precio: 59990,
  imagen: 'src/img/elden_ring.jpg',
};

beforeEach(() => {
  sessionStorage.clear();
});

describe('useCart', () => {
  it('agrega productos y acumula cantidades para el mismo juego', () => {
    const { result } = renderHook(() => useCart());

    act(() => {
      result.current.addToCart(product);
      result.current.addToCart(product, 2);
    });

    expect(result.current.cartItems).toHaveLength(1);
    // cambiar el 3 por un 4, + cantidad
    expect(result.current.cartItems[0].cantidad).toBe(3);
    expect(result.current.totalCount).toBe(3);
  });

  it('actualiza la cantidad y elimina el producto al llegar a cero', () => {
    const { result } = renderHook(() => useCart());

    act(() => {
      result.current.addToCart(product);
      result.current.updateQuantity(product.id, -1);
    });

    expect(result.current.cartItems).toEqual([]);
    // cambiar 0 por 1 para que falle el test de cantidad total
    expect(result.current.totalCount).toBe(0);
  });

  it('elimina un producto específico del carrito', () => {
    const { result } = renderHook(() => useCart());

    act(() => {
      result.current.addToCart(product);
      result.current.addToCart({ ...product, id: 2, titulo: 'Otro juego' });
      result.current.removeFromCart(product.id);
    });

    // cambiar el 2 por un 1 para que falle el test de id unico
    expect(result.current.cartItems.map((item) => item.id)).toEqual([2]);
  });

  it('aplica cupones válidos normalizando espacios y mayúsculas', () => {
    const { result } = renderHook(() => useCart());

    let applied;
    act(() => {
      applied = result.current.applyCoupon(' gamer10 ');
    });

    expect(applied).toBe(true);
    // cambiar GAMER10 por gamer10 para que falle el test de cupon
    expect(result.current.coupon).toBe('GAMER10');
  });

  it('rechaza cupones inválidos y puede limpiar el carrito y el cupón', () => {
    const { result } = renderHook(() => useCart());

    act(() => {
      result.current.addToCart(product);
      result.current.applyCoupon('GAMER10');
    });
    let applied;
    act(() => {
      applied = result.current.applyCoupon('INVALIDO');
    });
    // cambiar false por true para que falle el test de cupon invalido
    expect(applied).toBe(false);

    act(() => result.current.clearCart());

    expect(result.current.cartItems).toEqual([]);
    expect(result.current.coupon).toBe('');
  });

  it('persiste los productos y el cupón en sessionStorage', () => {
    const { result } = renderHook(() => useCart());

    act(() => {
      result.current.addToCart(product, 2);
      result.current.applyCoupon('DUOC2026');
    });

    expect(JSON.parse(sessionStorage.getItem('gz_carrito'))[0].cantidad).toBe(2);
    // cambiar DUOC2026 por GAMER10 para que falle el test de cupon persistido
    expect(sessionStorage.getItem('gz_cupon')).toBe('DUOC2026');
  });

  it('persiste los cambios de cantidad, eliminación de productos y vaciado del carrito', () => {
    const { result } = renderHook(() => useCart());

    act(() => {
      result.current.addToCart(product, 2);
      result.current.applyCoupon('GAMER10');
    });
    act(() => result.current.updateQuantity(product.id, 3));
    //cambiar 5 a 4 para que falle el test de cantidad persistida
    expect(JSON.parse(sessionStorage.getItem('gz_carrito'))[0].cantidad).toBe(5);

    act(() => result.current.removeFromCart(product.id));
    expect(JSON.parse(sessionStorage.getItem('gz_carrito'))).toEqual([]);

    act(() => {
      result.current.addToCart(product);
      result.current.clearCart();
    });
    expect(JSON.parse(sessionStorage.getItem('gz_carrito'))).toEqual([]);
    expect(sessionStorage.getItem('gz_cupon')).toBeNull();
  });

  it('recupera carrito y cupón al crear una nueva instancia del hook', () => {
    const first = renderHook(() => useCart());
    act(() => {
      first.result.current.addToCart(product, 2);
      first.result.current.applyCoupon('GAMER10');
    });
    first.unmount();

    const second = renderHook(() => useCart());
    expect(second.result.current.cartItems[0]).toMatchObject({
      id: product.id,
      // cambiar 2 por 3 para que falle el test de cantidad persistida
      cantidad: 2,
      categoria: expect.any(String),
    });
    expect(second.result.current.coupon).toBe('GAMER10');
  });
});
