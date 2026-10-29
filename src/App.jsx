import { useState } from 'react';
import { BrowserRouter, Routes, Route } from 'react-router-dom';
import { MainLayout } from '@/components/templates/MainLayout/MainLayout';
import { HomePage } from '@/pages/HomePage';
import { CatalogPage } from '@/pages/CatalogPage';
import { DetailPage } from '@/pages/DetailPage';
import { CartPage } from '@/pages/CartPage';
import { ContactPage } from '@/pages/ContactPage';
import { RegisterPage } from '@/pages/RegisterPage';
import { AboutPage } from '@/pages/AboutPage';
import { NewsPage } from '@/pages/NewsPage';
import { LoginPage } from '@/pages/LoginPage';
import { useCart } from '@/hooks/useCart';
import {
  authenticateUser,
  clearActiveUser,
  getActiveUser,
  registerUser,
  setActiveUser,
} from '@/utils/sessionUtils';

export function App() {
  const cart = useCart();
  const [currentUser, setCurrentUser] = useState(getActiveUser);

  const handleLogin = (email, password) => {
    const user = authenticateUser(email, password);
    if (!user) return null;
    setActiveUser(user);
    setCurrentUser(user);
    return user;
  };

  const handleRegister = (user) => {
    const result = registerUser(user);
    if (result.user) setCurrentUser(result.user);
    return result;
  };

  const handleLogout = () => {
    clearActiveUser();
    setCurrentUser(null);
  };

  return (
    <BrowserRouter>
      <MainLayout cartCount={cart.totalCount} currentUser={currentUser} onLogout={handleLogout}>
        <Routes>
          <Route path="/" element={<HomePage onAddToCart={cart.addToCart} />} />
          <Route path="/productos" element={<CatalogPage onAddToCart={cart.addToCart} />} />
          <Route path="/detalle/:id" element={<DetailPage onAddToCart={cart.addToCart} />} />
          <Route
            path="/carrito"
            element={(
              <CartPage
                cartItems={cart.cartItems}
                coupon={cart.coupon}
                currentUser={currentUser}
                onApplyCoupon={cart.applyCoupon}
                onClearCart={cart.clearCart}
                onClearCoupon={cart.clearCoupon}
                onRemoveItem={cart.removeFromCart}
                onUpdateQuantity={cart.updateQuantity}
                onCheckout={cart.clearCart}
              />
            )}
          />
          <Route path="/contacto" element={<ContactPage />} />
          <Route path="/registro" element={<RegisterPage onRegister={handleRegister} />} />
          <Route path="/login" element={<LoginPage onLogin={handleLogin} />} />
          <Route path="/noticias" element={<NewsPage />} />
          <Route path="/nosotros" element={<AboutPage />} />
          <Route path="*" element={<div className="alert alert-warning">Página no encontrada.</div>} />
        </Routes>
      </MainLayout>
    </BrowserRouter>
  );
}

export default App;