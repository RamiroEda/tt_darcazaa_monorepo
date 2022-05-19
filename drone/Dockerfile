FROM logiqx/python-lxml:3.8-slim-buster

WORKDIR /usr/app

RUN apt-get update
RUN apt-get -y install make gstreamer-1.0 gcc libc-dev

RUN pip list

COPY . .

RUN make install

CMD ["make", "run"]