

-- 1. Tablas Maestras (Independientes)
CREATE TABLE Localidad (
    localidad_id INT PRIMARY KEY,
    pais VARCHAR(100),
    nombre_ciudad VARCHAR(100),
    codigo_postal VARCHAR(10) -- La CC.AA. se deriva de aquí
);

CREATE TABLE Genero (
    genero_id INT PRIMARY KEY,
    nombre VARCHAR(100),
    descripcion TEXT
);

-- 2. Tablas con dependencias simples
CREATE TABLE Artista (
    artista_id INT PRIMARY KEY,
    nombre_artistico VARCHAR(150),
    pais_origen VARCHAR(100),
    descripcion TEXT,
    genero_id INT, -- FK a Genero
    CONSTRAINT fk_artista_genero FOREIGN KEY (genero_id) REFERENCES Genero(genero_id)
);

CREATE TABLE Usuario (
    usuario_id INT PRIMARY KEY,
    nombre VARCHAR(100),
    email VARCHAR(150) UNIQUE,
    contraseña VARCHAR(255),
    fecha_nacimiento DATE,
    generos_favoritos TEXT, -- Opcional: Podría ser otra tabla N:M
    localidad_id INT,
    fecha_registro TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_usuario_localidad FOREIGN KEY (localidad_id) REFERENCES Localidad(localidad_id)
);

CREATE TABLE Cancion (
    cancion_id INT PRIMARY KEY,
    titulo VARCHAR(200),
    artista_id INT,
    genero_id INT,
    fecha_lanzamiento DATE,
    duracion INT, -- Segundos
    album VARCHAR(200),
    CONSTRAINT fk_cancion_artista FOREIGN KEY (artista_id) REFERENCES Artista(artista_id),
    CONSTRAINT fk_cancion_genero FOREIGN KEY (genero_id) REFERENCES Genero(genero_id)
);

-- 3. Tablas de Relación y Entidades Dependientes
CREATE TABLE Usuario_Gustos (
    usuario_id INT,
    genero_id INT,
    artista_id INT, -- FK a Artista
    cancion_id INT, -- FK a Cancion
    PRIMARY KEY (usuario_id, genero_id),
    CONSTRAINT fk_gustos_usuario FOREIGN KEY (usuario_id) REFERENCES Usuario(usuario_id),
    CONSTRAINT fk_gustos_genero FOREIGN KEY (genero_id) REFERENCES Genero(genero_id),
    CONSTRAINT fk_gustos_artista FOREIGN KEY (artista_id) REFERENCES Artista(artista_id),
    CONSTRAINT fk_gustos_cancion FOREIGN KEY (cancion_id) REFERENCES Cancion(cancion_id)
);

CREATE TABLE Reproduccion (
    reproduccion_id INT PRIMARY KEY,
    usuario_id INT,
    cancion_id INT,
    fecha TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    localidad_id INT, -- Opcional
    CONSTRAINT fk_repro_usuario FOREIGN KEY (usuario_id) REFERENCES Usuario(usuario_id),
    CONSTRAINT fk_repro_cancion FOREIGN KEY (cancion_id) REFERENCES Cancion(cancion_id),
    CONSTRAINT fk_repro_localidad FOREIGN KEY (localidad_id) REFERENCES Localidad(localidad_id)
);

CREATE TABLE Ranking (
    localidad_id INT,
    cancion_id INT,
    fecha DATE,
    artista_id INT,
    posicion INT,
    PRIMARY KEY (localidad_id, cancion_id, fecha),
    CONSTRAINT fk_ranking_localidad FOREIGN KEY (localidad_id) REFERENCES Localidad(localidad_id),
    CONSTRAINT fk_ranking_cancion FOREIGN KEY (cancion_id) REFERENCES Cancion(cancion_id),
    CONSTRAINT fk_ranking_artista FOREIGN KEY (artista_id) REFERENCES Artista(artista_id)
);

CREATE TABLE Concierto (
    concierto_id INT PRIMARY KEY,
    artista_id INT,
    localidad_id INT,
    fecha_hora DATETIME,
    local VARCHAR(200),
    descripcion TEXT,
    CONSTRAINT fk_concierto_artista FOREIGN KEY (artista_id) REFERENCES Artista(artista_id),
    CONSTRAINT fk_concierto_localidad FOREIGN KEY (localidad_id) REFERENCES Localidad(localidad_id)
);

CREATE TABLE Colaboracion (
    cancion_id INT,
    artista_id INT,
    artista_2_id INT, -- FK a Artista
    PRIMARY KEY (cancion_id, artista_id),
    CONSTRAINT fk_colab_cancion FOREIGN KEY (cancion_id) REFERENCES Cancion(cancion_id),
    CONSTRAINT fk_colab_artista1 FOREIGN KEY (artista_id) REFERENCES Artista(artista_id),
    CONSTRAINT fk_colab_artista2 FOREIGN KEY (artista_2_id) REFERENCES Artista(artista_id)
);

CREATE TABLE Recomendacion (
    usuario_id INT,
    artista_id INT,
    concierto_id INT,
    PRIMARY KEY (artista_id, concierto_id),
    CONSTRAINT fk_reco_usuario FOREIGN KEY (usuario_id) REFERENCES Usuario(usuario_id),
    CONSTRAINT fk_reco_artista FOREIGN KEY (artista_id) REFERENCES Artista(artista_id),
    CONSTRAINT fk_reco_concierto FOREIGN KEY (concierto_id) REFERENCES Concierto(concierto_id)
);

CREATE TABLE Informacion_Pago (
    id_pago INT,
    usuario_id INT,
    concierto_id INT,
    fecha_pago DATETIME,
    localidad_id INT,
    id_transaccion VARCHAR(100),
    monto_pago DECIMAL(10, 2),
    metodo_pago VARCHAR(50),
    estado_pago VARCHAR(50),
    detalles_pago TEXT,
    PRIMARY KEY (usuario_id, concierto_id, fecha_pago),
    CONSTRAINT fk_pago_usuario FOREIGN KEY (usuario_id) REFERENCES Usuario(usuario_id),
    CONSTRAINT fk_pago_concierto FOREIGN KEY (concierto_id) REFERENCES Concierto(concierto_id),
    CONSTRAINT fk_pago_localidad FOREIGN KEY (localidad_id) REFERENCES Localidad(localidad_id)
);
