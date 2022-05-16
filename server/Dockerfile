FROM positivly/prisma-binaries:latest as prisma
FROM node:16-alpine

WORKDIR /app

ENV PRISMA_QUERY_ENGINE_BINARY=/app/prisma-engines/query-engine \
  PRISMA_MIGRATION_ENGINE_BINARY=/app/prisma-engines/migration-engine \
  PRISMA_INTROSPECTION_ENGINE_BINARY=/app/prisma-engines/introspection-engine \
  PRISMA_FMT_BINARY=/app/prisma-engines/prisma-fmt \
  PRISMA_CLI_QUERY_ENGINE_TYPE=binary \
  PRISMA_CLIENT_ENGINE_TYPE=binary
  
COPY --from=coollabsio/prisma-engine:latest /prisma-engines/query-engine /prisma-engines/migration-engine /prisma-engines/introspection-engine /prisma-engines/prisma-fmt /app/prisma-engines/

RUN apk update
RUN apk add openssl
RUN apk add sqlite

COPY . .
COPY ./wpa_supplicant.conf /etc/wpa_supplicant/

RUN npm install
RUN npx prisma generate
RUN npm run build
RUN npx prisma migrate deploy

EXPOSE 4000
CMD [ "npm", "start" ]
