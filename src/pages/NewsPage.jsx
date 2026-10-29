import { Badge } from '@/components/atoms/Badge/Badge';

export const NewsPage = () => (
  <>
    <header className="bg-purple-light py-5 border-bottom text-center rounded-4 mb-5">
      <h1 className="text-purple fw-bold display-5 mb-2">
        <i className="bi bi-newspaper me-2"></i>Noticias Gamer
      </h1>
      <p className="lead text-secondary page-intro mx-auto">
        Entérate de las últimas novedades, eventos y curiosidades del mundo de los videojuegos.
      </p>
    </header>

    <main>
      <section className="mb-5">
        <article className="card border-0 shadow-sm overflow-hidden rounded-4">
          <div className="row g-0 align-items-center">
            <div className="col-md-6">
              <img
                src="/src/img/baldurs_gate.jpg"
                className="news-feature-image"
                alt="Mundo de fantasía de Baldur's Gate 3"
              />
            </div>
            <div className="col-md-6">
              <div className="card-body p-4 p-lg-5">
                <Badge variant="purple" className="mb-3 px-3 py-2">DESTACADO</Badge>
                <h2 className="text-purple fw-bold mb-3">El mundo gamer sigue creciendo</h2>
                <p className="text-secondary leading-relaxed mb-3">
                  Los videojuegos continúan reuniendo a millones de jugadores alrededor del mundo, con nuevos lanzamientos, competencias internacionales y experiencias cada año más inmersivas.
                </p>
                <p className="text-secondary leading-relaxed mb-4">
                  En GamerZone estaremos compartiendo las novedades más relevantes para que nunca te pierdas lo que está pasando en la industria.
                </p>
                <div className="text-muted small">
                  <i className="bi bi-calendar3 text-purple me-2"></i>29 de septiembre de 2026
                </div>
              </div>
            </div>
          </div>
        </article>
      </section>

      <section className="row g-4">
        <div className="col-12 col-md-6">
          <article className="card h-100 card-gamer p-4 border-0 shadow-sm">
            <Badge variant="purple-light" className="align-self-start mb-2">Comunidad</Badge>
            <h2 className="h4 text-purple fw-bold mb-2">Tendencias del Gaming en Chile</h2>
            <p className="text-secondary small flex-grow-1">
              Cada vez son más los jugadores locales que participan en torneos de eSports y coleccionan ediciones físicas de sus títulos predilectos.
            </p>
            <div className="text-muted small mt-3"><i className="bi bi-clock text-purple me-1"></i>Lectura de 3 minutos</div>
          </article>
        </div>
        <div className="col-12 col-md-6">
          <article className="card h-100 card-gamer p-4 border-0 shadow-sm">
            <Badge variant="purple-light" className="align-self-start mb-2">Hardware</Badge>
            <h2 className="h4 text-purple fw-bold mb-2">La importancia de un buen setup</h2>
            <p className="text-secondary small flex-grow-1">
              Desde mandos ergonómicos hasta audífonos con cancelación de ruido, equiparte bien marca la diferencia en tus sesiones competitivas.
            </p>
            <div className="text-muted small mt-3"><i className="bi bi-clock text-purple me-1"></i>Lectura de 4 minutos</div>
          </article>
        </div>
      </section>
    </main>
  </>
);
