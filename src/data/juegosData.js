export const juegosData = [
    {
        id: 1,
        titulo: 'Elden Ring',
        precio: 59990,
        categoria: 'Action RPG / Fantasía Oscura',
        imagen: 'src/img/elden_ring.jpg',
        descripcion_corta: 'Álzate, Sinluz, y déjate guiar por la gracia para esgrimir el poder del Círculo de Elden y convertirte en el Señor del Círculo en las Tierras Intermedias.',
        video: 'src/videos/elden.webm',
        galeria: [
            'src/img/elden_ring.jpg',
            'https://images.unsplash.com/photo-1542751371-adc38448a05e?q=80&w=800&fit=crop',
            'https://images.unsplash.com/photo-1550745165-9bc0b252726f?q=80&w=800&fit=crop',
            'https://images.unsplash.com/photo-1511512578047-dfb367046420?q=80&w=800&fit=crop'
        ],
        acerca_de: `
            <p>EL NUEVO JUEGO DE ROL Y ACCIÓN DE FANTASÍA. Álzate, Sinluz, y déjate guiar por la gracia para esgrimir el poder del Círculo de Elden y convertirte en el Señor del Círculo en las Tierras Intermedias.</p>
            <p>Un vasto mundo lleno de emoción: Un vasto mundo donde los campos abiertos con una variedad de situaciones y enormes mazmorras con diseños complejos y tridimensionales se conectan fluidamente. A medida que exploras, te espera la alegría de descubrir amenazas desconocidas y abrumadoras.</p>
            <ul>
                <li><strong>Crea tu propio personaje:</strong> Además de personalizar la apariencia de tu personaje, puedes combinar libremente las armas, armaduras y magias que equipas.</li>
                <li><strong>Un drama épico nacido de un mito:</strong> Una historia de múltiples capas contada en fragmentos. Un drama épico en el que los diversos pensamientos de los personajes se cruzan en las Tierras Intermedias.</li>
            </ul>
        `,
        detalles: {
            desarrollador: 'FromSoftware Inc.',
            editor: 'Bandai Namco Entertainment',
            fecha: '25 Feb 2022',
            modos: 'Un jugador, Cooperativo en línea, JcJ en línea',
            idiomas: 'Español (interfaz y subtítulos), Inglés (voces)'
        },
        requisitos: {
            minimos: {
                so: 'Windows 10 (64-bit)',
                procesador: 'Intel Core i5-8400 o AMD Ryzen 3 3300X',
                memoria: '12 GB de RAM',
                graficos: 'NVIDIA GeForce GTX 1060 (3 GB) o AMD Radeon RX 580 (4 GB)',
                directx: 'Versión 12',
                almacenamiento: '60 GB de espacio disponible'
            },
            recomendados: {
                so: 'Windows 10 / 11 (64-bit)',
                procesador: 'Intel Core i7-8700K o AMD Ryzen 5 3600X',
                memoria: '16 GB de RAM',
                graficos: 'NVIDIA GeForce GTX 1070 (8 GB) o AMD Radeon RX Vega 56 (8 GB)',
                directx: 'Versión 12',
                almacenamiento: '60 GB de espacio disponible (SSD recomendado)'
            }
        }
    },
    {
        id: 2,
        titulo: 'Cyberpunk 2077',
        precio: 49990,
        categoria: 'RPG / Mundo Abierto Futurista',
        imagen: 'src/img/cyberpunk2077.jpeg',
        descripcion_corta: 'Cyberpunk 2077 es un RPG de acción y aventura de mundo abierto ambientado en la megalópolis de Night City, donde te pones en la piel de un mercenario cibernético.',
        video: 'https://cdn.akamai.steamstatic.com/steam/apps/256805177/movie480_vp9.webm',
        galeria: [
            'src/img/cyberpunk2077.jpeg',
            'https://images.unsplash.com/photo-1542751371-adc38448a05e?q=80&w=800&fit=crop',
            'https://images.unsplash.com/photo-1607604276583-eef5d076aa5f?q=80&w=800&fit=crop',
            'https://images.unsplash.com/photo-1600080972464-8e5f35f63d08?q=80&w=800&fit=crop'
        ],
        acerca_de: `
            <p>Conviértete en V, un mercenario al margen de la ley que persigue un implante único que permite alcanzar la inmortalidad. Personaliza las mejoras cibernéticas, las habilidades y el estilo de juego de tu personaje mientras exploras una inmensa ciudad donde tus decisiones dan forma a la historia.</p>
            <ul>
                <li><strong>Crea tu propio ciberdelincuente:</strong> Elige entre docenas de habilidades y armas mejorables según tu estilo de combate (sigilo, hackeo o fuerza bruta).</li>
                <li><strong>Explora la ciudad del futuro:</strong> Night City está repleta de cosas que hacer, lugares que ver y personas que conocer. Y tú decides a dónde ir, cuándo ir y cómo llegar.</li>
            </ul>
        `,
        detalles: {
            desarrollador: 'CD PROJEKT RED',
            editor: 'CD PROJEKT RED',
            fecha: '10 Dic 2020',
            modos: 'Un jugador',
            idiomas: 'Español (voces y textos), Inglés, Francés, Alemán'
        },
        requisitos: {
            minimos: {
                so: 'Windows 10 (64-bit)',
                procesador: 'Intel Core i7-6700 o AMD Ryzen 5 1600',
                memoria: '12 GB de RAM',
                graficos: 'NVIDIA GeForce GTX 1060 (6 GB) o AMD Radeon RX 580',
                directx: 'Versión 12',
                almacenamiento: '70 GB de espacio disponible (SSD recomendado)'
            },
            recomendados: {
                so: 'Windows 10 / 11 (64-bit)',
                procesador: 'Intel Core i7-12700 o AMD Ryzen 7 7800X3D',
                memoria: '16 GB de RAM',
                graficos: 'NVIDIA GeForce RTX 2060 SUPER o AMD Radeon RX 5700 XT',
                directx: 'Versión 12',
                almacenamiento: '70 GB en SSD'
            }
        }
    },
    {
        id: 3,
        titulo: 'The Legend of Zelda: Tears of the Kingdom',
        precio: 69990,
        categoria: 'Aventura / Acción / Mundo Abierto',
        imagen: 'src/img/zelda.jpg',
        descripcion_corta: 'Una aventura épica a través de la tierra y los cielos de Hyrule te espera en la secuela directa de The Legend of Zelda: Breath of the Wild.',
        galeria: [
            'src/img/zelda.jpg',
            'https://images.unsplash.com/photo-1550745165-9bc0b252726f?q=80&w=800&fit=crop',
            'https://images.unsplash.com/photo-1542751371-adc38448a05e?q=80&w=800&fit=crop',
            'https://images.unsplash.com/photo-1511512578047-dfb367046420?q=80&w=800&fit=crop'
        ],
        acerca_de: `
            <p>En esta secuela de The Legend of Zelda: Breath of the Wild, decidirás tu propio camino a través de los extensos paisajes de Hyrule y las misteriosas islas flotantes en los cielos infinitos.</p>
            <ul>
                <li><strong>Crea y experimenta:</strong> Aprovecha el poder de las nuevas habilidades de Link como Ultramano y Combinación para fabricar armas improvisadas y vehículos ingeniosos.</li>
                <li><strong>Explora las profundidades:</strong> Desciende a un mundo subterráneo lleno de peligros oscuros y recompensas legendarias.</li>
            </ul>
        `,
        detalles: {
            desarrollador: 'Nintendo EPD',
            editor: 'Nintendo',
            fecha: '12 May 2023',
            modos: 'Un jugador',
            idiomas: 'Español de América, Español de España, Inglés, Japonés'
        },
        requisitos: {
            minimos: {
                so: 'Consola Nintendo Switch / OLED / Lite',
                procesador: 'NVIDIA Tegra Custom',
                memoria: '4 GB LPDDR4',
                graficos: '720p Modo Portátil / 1080p Modo TV',
                directx: 'N/A (Nintendo OS)',
                almacenamiento: '18.2 GB de espacio en memoria'
            },
            recomendados: {
                so: 'Consola Nintendo Switch (Modo TV con Switch Pro Controller)',
                procesador: 'NVIDIA Tegra Custom',
                memoria: '4 GB LPDDR4',
                graficos: '1080p 60Hz compatible',
                directx: 'N/A (Nintendo OS)',
                almacenamiento: 'Tarjeta MicroSD Clase 10 recomendada'
            }
        }
    },
    {
        id: 4,
        titulo: 'Starfield',
        precio: 69990,
        categoria: 'Sci-Fi RPG / Exploración Espacial',
        imagen: 'src/img/starfield.jpeg',
        descripcion_corta: 'Starfield es el primer universo nuevo en más de 25 años de Bethesda Game Studios. Crea el personaje que quieras y explora con una libertad inigualable.',
        video: 'https://cdn.akamai.steamstatic.com/steam/apps/256950290/movie480_vp9.webm',
        galeria: [
            'src/img/starfield.jpeg',
            'https://images.unsplash.com/photo-1607604276583-eef5d076aa5f?q=80&w=800&fit=crop',
            'https://images.unsplash.com/photo-1542751371-adc38448a05e?q=80&w=800&fit=crop',
            'https://images.unsplash.com/photo-1511512578047-dfb367046420?q=80&w=800&fit=crop'
        ],
        acerca_de: `
            <p>En el año 2330, la humanidad se ha aventurado más allá de nuestro sistema solar para asentarse en nuevos planetas. Te unirás a Constelación, el último grupo de exploradores espaciales en busca de artefactos raros por toda la galaxia.</p>
            <ul>
                <li><strong>Navega y pilota tu nave:</strong> Pilota y personaliza el aspecto de tu nave espacial, modifica sistemas clave como armamento y escudos, y recluta a miembros de la tripulación.</li>
                <li><strong>Más de mil planetas:</strong> Explora mundos alienígenas, extrae recursos para fabricar objetos y construye bases avanzadas.</li>
            </ul>
        `,
        detalles: {
            desarrollador: 'Bethesda Game Studios',
            editor: 'Bethesda Softworks',
            fecha: '06 Sep 2023',
            modos: 'Un jugador',
            idiomas: 'Español (interfaz y subtítulos), Inglés (voces completas)'
        },
        requisitos: {
            minimos: {
                so: 'Windows 10 versión 21H1 (64-bit)',
                procesador: 'AMD Ryzen 5 2600X o Intel Core i7-6800K',
                memoria: '16 GB de RAM',
                graficos: 'AMD Radeon RX 5700 o NVIDIA GeForce GTX 1070 Ti',
                directx: 'Versión 12',
                almacenamiento: '125 GB de espacio disponible (SSD Obligatorio)'
            },
            recomendados: {
                so: 'Windows 10 / 11 actualizado',
                procesador: 'AMD Ryzen 5 3600X o Intel i5-10600K',
                memoria: '16 GB de RAM',
                graficos: 'AMD Radeon RX 6800 XT o NVIDIA GeForce RTX 2080',
                directx: 'Versión 12',
                almacenamiento: '125 GB en SSD NVMe'
            }
        }
    },
    {
        id: 5,
        titulo: 'Final Fantasy XVI',
        precio: 64990,
        categoria: 'Action RPG / Fantasía Oscura',
        imagen: 'src/img/final_fantasy_xvi.jpeg',
        descripcion_corta: 'Un trágico destino amenaza al reino de Valisthea. Sigue a Clive Rosfield en una historia de venganza y gigantescas batallas de Eikon.',
        galeria: [
            'src/img/final_fantasy_xvi.jpeg',
            'https://images.unsplash.com/photo-1542751371-adc38448a05e?q=80&w=800&fit=crop',
            'https://images.unsplash.com/photo-1550745165-9bc0b252726f?q=80&w=800&fit=crop',
            'https://images.unsplash.com/photo-1600080972464-8e5f35f63d08?q=80&w=800&fit=crop'
        ],
        acerca_de: `
            <p>Final Fantasy XVI transporta a los jugadores al oscuro mundo de Valisthea, una tierra bendecida por la luz de los Cristales Madre, donde la paz se ve amenazada por la propagación de las tierras marchitas y el choque mortal de los Eikons.</p>
            <ul>
                <li><strong>Combates colosales:</strong> Cuando los Dominantes desatan el poder de sus Eikons, se libran batallas a escala titánica con fluidez cinematográfica.</li>
                <li><strong>Acción visceral:</strong> Diseñado por el director de combate de Devil May Cry V para ofrecer una experiencia rápida, dinámica y técnica.</li>
            </ul>
        `,
        detalles: {
            desarrollador: 'Square Enix Creative Business Unit III',
            editor: 'Square Enix',
            fecha: '22 Jun 2023',
            modos: 'Un jugador',
            idiomas: 'Español latinoamericano (voces y textos), Inglés, Japonés'
        },
        requisitos: {
            minimos: {
                so: 'PlayStation 5 / Windows 10 (64-bit)',
                procesador: 'AMD Ryzen 5 1600 o Intel Core i5-8400',
                memoria: '16 GB de RAM',
                graficos: 'AMD Radeon RX 5700 o NVIDIA GeForce GTX 1070',
                directx: 'Versión 12',
                almacenamiento: '170 GB de espacio disponible (SSD)'
            },
            recomendados: {
                so: 'Windows 11 (64-bit)',
                procesador: 'AMD Ryzen 7 5700X o Intel Core i7-10700',
                memoria: '16 GB de RAM',
                graficos: 'AMD Radeon RX 6700 XT o NVIDIA GeForce RTX 2080',
                directx: 'Versión 12',
                almacenamiento: '170 GB en SSD ultra rápido'
            }
        }
    },
    {
        id: 6,
        titulo: 'Baldur\'s Gate 3',
        precio: 59990,
        categoria: 'CRPG / Rol Táctico D&D',
        imagen: 'src/img/baldurs_gate.jpg',
        descripcion_corta: 'Reúne a tu grupo y regresa a los Reinos Olvidados en una historia de compañerismo, traición, sacrificio y la atracción de un poder absoluto.',
        video: 'src/videos/baldurs.webm',
        galeria: [
            'src/img/baldurs_gate.jpg',
            'src/img/juegos/baldurs_gate/captura1.jpg',
            'src/img/juegos/baldurs_gate/captura2.png',
            'src/img/juegos/baldurs_gate/captura3.jpg',
            'src/img/juegos/baldurs_gate/captura4.jpg'
        ],
        acerca_de: `
            <p>Reúne a tu grupo y regresa a los Reinos Olvidados en una historia de compañerismo, traición, sacrificio, supervivencia y la atracción de un poder absoluto.</p>
            <p>Unas misteriosas aptitudes empiezan a surgir en tu interior por obra de un parásito de los azotamentes que te han implantado en el cerebro. Resístete y vuelve la oscuridad contra sí misma o abraza la corrupción y conviértete en el mal supremo.</p>
            <ul>
                <li><strong>Libertad absoluta de D&D:</strong> Elige entre 12 clases y 11 razas del Manual del jugador de D&D o crea tu propio héroe original.</li>
                <li><strong>Multijugador cooperativo de hasta 4 jugadores:</strong> Combina tus fuerzas en el combate por turnos y divide a tu grupo para cumplir misiones simultáneas.</li>
                <li><strong>Reconocimiento mundial:</strong> Ganador indiscutible del premio Juego del Año (GOTY 2023).</li>
            </ul>
        `,
        detalles: {
            desarrollador: 'Larian Studios',
            editor: 'Larian Studios',
            fecha: '03 Ago 2023',
            modos: 'Un jugador, Cooperativo en línea, Multijugador multiplataforma',
            idiomas: 'Español de España (interfaz y subtítulos), Inglés (voces completas)'
        },
        requisitos: {
            minimos: {
                so: 'Windows 10 de 64 bits',
                procesador: 'Intel Core i5-4690 o AMD FX 8350',
                memoria: '8 GB de RAM',
                graficos: 'NVIDIA GeForce GTX 970 o AMD Radeon RX 480 (4 GB VRAM)',
                directx: 'Versión 11',
                almacenamiento: '150 GB de espacio disponible (SSD requerido)'
            },
            recomendados: {
                so: 'Windows 10 / 11 de 64 bits',
                procesador: 'Intel Core i7-8700K o AMD Ryzen 5 3600',
                memoria: '16 GB de RAM',
                graficos: 'NVIDIA GeForce RTX 2060 SUPER o AMD Radeon RX 5700 XT (8 GB VRAM)',
                directx: 'Versión 11',
                almacenamiento: '150 GB de espacio en SSD'
            }
        }
    }
];