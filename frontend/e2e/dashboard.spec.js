const { test, expect } = require('@playwright/test');

test.describe('Acceso al Dashboard', () => {
  test('redirige a la pagina de login al intentar acceder al dashboard sin autenticacion', async ({ page }) => {
    // Intentar acceder directamente a /dashboard sin haber iniciado sesión
    await page.goto('/dashboard');

    // Verificar que es redirigido a / (login)
    await expect(page).toHaveURL('/');

    // Verificar que se ve el título "Iniciar Sesión" del login
    await expect(page.getByRole('heading', { name: 'Iniciar Sesión' })).toBeVisible();
  });
});
