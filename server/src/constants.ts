import dotenv = require('dotenv');

if (dotenv.config().parsed) {
    console.log('ENV', dotenv.config().parsed);
}

export const PORT = process.env.PORT ?? '4000';
export const HOST_IP = process.env.HOST_IP;
