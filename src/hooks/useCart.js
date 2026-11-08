import { useEffect, useState } from 'react';
import { isValidCoupon } from '@/utils/cartUtils';
import { juegosData } from '@/data/juegosData';
import {
  readSessionJson,
  readValidatedSessionValue,
  removeSessionValue,
  writeSessionValue,
} from '@/utils/sessionStorageUtils';

const CART_KEY = 'gz_carrito';
const COUPON_KEY = 'gz_cupon';

const isStoredCart = (cart) =>
  Array.isArray(cart) && cart.every((item) =>
    item
    && typeof item.id === 'number'
    && typeof item.titulo === 'string'
    && typeof item.precio === 'number'
    && Number.isFinite(item.precio)
    && Number.isInteger(item.cantidad)
    && item.cantidad > 0
  );

const readStoredCart = () => {
  const cart = readSessionJson(CART_KEY, {
    fallback: [],
    isValid: isStoredCart,
    label: 'el carrito',
  });
  return cart.map((item) => ({
    ...item,
    categoria: item.categoria || juegosData.find((game) => game.id === item.id)?.categoria || 'Videojuego',
  }));
};

export const useCart = () => {
  const [cartItems, setCartItems] = useState(readStoredCart);
  const [coupon, setCoupon] = useState(() => readValidatedSessionValue(COUPON_KEY, {
    fallback: '',
    isValid: isValidCoupon,
    label: 'el cupón',
  }));

  useEffect(() => {
    writeSessionValue(CART_KEY, JSON.stringify(cartItems));
  }, [cartItems]);

  useEffect(() => {
    if (coupon) writeSessionValue(COUPON_KEY, coupon);
    else removeSessionValue(COUPON_KEY);
  }, [coupon]);

  const addToCart = (product, quantity = 1) => {
    const category = product.categoria || juegosData.find((game) => game.id === product.id)?.categoria;
    const cartProduct = { ...product, categoria: category || 'Videojuego' };

    setCartItems((currentItems) => {
      const existingItem = currentItems.find((item) => item.id === cartProduct.id);
      if (existingItem) {
        return currentItems.map((item) =>
          item.id === cartProduct.id ? { ...item, cantidad: item.cantidad + quantity } : item
        );
      }

      return [...currentItems, { ...cartProduct, cantidad: quantity }];
    });
  };

  const updateQuantity = (id, change) => {
    setCartItems((currentItems) =>
      currentItems
        .map((item) => (item.id === id ? { ...item, cantidad: item.cantidad + change } : item))
        .filter((item) => item.cantidad > 0)
    );
  };

  const removeFromCart = (id) => {
    setCartItems((currentItems) => currentItems.filter((item) => item.id !== id));
  };

  const clearCart = () => {
    setCartItems([]);
    setCoupon('');
  };

  const applyCoupon = (code) => {
    const normalizedCode = code.trim().toUpperCase();
    if (!isValidCoupon(normalizedCode)) return false;
    setCoupon(normalizedCode);
    return true;
  };

  const totalCount = cartItems.reduce((total, item) => total + item.cantidad, 0);

  return {
    cartItems,
    coupon,
    totalCount,
    addToCart,
    updateQuantity,
    removeFromCart,
    clearCart,
    applyCoupon,
    clearCoupon: () => setCoupon(''),
  };
};
