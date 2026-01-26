#Construcción 
FROM node:lts-iron AS build-stage
WORKDIR /app

# Instalamos dependencias
COPY package*.json ./
RUN npm install

# Copiamos el código y generamos los archivos de producción (carpeta /dist)
COPY . .
RUN npm run build

#Producción (Servidor Web)
FROM nginx:stable-alpine AS production-stage

# Copiamos los archivos estáticos al directorio de Nginx
COPY --from=build-stage /app/dist /usr/share/nginx/html

# Exponemos el puerto 80 para el tráfico web
EXPOSE 80

CMD ["nginx", "-g", "daemon off;"]