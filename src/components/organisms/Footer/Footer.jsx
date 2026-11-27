import { Link } from 'react-router-dom';

export const Footer = () => {
  return (
    <footer className="py-5 mt-auto">
      <div className="container">
        <div className="row g-4">
          <div className="col-12 col-md-4">
            <h5 className="text-purple fw-bold mb-3">
              <i className="bi bi-controller text-warning"></i> GamerZone
            </h5>
            <p className="small text-muted">
              Tu tienda de confianza en videojuegos, consolas y accesorios de colección.
            </p>
          </div>
          <div className="col-6 col-md-4">
            <h6 className="text-purple fw-bold mb-3">Navegación</h6>
            <ul className="list-unstyled small">
              <li className="mb-2"><Link to="/">Inicio</Link></li>
              <li className="mb-2"><Link to="/productos">Nuestros Juegos</Link></li>
              <li className="mb-2"><Link to="/nosotros">Sobre Nosotros</Link></li>
              <li className="mb-2"><Link to="/noticias">Noticias Gamer</Link></li>
              <li className="mb-2"><Link to="/contacto">Contacto</Link></li>
            </ul>
          </div>
          <div className="col-6 col-md-4">
            <h6 className="text-purple fw-bold mb-3">Síguenos</h6>
            <div className="d-flex gap-3 fs-5 mb-3">
              <a href="https://instagram.com" target="_blank" rel="noreferrer" className="text-purple"><i className="bi bi-instagram"></i></a>
              <a href="https://discord.com" target="_blank" rel="noreferrer" className="text-purple"><i className="bi bi-discord"></i></a>
              <a href="https://youtube.com" target="_blank" rel="noreferrer" className="text-purple"><i className="bi bi-youtube"></i></a>
            </div>
            <p className="small text-muted mb-0">Contacto: soporte@gamerzone.cl</p>
          </div>
        </div>
        <div className="text-center pt-4 mt-4 border-top">
          <small className="text-muted">&copy; 2026 GamerZone Store - Duoc UC. Todos los derechos reservados.</small>
        </div>
      </div>
    </footer>
  );
};