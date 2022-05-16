FROM python:3.8-buster

WORKDIR /usr/app

RUN apt-get update
RUN apt-get -y install make
RUN apt-get -y install gcc
RUN curl -sSL https://raw.githubusercontent.com/python-poetry/poetry/master/get-poetry.py | python -
ENV PATH="${PATH}:/root/.poetry/bin"

COPY . .
COPY ./wpa_supplicant.conf /etc/wpa_supplicant/

RUN make install

CMD ["make", "run"]