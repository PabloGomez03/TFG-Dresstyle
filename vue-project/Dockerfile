#Construcción 
FROM node:lts-iron AS build-stage
WORKDIR /app

# Instalamos dependencias
COPY package*.json ./
RUN npm install

# Copiamos el código y generamos los archivos de producción (carpeta /dist)
COPY . .
RUN npm run build

FROM nginx:stable-alpine as production-stage
# Copiamos nuestro archivo de configuración personalizado
COPY nginx.conf /etc/nginx/conf.d/default.conf
# Copiamos los archivos compilados
COPY --from=build-stage /app/dist /usr/share/nginx/html
EXPOSE 80
CMD ["nginx", "-g", "daemon off;"]