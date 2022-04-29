FROM python:3.9.12-buster

WORKDIR /usr/app

RUN apt-get -y install make
RUN apt-get -y install c++ cpp
RUN curl -sSL https://raw.githubusercontent.com/python-poetry/poetry/master/get-poetry.py | python -
ENV PATH="${PATH}:/root/.poetry/bin"

COPY . .

RUN make install

CMD ["make", "run"]