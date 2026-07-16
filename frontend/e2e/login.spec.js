const { test, expect } = require('@playwright/test');

test.describe('Flujo de Autenticacion (Login)', () => {
  test('la pagina de inicio muestra el formulario de login', async ({ page }) => {
    await page.goto('/');

    // Verificar que se muestre el título del formulario
    await expect(page.getByRole('heading', { name: 'Iniciar Sesión' })).toBeVisible();

    // Verificar que los campos de email y contraseña estén visibles
    const emailInput = page.locator('input[type="email"]');
    const passwordInput = page.locator('input[type="password"]');
    await expect(emailInput).toBeVisible();
    await expect(passwordInput).toBeVisible();
    
    // Verificar que el botón de envío esté visible
    const submitButton = page.getByRole('button', { name: 'Iniciar Sesión' });
    await expect(submitButton).toBeVisible();
  });

  test('muestra un error al iniciar sesion con credenciales invalidas', async ({ page }) => {
    await page.goto('/');

    // Rellenar con credenciales inválidas
    await page.locator('input[type="email"]').fill('admin@reservas.com');
    await page.locator('input[type="password"]').fill('incorrecta');

    // Enviar formulario
    await page.getByRole('button', { name: 'Iniciar Sesión' }).click();

    // Verificar que se muestre un mensaje de error
    await expect(page.getByText('Credenciales inválidas')).toBeVisible();

    // Verificar que el usuario permanece en la página de login
    await expect(page).toHaveURL('/');
  });

  test('permite iniciar sesion con credenciales validas y redirige al dashboard', async ({ page }) => {
    await page.goto('/');

    // Rellenar con credenciales válidas
    await page.locator('input[type="email"]').fill('admin@reservas.com');
    await page.locator('input[type="password"]').fill('password');

    // Enviar formulario
    await page.getByRole('button', { name: 'Iniciar Sesión' }).click();

    // Verificar redirección al dashboard
    await expect(page).toHaveURL('/dashboard');

    // Verificar que se vea el panel administrativo
    await expect(page.getByRole('heading', { name: 'Sistema Reservas' })).toBeVisible();
  });
});
