import { existsSync, statSync } from 'node:fs';
import { resolve } from 'node:path';
import { describe, expect, it } from 'vitest';
import { juegosData } from './juegosData';



//npm test  --run src\pages\juegosData.test.js


const publicDirectory = resolve(process.cwd(), 'public');

describe('juegosData', () => {
  it('define juegos con identificadores únicos y datos de catálogo', () => {
    const ids = juegosData.map((game) => game.id);

    // cambiar length por length + 1 para que falle el test de id unico, +1 cantidad
    expect(new Set(ids).size).toBe(juegosData.length);
    juegosData.forEach((game) => {
      expect(game).toEqual(expect.objectContaining({
        titulo: expect.any(String),
        precio: expect.any(Number),
        categoria: expect.any(String),
        imagen: expect.any(String),
        descripcion_corta: expect.any(String),
      }));
    });
  });

  it('tiene imágenes y capturas locales que existen en public', () => {
    const localImagePaths = juegosData.flatMap((game) => [
      game.imagen,
      ...(game.galeria || []),
    ]).filter((resource) => !/^https?:\/\//i.test(resource));

    localImagePaths.forEach((resource) => {
      // cambiar true por false
      expect(existsSync(resolve(publicDirectory, resource))).toBe(true);
    });
  });

  it('mantiene cada imagen local bajo el máximo de 200 KB de la ERS', () => {
    const localImagePaths = juegosData.flatMap((game) => [
      game.imagen,
      ...(game.galeria || []),
    ]).filter((resource) => !/^https?:\/\//i.test(resource));

    localImagePaths.forEach((resource) => {
      expect(statSync(resolve(publicDirectory, resource)).size).toBeLessThanOrEqual(200 * 1024);
    });
  });

  it('no declara un tráiler para Elden Ring cuando no existe el recurso local', () => {
    const eldenRing = juegosData.find((game) => game.titulo === 'Elden Ring');

    expect(eldenRing.video).toBeNull();
  });
});
