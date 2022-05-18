FROM python:3.7-buster

WORKDIR /usr/app

RUN apt-get update
RUN apt-get -y install make gstreamer-1.0
RUN apt-get -y install libxml2-dev libxslt-dev python-dev
RUN apt-get -y install python3-lxml python-lxml
RUN apt-get -y install python3-gi python3-gi-cairo gir1.2-gtk-3.0

COPY . .

RUN make install

CMD ["make", "run"]