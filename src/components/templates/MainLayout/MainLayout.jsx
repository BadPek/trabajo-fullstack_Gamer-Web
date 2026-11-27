import { Navbar } from '@/components/organisms/Navbar/Navbar';
import { Footer } from '@/components/organisms/Footer/Footer';

export const MainLayout = ({ children, cartCount, currentUser, onLogout }) => {
  return (
    <div className="d-flex flex-column min-vh-100 bg-white text-dark">
      <Navbar cartCount={cartCount} currentUser={currentUser} onLogout={onLogout} />
      <main className="container my-5 flex-grow-1">{children}</main>
      <Footer />
    </div>
  );
};