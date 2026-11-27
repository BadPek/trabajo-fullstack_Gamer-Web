export const SystemRequirements = ({ requisitos }) => {
  if (!requisitos) return null;

  const { minimos, recomendados } = requisitos;

  return (
    <section className="mb-5">
      <div className="requisitos-box p-4 p-md-5 shadow">
        <h3 className="fw-bold mb-4 pb-2 border-bottom border-secondary text-white">
          <i className="bi bi-cpu me-2 text-warning"></i> Requisitos del Sistema
        </h3>
        <div className="row g-4">
          <div className="col-12 col-md-6 border-end border-secondary border-opacity-50">
            <h5 className="fw-bold text-uppercase mb-3 text-warning">Mínimo:</h5>
            <ul className="list-unstyled mb-0 lh-lg text-light small">
              <li><strong>SO:</strong> {minimos.so}</li>
              <li><strong>PROCESADOR:</strong> {minimos.procesador}</li>
              <li><strong>MEMORIA:</strong> {minimos.memoria}</li>
              <li><strong>GRÁFICOS:</strong> {minimos.graficos}</li>
              {minimos.directx && <li><strong>DIRECTX:</strong> {minimos.directx}</li>}
              <li><strong>ALMACENAMIENTO:</strong> {minimos.almacenamiento}</li>
            </ul>
          </div>
          <div className="col-12 col-md-6 ps-md-4">
            <h5 className="fw-bold text-uppercase mb-3 text-warning">Recomendado:</h5>
            <ul className="list-unstyled mb-0 lh-lg text-light small">
              <li><strong>SO:</strong> {recomendados.so}</li>
              <li><strong>PROCESADOR:</strong> {recomendados.procesador}</li>
              <li><strong>MEMORIA:</strong> {recomendados.memoria}</li>
              <li><strong>GRÁFICOS:</strong> {recomendados.graficos}</li>
              {recomendados.directx && <li><strong>DIRECTX:</strong> {recomendados.directx}</li>}
              <li><strong>ALMACENAMIENTO:</strong> {recomendados.almacenamiento}</li>
            </ul>
          </div>
        </div>
      </div>
    </section>
  );
};