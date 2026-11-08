import { useEffect, useRef, useState } from 'react';
import { Link } from 'react-router-dom';
import { ProductGrid } from '@/components/organisms/ProductGrid/ProductGrid';
import { juegosData } from '@/data/juegosData';

const offers = [
  {
    image: '/src/img/elden_ring.jpg',
    title: 'Grandes Ofertas de Temporada',
    text: 'Encuentra tus videojuegos favoritos y disfruta de nuestras promociones.',
  },
  {
    image: '/src/img/cyberpunk2077.jpeg',
    title: 'Títulos Galardonados y Accesorios Pro',
    text: 'Lo mejor de la industria gamer en un solo lugar.',
  },
];

const WELCOME_COUPON_KEY = 'gz_bienvenida_cupon_mostrada';

export const HomePage = ({ onAddToCart }) => {
  const [activeOffer, setActiveOffer] = useState(0);
  const [welcomeOpen, setWelcomeOpen] = useState(false);
  const welcomeDialogRef = useRef(null);
  const welcomeCloseRef = useRef(null);

  useEffect(() => {
    if (sessionStorage.getItem(WELCOME_COUPON_KEY)) return undefined;

    const timer = window.setTimeout(() => {
      sessionStorage.setItem(WELCOME_COUPON_KEY, 'true');
      setWelcomeOpen(true);
    }, 800);

    return () => window.clearTimeout(timer);
  }, []);

  useEffect(() => {
    const timer = window.setInterval(
      () => setActiveOffer((current) => (current + 1) % offers.length),
      6000
    );
    return () => window.clearInterval(timer);
  }, []);

  useEffect(() => {
    if (!welcomeOpen) return undefined;

    const previouslyFocused = document.activeElement;
    welcomeCloseRef.current?.focus();

    const handleDialogKeyDown = (event) => {
      if (event.key === 'Escape') {
        setWelcomeOpen(false);
        return;
      }

      if (event.key !== 'Tab') return;

      const focusableElements = welcomeDialogRef.current?.querySelectorAll(
        'button:not([disabled]), a[href]'
      );
      if (!focusableElements?.length) {
        event.preventDefault();
        welcomeDialogRef.current?.focus();
        return;
      }

      const firstElement = focusableElements[0];
      const lastElement = focusableElements[focusableElements.length - 1];
      if (event.shiftKey && document.activeElement === firstElement) {
        event.preventDefault();
        lastElement.focus();
      } else if (!event.shiftKey && document.activeElement === lastElement) {
        event.preventDefault();
        firstElement.focus();
      }
    };

    document.addEventListener('keydown', handleDialogKeyDown);
    return () => {
      document.removeEventListener('keydown', handleDialogKeyDown);
      if (previouslyFocused instanceof HTMLElement && previouslyFocused.isConnected) {
        previouslyFocused.focus();
      }
    };
  }, [welcomeOpen]);

  const showOffer = (index) => setActiveOffer((index + offers.length) % offers.length);

  return (
    <>
      {welcomeOpen && (
        <div className="welcome-modal-backdrop">
          <div
            ref={welcomeDialogRef}
            className="welcome-modal"
            role="dialog"
            aria-modal="true"
            aria-labelledby="welcome-modal-title"
            aria-describedby="welcome-modal-description"
            tabIndex={-1}
          >
            <button
              ref={welcomeCloseRef}
              className="welcome-modal-close"
              type="button"
              aria-label="Cerrar promoción"
              onClick={() => setWelcomeOpen(false)}
            >
              <i className="bi bi-x-lg" aria-hidden="true"></i>
            </button>
            <p className="text-uppercase fw-bold text-purple mb-2">¡Bienvenido a GamerZone!</p>
            <h2 id="welcome-modal-title" className="h3 fw-bold mb-3">Una oferta para tu próxima partida</h2>
            <p id="welcome-modal-description" className="text-secondary">
              Usa este cupón en tu carrito y obtén un 10% de descuento.
            </p>
            <p className="welcome-modal-coupon" aria-label="Cupón GAMER10">
              GAMER10
            </p>
            <Link
              to="/productos"
              className="btn btn-purple fw-bold"
              onClick={() => setWelcomeOpen(false)}
            >
              Explorar catálogo
            </Link>
          </div>
        </div>
      )}

      <header className="mb-5">
        <div id="offersCarousel" className="offers-carousel shadow">
          {offers.map((offer, index) => (
            <div
              key={offer.title}
              className={`offer-slide ${activeOffer === index ? 'active' : ''}`}
              aria-hidden={activeOffer !== index}
            >
              <img src={offer.image} alt="" className="offer-background" />
              <div className="offer-caption">
                <h1 className="display-6 fw-bold mb-2">{offer.title}</h1>
                <p className="lead mb-3">{offer.text}</p>
                <Link to="/productos" className="btn btn-warning fw-bold px-4">Ver catálogo</Link>
              </div>
            </div>
          ))}
          <button className="offer-control previous" type="button" onClick={() => showOffer(activeOffer - 1)} aria-label="Oferta anterior">
            <i className="bi bi-chevron-left"></i>
          </button>
          <button className="offer-control next" type="button" onClick={() => showOffer(activeOffer + 1)} aria-label="Oferta siguiente">
            <i className="bi bi-chevron-right"></i>
          </button>
          <div className="offer-indicators">
            {offers.map((offer, index) => (
              <button
                key={offer.title}
                type="button"
                className={activeOffer === index ? 'active' : ''}
                aria-label={`Mostrar oferta ${index + 1}`}
                aria-current={activeOffer === index}
                onClick={() => showOffer(index)}
              />
            ))}
          </div>
        </div>
      </header>

      <section>
        <div className="d-flex justify-content-between align-items-center mb-4 pb-2 border-bottom border-purple">
          <h2 className="text-purple fw-bold mb-0">
            <i className="bi bi-fire text-danger me-2"></i> Destacados Gamer
          </h2>
          <Link to="/productos" className="btn btn-outline-purple btn-sm">
            Ver Catálogo Completo <i className="bi bi-arrow-right"></i>
          </Link>
        </div>

        <ProductGrid
          products={juegosData}
          onAddToCart={onAddToCart}
          columnClassName="col-12 col-sm-6 col-lg-3"
        />
      </section>
    </>
  );
};
