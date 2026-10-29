import { useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { getEmailSuggestions, isAllowedEmail } from '@/utils/emailUtils';
import { Input } from '@/components/atoms/Input/Input';
import { FormField } from '@/components/molecules/FormField/FormField';

export const LoginPage = ({ onLogin }) => {
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [errors, setErrors] = useState({});
  const navigate = useNavigate();

  const handleSubmit = (event) => {
    event.preventDefault();
    const nextErrors = {};

    if (!email.trim()) nextErrors.email = 'El correo es obligatorio.';
    else if (email.length > 100) nextErrors.email = 'El correo no puede superar los 100 caracteres.';
    else if (!isAllowedEmail(email.trim())) nextErrors.email = 'Solo se permiten correos @duoc.cl, @profesor.duoc.cl o @gmail.com.';

    if (!password) nextErrors.password = 'La contraseña es obligatoria.';
    else if (password.length < 4 || password.length > 10) nextErrors.password = 'Debe tener entre 4 y 10 caracteres.';

    setErrors(nextErrors);
    if (Object.keys(nextErrors).length > 0) return;

    const result = onLogin(email.trim(), password);
    if (!result) {
      setErrors({
        email: 'Correo o contraseña incorrectos. Verifica tus datos o crea una cuenta.',
        password: 'Verifica tu contraseña.',
      });
      return;
    }

    navigate('/', { replace: true });
  };

  return (
    <div className="row justify-content-center">
      <div className="col-12 col-sm-10 col-md-8 col-lg-5">
        <div className="card border-0 shadow-sm p-4 p-md-5 rounded-4">
          <div className="text-center mb-4">
            <div className="fs-1 text-purple mb-2"><i className="bi bi-person-lock"></i></div>
            <h1 className="h2 text-purple fw-bold mb-1">Iniciar sesión</h1>
            <p className="text-secondary small">Ingresa tus credenciales para acceder a tu cuenta.</p>
          </div>

          <form onSubmit={handleSubmit} noValidate>
            <FormField
              id="login-email"
              label="Correo electrónico"
              required
              error={errors.email}
              hint="Dominios permitidos: @duoc.cl, @profesor.duoc.cl y @gmail.com"
              control={(
                <Input
                  type="email"
                  list="login-email-suggestions"
                  autoComplete="email"
                  maxLength={100}
                  placeholder="nombre@duoc.cl o @gmail.com"
                  value={email}
                  onChange={(event) => setEmail(event.target.value)}
                  required
                />
              )}
            >
              <datalist id="login-email-suggestions">
                {getEmailSuggestions(email).map((suggestion) => <option value={suggestion} key={suggestion} />)}
              </datalist>
            </FormField>
            <FormField
              id="login-password"
              label="Contraseña"
              required
              error={errors.password}
              control={<Input type="password" minLength={4} maxLength={10} autoComplete="current-password" value={password} onChange={(event) => setPassword(event.target.value)} required />}
            />

            <div className="alert alert-light border small text-muted mb-3 p-2 rounded-3">
              <i className="bi bi-info-circle text-purple me-1"></i>
              Cuenta de prueba: <strong>estudiante@duoc.cl</strong> (clave: <strong>1234</strong>).
            </div>
            <button type="submit" className="btn btn-purple btn-lg w-100 fw-bold shadow-sm mb-3">
              <i className="bi bi-box-arrow-in-right me-1"></i>Ingresar
            </button>
            <div className="text-center small">
              <span className="text-muted">¿No tienes una cuenta? </span>
              <Link to="/registro" className="text-purple fw-bold text-decoration-none">Regístrate aquí</Link>
            </div>
          </form>
        </div>
      </div>
    </div>
  );
};
