const { test, expect } = require('@playwright/test');

function fechaFutura(diasDesdeHoy) {
  const fecha = new Date();
  fecha.setDate(fecha.getDate() + diasDesdeHoy);
  return fecha.toISOString().split('T')[0];
}

test.describe('Formulario publico de reservas', () => {
  test('carga la lista de servicios disponibles desde la API', async ({ page }) => {
    await page.goto('/reservar');

    const selectServicio = page.locator('select[name="idServicio"]');
    await expect(selectServicio).toBeVisible();

    // Espera a que las opciones lleguen desde /api/servicios (mas que el placeholder inicial)
    await expect
      .poll(async () => (await selectServicio.locator('option').all()).length)
      .toBeGreaterThan(1);

    await expect(selectServicio.getByText('Consulta General', { exact: false })).toHaveCount(1);
  });

  test('permite completar y enviar una nueva reserva exitosamente', async ({ page }) => {
    await page.goto('/reservar');

    await page.locator('input[name="nombre"]').fill('Cliente de Prueba E2E');
    await page.locator('input[name="telefono"]').fill('0991234567');
    await page.locator('input[name="email"]').fill(`e2e.${Date.now()}@test.com`);

    const selectServicio = page.locator('select[name="idServicio"]');
    await expect.poll(async () => (await selectServicio.locator('option').all()).length).toBeGreaterThan(1);
    await selectServicio.selectOption({ index: 1 });

    await page.locator('input[name="fecha"]').fill(fechaFutura(7));
    await page.locator('select[name="hora"]').selectOption('10:00');

    await page.getByRole('button', { name: 'Confirmar Reserva' }).click();

    await expect(page.getByText('Reserva creada exitosamente')).toBeVisible();
  });
});
