FROM jaywizard/prisma-arm64:latest

WORKDIR /usr/app

RUN apt-get -qy update
RUN apt-get -qy install openssl

COPY . .

RUN npm install
RUN npx prisma generate
RUN npm run build

EXPOSE 4000
CMD [ "npm", "start" ]
