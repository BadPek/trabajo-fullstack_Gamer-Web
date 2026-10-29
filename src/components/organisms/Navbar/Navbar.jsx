import { Link, NavLink } from 'react-router-dom';
import { useState } from 'react';
import { Badge } from '@/components/atoms/Badge/Badge';

export const Navbar = ({ cartCount = 0, currentUser, onLogout }) => {
  const [menuOpen, setMenuOpen] = useState(false);
  const closeMenu = () => setMenuOpen(false);

  return (
    <nav className="navbar navbar-expand-lg navbar-dark navbar-purple sticky-top">
      <div className="container">
        <Link className="navbar-brand fw-bold fs-4 d-flex align-items-center" to="/">
          <i className="bi bi-controller me-2 fs-3 text-warning"></i> GamerZone
        </Link>
        <button
          className="navbar-toggler border-0"
          type="button"
          aria-controls="navbarNav"
          aria-expanded={menuOpen}
          aria-label={menuOpen ? 'Cerrar menú' : 'Abrir menú'}
          onClick={() => setMenuOpen((isOpen) => !isOpen)}
        >
          <span className="navbar-toggler-icon" />
        </button>
        <div className={`collapse navbar-collapse ${menuOpen ? 'show' : ''}`} id="navbarNav">
          <ul className="navbar-nav me-auto mb-2 mb-lg-0">
            <li className="nav-item">
              <NavLink end className={({ isActive }) => `nav-link ${isActive ? 'active' : ''}`} to="/" onClick={closeMenu}>
                <i className="bi bi-house-door me-1"></i> Inicio
              </NavLink>
            </li>
            <li className="nav-item">
              <NavLink className={({ isActive }) => `nav-link ${isActive ? 'active' : ''}`} to="/productos" onClick={closeMenu}>
                <i className="bi bi-grid me-1"></i> Juegos
              </NavLink>
            </li>
            <li className="nav-item">
              <NavLink className={({ isActive }) => `nav-link ${isActive ? 'active' : ''}`} to="/nosotros" onClick={closeMenu}>
                <i className="bi bi-people me-1"></i> Nosotros
              </NavLink>
            </li>
            <li className="nav-item">
              <NavLink className={({ isActive }) => `nav-link ${isActive ? 'active' : ''}`} to="/noticias" onClick={closeMenu}>
                <i className="bi bi-newspaper me-1"></i> Noticias
              </NavLink>
            </li>
            <li className="nav-item">
              <NavLink className={({ isActive }) => `nav-link ${isActive ? 'active' : ''}`} to="/contacto" onClick={closeMenu}>
                <i className="bi bi-envelope me-1"></i> Contacto
              </NavLink>
            </li>
          </ul>
          <div className="d-flex gap-2 align-items-center flex-wrap">
            {currentUser ? (
              <>
                <span className="text-white small">Hola, {currentUser.nombre}</span>
                <button className="btn btn-outline-light btn-sm" type="button" onClick={() => { onLogout(); closeMenu(); }}>
                  Cerrar sesión
                </button>
              </>
            ) : (
              <>
                <Link to="/login" className="btn btn-outline-light btn-sm px-3" onClick={closeMenu}>
                  Iniciar sesión
                </Link>
                <Link to="/registro" className="btn btn-light btn-sm text-purple fw-bold px-3" onClick={closeMenu}>
                  Registrarse
                </Link>
              </>
            )}
            <Link to="/carrito" className="btn btn-warning btn-sm d-flex align-items-center ms-lg-2 fw-bold" onClick={closeMenu}>
              <i className="bi bi-cart3 me-1"></i> Carrito
              <Badge variant="dark" className="ms-2">{cartCount}</Badge>
            </Link>
          </div>
        </div>
      </div>
    </nav>
  );
};