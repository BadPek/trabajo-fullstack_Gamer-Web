import { Link } from 'react-router-dom';
import { Badge } from '@/components/atoms/Badge/Badge';

export const AboutPage = () => (
  <>
    <header className="bg-purple-light py-5 border-bottom text-center rounded-4 mb-5">
      <div className="container">
        <h1 className="text-purple fw-bold display-5 mb-2">
          <i className="bi bi-controller text-warning me-2"></i>Sobre GamerZone
        </h1>
        <p className="lead text-secondary mx-auto page-intro">
          Somos una tienda dedicada a los amantes de los videojuegos. En GamerZone buscamos ofrecer juegos, accesorios y productos de colección para todo tipo de jugadores.
        </p>
      </div>
    </header>

    <main>
      <section className="row align-items-center mb-5 g-5">
        <div className="col-lg-6">
          <Badge variant="purple-light" className="px-3 py-2 mb-2 fw-semibold">Nuestra Historia</Badge>
          <h2 className="text-purple fw-bold mb-3">¿Quiénes somos?</h2>
          <p className="text-secondary leading-relaxed">
            GamerZone nace como un espacio pensado para reunir diferentes productos relacionados con el mundo gamer en un solo lugar.
          </p>
          <p className="text-secondary leading-relaxed">
            Nuestro objetivo es entregar una experiencia sencilla y cómoda, permitiendo encontrar videojuegos, accesorios y artículos de colección de manera rápida.
          </p>
          <Link to="/productos" className="btn btn-purple fw-bold mt-2">
            <i className="bi bi-bag-fill me-1"></i>Ver productos
          </Link>
        </div>
        <div className="col-lg-6 text-center">
          <div className="bg-purple-light border border-purple rounded-4 p-5 shadow-sm">
            <i className="bi bi-joystick text-purple about-joystick"></i>
            <h3 className="mt-3 fw-bold text-purple">Tu Mundo Gamer</h3>
            <p className="text-secondary mb-0">Juegos, accesorios, figuras y mucho más.</p>
          </div>
        </div>
      </section>

      <section className="mt-5">
        <h2 className="text-center text-purple fw-bold mb-4">¿Qué buscamos ofrecer?</h2>
        <div className="row g-4 text-center">
          {[
            { icon: 'bi-controller', title: 'Variedad', text: 'Diferentes videojuegos, accesorios y productos para todo tipo de jugadores.' },
            { icon: 'bi-lightning-charge', title: 'Rapidez', text: 'Procesos simples e intuitivos para que encuentres lo que buscas sin complicaciones.' },
            { icon: 'bi-shield-check', title: 'Confianza', text: 'Atención personalizada y artículos garantizados para tu tranquilidad.' },
          ].map((value) => (
            <div className="col-12 col-md-4" key={value.title}>
              <article className="card h-100 card-gamer p-4 border-0 shadow-sm">
                <i className={`bi ${value.icon} text-purple display-4`}></i>
                <h3 className="h4 mt-3 text-purple fw-bold">{value.title}</h3>
                <p className="text-secondary mb-0">{value.text}</p>
              </article>
            </div>
          ))}
        </div>
      </section>
    </main>
  </>
);
