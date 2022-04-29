FROM toriaezunama/prisma-arm32v7:node-14

WORKDIR /usr/app

RUN apt-get -qy update
RUN apt-get -qy install openssl

COPY . .

RUN npm install
RUN npx prisma generate
RUN npm run build

EXPOSE 4000
CMD [ "npm", "start" ]
