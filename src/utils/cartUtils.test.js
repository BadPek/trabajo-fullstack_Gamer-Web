import { describe, it, expect } from 'vitest';
import { calculateSubtotal, applyCouponDiscount } from './cartUtils';

describe('cartUtils', () => {
  const mockItems = [
    { id: 1, precio: 59990, cantidad: 1 },
    { id: 2, precio: 49990, cantidad: 2 },
  ];

  it('calculateSubtotal debe calcular correctamente la suma de los productos con sus cantidades', () => {
    expect(calculateSubtotal(mockItems)).toBe(159970);
  });

  it('applyCouponDiscount debe aplicar un 10% de descuento cuando el cupón es GAMER10', () => {
    const { discountAmount, finalTotal } = applyCouponDiscount(100000, 'GAMER10');
    expect(discountAmount).toBe(10000);
    expect(finalTotal).toBe(90000);
  });

  it('applyCouponDiscount no debe aplicar descuento si el cupón es inválido', () => {
    const { discountAmount, finalTotal } = applyCouponDiscount(100000, 'INVALIDO');
    expect(discountAmount).toBe(0);
    expect(finalTotal).toBe(100000);
  });

});