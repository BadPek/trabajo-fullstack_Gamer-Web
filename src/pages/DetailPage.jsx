import { useEffect, useState } from 'react';
import { Link, useParams } from 'react-router-dom';
import { juegosData } from '@/data/juegosData';
import { SystemRequirements } from '@/components/organisms/SystemRequirements/SystemRequirements';
import { Button } from '@/components/atoms/Button/Button';
import { Badge } from '@/components/atoms/Badge/Badge';
import { Input } from '@/components/atoms/Input/Input';
import { getImageUrl } from '@/utils/imageUtils';

export const DetailPage = ({ onAddToCart }) => {
  const { id } = useParams();
  const juego = juegosData.find((item) => item.id === Number(id));
  const [quantity, setQuantity] = useState(1);
  const [selectedMedia, setSelectedMedia] = useState(null);
  const [added, setAdded] = useState(false);

  useEffect(() => {
    setQuantity(1);
    setSelectedMedia(null);
    setAdded(false);
  }, [id]);

  if (!juego) {
    return (
      <div className="alert alert-warning">
        No encontramos ese juego. <Link to="/productos">Volver al catálogo</Link>.
      </div>
    );
  }

  const gallery = juego.galeria?.length ? juego.galeria : [juego.imagen];
  const trailer = juego.video === 'src/videos/elden.webm' ? null : juego.video;
  const mediaItems = [
    ...(trailer ? [{ type: 'video', src: trailer, label: 'Tráiler' }] : []),
    ...gallery.map((src, index) => ({ type: 'image', src, label: `Captura ${index + 1}` })),
  ];
  const currentMedia = selectedMedia || mediaItems[trailer ? 1 : 0];

  const handleAddToCart = () => {
    onAddToCart(juego, quantity);
    setAdded(true);
    window.setTimeout(() => setAdded(false), 1800);
  };

  return (
    <>
      <div className="d-flex justify-content-between align-items-center mb-3">
        <nav aria-label="breadcrumb">
          <ol className="breadcrumb mb-0">
            <li className="breadcrumb-item"><Link to="/" className="text-purple">Inicio</Link></li>
            <li className="breadcrumb-item"><Link to="/productos" className="text-purple">Juegos</Link></li>
            <li className="breadcrumb-item active" aria-current="page">{juego.titulo}</li>
          </ol>
        </nav>
        <Link to="/productos" className="btn btn-outline-purple btn-sm">
          <i className="bi bi-arrow-left me-1"></i> Catálogo
        </Link>
      </div>

      <div className="mb-4">
        <h1 className="display-6 fw-bold text-dark mb-1">{juego.titulo}</h1>
        <div className="d-flex align-items-center gap-2 flex-wrap">
          <span className="text-warning" aria-label="Valoración: cinco estrellas">
            {[...Array(5)].map((_, index) => <i key={index} className="bi bi-star-fill"></i>)}
          </span>
          <span className="fw-bold text-purple">4.9 / 5.0</span>
          <span className="text-muted small">| Reseñas generales: <strong>Extremadamente positivas</strong></span>
        </div>
      </div>

      <div className="row g-4 mb-5">
        <div className="col-12 col-lg-7">
          <div className="detail-media-viewer mb-3">
            {currentMedia.type === 'video' ? (
              <video
                key={currentMedia.src}
                className="w-100 h-100"
                controls
                poster={getImageUrl(juego.imagen)}
                aria-label={`Tráiler de ${juego.titulo}`}
              >
                <source src={getImageUrl(currentMedia.src)} />
                Tu navegador no soporta la reproducción de video.
              </video>
            ) : (
              <img src={getImageUrl(currentMedia.src)} alt={currentMedia.label} className="w-100 h-100" />
            )}
          </div>
          <div className="row g-2" aria-label="Galería del juego">
            {mediaItems.map((media) => (
              <div key={`${media.type}-${media.src}`} className="col-3 col-sm-2">
                <button
                  type="button"
                  className={`media-thumbnail ${currentMedia.src === media.src && currentMedia.type === media.type ? 'active' : ''}`}
                  onClick={() => setSelectedMedia(media)}
                  aria-label={`Mostrar ${media.label}`}
                  aria-pressed={currentMedia.src === media.src && currentMedia.type === media.type}
                >
                  {media.type === 'video' ? (
                    <span className="thumbnail-trailer"><i className="bi bi-play-circle-fill fs-3 text-warning"></i><small>TRÁILER</small></span>
                  ) : (
                    <img src={getImageUrl(media.src)} alt="" />
                  )}
                </button>
              </div>
            ))}
          </div>
        </div>

        <div className="col-12 col-lg-5">
          <div className="card border-0 shadow-sm p-4 rounded-4 bg-purple-light h-100 d-flex flex-column justify-content-between">
            <div>
              <Badge variant="purple" className="px-3 py-2 fs-6 mb-3">{juego.categoria}</Badge>
              <p className="text-secondary small mb-4">{juego.descripcion_corta}</p>
              <div className="bg-white p-3 rounded-3 border mb-4">
                <span className="text-muted small d-block">Precio Final (IVA incluido):</span>
                <div className="d-flex align-items-baseline gap-2">
                  <span className="display-5 fw-bold text-purple">${juego.precio.toLocaleString('es-CL')}</span>
                  <Badge variant="success">Clave Digital</Badge>
                </div>
              </div>
              <div className="mb-4">
                <label className="form-label small fw-semibold text-secondary mb-1" htmlFor="detail-quantity">Cantidad de licencias</label>
                <div className="input-group detail-quantity-control">
                  <button className="btn btn-outline-purple" type="button" onClick={() => setQuantity((value) => Math.max(1, value - 1))} aria-label="Restar cantidad">-</button>
                  <Input id="detail-quantity" type="number" className="text-center fw-bold" value={quantity} readOnly />
                  <button className="btn btn-outline-purple" type="button" onClick={() => setQuantity((value) => Math.min(5, value + 1))} aria-label="Aumentar cantidad" disabled={quantity >= 5}>+</button>
                </div>
              </div>
            </div>
            <Button variant={added ? 'success' : 'purple'} size="lg" onClick={handleAddToCart}>
              {added ? <><i className="bi bi-check-circle-fill me-2"></i>¡Añadido al carrito!</> : <><i className="bi bi-cart-plus me-2"></i>Añadir al carrito</>}
            </Button>
          </div>
        </div>
      </div>

      <div className="row g-4 mb-5">
        <div className="col-12 col-lg-8">
          <div className="card border-0 shadow-sm p-4 p-md-5 rounded-4 bg-white h-100">
            <h2 className="h3 text-purple fw-bold mb-4 pb-2 border-bottom border-purple">
              <i className="bi bi-info-circle me-2"></i>Acerca de este juego
            </h2>
            <div className="text-secondary leading-relaxed" dangerouslySetInnerHTML={{ __html: juego.acerca_de }} />
          </div>
        </div>
        <div className="col-12 col-lg-4">
          <div className="card border-0 shadow-sm p-4 rounded-4 bg-purple-light h-100">
            <h2 className="h6 text-purple fw-bold mb-3 text-uppercase">Ficha técnica</h2>
            <dl className="row mb-0 small text-secondary">
              <dt className="col-5">Desarrollador:</dt><dd className="col-7 text-dark fw-semibold">{juego.detalles?.desarrollador || '-'}</dd>
              <dt className="col-5">Editor:</dt><dd className="col-7 text-dark fw-semibold">{juego.detalles?.editor || '-'}</dd>
              <dt className="col-5">Lanzamiento:</dt><dd className="col-7 text-dark fw-semibold">{juego.detalles?.fecha || '-'}</dd>
              <dt className="col-5">Modos:</dt><dd className="col-7 text-dark fw-semibold">{juego.detalles?.modos || '-'}</dd>
              <dt className="col-5">Idiomas:</dt><dd className="col-7 text-dark fw-semibold">{juego.detalles?.idiomas || '-'}</dd>
            </dl>
          </div>
        </div>
      </div>

      <SystemRequirements requisitos={juego.requisitos} />
    </>
  );
};
