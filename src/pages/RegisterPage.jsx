import { useState } from 'react';
import { Link } from 'react-router-dom';
import { getEmailSuggestions, isAllowedEmail } from '@/utils/emailUtils';
import { validateRut } from '@/utils/rutUtils';
import { Input } from '@/components/atoms/Input/Input';
import { FormField } from '@/components/molecules/FormField/FormField';

const initialForm = {
  rut: '',
  nombre: '',
  apellido: '',
  correo: '',
  pass: '',
  region: '',
  comuna: '',
  direccion: '',
};

const comunasPorRegion = {
  metropolitana: ['Santiago Centro', 'Providencia', 'Las Condes', 'Maipú', 'Puente Alto', 'Ñuñoa'],
  valparaiso: ['Viña del Mar', 'Valparaíso', 'Quilpué', 'Villa Alemana', 'Concón'],
  biobio: ['Concepción', 'Talcahuano', 'San Pedro de la Paz', 'Chiguayante', 'Los Ángeles'],
};

export const RegisterPage = ({ onRegister }) => {
  const [form, setForm] = useState(initialForm);
  const [errors, setErrors] = useState({});
  const [success, setSuccess] = useState(false);

  const updateField = (field, value) => {
    setForm((current) => ({ ...current, [field]: value, ...(field === 'region' ? { comuna: '' } : {}) }));
    setErrors((current) => ({ ...current, [field]: '' }));
    setSuccess(false);
  };

  const handleSubmit = (event) => {
    event.preventDefault();
    const nextErrors = {};
    const rut = form.rut.trim();
    const nombre = form.nombre.trim();
    const apellido = form.apellido.trim();
    const correo = form.correo.trim();
    const direccion = form.direccion.trim();

    if (!rut) nextErrors.rut = 'El RUT es obligatorio.';
    else if (!validateRut(rut)) nextErrors.rut = 'RUT inválido. Verifica el dígito verificador (Ej: 19011022-2).';

    if (!nombre) nextErrors.nombre = 'El nombre es obligatorio.';
    else if (nombre.length > 50) nextErrors.nombre = 'Máximo 50 caracteres.';

    if (!apellido) nextErrors.apellido = 'Los apellidos son obligatorios.';
    else if (apellido.length > 100) nextErrors.apellido = 'Máximo 100 caracteres.';

    if (!correo) nextErrors.correo = 'El correo es obligatorio.';
    else if (correo.length > 100) nextErrors.correo = 'Máximo 100 caracteres.';
    else if (!isAllowedEmail(correo)) nextErrors.correo = 'Solo se permiten correos @duoc.cl, @profesor.duoc.cl o @gmail.com.';

    if (!form.pass) nextErrors.pass = 'La contraseña es obligatoria.';
    else if (form.pass.length < 4 || form.pass.length > 10) nextErrors.pass = 'Debe tener entre 4 y 10 caracteres.';

    if (!direccion) nextErrors.direccion = 'La dirección es obligatoria.';
    else if (direccion.length > 300) nextErrors.direccion = 'Máximo 300 caracteres.';

    setErrors(nextErrors);
    if (Object.keys(nextErrors).length > 0) return;

    const result = onRegister({ ...form, rut, nombre, apellido, correo, direccion });
    if (result.error) {
      setErrors({ correo: result.error });
      return;
    }

    setSuccess(true);
    setForm(initialForm);
  };

  return (
    <div className="row justify-content-center">
      <div className="col-12 col-md-10 col-lg-8">
        <div className="card border-0 shadow-sm p-4 p-md-5 rounded-4">
          <div className="text-center mb-4">
            <div className="fs-1 text-purple mb-2"><i className="bi bi-person-badge"></i></div>
            <h1 className="h2 text-purple fw-bold mb-1">Registro de Nuevo Usuario</h1>
            <p className="text-secondary small">Crea tu cuenta para comprar en GamerZone.</p>
          </div>

          {success && (
            <div className="alert alert-success" role="status">
              ¡Cuenta creada con éxito! Has iniciado sesión automáticamente.
            </div>
          )}

          <form onSubmit={handleSubmit} noValidate>
            <FormField
              id="register-rut"
              label="RUT"
              required
              error={errors.rut}
              hint="Con o sin puntos y guion. Validación Módulo 11."
              control={<Input type="text" placeholder="Ej: 19011022-2 o 19.011.022-2" maxLength={12} value={form.rut} onChange={(event) => updateField('rut', event.target.value)} required />}
            />

            <div className="row g-3 mb-3">
              <div className="col-12 col-md-6">
                <FormField
                  id="register-name"
                  label="Nombre"
                  required
                  error={errors.nombre}
                  className=""
                  control={<Input maxLength={50} value={form.nombre} onChange={(event) => updateField('nombre', event.target.value)} required />}
                />
              </div>
              <div className="col-12 col-md-6">
                <FormField
                  id="register-surname"
                  label="Apellidos"
                  required
                  error={errors.apellido}
                  className=""
                  control={<Input maxLength={100} value={form.apellido} onChange={(event) => updateField('apellido', event.target.value)} required />}
                />
              </div>
            </div>

            <FormField
              id="register-email"
              label="Correo electrónico"
              required
              error={errors.correo}
              hint="Dominios permitidos: @duoc.cl, @profesor.duoc.cl y @gmail.com"
              control={(
                <Input
                  type="email"
                  list="register-email-suggestions"
                  autoComplete="email"
                  maxLength={100}
                  placeholder="nombre@duoc.cl o @gmail.com"
                  value={form.correo}
                  onChange={(event) => updateField('correo', event.target.value)}
                  required
                />
              )}
            >
              <datalist id="register-email-suggestions">
                {getEmailSuggestions(form.correo).map((suggestion) => <option value={suggestion} key={suggestion} />)}
              </datalist>
            </FormField>
            <FormField
              id="register-password"
              label="Contraseña"
              required
              error={errors.pass}
              control={<Input type="password" minLength={4} maxLength={10} autoComplete="new-password" value={form.pass} onChange={(event) => updateField('pass', event.target.value)} required />}
            />

            <div className="row g-3 mb-3">
              <div className="col-12 col-md-6">
                <FormField
                  id="register-region"
                  label="Región"
                  className=""
                  control={(
                    <Input as="select" value={form.region} onChange={(event) => updateField('region', event.target.value)}>
                      <option value="">Selecciona una región</option>
                      <option value="metropolitana">Región Metropolitana</option>
                      <option value="valparaiso">Región de Valparaíso</option>
                      <option value="biobio">Región del Biobío</option>
                    </Input>
                  )}
                />
              </div>
              <div className="col-12 col-md-6">
                <FormField
                  id="register-commune"
                  label="Comuna"
                  className=""
                  control={(
                    <Input as="select" value={form.comuna} onChange={(event) => updateField('comuna', event.target.value)} disabled={!form.region}>
                      <option value="">{form.region ? 'Selecciona una comuna' : 'Primero elige una región'}</option>
                      {(comunasPorRegion[form.region] || []).map((comuna) => <option value={comuna} key={comuna}>{comuna}</option>)}
                    </Input>
                  )}
                />
              </div>
            </div>

            <FormField
              id="register-address"
              label="Dirección de despacho"
              required
              className="mb-4"
              error={errors.direccion}
              control={<Input maxLength={300} placeholder="Ej: Av. Providencia 1234, Depto 402" value={form.direccion} onChange={(event) => updateField('direccion', event.target.value)} required />}
            />

            <button type="submit" className="btn btn-purple btn-lg w-100 fw-bold shadow-sm mb-3">
              <i className="bi bi-check-circle me-1"></i>Crear mi cuenta
            </button>
            <div className="text-center small">
              <span className="text-muted">¿Ya tienes cuenta? </span>
              <Link to="/login" className="text-purple fw-bold text-decoration-none">Inicia sesión</Link>
            </div>
          </form>
        </div>
      </div>
    </div>
  );
};